# ==============================================================================
# TESMINUX AI BIBLE
# ==============================================================================
#
# File: AI_GUIDELINES.md
#
# This file is the primary knowledge source for every AI and developer working
# on Tesminux.
#
# Every architectural decision should follow this document.
#
# ==============================================================================

# Chapter 1
# PROJECT VISION
# AI CONTRACT
# DEVELOPMENT PHILOSOPHY

---

# 1.0 About Tesminux

Tesminux is an Android terminal emulator written around a Rust core.

The goal is NOT to copy Termux.

The goal is NOT to clone another application.

Tesminux exists to become a modern, lightweight, secure and fully open-source
terminal designed with long-term maintainability in mind.

Every design decision should move the project toward that goal.

---

# 1.1 Mission

Tesminux should become one of the best open-source Android terminal
applications.

The application should be:

• Fast

• Stable

• Lightweight

• Modular

• Beautiful

• Easy to maintain

• Beginner friendly

• Power user friendly

The project should remain understandable years later.

---

# 1.2 Vision

Tesminux should eventually include:

- Multi Terminal Tabs
- File Manager
- SSH
- SFTP
- Git
- GitHub
- Theme System
- Plugin Support
- Multiple Shells
- Split Screen
- Material You
- External Keyboard Support
- Search
- Command History
- Terminal Profiles
- Settings
- Backup
- Restore
- Session Persistence

without sacrificing simplicity.

---

# 1.3 Philosophy

The project follows these principles.

1.

Architecture first.

2.

Code quality before speed.

3.

User experience before developer convenience.

4.

Readability before cleverness.

5.

Maintainability before feature count.

6.

Security before optimization.

7.

Refactor instead of rewrite.

---

# 1.4 Non Goals

Tesminux is NOT trying to become:

- Linux distribution
- IDE
- Game launcher
- Desktop Environment

Those are outside project scope.

---

# 1.5 AI Contract

Every AI working on Tesminux MUST follow these rules.

The AI MUST understand the current architecture before modifying code.

The AI MUST inspect related modules before implementing features.

The AI MUST preserve working behavior whenever possible.

The AI MUST explain architectural changes.

The AI MUST prefer refactoring instead of rewriting.

The AI MUST avoid duplicate code.

The AI MUST avoid duplicate JNI exports.

The AI MUST avoid duplicate structs.

The AI MUST avoid duplicate modules.

The AI MUST avoid unnecessary dependencies.

The AI MUST avoid changing Cargo.toml without permission.

---

# 1.6 AI MUST NEVER

Never invent project structure.

Never guess APIs.

Never create fake modules.

Never output placeholder implementations as production code.

Never leave TODOs instead of implementations.

Never break existing functionality.

Never replace stable code with example code.

Never ignore compiler errors.

Never ignore warnings without reason.

Never bypass safety checks.

Never hide bugs.

Never remove documentation.

---

# 1.7 If Information Is Missing

When required information does not exist:

STOP.

Ask the user.

Do not guess.

Never hallucinate architecture.

Never assume APIs exist.

Never fabricate module names.

---

<<<<<<< HEAD
# 1.8 Decision Order

Whenever multiple solutions exist:

1. Keep compatibility.

2. Keep architecture clean.

3. Keep code modular.

4. Improve readability.

5. Improve performance.

6. Add features.

This order should never change.

---

# 1.9 Project Identity

Project Name

Tesminux

Programming Language

Rust

Platform

Android

UI

Jetpack Compose

Bridge

JNI

Core

Rust

License

GPL-3.0

Minimum Android

Android 7+

---

# 1.10 Long Term Goal

Tesminux should become a professional Android terminal application
that remains easy to maintain for many years.

No feature is worth sacrificing architecture.

No optimization is worth sacrificing readability.

Good software is built by continuously improving the architecture,
not by continuously increasing complexity.

End of Chapter 1.

# ==============================================================================
# CHAPTER 2
# COMPLETE PROJECT ARCHITECTURE
# ==============================================================================

This chapter defines the official Tesminux architecture.

Every module, file and subsystem must respect this architecture.

No implementation may violate these rules without explicit approval.

Architecture consistency is considered more important than adding new features.

==============================================================================

2.0 CORE PRINCIPLES

The project follows a layered architecture.

+------------------------------------------------------+
|                 Android UI (Compose)                 |
+------------------------------------------------------+
|                  JNI Bridge (lib.rs)                 |
+------------------------------------------------------+
|                 Tesminux Rust Core                   |
+------------------------------------------------------+
|   Terminal   Sessions   Commands   History   Config  |
+------------------------------------------------------+
|                    PTY / Shell                       |
+------------------------------------------------------+
|                    Android OS                        |
+------------------------------------------------------+

No module may skip layers.

The UI must never directly manipulate terminal internals.

JNI is the only bridge.

==============================================================================

2.1 HIGH LEVEL MODULES

The project consists of independent modules.

Core Modules

• terminal
• commands
• history
• session
• pty
• config
• settings
• filesystem
• theme
• plugins (future)

Each module owns exactly one responsibility.

==============================================================================

2.2 RESPONSIBILITY RULE

Every file answers exactly ONE question.

Examples

terminal.rs

