use aesni::Aes256;
use cfb8::Cfb8;
use libdeflater::{Compressor, Decompressor};
use sha2::Sha256;

#[repr(C)]
pub(crate) struct Context {
  pub(crate) encryption_mode_toggle: bool,
  pub(crate) debug: bool,

  pub(crate) counter: i64,
  pub(crate) aes: Option<Cfb8<Aes256>>,
  pub(crate) key: Option<Vec<u8>>,
  pub(crate) digest: Sha256,

  pub(crate) preallocate_size: usize,
  pub(crate) compressor: Compressor,
  pub(crate) decompressor: Decompressor,
}

impl AsMut<Context> for Context {
  fn as_mut(&mut self) -> &mut Context {
    self
  }
}
