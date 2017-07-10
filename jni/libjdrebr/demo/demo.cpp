#include "drmlib.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <assert.h>
#include "drmalgorithm.h"

int function_call_demo()
{
	int nRet = -1;

	// 加密在PC端，性能足够用，不需要分块加密，不调用cipher_update
	Cipher cipher = NULL;
	nRet = cipher_create(&cipher, "1/1/0");
	if(DRM_Err_Ok != nRet)
	{
		printf("cipher_create failed!\n");
		return -1;
	}

	/*
	unsigned char key[1024] = {0}; // pKey申请的内存要足够大，否则返回DRM_Err_Buffer_Insufficient错误码，再按照nkeylen申请内存。
	int keylen = 0;
	nRet = cipher_generatekey(&pencrypt, key, &keylen); 
	if(DRM_Err_Ok != nRet)
	{
	printf("cipher_generatekey failed!\n");
	return -1;
	}
	*/
	unsigned char * key = (unsigned char*)"12345678901234561234567890123456";
	int keylen = 32;
	const unsigned char *IV = (const unsigned char *)"1234567812345678";
	nRet = cipher_init(cipher, OPMODE_ENCRYPT, key, keylen, IV, 16);
	if(DRM_Err_Ok != nRet)
	{
		printf("cipher_init failed!\n");
		return -1;
	}

	unsigned char input[10] = {0};
	memcpy(input, "0123456789", 10);
	unsigned char output[1024] = {0}; // output buffer比input大一个block的长度即可。目前算法的block 长度为16字节。
	// output buffer不够大的话会返回DRM_Err_Buffer_Insufficient错误码，再按照nOutputlen申请内存。
	int outputlen = 1024;
	nRet = cipher_final(cipher, input, 0, output, &outputlen);
	if(DRM_Err_Ok != nRet)
	{
		printf("cipher_final failed!\n");
		return -1;
	}

	nRet = cipher_destroy(cipher);
	if(DRM_Err_Ok != nRet)
	{
		printf("cipher_destroy failed!\n");
		return -1;
	}


	// 由于解密数据的客户端性能受限，解密需要采用分块方式，根据输入数据buffer的大小，多次调用cipher_update解密
	// 例如待加密数据为01234567899876543210abcdefg
	Cipher pdecrypt = NULL;
	nRet = cipher_create(&pdecrypt, "1/0/0");
	if(DRM_Err_Ok != nRet)
	{
		printf("cipher_create failed!\n");
		return -1;
	}


	nRet = cipher_init(pdecrypt, OPMODE_DECRYPT, key, keylen, IV, 8);
	if(DRM_Err_Ok != nRet)
	{
		printf("cipher_init failed!\n");
		return -1;
	}

	// 解密0123456789
	unsigned char inbuffer[10] = {0};
	memcpy(inbuffer, "0123456789", 10);
	unsigned char outBuffer[10] = {0}; 
	int outbufferlen = 10;
	nRet = cipher_update(pdecrypt, inbuffer, 10, outBuffer, &outbufferlen);
	if(DRM_Err_Ok != nRet)
	{
		printf("cipher_update failed!\n");
		return -1;
	}

	// 解密9876543210
	memset(inbuffer, 0, 10);
	memcpy(inbuffer, "987654321", 10);
	memset(outBuffer, 0, 10);
	outbufferlen = 10;
	nRet = cipher_update(pdecrypt, inbuffer, 10, outBuffer, &outbufferlen);
	if(DRM_Err_Ok != nRet)
	{
		printf("cipher_update failed!\n");
		return -1;
	}

	// 解密结尾部分，需要调用cipher_final
	unsigned char finalInbuffer[7] = {0};
	memcpy(finalInbuffer, "abcdefg", 7);
	unsigned char finalOutbuffer[1024] = {0}; // 申请的内存要足够大，否则返回DRM_Err_Buffer_Insufficient错误码，再按照nfinalOutbufferlen申请内存。
	int finalOutbufferlen = 1024;
	nRet = cipher_final(pdecrypt, finalInbuffer, 7, finalOutbuffer, &finalOutbufferlen);
	if(DRM_Err_Ok != nRet)
	{
		printf("cipher_final failed!\n");
		return -1;
	}

	nRet = cipher_destroy(pdecrypt);
	if(DRM_Err_Ok != nRet)
	{
		printf("cipher_destroy failed!\n");
		return -1;
	}
	return 0;
}


