use std::alloc::System;

#[global_allocator]
static GLOBAL: System = std::alloc::System;

mod encryption;
mod jni;
mod compression;
mod context;