Responsible for terminal lifecycle only.

history.rs

Responsible only for command history.

session.rs

Responsible only for session management.

commands.rs

Responsible only for built-in commands.

pty.rs

Responsible only for PTY communication.

Never merge unrelated responsibilities.

==============================================================================

2.3 DIRECTORY STRUCTURE

Official layout

src/

lib.rs

commands/

terminal/

history/

pty/

session/

config/

filesystem/

theme/

android/

jni/

tests/

docs/

assets/

Every folder should contain only related code.

==============================================================================

2.4 TERMINAL ENGINE

The terminal engine is the heart of Tesminux.

Nothing may directly bypass it.

Responsibilities

Start terminal

Stop terminal

Restart terminal

Read output

Write input

Manage PTY

History integration

Session integration

Everything else is external.

==============================================================================

2.5 JNI

JNI exists only for communication.

JNI never contains business logic.

Allowed

Convert strings

Convert booleans

Convert integers

Forward calls

Not allowed

PTY logic

History logic

Parsing

Session management

Command execution

JNI should stay as thin as possible.

==============================================================================

2.6 SESSION MANAGER

SessionManager owns all sessions.

Nothing else may own sessions.

Responsibilities

Create session

Destroy session

Switch session

Current session

Session count

Session IDs

Never manipulate Vec<Session> outside SessionManager.

==============================================================================

2.7 SESSION OBJECT

Each Session owns

Terminal

Session ID

Future metadata

Nothing more.

Session must never own UI.

==============================================================================

2.8 TERMINAL OBJECT

Terminal owns

PTY

History

State

Output

Nothing else.

Terminal never owns Android objects.

==============================================================================

2.9 COMMAND SYSTEM

Built-in commands execute before PTY.

Flow

User Input

↓

Built-in Commands

↓

PTY

If built-in command handles input

PTY never receives it.

==============================================================================

2.10 HISTORY

History is independent.

Responsibilities

Load

Save

Append

Clear

Search (future)

Autocomplete (future)

==============================================================================

2.11 PTY

PTY owns

Shell Process

stdin

stdout

reader thread

writer

buffer

PTY never touches Android.

==============================================================================

2.12 THREADING

Allowed Threads

UI Thread

Reader Thread

Worker Thread

Background Tasks

Never block UI Thread.

==============================================================================

2.13 OUTPUT FLOW

Keyboard

↓

JNI

↓

Terminal

↓

Command System

↓

PTY

↓

Shell

↓

PTY

↓

Terminal Buffer

↓

JNI

↓

Compose

==============================================================================

2.14 INPUT FLOW

Compose TextField

↓

JNI

↓

Terminal.write()

↓

Commands

↓

PTY

==============================================================================

2.15 DEPENDENCY GRAPH

Compose

↓

JNI

↓

Terminal

↓

PTY

Terminal

↓

History

Session

↓

Terminal

Commands

↓

Terminal

History never depends on Session.

PTY never depends on Compose.

==============================================================================

2.16 FILE SIZE POLICY

Target

200-400 lines

Warning

600 lines

Maximum

800 lines

Split modules before exceeding limits.

==============================================================================

2.17 MODULE OWNERSHIP

Every struct has one owner.

Never duplicate ownership.

Example

SessionManager

owns Session

Session

owns Terminal

Terminal

owns PTY

PTY

owns Process

Ownership must remain clear.

==============================================================================

2.18 FUTURE MODULES

Future architecture

SSH

FTP

SFTP

Git

GitHub

Plugin API

Language Runtime

Package Manager

Every new module must remain isolated.

==============================================================================

2.19 REFACTOR POLICY

Refactor only one subsystem at a time.

Never rewrite the whole project.

Always preserve stable APIs whenever possible.

==============================================================================

2.20 FINAL ARCHITECTURE RULE

Architecture is the product.

Features are temporary.

Architecture is permanent.

Every commit must improve the architecture.

Never trade architecture for speed.

==============================================================================
END OF CHAPTER 2
==============================================================================

# ==============================================================================
# CHAPTER 3
# OFFICIAL RUST CODING STANDARD
# ==============================================================================

This chapter defines the mandatory Rust coding standards for Tesminux.

Every Rust source file inside the project MUST follow these rules.

These rules exist to maximize readability, maintainability,
performance and long-term project stability.

==============================================================================

3.0 PHILOSOPHY

Rust code should be boring.

"Boring" means:

Predictable.

Simple.

Readable.

Maintainable.

Easy to debug.

Easy to refactor.

Never write code to impress.

Write code another developer can understand after two years.

==============================================================================

3.1 READABILITY FIRST

Readable code is preferred over short code.

Preferred

let current_session = manager.current();

Avoid

let s = m.c();

Variable names should explain intent.

==============================================================================

3.2 FILE ORGANIZATION

Every Rust file should follow this order.

1.

Module Documentation

2.

Imports

3.

Constants

4.

Type Aliases

5.

Enums

6.

Structs

7.

Traits

8.

Implementations

9.

Private Helpers

10.

Tests

Never randomly mix declarations.

==============================================================================

3.3 IMPORT RULES

Imports should be grouped.

Standard Library

↓

External Crates

↓

Project Modules

