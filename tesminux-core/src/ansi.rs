#[derive(Debug, PartialEq, Clone)]
pub enum AnsiSequence {
    Text(String),
    ClearScreen,
    SetColor(u8),
    ResetColor,
    CursorMove { row: u16, col: u16 },
}

pub struct AnsiParser;

impl AnsiParser {
    pub fn new() -> Self {
        Self
    }

    /// Strips ANSI escape sequences from string input
    pub fn strip_ansi_codes(input: &str) -> String {
        let mut result = String::with_capacity(input.len());
        let mut in_sequence = false;

        for ch in input.chars() {
            if ch == '\x1b' {
                in_sequence = true;
                continue;
            }

            if in_sequence {
                if ch.is_ascii_alphabetic() || ch == '~' {
                    in_sequence = false;
                }
                continue;
            }

            result.push(ch);
        }

        result
    }

    pub fn parse(&self, input: &str) -> Vec<AnsiSequence> {
        let clean = Self::strip_ansi_codes(input);
        if clean.is_empty() {
            vec![]
        } else {
            vec![AnsiSequence::Text(clean)]
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_strip_ansi_codes() {
        let input = "\x1b[31mRed Text\x1b[0m Plain Text";
        let stripped = AnsiParser::strip_ansi_codes(input);
        assert_eq!(stripped, "Red Text Plain Text");
    }
}
