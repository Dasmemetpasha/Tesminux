pub enum CommandResult {
    Handled(String),
    NotHandled,
}

pub fn execute(command: &str) -> CommandResult {
    let command = command.trim();

    match command {
        "about" => CommandResult::Handled(
            "Tesminux v0.2-dev\n\
Rust Powered\n\
GPL-3.0 License\n\
https://github.com/Dasmemetpasha/Tesminux"
                .to_string(),
        ),

        "version" => CommandResult::Handled(
            "Tesminux v0.2.0-dev".to_string(),
        ),

        "help" => CommandResult::Handled(
            "\
Built-in commands:

help
about
version
clear
history
"
            .to_string(),
        ),

        _ => CommandResult::NotHandled,
    }
}
