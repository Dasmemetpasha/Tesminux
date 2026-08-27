pub mod ansi;
pub mod commands;
pub mod filesystem;
pub mod terminal;

use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint};
use jni::JNIEnv;

use std::fs;
use std::sync::{Mutex, OnceLock};

use crate::terminal::session::SessionManager;

const CACHE: &str = "/data/data/com.tesminux.app/cache";

static SESSIONS: OnceLock<Mutex<SessionManager>> = OnceLock::new();

fn get_sessions() -> &'static Mutex<SessionManager> {
    SESSIONS.get_or_init(|| Mutex::new(SessionManager::new()))
}

fn prepare_environment() -> Result<(), Box<dyn std::error::Error>> {
    // /storage/emulated/0 is the user's external storage root and always exists.
    // We only need to ensure the app cache directory is available.
    fs::create_dir_all(CACHE)?;
    Ok(())
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_tesminux_app_MainActivity_tesminuxStart(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    if prepare_environment().is_err() {
        return -1;
    }

    let sessions = get_sessions();

    let mut sessions = match sessions.lock() {
        Ok(value) => value,
        Err(_) => return -1,
    };

    let terminal = match sessions.current_terminal_mut() {
        Some(t) => t,
        None => return -1,
    };

    match terminal.start() {
        Ok(_) => 0,
        Err(_) => -1,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_tesminux_app_MainActivity_tesminuxWrite(
    mut env: JNIEnv,
    _class: JClass,
    input: JString,
) -> jint {
    let input: String = match env.get_string(&input) {
        Ok(value) => value.into(),
        Err(_) => return -1,
    };

    let sessions = get_sessions();

    let mut sessions = match sessions.lock() {
        Ok(value) => value,
        Err(_) => return -1,
    };

    let terminal = match sessions.current_terminal_mut() {
        Some(t) => t,
        None => return -1,
    };

    match terminal.write(&input) {
        Ok(_) => 0,
        Err(_) => -1,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_tesminux_app_MainActivity_tesminuxRead<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> JString<'local> {
    let sessions = get_sessions();

    let output = match sessions.lock() {
        Ok(sessions) => match sessions.current_terminal() {
            Some(t) => t.output(),
            None => String::new(),
        },
        Err(_) => String::new(),
    };

    match env.new_string(output) {
        Ok(s) => s,
        Err(_) => env.new_string("").unwrap_or_else(|_| JString::default()),
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_tesminux_app_MainActivity_tesminuxIsRunning(
    _env: JNIEnv,
    _class: JClass,
) -> jboolean {
    let sessions = get_sessions();

    let mut sessions = match sessions.lock() {
        Ok(value) => value,
        Err(_) => return 0,
    };

    match sessions.current_terminal_mut() {
        Some(terminal) => {
            if terminal.is_running() {
                1
            } else {
                0
            }
        }
        None => 0,
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_tesminux_app_MainActivity_tesminuxClear(
    _env: JNIEnv,
    _class: JClass,
) {
    let sessions = get_sessions();

    if let Ok(mut sessions) = sessions.lock() {
        let terminal = sessions.current_terminal_mut();
        if let Some(terminal) = terminal {
            terminal.clear_output();
        }
    }
}