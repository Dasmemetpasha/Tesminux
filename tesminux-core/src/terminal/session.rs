use super::Terminal;

pub struct Session {
    id: usize,
    terminal: Terminal,
}

impl Session {
    pub fn new(id: usize) -> Self {
        Self {
            id,
            terminal: Terminal::new(),
        }
    }

    pub fn id(&self) -> usize {
        self.id
    }

    pub fn terminal(&self) -> &Terminal {
        &self.terminal
    }

    pub fn terminal_mut(&mut self) -> &mut Terminal {
        &mut self.terminal
    }
}

pub struct SessionManager {
    sessions: Vec<Session>,
    current: usize,
    next_id: usize,
}

impl SessionManager {
    pub fn new() -> Self {
        let mut manager = Self {
            sessions: Vec::new(),
            current: 0,
            next_id: 1,
        };

        manager.create_session();
        manager
    }

    pub fn create_session(&mut self) -> usize {
        let id = self.next_id;
        self.next_id += 1;

        self.sessions.push(Session::new(id));
        self.current = self.sessions.len() - 1;

        id
    }

    pub fn close_current(&mut self) -> bool {
        if self.sessions.len() <= 1 {
            return false;
        }

        self.sessions.remove(self.current);

        if self.current >= self.sessions.len() {
            self.current = self.sessions.len() - 1;
        }

        true
    }

    pub fn switch_to(&mut self, index: usize) -> bool {
        if index >= self.sessions.len() {
            return false;
        }

        self.current = index;
        true
    }

    pub fn current(&self) -> Option<&Session> {
        self.sessions.get(self.current)
    }

    pub fn current_mut(&mut self) -> Option<&mut Session> {
        self.sessions.get_mut(self.current)
    }

    pub fn get(&self, index: usize) -> Option<&Session> {
        self.sessions.get(index)
    }

    pub fn get_mut(&mut self, index: usize) -> Option<&mut Session> {
        self.sessions.get_mut(index)
    }

    pub fn sessions(&self) -> &[Session] {
        &self.sessions
    }

    pub fn count(&self) -> usize {
        self.sessions.len()
    }

    pub fn current_index(&self) -> usize {
        self.current
    }

    pub fn current_id(&self) -> Option<usize> {
        self.current().map(|s| s.id())
    }

    pub fn current_terminal(&self) -> Option<&Terminal> {
    self.current().map(|s| s.terminal())
}

pub fn current_terminal_mut(&mut self) -> Option<&mut Terminal> {
    self.current_mut().map(|s| s.terminal_mut())
}

pub fn current_session_id(&self) -> usize {
    self.current_id().unwrap_or(0)
}

pub fn has_sessions(&self) -> bool {
    !self.sessions.is_empty()
}
}