use std::path::{Path, PathBuf};

pub struct FileManager {
    root: PathBuf,
}

impl FileManager {
    pub fn new<P: AsRef<Path>>(root: P) -> Self {
        Self {
            root: root.as_ref().to_path_buf(),
        }
    }

    pub fn root(&self) -> &Path {
        &self.root
    }

    pub fn exists<P: AsRef<Path>>(&self, path: P) -> bool {
        self.root.join(path).exists()
    }
}
