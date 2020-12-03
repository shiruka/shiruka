use jni::{JavaVM, JNIEnv};
use jni::descriptors::Desc;
use jni::errors::Error;
use jni::objects::{GlobalRef, JClass, JMethodID, JValue};
use jni::sys::{jboolean, jbyteArray, jint, jlong, JNI_VERSION_1_8, jobject};
use libdeflater::{CompressionLvl, Compressor, Decompressor};
use sha2::{Digest, Sha256};
use std::{mem, slice};
use std::os::raw::c_void;
use std::panic::catch_unwind;
use std::sync::Once;

use crate::compression::CompressT;
use crate::context::Context;
use crate::encryption::CryptoT;

const INVALID_JNI_VERSION: jint = 0;

static INIT: Once = Once::new();
static mut SIZED_MEMORY_POINTER_CONSTRUCTOR: Option<JMethodID> = None;
static mut SIZED_MEMORY_POINTER_CLASS: Option<GlobalRef> = None;

#[allow(non_snake_case)]
#[no_mangle]
pub extern "system" fn JNI_OnLoad(vm: JavaVM, _: *mut c_void) -> jint {
  let env = vm.get_env().expect("Cannot get reference to the JNIEnv");

  catch_unwind(|| {
    init_cache(&env);
    JNI_VERSION_1_8
  })
    .unwrap_or(INVALID_JNI_VERSION)
}

pub fn init_cache(env: &JNIEnv) {
  INIT.call_once(|| unsafe { cache_methods(env) });
}

unsafe fn cache_methods(env: &JNIEnv) {
  SIZED_MEMORY_POINTER_CLASS =
    get_class(env, "io/github/shiruka/shiruka/natives/SizedMemoryPointer");
  SIZED_MEMORY_POINTER_CONSTRUCTOR =
    get_constructor(env, SIZED_MEMORY_POINTER_CLASS.clone().unwrap(), "(JI)V");
}

fn get_constructor(env: &JNIEnv, class_ref: GlobalRef, sig: &str) -> Option<JMethodID<'static>> {
  let class = JClass::from(class_ref.as_obj());
  let method_id_result: Result<JMethodID, Error> = (class, sig).lookup(env);
  let method_id = method_id_result.map(|mid| mid.into_inner().into())
    .unwrap_or_else(|_| panic!("Could not lookup method id for sig {}", sig));
  Some(method_id)
}

fn get_class(env: &JNIEnv, class: &str) -> Option<GlobalRef> {
  let class = env
    .find_class(class)
    .unwrap_or_else(|_| panic!("Class {} not found", class));
  Some(env.new_global_ref(class).unwrap())
}

#[no_mangle]
pub extern "system" fn Java_io_github_shiruka_shiruka_natives_NativeProcessor_createNewContext(_env: JNIEnv, _class: JClass, encryption_mode_toggle: jboolean) -> jlong {
  let ctx = Box::new(Context {
    encryption_mode_toggle: encryption_mode_toggle != 0,
    debug: false,

    counter: 0,
    key: None,
    aes: None,
    digest: Sha256::new(),

    preallocate_size: 2 * 1024 * 1024,
    compressor: Compressor::new(CompressionLvl::default()),
    decompressor: Decompressor::new(),
  });

  let a = ctx.as_ref() as *const Context;

  mem::forget(ctx);
  a as i64
}

#[no_mangle]
pub extern "system" fn Java_io_github_shiruka_shiruka_natives_NativeProcessor_enableCrypto(env: JNIEnv, _class: JClass, ctx: jlong, key: jbyteArray, iv: jbyteArray) {
  let key_vec = env.convert_byte_array(key).unwrap();
  let iv_vec = env.convert_byte_array(iv).unwrap();

  let raw_ptr = ctx as *mut Context;
  let context: &mut Context = unsafe { raw_ptr.as_mut().unwrap() };

  context.init_state(key_vec.as_slice(), iv_vec.as_slice());
}

#[no_mangle]
pub extern "system" fn Java_io_github_shiruka_shiruka_natives_NativeProcessor_destroyContext(_env: JNIEnv, _class: JClass, ctx: jlong) {
  let raw_ptr = ctx as *mut Context;
  mem::drop(raw_ptr)
}

