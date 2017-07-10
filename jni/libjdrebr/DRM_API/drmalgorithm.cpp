#include "drmalgorithm.h"
#include <stdlib.h>
#include <memory.h>
#include <assert.h>
#include <string.h>

extern "C"
{
#include "p1363.h"
#include "sha256.h"
}

struct block_cipher
{

};

#define AES_BLOCK_SIZE 16
struct miracl_aes
{
	aes a;
	int cipher_mode;
	int op_mode;
	int padding_scheme;
	unsigned char cache_buf[AES_BLOCK_SIZE * 2];
	char cache_len;
};


DRM_Err
cipher_create_miracl_aes(PCipher pcipher, int mode, int padding)
{
	miracl_aes *palg = (miracl_aes*)malloc(sizeof(miracl_aes));
	palg->cipher_mode = mode;
	*pcipher = (PCipher)palg;
	return DRM_Err_Ok;
}

EBOOK_DRM_API DRM_Err 
cipher_create(PCipher pcipher, const char* transformation)
{
	typedef DRM_Err ( *cipher_create_proc)(PCipher, int /* mode */, int /* padding*/);
	// parse transformation
	int padding_scheme;
	int mode;
	if (!strcmp("1/0/0", transformation))
	{
		mode = MR_ECB;
		padding_scheme = 0;
	}
	else if (!strcmp("1/1/0", transformation))
	{
		mode = MR_CBC;
		padding_scheme = 0;
	}
	else
		return DRM_Err_Algorithm_Invalid;
	// 1/0/0: aes/ecb/pkcs5
	// 1/1/0: aes/cbc/pkcs5
	cipher_create_proc  cipher_create_ptr = &cipher_create_miracl_aes;
	cipher_create_ptr(pcipher, mode, padding_scheme);
	return DRM_Err_Ok;
}

EBOOK_DRM_API DRM_Err 
cipher_init(Cipher cipher, int opmode, 
			const unsigned char* key, int key_len,
			const unsigned char* IV,
			int IV_len)
{
	//BOOL aes_init(aes* a,int mode,int nk,char *key,char *iv)
	/* Key=nk bytes */
	/* currently NB,nk = 16, 24 or 32          */
	//#define MR_ECB   0
	//#define MR_CBC   1
	miracl_aes* paes = (miracl_aes*)cipher;
	if (!key || key_len != 32)
		return DRM_Err_Key_Length_Invalid;
	if ((paes->cipher_mode == MR_CBC) && (IV && IV_len != 16))
		return DRM_Err_IV_Length_Invalid;
	
	aes_init(&paes->a, paes->cipher_mode, key_len, (char*)key, (char*)IV);
	paes->op_mode = opmode;
	paes->cache_len = 0;

	return DRM_Err_Ok;
}