Example

use std::fs;
use std::path::Path;

use anyhow::Result;

use crate::terminal::Terminal;

==============================================================================

3.4 STRUCT DESIGN

Each struct owns one responsibility.

Bad

struct Terminal {

pty

history

settings

theme

network

plugins

}

Good

Terminal

History

Settings

Theme

PluginManager

==============================================================================

3.5 ENUMS

Enums should represent states.

Good

Running

Stopped

Starting

Closing

Bad

enum Data

==============================================================================

3.6 FUNCTION SIZE

Ideal

5–30 lines

Acceptable

50 lines

Warning

80 lines

Maximum

120 lines

Split large functions immediately.

==============================================================================

3.7 FUNCTION RULES

Every function should answer one question.

Good

start()

stop()

restart()

write()

read()

Avoid

start_and_initialize_everything()

==============================================================================

3.8 RETURN TYPES

Prefer

Result<T>

Option<T>

Never use panic for expected failures.

==============================================================================

3.9 ERROR HANDLING

Errors are data.

Every recoverable failure returns Result.

Never ignore errors.

Never silently swallow errors.

==============================================================================

3.10 UNWRAP POLICY

Production code

Forbidden

unwrap()

expect()

Allowed

Unit Tests

Benchmarks

Prototypes

==============================================================================

3.11 COMMENTS

Comments explain WHY.

Code explains HOW.

Bad

// increment x

x += 1;

Good

// Skip invalid UTF-8 because Android shell output
// may contain partial sequences.

==============================================================================

3.12 DOCUMENTATION

Every public item must use rustdoc.

///

Explain

Purpose

Arguments

Return Value

Errors

Example

==============================================================================

3.13 TRAITS

Prefer traits over duplicated code.

Never create traits with one implementation unless future expansion exists.

==============================================================================

3.14 OWNERSHIP

Respect ownership.

Avoid cloning unnecessarily.

Borrow when possible.

Move only when ownership changes.

==============================================================================

3.15 BORROWING

Prefer

&str

instead of

String

when ownership isn't required.

Prefer

&Path

instead of

PathBuf

when possible.

==============================================================================

3.16 STRINGS

Avoid repeated allocations.

Reuse buffers.

Prefer push_str().

Avoid format! in loops.

==============================================================================

3.17 VECTORS

Reserve capacity when known.

Example

Vec::with_capacity()

==============================================================================

3.18 ITERATORS

Prefer iterators over manual indexing.

Preferred

iter()

iter_mut()

map()

filter()

collect()

==============================================================================

3.19 MATCH

Prefer exhaustive match.

Avoid wildcard if new variants may appear.

==============================================================================

3.20 IF STATEMENTS

Avoid deep nesting.

Preferred

Early return.

==============================================================================

3.21 CONSTANTS

Magic numbers are forbidden.

Use const.

==============================================================================

3.22 MODULE VISIBILITY

Everything private by default.

Expose only required APIs.

==============================================================================

3.23 THREAD SAFETY

Shared state must use

Mutex

RwLock

Arc

Never use unsafe synchronization.

==============================================================================

3.24 PERFORMANCE

Measure before optimizing.

Architecture is more important than micro-optimizations.

==============================================================================

3.25 CLIPPY

Every new code should conceptually pass

cargo clippy

without unnecessary warnings.

==============================================================================

3.26 FORMATTING

Project formatting follows

cargo fmt

Never manually align code.

==============================================================================

3.27 TESTING

Every core module should eventually contain

#[cfg(test)]

unit tests.

==============================================================================

3.28 REFACTOR

Refactor in small commits.

Never rewrite the project in one commit.

==============================================================================

3.29 DEPENDENCIES

Every new dependency requires approval.

Prefer std.

Prefer existing project code.

==============================================================================

3.30 FINAL RULE

Future maintainability is the highest priority.

Readable code outlives clever code.

==============================================================================
END OF CHAPTER 3
==============================================================================

# ==============================================================================
# CHAPTER 4
# ANDROID / JNI STANDARDS
# ==============================================================================

This chapter defines the official Android architecture.

==============================================================================

4.0 PURPOSE

Android UI exists only to present information.

Business logic belongs inside Rust.

Compose must remain as thin as possible.

==============================================================================

4.1 RESPONSIBILITY

Android

↓

Display UI

↓

Receive User Input

↓

Call JNI

↓

Receive Result

↓

Update UI

Nothing more.

==============================================================================

4.2 UI RULE

Never execute business logic inside Compose.

Never parse terminal output inside Compose.

Never implement command logic in Kotlin.

==============================================================================

4.3 JNI RULE

JNI is a bridge.

JNI is NOT the application.

JNI should only

Receive

↓

Convert

↓

Forward

↓

Return

==============================================================================

4.4 JNI EXPORTS

Each JNI function has exactly one responsibility.

Good

tesminuxStart()

tesminuxWrite()

tesminuxRead()

tesminuxClear()

Bad

tesminuxStartAndCreateSession()

==============================================================================

4.5 DUPLICATE POLICY

JNI exports must never be duplicated.

Compiler errors caused by duplicated JNI exports
must be fixed immediately.

==============================================================================

4.6 MEMORY

