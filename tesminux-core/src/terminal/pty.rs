use portable_pty::{native_pty_system, CommandBuilder, PtySize};
use std::io::{Read, Write};
use std::sync::{Arc, Mutex};
use std::thread;

const HOME: &str = "/data/data/com.tesminux.app/files/home";
const CACHE: &str = "/data/data/com.tesminux.app/cache";

pub struct Pty {
    master: Box<dyn portable_pty::MasterPty + Send>,
    child: Box<dyn portable_pty::Child + Send>,
    writer: Mutex<Box<dyn Write + Send>>,
    output: Arc<Mutex<String>>,
}

impl Pty {
    pub fn new() -> Result<Self, Box<dyn std::error::Error>> {
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

                        if let Ok(mut output) = output_clone.lock() {
                            output.push_str(&text);

                            if output.len() > 1024 * 1024 {
                                let remove = output.len() - 1024 * 1024;
                                output.drain(..remove);
                            }
                        }
                    }

                    Err(_) => break,
                }
            }
        });

        Ok(Self {
            master: pair.master,
            child,
            writer: Mutex::new(writer),
            output,
        })
    }

    pub fn write_input(
        &self,
        input: &str,
    ) -> Result<(), Box<dyn std::error::Error>> {
        let mut writer = self
            .writer
            .lock()
            .map_err(|_| "PTY writer kilidi bozuldu")?;

        writer.write_all(input.as_bytes())?;
        writer.flush()?;

        Ok(())
    }

    pub fn get_output(&self) -> String {
        match self.output.lock() {
            Ok(output) => output.clone(),
            Err(_) => String::new(),
        }
    }

    // ======== YENİ EKLENEN FONKSİYON ========
    pub fn clear_output(&self) {
        if let Ok(mut output) = self.output.lock() {
            output.clear();
        }
    }
    // =======================================

    pub fn is_running(
        &mut self,
    ) -> Result<bool, Box<dyn std::error::Error>> {
        Ok(self.child.try_wait()?.is_none())
    }
}