EBOOK_DRM_API DRM_Err
cipher_update(Cipher cipher, const unsigned char* in_data, int in_data_len, 
			  unsigned char* out_data, IN OUT int* out_data_len)
{

	miracl_aes* paes = (miracl_aes*)cipher;
	//memset(out_data, 0, *out_data_len);
	//process buffer
	int num_blocks;
	const unsigned char* in_ptr;
	unsigned char* out_ptr;
	*out_data_len = 0;
	int bytes_out = 0;
	if (!in_data || !in_data_len)
	{
		if (!out_data_len)
			return DRM_Err_Parameter_Invalid;
		else
		{
			*out_data_len = 0;
			return DRM_Err_Ok;
		}
	}
	if (paes->op_mode == OPMODE_ENCRYPT)
	{
		// process padded block
		if (paes->cache_len + in_data_len < AES_BLOCK_SIZE) // data too few, just cache
		{
			memcpy(paes->cache_buf + paes->cache_len, in_data, in_data_len);
			paes->cache_len += in_data_len;
		}
		else
		{
			in_ptr = in_data;
			out_ptr = out_data;
			int bytes_mid = in_data_len;

			if (paes->cache_len) // first, process cached block and update IV
			{
				memcpy(paes->cache_buf + paes->cache_len, in_ptr, AES_BLOCK_SIZE - paes->cache_len);
				aes_encrypt(&paes->a, (char*)paes->cache_buf, AES_BLOCK_SIZE, (char*)out_ptr, FALSE);
				memcpy(paes->a.f, out_ptr, AES_BLOCK_SIZE);
				//memcpy(paes->a.f, paes->cache_buf, AES_BLOCK_SIZE);

				out_ptr += AES_BLOCK_SIZE;
				in_ptr += AES_BLOCK_SIZE - paes->cache_len;
				bytes_mid -= AES_BLOCK_SIZE - paes->cache_len;
				paes->cache_len = 0;
				bytes_out += AES_BLOCK_SIZE;
			}

			num_blocks = bytes_mid / AES_BLOCK_SIZE;
			// now, complete bocks
			for (int i = 0; i < num_blocks; ++i)
			{
				aes_encrypt(&paes->a, (char*)in_ptr, AES_BLOCK_SIZE, (char*)out_ptr, FALSE);
				memcpy(paes->a.f, out_ptr, AES_BLOCK_SIZE);
				//memcpy(paes->a.f, out_ptr, AES_BLOCK_SIZE);
				in_ptr += AES_BLOCK_SIZE;
				out_ptr += AES_BLOCK_SIZE;
				bytes_out += AES_BLOCK_SIZE;
			}
			// remaining
			int bytes_tail = bytes_mid % AES_BLOCK_SIZE;
			if (bytes_tail)
			{
				paes->cache_len = bytes_tail;
				memcpy(paes->cache_buf, in_ptr, bytes_tail);
			}
		}
	} 
	else if (paes->op_mode == OPMODE_DECRYPT)
	{
		// process padded block
		if (paes->cache_len + in_data_len <= AES_BLOCK_SIZE) // data too few, just cache
		{
			memcpy(paes->cache_buf + paes->cache_len, in_data, in_data_len);
			paes->cache_len += in_data_len;
		}
		else
		{
			in_ptr = in_data;
			out_ptr = out_data;
			int bytes_mid = in_data_len;

			if (paes->cache_len) // first, process cached block and update IV
			{
				memcpy(paes->cache_buf + paes->cache_len, in_ptr, AES_BLOCK_SIZE - paes->cache_len);
				aes_decrypt(&paes->a, (char*)paes->cache_buf, AES_BLOCK_SIZE, (char*)out_ptr, FALSE);
				//memcpy(paes->a.f, out_ptr, AES_BLOCK_SIZE);
				memcpy(paes->a.f, paes->cache_buf, AES_BLOCK_SIZE);
				out_ptr += AES_BLOCK_SIZE;
				in_ptr += AES_BLOCK_SIZE - paes->cache_len;
				bytes_mid -= AES_BLOCK_SIZE - paes->cache_len;
				paes->cache_len = 0;
				bytes_out += AES_BLOCK_SIZE;
			}

			num_blocks = bytes_mid / AES_BLOCK_SIZE;
			if (bytes_mid % AES_BLOCK_SIZE == 0)
				num_blocks--; //unlike encrypt, something must be left in the cache, 'cause we don't know that whether next call is cipher_final
			// now, complete bocks
			for (int i = 0; i < num_blocks; ++i)
			{
				aes_decrypt(&paes->a, (char*)in_ptr, AES_BLOCK_SIZE, (char*)out_ptr, FALSE);
				//memcpy(paes->a.f, out_ptr, AES_BLOCK_SIZE);
				memcpy(paes->a.f, in_ptr, AES_BLOCK_SIZE);
				in_ptr += AES_BLOCK_SIZE;
				out_ptr += AES_BLOCK_SIZE;
				bytes_out += AES_BLOCK_SIZE;
			}
			// remaining
			int bytes_tail = bytes_mid -  num_blocks * AES_BLOCK_SIZE;
			if (bytes_tail)
			{
				paes->cache_len = bytes_tail;
				memcpy(paes->cache_buf, in_ptr, bytes_tail);
			}
		}
	}
	else
		assert(0);
	
	*out_data_len = bytes_out;
	return DRM_Err_Ok;
}