JNI never stores Android Context.

JNI never stores Activity.

JNI never owns UI objects.

==============================================================================

4.7 THREADING

UI Thread

↓

JNI

↓

Rust

↓

Worker Thread

Never block Android Main Thread.

==============================================================================

4.8 TERMINAL OUTPUT

Terminal output belongs to Rust.

Compose only renders text.

==============================================================================

4.9 TERMINAL INPUT

Compose

↓

JNI

↓

Terminal.write()

==============================================================================

4.10 STATE

Compose owns UI state.

Rust owns terminal state.

Never mix ownership.

==============================================================================

4.11 FUTURE

Future JNI additions must remain backwards compatible whenever possible.

==============================================================================

END OF CHAPTER 4

# ==============================================================================
# CHAPTER 5
# TERMINAL ENGINE
# ==============================================================================

The Terminal module is the heart of Tesminux.

==============================================================================

5.0 PURPOSE

The Terminal controls

PTY

History

State

Input

Output

Nothing else.

==============================================================================

5.1 TERMINAL LIFECYCLE

new()

↓

start()

↓

running

↓

stop()

↓

drop()

==============================================================================

5.2 START RULE

Calling start()

twice

must never panic.

Second call should safely return.

==============================================================================

5.3 WRITE

Terminal::write()

Always validates state.

If PTY is unavailable

return Result::Err.

==============================================================================

5.4 OUTPUT

Output belongs inside Terminal.

JNI only reads it.

==============================================================================

5.5 BUFFER

Terminal owns output buffer.

Only Terminal may clear it.

==============================================================================

5.6 HISTORY

History belongs to Terminal.

Never manipulate history directly from JNI.

==============================================================================

5.7 SESSION

Terminal never knows about SessionManager.

SessionManager owns Terminal.

Not the opposite.

==============================================================================

5.8 FUTURE

Future features

Autocomplete

Aliases

Profiles

Bookmarks

Macros

must remain optional.

==============================================================================

END OF CHAPTER 5

# ==============================================================================
# CHAPTER 6
# SESSION MANAGER
# ==============================================================================

SessionManager owns every Session.

==============================================================================

6.0 GOAL

Provide multiple independent terminals.

==============================================================================

6.1 OWNERSHIP

SessionManager

↓

Session

↓

Terminal

↓

PTY

==============================================================================

6.2 CREATE

create_session()

Must

Create Terminal

Assign ID

Select new session

==============================================================================

6.3 SWITCH

Switching session

must never destroy PTY.

==============================================================================

6.4 CLOSE

Last session

cannot be removed.

At least one session always exists.

==============================================================================

6.5 SESSION ID

IDs never change.

Indices may change.

==============================================================================

6.6 FUTURE

Future metadata

Working directory

Name

Color

Creation time

Profile

==============================================================================

END OF CHAPTER 6

# ==============================================================================
# CHAPTER 7
# PTY ENGINE
# ==============================================================================

7.0 PURPOSE

The PTY module provides communication between Tesminux and the shell.

Responsibilities:

- Spawn shell
- Read stdout
- Write stdin
- Process lifecycle
- Buffer output
- Detect termination

==============================================================================

7.1 RULES

PTY never knows Android.

PTY never knows Compose.

PTY never knows JNI.

PTY only communicates with Terminal.

==============================================================================

7.2 PROCESS LIFECYCLE

Create

↓

Spawn

↓

Running

↓

Exit

↓

Cleanup

==============================================================================

7.3 BUFFER POLICY

Output is buffered.

Reads must never block UI.

==============================================================================

7.4 THREADS

Reader Thread

Writer Thread

Main Thread

No busy waiting.

==============================================================================

END OF CHAPTER 7

# ==============================================================================
# CHAPTER 8
# HISTORY SYSTEM
# ==============================================================================

History stores previously executed commands.

Features

- Append
- Save
- Load
- Clear
- Search (future)
- Autocomplete (future)

Rules

History never executes commands.

History never owns PTY.

History belongs to Terminal.

Duplicate entries may optionally be filtered.

History file must survive application restart.

END OF CHAPTER 8

# ==============================================================================
# CHAPTER 9
# COMMAND ENGINE
# ==============================================================================

Built-in commands execute before PTY.

Flow

User

↓

Command Engine

↓

PTY

If handled

↓

Return

Else

↓

Forward to shell

Built-in commands

help

about

version

history

clear

Future

alias

config

theme

plugins

END OF CHAPTER 9

# ==============================================================================
# CHAPTER 10
# FILE MANAGER
# ==============================================================================

Goals

Modern file browser.

Responsibilities

Browse

Rename

Delete

Copy

Move

Create Folder

Create File

Search

Future

Bookmarks

Favorites

Multi-select

Zip

Unzip

Preview

Rules

FileManager never accesses UI.

All operations go through Rust core.

END OF CHAPTER 10

# ==============================================================================
# CHAPTER 11
# SETTINGS SYSTEM
# ==============================================================================

11.0 PURPOSE

The Settings module stores every user configurable option.

Settings must survive application restarts.

==============================================================================

11.1 RESPONSIBILITIES

Store user preferences.

Load settings during startup.

Save settings after modification.

Provide default values.

