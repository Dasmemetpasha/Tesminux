/// Parsed ANSI token / sequence
#[derive(Debug, PartialEq, Eq, Clone)]
pub enum AnsiSequence {
    Text(String),
    ClearScreen,
    SetColor(u8),
    ResetColor,
    CursorMove { row: u16, col: u16 },
}

/// Represents a styled text segment with optional 8-bit ANSI foreground color and bold flag.
#[derive(Debug, PartialEq, Eq, Clone)]
pub struct AnsiSegment {
    pub text: String,
    pub fg_color: Option<u8>,
    pub is_bold: bool,
}

/// Helper for parsing ANSI sequence strings.
pub struct AnsiParser;

impl AnsiParser {
    pub fn new() -> Self {
        Self
    }

    /// Strips ANSI escape sequences from string input.
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

    /// Parses raw output into styled `AnsiSegment` instances.
    pub fn parse_segments(input: &str) -> Vec<AnsiSegment> {
        let mut segments = Vec::new();
        let mut current_text = String::new();
        let mut current_fg: Option<u8> = None;
        let mut current_bold = false;

        let mut chars = input.chars().peekable();

        while let Some(ch) = chars.next() {
            if ch == '\x1b' && chars.peek() == Some(&'[') {
                chars.next(); // Consume '['

                // Flush pending text before sequence change
                if !current_text.is_empty() {
                    segments.push(AnsiSegment {
                        text: std::mem::take(&mut current_text),
                        fg_color: current_fg,
                        is_bold: current_bold,
                    });
                }

                let mut params = String::new();
                while let Some(&p) = chars.peek() {
                    if p.is_ascii_digit() || p == ';' {
                        params.push(p);
                        chars.next();
                    } else {
                        break;
                    }
                }

                let cmd_char = chars.next().unwrap_or(' ');
                if cmd_char == 'm' {
                    // SGR (Select Graphic Rendition)
                    if params.is_empty() || params == "0" {
                        current_fg = None;
                        current_bold = false;
                    } else {
                        for param in params.split(';') {
                            match param.parse::<u8>() {
                                Ok(0) => {
                                    current_fg = None;
                                    current_bold = false;
                                }
                                Ok(1) => current_bold = true,
                                Ok(c @ 30..=37) => current_fg = Some(c - 30),
                                Ok(c @ 90..=97) => current_fg = Some(c - 90 + 8),
                                Ok(39) => current_fg = None,
                                _ => {}
                            }
                        }
                    }
                }
            } else {
                current_text.push(ch);
            }
        }

        if !current_text.is_empty() {
            segments.push(AnsiSegment {
                text: current_text,
                fg_color: current_fg,
                is_bold: current_bold,
            });
        }

        segments
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

impl Default for AnsiParser {
    fn default() -> Self {
        Self::new()
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

    #[test]
    fn test_parse_segments() {
        let input = "Normal \x1b[31mRed\x1b[0m \x1b[1;32mBold Green\x1b[0m";
        let segments = AnsiParser::parse_segments(input);

        assert_eq!(segments.len(), 4);
        assert_eq!(
            segments[0],
            AnsiSegment {
                text: "Normal ".to_string(),
                fg_color: None,
                is_bold: false,
            }
        );
        assert_eq!(
            segments[1],
            AnsiSegment {
                text: "Red".to_string(),
                fg_color: Some(1),
                is_bold: false,
            }
        );
        assert_eq!(
            segments[2],
            AnsiSegment {
                text: " ".to_string(),
                fg_color: None,
                is_bold: false,
            }
        );
        assert_eq!(
            segments[3],
            AnsiSegment {
                text: "Bold Green".to_string(),
                fg_color: Some(2),
                is_bold: true,
            }
        );
    }
}
