/// Result of executing a built-in Tesminux core command.
#[derive(Debug, PartialEq, Eq)]
pub enum CommandResult {
    /// Command was handled internally, returning string output.
    Handled(String),
    /// Command was not a built-in command and should be passed to shell/PTY.
    NotHandled,
}

/// Executes internal built-in Tesminux commands.
/// Returns `CommandResult::Handled(output)` if matched, or `CommandResult::NotHandled`.
pub fn execute(command: &str) -> CommandResult {
    let command = command.trim();

    match command {
        "about" => CommandResult::Handled(
            "Tesminux v10.0.0\n\
Rust Powered Terminal for Android\n\
GPL-3.0 License\n\
https://github.com/Dasmemetpasha/Tesminux"
                .to_string(),
        ),

        "version" => CommandResult::Handled("Tesminux v10.0.0".to_string()),

        "whoami" => CommandResult::Handled("tesminux_user (Android Shell)".to_string()),

        "sysinfo" => CommandResult::Handled(format!(
            "Tesminux System Diagnostics v10.0.0\n\
Architecture: {}\n\
Target OS: {}\n\
Logical CPU Cores: {}\n\
Home Directory: /storage/emulated/0\n\
Cache Directory: /data/data/com.tesminux.app/cache",
            std::env::consts::ARCH,
            std::env::consts::OS,
            std::thread::available_parallelism()
                .map(|n| n.get().to_string())
                .unwrap_or_else(|_| "1".to_string())
        )),

        "date" => {
            let duration = std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap_or_default();
            let secs = duration.as_secs();
            let days = secs / 86400;
            let hours = (secs % 86400) / 3600;
            let mins = (secs % 3600) / 60;
            let s = secs % 60;
            CommandResult::Handled(format!(
                "UTC Timestamp: {}s (Days: {}, Time: {:02}:{:02}:{:02} UTC)",
                secs, days, hours, mins, s
            ))
        }

        "env" => CommandResult::Handled(
            "HOME=/storage/emulated/0\n\
TMPDIR=/data/data/com.tesminux.app/cache\n\
PATH=/system/bin:/system/xbin:/vendor/bin:/vendor/xbin\n\
TERM=xterm-256color\n\
SHELL=/system/bin/sh"
                .to_string(),
        ),

        "help" => CommandResult::Handled(
            "Built-in commands:\n\
  help      - Display this help message\n\
  about     - About Tesminux project\n\
  version   - Show version info\n\
  sysinfo   - Display system & Rust runtime diagnostics\n\
  whoami    - Show current terminal session user\n\
  date      - Show system UTC timestamp and time\n\
  env       - Show environment variables\n\
  clear     - Clear terminal screen\n"
                .to_string(),
        ),

        _ => CommandResult::NotHandled,
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_execute_builtin_commands() {
        match execute("version") {
            CommandResult::Handled(out) => assert!(out.contains("v10.0.0")),
            _ => panic!("Expected Handled for version"),
        }

        match execute("sysinfo") {
            CommandResult::Handled(out) => assert!(out.contains("Tesminux System Diagnostics")),
            _ => panic!("Expected Handled for sysinfo"),
        }

        match execute("date") {
            CommandResult::Handled(out) => assert!(out.contains("UTC Timestamp")),
            _ => panic!("Expected Handled for date"),
        }

        match execute("whoami") {
            CommandResult::Handled(out) => assert!(out.contains("tesminux_user")),
            _ => panic!("Expected Handled for whoami"),
        }

        match execute("env") {
            CommandResult::Handled(out) => assert!(out.contains("HOME=")),
            _ => panic!("Expected Handled for env"),
        }

        match execute("custom_shell_command") {
            CommandResult::NotHandled => {}
            _ => panic!("Expected NotHandled for external command"),
        }
    }
}
