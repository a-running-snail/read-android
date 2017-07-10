from javax.crypto import *
from java.security import *
from javax.crypto.spec import IvParameterSpec, SecretKeySpec
import binascii
import sys

from ctypes import *

s = '''
KEY = 0000000000000000000000000000000000000000000000000000000000000000
IV = 00000000000000000000000000000000
PLAINTEXT = 014730f80ac625fe84f026c60bfd547d
CIPHERTEXT = 5c9d844ed46f9885085e5d6a4f94c7d7
'''
def eval_fields(x):
   globals()[x[0].lower()] = binascii.a2b_hex(x[1])


import re
pat = re.compile("(?P<k>\w+)\s*=\s*(?P<kv>[0-9A-Fa-f]+)\s*", re.DOTALL)
key = iv = plaintext = ciphertext = ''
result = pat.findall(s)
map(eval_fields, result)
print 'key: ', len(key)
   
def test_jce():
    if 1:
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, 'AES'), IvParameterSpec(iv))
    else:
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, 'AES'))
    p1 = "this is a test of part one"
    p2 = "now part two"
    p3 = "last, part three         aaaaaaa"
    #s1 = cipher.update(p1)
    #s2 = cipher.update(p2)
    #s3 = cipher.update(p3)
    #secret = cipher.doFinal()
    secret = cipher.doFinal(plaintext)
    print binascii.b2a_hex(secret.tostring())

def drm_cipher_file(transform, opmode, key, iv, name_in, name_out):
    fin = open(name_in, "rb");
    fout = open(name_out, "wb")
    cipher = Cipher.getInstance(transform)
    cipher.init(opmode, SecretKeySpec(key, 'AES'), IvParameterSpec(iv))
    cache_len = 64
    while True:
        buf = fin.read(cache_len)
        if not buf:
            fout.write(cipher.doFinal())
            break
        buf2 = cipher.update(buf)
        fout.write(buf2)
    fin.close()
    fout.close()
    
if __name__ == "__main__":
    if (len(sys.argv) < 3):
        print >>sys.stderr, "Usage: jython jce.py input_file_name output_file_name [1]\n\tOptional 1 decryption"
        sys.exit(-1)
    #test_drmlib_cipher()
    opmode = Cipher.ENCRYPT_MODE
    if len(sys.argv) > 3 and int(sys.argv[3]) == 1:
        opmode= Cipher.DECRYPT_MODE
        print "decrypt"
    iv = plaintext
    drm_cipher_file("AES/CBC/PKCS5Padding", opmode, key, iv, sys.argv[1], sys.argv[2])
    