void file_demo(const char* file_in, const char* file_out,
			   const char* transformation, /* algorithm */
			   int opmode, /* encrypt or decrypt */
			   const unsigned char* key, int key_len,
			   const unsigned char* iv, int iv_len
			   )
{
	FILE* f_in = fopen(file_in, "rb");
	FILE* f_out = fopen(file_out, "wb");
	Cipher cipher = NULL;
	DRM_Err nRet;
	nRet = cipher_create(&cipher, "1/1/0"); /* AES/CBC/PKCS5Padding */
	nRet = cipher_init(cipher, opmode, key, key_len, iv, iv_len);
#define BUF_LEN 1024 /* 长度可任意*/
	char in_data[BUF_LEN+32];
	char out_data[BUF_LEN + 16];
	int in_data_len, out_data_len;
	int const_buf_len = BUF_LEN;
	if (opmode == OPMODE_DECRYPT)
	{
		//const_buf_len += 1;
		printf("Decrypting with buf size: %d...\n", const_buf_len);
	}
	for (;;)
	{
		in_data_len = fread(in_data, 1, const_buf_len, f_in);
		out_data_len = sizeof(out_data) / sizeof(out_data[0]);

		if (in_data_len == const_buf_len)
		{
			nRet = cipher_update(cipher, (const unsigned char*)in_data, in_data_len, (unsigned char*)out_data, &out_data_len);
			assert(out_data_len <= (in_data_len + 16));
			if (nRet == DRM_Err_Ok)
			{
				if (out_data_len > 0)
					fwrite(out_data, 1, out_data_len, f_out);	
			}
			else
				assert(0);
		}
		else //last block, can call cipher_update + cipher_final, or jsut cipher_final
		{
#if 0
			nRet = cipher_update(cipher, (const unsigned char*)in_data, in_data_len, (unsigned char*)out_data, &out_data_len);
			if (nRet == DRM_Err_Ok)
			{
				if (out_data_len > 0)
					fwrite(out_data, 1, out_data_len, f_out);	
			}
			else
				assert(0);
			nRet = cipher_final(cipher, NULL, 0, (unsigned char*)out_data, &out_data_len);
#else
			nRet = cipher_final(cipher, (const unsigned char*)in_data, in_data_len, (unsigned char*)out_data, &out_data_len);
			assert(out_data_len <= (in_data_len + 16));
#endif
			if (nRet == DRM_Err_Ok)
			{
				if (out_data_len > 0)
					fwrite(out_data, 1, out_data_len, f_out);
				break;
			}
			else
				assert(0);
		}
	} // end for(;;)
	cipher_destroy(cipher);
	fclose(f_in);
	fclose(f_out);
}

int main(int argc, char* argv[])
{
	char* file_in = argv[1];
	char* file_out = argv[2];
	int opmode = OPMODE_ENCRYPT;
	if (argc == 4 && atoi(argv[3]) == 1)
		opmode = OPMODE_DECRYPT;
	char* transformation = "1/1/0";
	char key[32];
	char iv[16];
	memset(key, 0, 32);
	memset(iv, 0, 16);
#if 1
	file_demo(file_in, file_out, transformation, opmode, (const unsigned char*)key, 32, NULL, 0);
#else
	file_demo(file_in, file_out, transformation, opmode, (const unsigned char*)key, 32, (const unsigned char*)iv, 16);
#endif
}