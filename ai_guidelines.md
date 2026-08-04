# AI Coding Guidelines for Rust (Android Terminal Tool)

You are an expert Rust software engineer. Follow every rule in this document strictly. Do not skip any rule.

---

## RULE 1: Safety & `unsafe` Policy (STRICT)
* **`unsafe` IS FORBIDDEN:**
  * Put `#![deny(unsafe_code)]` at the root of every crate/file.
  * Never write `unsafe { ... }` blocks for any reason.
  * Use safe standard Rust code and safe crates only.

---

## RULE 2: Modular Architecture & File Limits
* **Strict Modular Structure:**
  * Every module must have ONE clear responsibility (e.g., `ui/`, `network/`, `config/`).
  * Never mix business logic with Terminal UI logic.
* **800-Line Maximum Code Limit:**
  * **NO SINGLE FILE or CODE BLOCK may exceed 800 lines.**
  * If a file approaches ~600 lines, split it into smaller sub-modules immediately (e.g., `parser.rs` -> `parser/lexer.rs`, `parser/ast.rs`).

---

## RULE 3: Documentation First & Read Extra `.md` Files
* **Mandatory Inline Documentation:**
  * Every function, struct, enum, and trait MUST have `///` rustdoc comments explaining:
    1. What it does.
    2. What arguments it takes.
    3. What it returns / what errors it can throw.
* **Read `.md` Files Before Modifying Code:**
  * Before changing or creating any code file, read all related `.md` documentation files in the project/module directory first.
  * Keep module-specific `.md` files updated whenever you change architecture or flow.

---

## RULE 4: Unit Testing Requirement
* **Mandatory Unit Tests:**
  * Write `#[cfg(test)]` unit tests for every newly created or refactored core logic module.
  * Ensure critical execution paths and potential error cases are covered by tests.

---

## RULE 5: Allowed Crates & Dependencies
* **Restricted Dependency Usage:**
  * Prefer standard library types first.
  * Approved core crates: `ratatui` or `crossterm` (for TUI), `tokio` (for async), `serde` / `serde_json` (for serialization), `anyhow` / `thiserror` (for error handling).
  * Do NOT add new third-party crates to `Cargo.toml` without explicit user permission.

---

## RULE 6: Change Summary Output
* **Mandatory Response Format:**
  * At the end of every response where code is modified or created, provide a brief bulleted summary of what was changed and why.

---

## RULE 7: Self-Correction & Verification Loop
* **Double-Check Before Output:**
  * Mentally test the code against `cargo check` and `cargo clippy` rules before answering.
  * Check for borrow checker issues, lifetime mismatches, and type safety.
  * If you find an error in your code while writing, correct it before presenting the final answer.
* **No Quick Fixes:**
  * Fix the root cause of an error. Do not silence compiler warnings or wrap bad code in quick workarounds.

---

## RULE 8: Error Handling Rules
* **No Crashes in Production:**
  * Never use `.unwrap()` or `.expect()` outside unit tests.
* **Error Types:**
  * Use `anyhow` for application-level context handling.
  * Use `thiserror` for library or domain-level error enums.

---

## RULE 9: System Defaults
* **Async:** Use `tokio` for async tasks. Keep async isolated from core UI logic.
* **Dispatch:** Prefer static dispatch (Generics / Traits) over dynamic dispatch (`Box<dyn Trait>`).