Validate configuration values.

==============================================================================

11.2 SETTINGS CATEGORIES

General

Appearance

Terminal

Keyboard

Performance

Storage

Developer

Experimental

==============================================================================

11.3 STORAGE

Settings should be serialized.

Preferred formats:

JSON

TOML

Future migrations must preserve compatibility.

==============================================================================

11.4 DEFAULT VALUES

Every setting must define a default value.

The application must always be able to start using defaults.

==============================================================================

11.5 VALIDATION

Every value must be validated before saving.

Invalid values must never crash Tesminux.

==============================================================================

11.6 FUTURE SETTINGS

Font Size

Cursor Style

Cursor Blink

Theme

Material You

Word Wrap

Terminal Transparency

Bell

Animations

Startup Command

Shell Selection

==============================================================================

END OF CHAPTER 11

# ==============================================================================
# CHAPTER 12
# THEME ENGINE
# ==============================================================================

12.0 GOAL

Provide a modern appearance while keeping rendering efficient.

==============================================================================

12.1 THEMES

Light

Dark

System

Material You

Custom

==============================================================================

12.2 TERMINAL COLORS

ANSI 16

ANSI 256

TrueColor

==============================================================================

12.3 RESPONSIBILITIES

Manage colors.

Manage fonts.

Manage icon style.

Manage spacing.

==============================================================================

12.4 RULES

Themes never modify terminal behavior.

Themes only affect appearance.

==============================================================================

12.5 FUTURE

Import themes.

Export themes.

Online theme repository.

==============================================================================

END OF CHAPTER 12

# ==============================================================================
# CHAPTER 13
# CONFIGURATION
# ==============================================================================

13.0 PURPOSE

Configuration stores application level information.

==============================================================================

13.1 CONFIG FILE

Contains:

Version

Migration

Flags

Developer options

==============================================================================

13.2 VERSIONING

Configuration version must always be stored.

Future upgrades must migrate automatically.

==============================================================================

13.3 MIGRATIONS

Never delete unknown fields.

Gracefully migrate old versions.

==============================================================================

13.4 BACKUP

Configuration should support backup and restore.

==============================================================================

13.5 SAFETY

Corrupted configuration should never prevent application startup.

Fallback to defaults if recovery fails.

==============================================================================

END OF CHAPTER 13

# ==============================================================================
# CHAPTER 14
# ERROR HANDLING
# ==============================================================================

14.0 PRINCIPLE

Errors are expected.

Crashes are failures.

==============================================================================

14.1 RULES

Never panic during normal execution.

Prefer Result<T, E>.

Never ignore recoverable errors.

==============================================================================

14.2 ERROR TYPES

IO

JNI

PTY

Filesystem

Configuration

Serialization

Permission

==============================================================================

14.3 USER EXPERIENCE

Users should receive understandable messages.

Avoid exposing internal Rust errors.

==============================================================================

14.4 LOGGING

Every important error should be logged.

==============================================================================

14.5 RECOVERY

Whenever possible:

Recover

Continue

Notify

==============================================================================

END OF CHAPTER 14

# ==============================================================================
# CHAPTER 15
# LOGGING SYSTEM
# ==============================================================================

15.0 PURPOSE

Logs help debugging without affecting users.

==============================================================================

15.1 LOG LEVELS

TRACE

DEBUG

INFO

WARN

ERROR

==============================================================================

15.2 WHAT TO LOG

Startup

Shutdown

Session creation

Session closing

PTY creation

Errors

Warnings

Configuration loading

==============================================================================

15.3 WHAT NOT TO LOG

Passwords

Private keys

Authentication tokens

Sensitive user data

==============================================================================

15.4 ROTATION

Future versions should rotate log files automatically.

==============================================================================

15.5 DEBUG MODE

Developer mode may enable verbose logging.

Release mode should minimize unnecessary output.

==============================================================================

END OF CHAPTER 15

# ==============================================================================
# CHAPTER 16
# SECURITY
# ==============================================================================

16.0 PURPOSE

Security is a first-class feature.

No feature is allowed to reduce application security.

==============================================================================

16.1 PRINCIPLES

Least Privilege

Minimal Permissions

No Hidden Behavior

Open Source Verification

==============================================================================

16.2 USER DATA

User files belong to the user.

Tesminux never modifies user files without explicit request.

==============================================================================

16.3 TERMINAL

Terminal commands are executed only when initiated by the user.

Tesminux never executes hidden commands.

==============================================================================

16.4 FILESYSTEM

Never access protected Android directories unless permission exists.

==============================================================================

16.5 JNI

JNI must validate every incoming parameter.

==============================================================================

16.6 MEMORY

Never leak sensitive data.

Buffers containing passwords should be cleared when possible.

==============================================================================

16.7 FUTURE

SSH keys

Encrypted storage

Secure preferences

Biometric unlock

==============================================================================

END OF CHAPTER 16

# ==============================================================================
# CHAPTER 17
# PERFORMANCE
# ==============================================================================

17.0 GOAL

Fast startup.

Low memory usage.

Smooth scrolling.

==============================================================================

17.1 STARTUP

Target startup time

<500ms

==============================================================================

17.2 MEMORY

Avoid unnecessary allocations.

