use portable_pty::{native_pty_system, CommandBuilder, PtySize};
use std::io::{Read, Write};
use std::process::{ChildStdin, Command, Stdio};
use std::sync::{Arc, Mutex};
use std::thread;

const HOME: &str = "/storage/emulated/0";
const CACHE: &str = "/data/data/com.tesminux.app/cache";

enum PtyBackend {
    Native {
        _master: Box<dyn portable_pty::MasterPty + Send>,
        child: Box<dyn portable_pty::Child + Send>,
        writer: Mutex<Box<dyn Write + Send>>,
    },
    Pipe {
        writer: Mutex<ChildStdin>,
    },
}

pub struct Pty {
    backend: PtyBackend,
    output: Arc<Mutex<String>>,
}

impl Pty {
    pub fn new() -> Result<Self, Box<dyn std::error::Error>> {
        // Attempt Native PTY first
        if let Ok(pty) = Self::try_create_native() {
            return Ok(pty);
        }

        // Fallback to Pipe-based shell process
        Self::create_pipe_fallback()
    }

    fn try_create_native() -> Result<Self, Box<dyn std::error::Error>> {
        let system = native_pty_system();

        let pair = system.openpty(PtySize {
            rows: 30,
            cols: 100,
            pixel_width: 0,
            pixel_height: 0,
        })?;

        let shell = "/system/bin/sh";
        let mut command = CommandBuilder::new(shell);

        command.env("HOME", HOME);
        command.env("TMPDIR", CACHE);
        command.env(
            "PATH",
            "/system/bin:/system/xbin:/vendor/bin:/vendor/xbin",
        );
        command.cwd(HOME);
        command.env("SHELL", shell);
        command.env("TERM", "xterm-256color");
        command.env("LANG", "C.UTF-8");

        let child = pair.slave.spawn_command(command)?;
        let mut reader = pair.master.try_clone_reader()?;
        let writer = pair.master.take_writer()?;

        let output = Arc::new(Mutex::new(String::new()));
        let output_clone = Arc::clone(&output);

        thread::spawn(move || {
            let mut buffer = [0u8; 4096];
            loop {
                match reader.read(&mut buffer) {
                    Ok(0) => break,
                    Ok(size) => {
                        let text = String::from_utf8_lossy(&buffer[..size]);
                        if let Ok(mut out) = output_clone.lock() {
                            out.push_str(&text);
                            if out.len() > 1024 * 1024 {
                                let remove = out.len() - 1024 * 1024;
                                out.drain(..remove);
                            }
                        }
                    }
                    Err(_) => break,
                }
            }
        });

        Ok(Self {
            backend: PtyBackend::Native {
                _master: pair.master,
                child,
                writer: Mutex::new(writer),
            },
            output,
        })
    }

    fn create_pipe_fallback() -> Result<Self, Box<dyn std::error::Error>> {
        let shell = if std::path::Path::new("/system/bin/sh").exists() {
            "/system/bin/sh"
        } else {
            "sh"
        };

        let mut cmd = Command::new(shell);
        cmd.env("HOME", HOME);
        cmd.env("TMPDIR", CACHE);
        cmd.env(
            "PATH",
            "/system/bin:/system/xbin:/vendor/bin:/vendor/xbin",
        );
        cmd.env("SHELL", shell);
        cmd.env("TERM", "xterm-256color");
        cmd.stdin(Stdio::piped());
        cmd.stdout(Stdio::piped());
        cmd.stderr(Stdio::piped());

        let mut child = cmd.spawn()?;
        let stdin = child.stdin.take().ok_or("Standard input kanalı açılamadı")?;
        let mut stdout = child.stdout.take().ok_or("Standard çıktı kanalı açılamadı")?;
        let mut stderr = child.stderr.take().ok_or("Standard hata kanalı açılamadı")?;

        let output = Arc::new(Mutex::new(String::new()));
        let output_out = Arc::clone(&output);
        let output_err = Arc::clone(&output);

        thread::spawn(move || {
            let mut buffer = [0u8; 4096];
            loop {
                match stdout.read(&mut buffer) {
                    Ok(0) => break,
                    Ok(size) => {
                        let text = String::from_utf8_lossy(&buffer[..size]);
                        if let Ok(mut out) = output_out.lock() {
                            out.push_str(&text);
                            if out.len() > 1024 * 1024 {
                                let remove = out.len() - 1024 * 1024;
                                out.drain(..remove);
                            }
                        }
                    }
                    Err(_) => break,
                }
            }
        });

        thread::spawn(move || {
            let mut buffer = [0u8; 4096];
            loop {
                match stderr.read(&mut buffer) {
                    Ok(0) => break,
                    Ok(size) => {
                        let text = String::from_utf8_lossy(&buffer[..size]);
                        if let Ok(mut out) = output_err.lock() {
                            out.push_str(&text);
                            if out.len() > 1024 * 1024 {
                                let remove = out.len() - 1024 * 1024;
                                out.drain(..remove);
                            }
                        }
                    }
                    Err(_) => break,
                }
            }
        });

        Ok(Self {
            backend: PtyBackend::Pipe {
                writer: Mutex::new(stdin),
            },
            output,
        })
    }

    pub fn write_input(
        &self,
        input: &str,
    ) -> Result<(), Box<dyn std::error::Error>> {
        let sanitized_input = input.replace('\0', "");
        match &self.backend {
            PtyBackend::Native { writer, .. } => {
                let mut w = writer
                    .lock()
                    .map_err(|_| "PTY writer kilidi bozuldu")?;
                w.write_all(sanitized_input.as_bytes())?;
                w.flush()?;
            }
            PtyBackend::Pipe { writer } => {
                let mut w = writer
                    .lock()
                    .map_err(|_| "Pipe writer kilidi bozuldu")?;
                w.write_all(sanitized_input.as_bytes())?;
                w.flush()?;
            }
        }

        Ok(())
    }

    pub fn get_output(&self) -> String {
        match self.output.lock() {
            Ok(output) => output.clone(),
            Err(_) => String::new(),
        }
    }

    pub fn clear_output(&self) {
        if let Ok(mut output) = self.output.lock() {
            output.clear();
        }
    }

    pub fn resize(&self, rows: u16, cols: u16) -> Result<(), Box<dyn std::error::Error>> {
        if let PtyBackend::Native { _master, .. } = &self.backend {
            _master.resize(PtySize {
                rows,
                cols,
                pixel_width: 0,
                pixel_height: 0,
            })?;
        }
        Ok(())
    }

    pub fn is_running(
        &mut self,
    ) -> Result<bool, Box<dyn std::error::Error>> {
        match &mut self.backend {
            PtyBackend::Native { child, .. } => Ok(child.try_wait()?.is_none()),
            PtyBackend::Pipe { .. } => Ok(true),
        }
    }
}