#[no_mangle]
pub extern "system" fn Java_io_github_shiruka_shiruka_natives_NativeProcessor_debug(_env: JNIEnv, _class: JClass, ctx: jlong, debug_mode: jboolean) {
  let raw_ptr = ctx as *mut Context;
  let context: &mut Context = unsafe { raw_ptr.as_mut().unwrap() };
  context.debug = debug_mode != 0;
}

#[no_mangle]
pub extern "system" fn Java_io_github_shiruka_shiruka_natives_NativeProcessor_preallocateSize(_env: JNIEnv, _class: JClass, ctx: jlong, preallocate_size: jint) {
  let raw_ptr = ctx as *mut Context;
  let context: &mut Context = unsafe { raw_ptr.as_mut().unwrap() };
  context.preallocate_size = preallocate_size as usize;
}

#[no_mangle]
pub extern "system" fn Java_io_github_shiruka_shiruka_natives_NativeProcessor_process(env: JNIEnv, _class: JClass, ctx: jlong, memory_pointer: jobject) -> jobject {
  let res_mem_address = env.call_method(memory_pointer, "getAddress", "()J", &[]);
  let mem_address: i64 = res_mem_address.unwrap().j().unwrap();

  let res_size = env.call_method(memory_pointer, "getSize", "()I", &[]);
  let size: i32 = res_size.unwrap().i().unwrap();

  let data: &mut [u8] = unsafe { slice::from_raw_parts_mut(mem_address as *mut u8, size as usize) };

  let result_ptr: *const u8;
  let result_size: usize;

  let raw_ptr = ctx as *mut Context;
  let context: &mut Context = unsafe { raw_ptr.as_mut().unwrap() };
  if context.encryption_mode_toggle {
    if context.debug {
      let mut start = std::time::Instant::now();
      let mut compressed = context.compress(data, size);
      println!("compression of {:?} bytes took {:?}", size, start.elapsed());
      let compressed_size = compressed.len();
      start = std::time::Instant::now();
      let processed = context.process(compressed.as_mut_slice());
      println!("encryption of {:?} bytes took {:?}", compressed_size, start.elapsed());
      result_ptr = processed.as_ptr();
      result_size = processed.len();
      mem::forget(processed);
    } else {
      let mut compressed = context.compress(data, size);
      let processed = context.process(compressed.as_mut_slice());

      result_ptr = processed.as_ptr();
      result_size = processed.len();
      mem::forget(processed);
    }
  } else {
    if context.debug {
      let mut start = std::time::Instant::now();
      let decrypted = context.process(data);
      if decrypted.len() == 0 {
        return create_jvm_fat_pointer(env, 0 as i64, 0 as i32);
      }

      println!("decryption of {:?} bytes took {:?}", size, start.elapsed());
      let compressed_size = decrypted.len();
      start = std::time::Instant::now();
      let decompressed = context.decompress(decrypted.as_slice(), context.preallocate_size);
      println!("decompression of {:?} bytes took {:?}", compressed_size, start.elapsed());
      result_ptr = decompressed.as_ptr();
      result_size = decompressed.len();
      mem::forget(decompressed);
    } else {
      let decrypted = context.process(data);
      if decrypted.len() == 0 {
        return create_jvm_fat_pointer(env, 0 as i64, 0 as i32);
      }

      let decompressed = context.decompress(decrypted.as_slice(), context.preallocate_size);

      result_ptr = decompressed.as_ptr();
      result_size = decompressed.len();
      mem::forget(decompressed);
    }
  }

  create_jvm_fat_pointer(env, result_ptr as i64, result_size as i32)
}

fn create_jvm_fat_pointer<'a>(env: JNIEnv, result_ptr: i64, result_size: i32) -> jobject {
  let class_ref = unsafe { SIZED_MEMORY_POINTER_CLASS.clone().unwrap() };
  let class = JClass::from(class_ref.as_obj());
  let method_id = unsafe { SIZED_MEMORY_POINTER_CONSTRUCTOR.unwrap() };

  let arguments: &[JValue] = &[JValue::from(result_ptr), JValue::from(result_size)];
  env.new_object_unchecked(class, method_id, arguments)
    .unwrap_or_else(|_| panic!("Could not create new fat pointer"))
    .into_inner()
}