Reuse buffers.

==============================================================================

17.3 CPU

Avoid busy loops.

Sleep while idle.

==============================================================================

17.4 UI

Never block Compose.

Heavy work belongs to Rust worker threads.

==============================================================================

17.5 BENCHMARKS

Measure before optimizing.

==============================================================================

END OF CHAPTER 17

# ==============================================================================
# CHAPTER 18
# MEMORY MANAGEMENT
# ==============================================================================

18.0 OWNERSHIP

Rust ownership rules must never be bypassed.

==============================================================================

18.1 ARC

Use Arc only when ownership sharing is required.

==============================================================================

18.2 MUTEX

Use Mutex only for mutable shared state.

==============================================================================

18.3 CLONE

Avoid cloning large structures.

==============================================================================

18.4 DROP

Resources should be released automatically.

==============================================================================

18.5 LEAKS

Memory leaks are considered critical bugs.

==============================================================================

END OF CHAPTER 18

# ==============================================================================
# CHAPTER 19
# THREADING
# ==============================================================================

19.0 GOAL

Keep UI responsive.

==============================================================================

19.1 THREAD TYPES

UI Thread

PTY Reader

PTY Writer

Worker Thread

==============================================================================

19.2 RULES

Never block UI.

Never spin forever.

==============================================================================

19.3 COMMUNICATION

Use channels when appropriate.

Avoid global mutable state.

==============================================================================

19.4 LOCKING

Hold locks for the shortest time possible.

==============================================================================

19.5 DEADLOCKS

Nested locking should be avoided.

==============================================================================

END OF CHAPTER 19

# ==============================================================================
# CHAPTER 20
# PLUGIN API
# ==============================================================================

20.0 PURPOSE

Allow future extensions without modifying the core.

==============================================================================

20.1 GOALS

Safe

Modular

Optional

Versioned

==============================================================================

20.2 PLUGINS MAY

Add commands

Add themes

Add tools

Add integrations

==============================================================================

20.3 PLUGINS MAY NOT

Modify core memory.

Replace Terminal.

Replace PTY.

Modify SessionManager directly.

==============================================================================

20.4 VERSIONING

Plugin API must remain stable whenever possible.

Breaking changes require version increment.

==============================================================================

20.5 FUTURE

Marketplace

Plugin signing

Sandboxing

Dependency resolution

==============================================================================

END OF CHAPTER 20

# ==============================================================================
# CHAPTER 21
# PACKAGE MANAGER
# ==============================================================================

21.0 PURPOSE

Provide a unified interface for installing, updating and removing packages.

The package manager must support multiple package sources in the future.

==============================================================================

21.1 DESIGN GOALS

Simple

Reliable

Extensible

Offline Friendly

==============================================================================

21.2 PACKAGE SOURCES

Future supported sources

APT

APK

Pacman

DNF

Custom repositories

==============================================================================

21.3 OPERATIONS

Install

Remove

Update

Upgrade

Search

List

Verify

==============================================================================

21.4 RULES

Never execute package operations without user confirmation.

Package installation logs must be preserved.

==============================================================================

END OF CHAPTER 21

# ==============================================================================
# CHAPTER 22
# SSH SYSTEM
# ==============================================================================

22.0 PURPOSE

Provide secure remote shell access.

==============================================================================

22.1 FEATURES

Password Login

Public Key Login

Host Verification

Known Hosts

Port Selection

==============================================================================

22.2 SECURITY

Private keys must never leave the device.

Unknown hosts require confirmation.

==============================================================================

22.3 FUTURE

SSH Config

Port Forwarding

Agent Forwarding

==============================================================================

END OF CHAPTER 22

# ==============================================================================
# CHAPTER 23
# GIT ENGINE
# ==============================================================================

23.0 PURPOSE

Provide native Git integration.

==============================================================================

23.1 FEATURES

Clone

Commit

Push

Pull

Fetch

Status

Branches

Tags

==============================================================================

23.2 DESIGN

Git module must remain independent.

No UI code.

No Android dependencies.

==============================================================================

23.3 FUTURE

Interactive rebase

Cherry-pick

Bisect

==============================================================================

END OF CHAPTER 23

# ==============================================================================
# CHAPTER 24
# GITHUB
# ==============================================================================

24.0 PURPOSE

Optional GitHub integration.

==============================================================================

24.1 FEATURES

Login

Repositories

Issues

Pull Requests

Releases

Notifications

==============================================================================

24.2 SECURITY

OAuth preferred.

Tokens stored securely.

Never expose credentials in logs.

==============================================================================

24.3 FUTURE

GitHub Actions

Discussions

Projects

Copilot compatibility

==============================================================================

END OF CHAPTER 24

# ==============================================================================
# CHAPTER 25
# STORAGE
# ==============================================================================

25.0 PURPOSE

Manage Android filesystem safely.

==============================================================================

25.1 STORAGE TYPES

Internal Storage

External Storage

SAF

Cache

==============================================================================

25.2 RULES

Never assume filesystem permissions.

Always verify access.

==============================================================================

25.3 CACHE

Temporary files belong in cache.

Cache must be removable.

==============================================================================

25.4 FUTURE

Cloud backup

