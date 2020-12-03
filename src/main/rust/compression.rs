use crate::context::Context;

pub(crate) trait CompressT {
  fn decompress(&mut self, data: &[u8], preallocate_size: usize) -> Box<Vec<u8>>;
  fn compress(&mut self, data: &[u8], size: i32) -> Vec<u8>;
}

impl CompressT for Context {
  fn decompress(&mut self, data: &[u8], preallocate_size: usize) -> Box<Vec<u8>> {
    let mut decoded_data = Box::new(vec![0u8; preallocate_size]);
    let result = self.decompressor.deflate_decompress(data, decoded_data.as_mut_slice());

    if result.is_err() {
      return Box::new(Vec::with_capacity(0));
    } else {
      decoded_data.resize(result.unwrap(), 0);
      decoded_data
    }
  }

  fn compress(&mut self, data: &[u8], size: i32) -> Vec<u8> {
    let compressed_size = self.compressor.deflate_compress_bound(size as usize);

    let mut compressed_data = Vec::new();
    compressed_data.resize(compressed_size, 0);

    let actual_sz = self.compressor.deflate_compress(data, &mut compressed_data).unwrap();
    compressed_data.resize(actual_sz, 0);
    compressed_data
  }
}
