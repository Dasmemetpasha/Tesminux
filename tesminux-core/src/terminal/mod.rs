pub mod pty;

pub struct Terminal {
    pty: Option<pty::Pty>,
}

impl Terminal {
    pub fn new() -> Self {
        Self { pty: None }
    }

    pub fn start(&mut self) -> Result<(), Box<dyn std::error::Error>> {
        if self.pty.is_some() {
            return Ok(());
        }

        let pty = pty::Pty::new()?;
        self.pty = Some(pty);

        Ok(())
    }

    pub fn write(&mut self, input: &str) -> Result<(), Box<dyn std::error::Error>> {
        match &self.pty {
            Some(pty) => pty.write_input(input),
            None => Err("Terminal başlatılmamış".into()),
        }
    }

    pub fn output(&self) -> String {
        match &self.pty {
            Some(pty) => pty.get_output(),
            None => String::new(),
        }
    }

    pub fn is_running(&mut self) -> bool {
        match &mut self.pty {
            Some(pty) => pty.is_running().unwrap_or(false),
            None => false,
        }
    }
}