Encrypted storage

Automatic cleanup

==============================================================================

END OF CHAPTER 25

# ==============================================================================
# CHAPTER 26
# TESTING FRAMEWORK
# ==============================================================================

26.0 PURPOSE

Testing guarantees long-term project stability.

Every core module should eventually be covered by automated tests.

==============================================================================

26.1 TEST TYPES

Unit Tests

Integration Tests

Regression Tests

Performance Tests

JNI Tests

UI Tests

==============================================================================

26.2 UNIT TESTS

Every module should test:

Normal behavior

Boundary conditions

Error conditions

==============================================================================

26.3 INTEGRATION TESTS

Verify interaction between:

Terminal

PTY

SessionManager

History

Commands

==============================================================================

26.4 REGRESSION TESTS

Every fixed bug should receive a regression test.

The same bug must never return unnoticed.

==============================================================================

26.5 PERFORMANCE TESTS

Measure:

Startup time

Memory usage

Terminal latency

Output rendering

==============================================================================

26.6 RULES

Tests must never depend on execution order.

Tests should remain deterministic.

==============================================================================

END OF CHAPTER 26

# ==============================================================================
# CHAPTER 27
# CI / CD
# ==============================================================================

27.0 PURPOSE

Automate quality checks before release.

==============================================================================

27.1 PIPELINE

Checkout

↓

Cargo fmt

↓

Cargo clippy

↓

Cargo test

↓

Android Build

↓

Artifact Generation

==============================================================================

27.2 REQUIRED CHECKS

Formatting

Compilation

Tests

Warnings

==============================================================================

27.3 OPTIONAL CHECKS

Benchmarks

Coverage

Security Audit

==============================================================================

27.4 RELEASE BRANCHES

main

Stable releases.

develop

Active development.