EBOOK_DRM_API DRM_Err
cipher_final(Cipher cipher, CAN_0 const unsigned char* in_data, CAN_0 int in_data_len, 
			 unsigned char* out_data, IN OUT int* out_data_len)
{
	miracl_aes* paes = (miracl_aes*)cipher;
	DRM_Err ret;
	int bytes_out;
	if (paes->op_mode == OPMODE_ENCRYPT)
	{
		bytes_out = *out_data_len;
		if ((ret = cipher_update(cipher, in_data, in_data_len, out_data, &bytes_out)) != DRM_Err_Ok)
			return ret;
		// remaining
		unsigned char* buf = paes->cache_buf + paes->cache_len;
		int pad_len = AES_BLOCK_SIZE - paes->cache_len;
		unsigned char pad_char = (unsigned char)(pad_len);
		for (int i = 0; i < pad_len; ++i)
		{
			buf[i] = pad_len;
		}
		aes_encrypt(&paes->a, (char*)paes->cache_buf, AES_BLOCK_SIZE, (char*)out_data + bytes_out, FALSE);
		*out_data_len = bytes_out + AES_BLOCK_SIZE;
	} else if (paes->op_mode == OPMODE_DECRYPT)
	{
		bytes_out = *out_data_len;
		if ((ret = cipher_update(cipher, in_data, in_data_len, out_data, &bytes_out)) != DRM_Err_Ok)
			return ret;
		// remaining
		if (paes->cache_len != (char)AES_BLOCK_SIZE)
			return DRM_Err_Secret_Length_Invalid;
		aes_decrypt(&paes->a, (char*)paes->cache_buf, AES_BLOCK_SIZE, (char*)out_data + bytes_out, FALSE);
		unsigned char pad_len = *(out_data + bytes_out + AES_BLOCK_SIZE - 1);
		assert(pad_len <= (unsigned char)AES_BLOCK_SIZE);
		if (pad_len > (unsigned char)AES_BLOCK_SIZE)
			return DRM_Err_Padding_Invalid;
		for (int j = 1; j <= pad_len; ++j)
		{
			assert(out_data[ bytes_out+AES_BLOCK_SIZE- j] == pad_len);
			if (out_data[ bytes_out+AES_BLOCK_SIZE- j] != pad_len)
				return DRM_Err_Padding_Invalid;
		}
		*out_data_len = bytes_out + AES_BLOCK_SIZE - pad_len;
	}
	return DRM_Err_Ok;
}


//EBOOK_DRM_API DRM_Err
//digestor_create(PDigestor pdigestor, const char* transformation)
//{
//	if (transformation && strcmp(transformation, "1")) 
//	{
//		hash_state* psha = (hash_state*)malloc(sizeof(hash_state));
//		*pdigestor = psha;
//		sha_init(psha);
//		return DRM_Err_Ok;
//	}
//	else
//		return DRM_Err_Algorithm_Invalid;
//}
//
//EBOOK_DRM_API DRM_Err
//digestor_update(Digestor digestor, const unsigned char* data, int data_len)
//{
//	 sha_process((hash_state*)digestor, (unsigned char*)data, data_len);
//	 return DRM_Err_Ok;
//}
//
//EBOOK_DRM_API DRM_Err
//digestor_final(Digestor digestor, CAN_0 const unsigned char* data, CAN_0 int data_len, 
//			   unsigned char* digest, IN OUT int* digest_len)
//{
//	if (!digest_len || *digest_len < 32)
//		return DRM_Err_Buffer_Insufficient;
//	digestor_update(digestor, data, data_len);
//	sha_done((hash_state*)digestor, digest);
//	*digest_len = 32;
//	return DRM_Err_Ok;
//}
//
//EBOOK_DRM_API DRM_Err
//digestor_destroy(Digestor digestor)
//{
//	free(digestor);
//	return DRM_Err_Ok;
//}

EBOOK_DRM_API DRM_Err
cipher_destroy(Cipher cipher)
{
	free(cipher);
	return DRM_Err_Ok;
}

//EBOOK_DRM_API DRM_Err
//cipher_generatekey(Cipher cipher, unsigned char* key, IN OUT int *keylen)
//{
//	return DRM_Err_Unkown;
//}
//
//EBOOK_DRM_API DRM_Err
//cipher_stream_create(PCipherStream pstream, const char* transformation)
//{
//	return DRM_Err_Unkown;
//}
//
//EBOOK_DRM_API DRM_Err
//cipher_stream_init(PCipherStream pstream, int opmode, 
//				   const unsigned char* key, int key_len,
//				   const unsigned char* IV, 	int IV_len,
//				   read_proc read_ptr, void* read_userp,
//				   write_proc write_ptr, void* write_userp
//				   )
//{
//	return DRM_Err_Unkown;
//}
//
//EBOOK_DRM_API DRM_Err
//cipher_stream_transform(PCipherStream pstream)
//{
//	return DRM_Err_Unkown;
//}
//
//
//EBOOK_DRM_API DRM_Err
//cipher_stream_destroy(PCipherStream pstream)
//{
//	return DRM_Err_Unkown;
//}
