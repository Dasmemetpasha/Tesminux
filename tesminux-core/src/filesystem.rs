use std::fs;
use std::path::{Path, PathBuf};

/// Manages directory paths and file operations within the Tesminux sandbox.
pub struct FileManager {
    root: PathBuf,
}

impl FileManager {
    /// Creates a new `FileManager` rooted at the given directory path.
    pub fn new<P: AsRef<Path>>(root: P) -> Self {
        Self {
            root: root.as_ref().to_path_buf(),
        }
    }

    /// Returns the root path.
    pub fn root(&self) -> &Path {
        &self.root
    }

    /// Checks if a relative path exists within the sandbox root.
    pub fn exists<P: AsRef<Path>>(&self, path: P) -> bool {
        self.root.join(path).exists()
    }

    /// Creates a directory relative to the sandbox root.
    pub fn create_dir<P: AsRef<Path>>(&self, path: P) -> std::io::Result<()> {
        fs::create_dir_all(self.root.join(path))
    }

    /// Lists entries in a directory relative to the root.
    pub fn list_dir<P: AsRef<Path>>(&self, path: P) -> std::io::Result<Vec<String>> {
        let full_path = self.root.join(path);
        let entries = fs::read_dir(full_path)?;
        let mut result = Vec::new();

        for entry in entries.flatten() {
            if let Some(name) = entry.file_name().to_str() {
                result.push(name.to_string());
            }
        }

        Ok(result)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::env;

    #[test]
    fn test_file_manager() {
        let temp_dir = env::temp_dir().join("tesminux_fs_test");
        let fm = FileManager::new(&temp_dir);

        assert_eq!(fm.root(), temp_dir.as_path());

        assert!(fm.create_dir("test_sub_dir").is_ok());
        assert!(fm.exists("test_sub_dir"));

        let list = fm.list_dir("").unwrap();
        assert!(list.contains(&"test_sub_dir".to_string()));

        let _ = fs::remove_dir_all(temp_dir);
    }
}