feature/*

Experimental work.

==============================================================================

27.5 GOAL

Every commit on main should build successfully.

==============================================================================

END OF CHAPTER 27

# ==============================================================================
# CHAPTER 28
# RELEASE ENGINEERING
# ==============================================================================

28.0 VERSIONING

Semantic Versioning

MAJOR.MINOR.PATCH

Example

1.4.2

==============================================================================

28.1 RELEASE TYPES

Alpha

Beta

Release Candidate

Stable

Hotfix

==============================================================================

28.2 REQUIREMENTS

Successful build

Updated documentation

Version bump

Release notes

==============================================================================

28.3 CHANGELOG

Every release must include:

New Features

Bug Fixes

Breaking Changes

Known Issues

==============================================================================

28.4 SIGNING

Future releases should support cryptographic signing.

==============================================================================

END OF CHAPTER 28

# ==============================================================================
# CHAPTER 29
# CONTRIBUTION GUIDE
# ==============================================================================

29.0 PURPOSE

Maintain a consistent contribution process.

==============================================================================

29.1 BEFORE CODING

Read AI_GUIDELINES.md

Understand the architecture.

Review existing modules.

==============================================================================

29.2 PULL REQUESTS

One feature per pull request.

Small focused changes are preferred.

==============================================================================

29.3 COMMIT MESSAGES

Use descriptive commit messages.

Examples

Add session switching

Fix PTY output race

Improve history persistence

==============================================================================

29.4 CODE REVIEW

Review for:

Correctness

Readability

Performance

Security

Documentation

==============================================================================

29.5 RESPECT

All contributors should maintain a respectful and constructive environment.

==============================================================================

END OF CHAPTER 29

# ==============================================================================
# CHAPTER 30
# AI BEHAVIOUR SPECIFICATION
# ==============================================================================

30.0 PURPOSE

Define how AI assistants should contribute to Tesminux.

==============================================================================

30.1 BEFORE WRITING CODE

Understand the request.

Inspect related modules.

Preserve architecture.

==============================================================================

30.2 WHEN MODIFYING CODE

Prefer extending existing systems.

Avoid unnecessary rewrites.

Avoid duplicated implementations.

==============================================================================

30.3 WHEN FIXING BUGS

Identify the root cause.

Avoid temporary workarounds when a proper fix is feasible.

Add regression tests when possible.

==============================================================================

30.4 DOCUMENTATION

Every significant architectural change should update documentation.

==============================================================================

30.5 COMMUNICATION

Explain why a change is made.

Mention trade-offs.

State assumptions clearly.

==============================================================================

30.6 NEVER

Never invent APIs.

Never fabricate project structure.

Never duplicate JNI exports.

Never remove working functionality without replacement.

Never silently introduce breaking changes.

==============================================================================

END OF CHAPTER 30

# ==============================================================================
# CHAPTER 31
# API DESIGN
# ==============================================================================

31.0 PURPOSE

Every public API in Tesminux must be stable, predictable and documented.

==============================================================================

31.1 DESIGN PRINCIPLES

Simple

Explicit

Predictable

Minimal

==============================================================================

31.2 FUNCTION DESIGN

Each function performs one responsibility.

Avoid giant APIs.

==============================================================================

31.3 NAMING

Good

start()

stop()

restart()

write()

read()

Bad

execute_everything()

==============================================================================

31.4 PARAMETERS

Prefer explicit arguments.

Avoid boolean flags with unclear meaning.

Bad

start(true,false,true)

Good

start(StartOptions)

==============================================================================

31.5 RETURN TYPES

Prefer

Result<T,E>

Option<T>

Avoid sentinel values.

==============================================================================

31.6 BACKWARD COMPATIBILITY

Public APIs should remain compatible whenever possible.

==============================================================================

END OF CHAPTER 31

# ==============================================================================
# CHAPTER 32
# CODE REVIEW
# ==============================================================================

32.0 PURPOSE

Every merge should improve Tesminux.

==============================================================================

32.1 REVIEW CHECKLIST

Architecture

Readability

Performance

Memory

Security

Documentation

==============================================================================

32.2 QUESTIONS

Is it readable?

Is it modular?

Does it duplicate code?

Can it panic?

Does it leak resources?

==============================================================================

32.3 REJECTION

Reject code that

duplicates logic

adds technical debt

breaks architecture

==============================================================================

END OF CHAPTER 32

# ==============================================================================
# CHAPTER 33
# DEBUGGING
# ==============================================================================

33.0 GOAL

Debug systematically.

Never guess.

==============================================================================

33.1 PROCESS

Reproduce

↓

Locate

↓

Fix

↓

Test

↓

Commit

==============================================================================

33.2 RULES

Never hide errors.

Never ignore warnings.

==============================================================================

33.3 LOGGING

Collect enough information.

Avoid excessive logs.

==============================================================================

33.4 BUGS

Every bug has a root cause.

Fix root causes.

==============================================================================

END OF CHAPTER 33

# ==============================================================================
# CHAPTER 34
# NAMING
# ==============================================================================

34.0 FILES

snake_case

==============================================================================

34.1 STRUCTS

PascalCase

==============================================================================

34.2 FUNCTIONS

snake_case

==============================================================================

34.3 CONSTANTS

UPPER_CASE

==============================================================================

34.4 MODULES

Short

Meaningful

==============================================================================

34.5 VARIABLES

Descriptive names.

Avoid

a

b

tmp1

tmp2

==============================================================================

END OF CHAPTER 34

# ==============================================================================
# CHAPTER 35
# ARCHITECTURE DECISION RECORDS
# ==============================================================================

35.0 PURPOSE

Major technical decisions should be recorded.

==============================================================================

35.1 RECORD

Context

Decision

Alternatives

Consequences

==============================================================================

35.2 EXAMPLES

Why Rust?

Why JNI?

Why PTY?

Why Compose?

==============================================================================

35.3 BENEFITS

Future contributors understand historical decisions.

==============================================================================

END OF CHAPTER 35

# ==============================================================================
# CHAPTER 36
# BREAKING CHANGES
# ==============================================================================

36.0 PURPOSE

Minimize disruptions.

==============================================================================

36.1 BEFORE BREAKING

Evaluate alternatives.

==============================================================================

36.2 DOCUMENT

Explain

Why

Impact

Migration

==============================================================================

36.3 VERSIONING

Breaking changes require a major version increment.

==============================================================================

END OF CHAPTER 36

# ==============================================================================
# CHAPTER 37
# ROADMAP
# ==============================================================================

Near Term

Multi-terminal

Settings

Themes

File Manager

Mid Term

SSH

Git

Plugin API

Package Manager

Long Term

IDE Features

Cloud Sync

Extension Marketplace

Remote Development

==============================================================================

END OF CHAPTER 37

# ==============================================================================
# CHAPTER 38
# DOCUMENTATION
# ==============================================================================

Every module must explain

Purpose

Responsibilities

Dependencies

Public API

Examples

Documentation must stay synchronized with code.

==============================================================================

END OF CHAPTER 38

# ==============================================================================
# CHAPTER 39
# LONG TERM PRINCIPLES
# ==============================================================================

Tesminux is built for years, not weeks.

Prefer quality.

Prefer stability.

Prefer maintainability.

Avoid unnecessary rewrites.

Protect architecture.

==============================================================================

END OF CHAPTER 39

# ==============================================================================
# CHAPTER 40
# FINAL RULES
# ==============================================================================

Every commit should improve Tesminux.

Never sacrifice architecture for speed.

Never duplicate code.

Never ignore compiler warnings.

Never ignore user experience.

Document important decisions.

Keep modules small.

Keep APIs stable.

Respect Rust ownership.

Respect Android lifecycle.

Respect open-source principles.

Remember:

Readable code survives.

Good architecture survives longer.

The goal is not to finish Tesminux.

The goal is to make Tesminux maintainable for the next ten years.

## RULE 9: System Defaults
* **Async:** Use `tokio` for async tasks. Keep async isolated from core UI logic.
* **Dispatch:** Prefer static dispatch (Generics / Traits) over dynamic dispatch (`Box<dyn Trait>`).

---

## RULE 10: Single Session Focus (No Tab / Multi-Session Overhead)
* **Single Terminal Session Architecture:**
  * Keep Tesminux lightweight, fast, and stable by focusing strictly on a single, high-performance terminal session.
  * Do NOT include multi-tab bars, session switching tabs, or complex multi-session UI logic. All operations should run within the single active terminal session.

==============================================================================
END OF AI_GUIDELINES
==============================================================================
