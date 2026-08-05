use std::fs;
use std::path::Path;

const HISTORY_FILE: &str =
    "/data/data/com.tesminux.app/files/home/.tesminux_history";

pub struct History {
    commands: Vec<String>,
}

impl History {
    pub fn new() -> Self {
        Self {
            commands: Vec::new(),
        }
    }

    pub fn load(
        &mut self,
    ) -> Result<(), Box<dyn std::error::Error>> {
        self.commands.clear();

        if !Path::new(HISTORY_FILE).exists() {
            return Ok(());
        }

        let content = fs::read_to_string(HISTORY_FILE)?;

        for line in content.lines() {
            let cmd = line.trim();

            if !cmd.is_empty() {
                self.commands.push(cmd.to_string());
            }
        }

        Ok(())
    }

    pub fn save(
        &self,
    ) -> Result<(), Box<dyn std::error::Error>> {
        fs::write(HISTORY_FILE, self.commands.join("\n"))?;
        Ok(())
    }

    pub fn push(&mut self, command: &str) {
        let command = command.trim();

        if command.is_empty() {
            return;
        }

        // Aynı komutu art arda kaydetme
        if self.commands.last().map(|s| s.as_str()) == Some(command) {
            return;
        }

        self.commands.push(command.to_string());

        // En fazla 1000 komut
        if self.commands.len() > 1000 {
            self.commands.remove(0);
        }
    }

    pub fn clear(&mut self) {
        self.commands.clear();
    }

    pub fn items(&self) -> &[String] {
        &self.commands
    }

    pub fn last(&self) -> Option<&String> {
        self.commands.last()
    }

    pub fn len(&self) -> usize {
        self.commands.len()
    }

    pub fn is_empty(&self) -> bool {
        self.commands.is_empty()
    }
}