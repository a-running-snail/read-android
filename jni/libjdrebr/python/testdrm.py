import os, sys
import binascii
#from Crypto.Cipher import AES

BLOCK_SIZE = 32
secret = os.urandom(BLOCK_SIZE)

s = '''
KEY = 0000000000000000000000000000000000000000000000000000000000000000
IV = 00000000000000000000000000000000
PLAINTEXT = 014730f80ac625fe84f026c60bfd547d
CIPHERTEXT = 5c9d844ed46f9885085e5d6a4f94c7d7

'''
def eval_fields(x):
    #print x[0].lower(), x[1], len(binascii.a2b_hex(x[1]))
    globals()[x[0].lower()] = binascii.a2b_hex(x[1])
   
import re
pat = re.compile("(?P<k>\w+)\s*=\s*(?P<kv>[0-9A-Fa-f]+)\s*", re.DOTALL)
key = iv = plaintext = ciphertext = ''
result = pat.findall(s)
map(eval_fields, result)

from ctypes import *
#drmlib = cdll.DRMLibd
drmlib = cdll.LoadLibrary(r"D:\svn2\ebook-drm\branches\DRM_API\DRM_API\Debug\drm_api_dll_gyp.dll")
miracl = cdll.LoadLibrary(r"D:\src\miracl\Debug\miracl.dll")
miracl = drmlib

def py_encrypt_aes(key, mode, iv, plain):
    e = AES.new(key, mode, iv)
    return e.encrypt(plain)

def pointer_p_to_bytes(p, bytes):
    #print 'bytes: ', bytes
    data = cast(p, POINTER(c_ubyte * bytes))
    byteData = ''.join(map(chr, data.contents))
    return byteData  

#wrapper functions for the new drmlib encrypt/decrypt interface

OPMODE_ENCRYPT = 0
OPMODE_DECRYPT = 1

DRM_Err_Ok = 0
DRM_Err_Unkown = -1
DRM_Err_Algorithm_Invalid = 1  
DRM_Err_Buffer_Insufficient = 2
   
''' 
wrapper class and function to test cipher_xxx function series
'''
class drmlib_cipher_exception(Exception):
    pass

class drmlib_cipher():
    def __init__(self, clib, transformation):
        self.cipher = c_void_p()
        if DRM_Err_Ok != clib.cipher_create(byref(self.cipher), transformation):
            raise drmlib_cipher_exception("cipher_create")
        self.drmlib = clib
        
    def init(self, mode, key, iv):
        self.drmlib.cipher_init(self.cipher, mode, key, len(key), iv, len(iv))
        
    def update(self, data_in):
        in_len = len(data_in)
        data_out = create_string_buffer(in_len + 16)
        out_len = c_int(in_len + 16)
        if DRM_Err_Ok != self.drmlib.cipher_update(self.cipher, data_in, in_len, byref(data_out), byref(out_len)):
            raise drmlib_cipher_exception("update")
        return pointer_p_to_bytes(data_out, out_len.value)
    
    def final(self, data_in = None):
        if data_in:
            in_len = len(data_in)
        else:
            in_len = 0
        data_out = create_string_buffer(in_len + 16)
        out_len = c_int(in_len + 16)
        if DRM_Err_Ok != self.drmlib.cipher_final(self.cipher, data_in, in_len, byref(data_out), byref(out_len)):
            raise drmlib_cipher_exception("final")
        return pointer_p_to_bytes(data_out, out_len.value)
    
    def __del__(self):
        self.drmlib.cipher_destroy(self.cipher)

def test_digest():
    digestor = c_void_p()
    drmlib.digestor_create(byref(digestor))
    digest = create_string_buffer(32)
    digest_len = c_int(32)
    drmlib.digestor_update(digestor, s, len(s))
    drmlib.digestor_final(digestor, s2, len(s2), byref(digest), byref(digest_len))
    drmlib.digestor_destroy(digestor)
    print "drmlib.digestor_final: %d, %s" % (digest_len.value, binascii.b2a_hex(pointer_p_to_bytes(digest, digest_len.value)))
    from Crypto.Hash import SHA256
    sha = SHA256.new()
    sha.update(s)
    sha.update(s2)
    digest = sha.hexdigest()
    print "pycrypto, sha256: %d, %s" % (len(digest) / 2, digest)
    
def test_drmlib_cipher():
    cipher = drmlib_cipher(drmlib, "1/0/0")
    cipher.init(OPMODE_ENCRYPT, key, iv)
    secret = cipher.final(plaintext)
    print binascii.b2a_hex(secret)
    
    
def drm_cipher_file(transform, opmode, key, iv, name_in, name_out):
    fin = open(name_in, "rb");
    fout = open(name_out, "wb")
    cipher = drmlib_cipher(drmlib, transform)
    cipher.init(opmode, key, iv)
    cache_len = 640
    while True:
        buf = fin.read(cache_len)
        if not buf:
            fout.write(cipher.final())
            break
        buf2 = cipher.update(buf)
        fout.write(buf2)
    fin.close()
    fout.close()
    
    
if __name__ == "__main__":
    if (len(sys.argv) < 3):
        print >>sys.stderr, "Usage: python testdrm.py input_file_name output_file_name [1]\n\tOptional 1 decryption"
        sys.exit(-1)
    #test_drmlib_cipher()
    opmode = OPMODE_ENCRYPT
    if len(sys.argv) > 3 and int(sys.argv[3]) == 1:
        opmode= OPMODE_DECRYPT
        print "decrypt"
    iv = plaintext
    drm_cipher_file("1/1/0", opmode, key, iv, sys.argv[1], sys.argv[2])
    
