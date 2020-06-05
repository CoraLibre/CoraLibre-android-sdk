#!/usr/bin/env python3

import hashlib
import hmac
from math import ceil

tek_val1 = [0x12,
	 0x34,
	 0x56,
	 0x67,
	 0x78,
	 0x9A,
	 0xBC,
	 0xDE,
	 0xF1,
	 0x23,
	 0x45,
	 0x67,
	 0x89,
	 0xAB,
	 0xCD,
	 0xEF]
tek_val1 = bytearray(tek_val1)

# The following code was taken and modified from
# https://en.wikipedia.org/wiki/HKDF#Example:_Python_implementation

hash_len = 16
def hmac_sha256(key, data):
    return hmac.new(key, data, hashlib.sha256).digest()

def hkdf(length, ikm, salt = b"", info = b""):
    if len(salt) == 0:
        salt = bytes([0]*hash_len)
    prk = hmac_sha256(salt, ikm)
    t = b""
    okm = b""
    for i in range(ceil(length / hash_len)):
        t = hmac_sha256(prk, t + info + bytes([1+i]))
        okm += t
    return okm[:length]

print("private static final byte[] RPIK_VAL1 = new byte[] {")
for i in hkdf(16, tek_val1, b"", "EN-RPIK".encode()):
	print("    (byte) "  + str(i) + ",")
print("};")

print("private static final byte[] AEMK_VAL1 = new byte[] {")
for i in hkdf(16, tek_val1, b"", "EN-AEMK".encode()):
	print("    (byte) "  + str(i) + ",")
print("};")
