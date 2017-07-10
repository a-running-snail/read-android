// DRMLib.cpp : 定义 DLL 应用程序的导出函数。
//

#include "DRMLib.h"
#include "Utility.h"
#include <time.h>
#include "drmalgorithm.h"
#include <stdlib.h>
#include "des.h"
#include "md5.h"
extern "C"
{
#include "p1363.h"
}

//反跟踪开关设置
//#define ANTI_DEBUG

static char* RIGHT_VER = (char*)"0001";
static char* KEY_VER = (char*)"0001";

#define VER_LEN            4
#define HASH_LEN           32

#define IV_CIPHER_VER_1       (const unsigned char*)"0000000000000000"
#define IV_LEN_CIPHER_VER_1   16
#define CIPHER_CREATE_MODE_1  "1/1/0"

#define LENGTH_TAG         4
#define LENGTH_DEV_TAG     5
#define LENGTH_LEN         4

#define TAG_EBOOK_ID       "<ID>"
#define TAG_CONTENT_KEY    "<CK>"
#define TAG_DEV_ID_HASH    "<HH>"
#define TAG_RIGHT_HASH     "<HS>"

#define TAG_DEV_CPU        "#CPU#"
#define TAG_DEV_HAD        "#HDD#"
#define TAG_DEV_MAC        "#MAC#"

#define TAG_SEPARATE       ";;"


//加密算法与描述，放在内容密钥的前两个字节，用来记录该密钥对应的算法和加密模式
#define VER_ENCRYPT_TYPE_MODE  "11"
#define LEN_ENCRYPT_TYPE_MODE   2

bool FileEncryptAES(char* szKey, int nKeyLen, char* szInFileName, char* szOutFileName)
{
	return file_AES_encrypt(szKey, nKeyLen, szInFileName, szOutFileName);
}

bool FileDecryptAES(char* szKey, int nKeyLen, char* szInFileName, char* szOutFileName)
{
	return file_AES_decrypt(szKey, nKeyLen, szInFileName, szOutFileName);
}

bool StringEncryptAES(char* szKey, char* pInput, int nInputLen, char** pOutput, int* nOutputLen, bool bPadding)
{
	return string_AES_encrypt(szKey, pInput, nInputLen, pOutput, nOutputLen, bPadding);
}

bool StringDecryptAES(char* szKey, char* pInput, int nInputLen, char** pOutput, int* nOutputLen, bool bPadding)
{
	return string_AES_decrypt(szKey, pInput, nInputLen, pOutput, nOutputLen, bPadding);
}

void FreePtrAES(void* ptr)
{
	FreePtr_AES(ptr);
}

int CreateCipher(PCipher pcipher)
{
	int nRet = ERR_OTHER;
	if(!strcmp(VER_ENCRYPT_TYPE_MODE, "11"))
	{
		if(!cipher_create(pcipher, CIPHER_CREATE_MODE_1))
			nRet = 0;
	}
	return nRet;
}

int InitCipher(Cipher cipher, int opmode, const unsigned char* key, int key_len)
{	
	int nRet = ERR_OTHER;
	if(!strcmp(VER_ENCRYPT_TYPE_MODE, "11"))
	{
		char* hash_key;
		int hash_key_len;
		Hash256((char*)key, key_len, &hash_key, &hash_key_len);

		if(!cipher_init(cipher, opmode, (unsigned char*)hash_key, hash_key_len, IV_CIPHER_VER_1, IV_LEN_CIPHER_VER_1))
			nRet = 0;

		FreePtrAES(hash_key);
	}
	return nRet;
}

int UpdateCipher(Cipher cipher, const unsigned char* in_data, int in_data_len, 
				 unsigned char* out_data, int* out_data_len)
{
	int nRet = ERR_OTHER;
	if(!cipher_update(cipher, in_data, in_data_len, out_data, out_data_len))
		nRet = 0;
	return nRet;
}

int FinalCipher(Cipher cipher, const unsigned char* in_data, int in_data_len,
				unsigned char* out_data, int* out_data_len)
{
	int nRet = ERR_OTHER;
	if(!cipher_final(cipher, in_data, in_data_len, out_data, out_data_len))
		nRet = 0;
	return nRet;
}

int DestroyCipher(Cipher cipher)
{
	int nRet = ERR_OTHER;
	if(!cipher_destroy(cipher))
		nRet = 0;
	return nRet;
}

void Hash256(char* pInput, int nInputLen, char** pOutput, int* nOutputLen)
{
	Hash_256(pInput, nInputLen, pOutput, nOutputLen);
}

int ByteToHex(const unsigned char* pByte, const int nByteLen, char** pHex, int* nHexLen)
{
	if((NULL == pByte) || (0 == nByteLen) || (NULL == pHex) || (NULL == nHexLen))
		return ERR_PARAMETER_INVALID;

	*pHex = new char[nByteLen * 2 + 1]; 
	if(NULL == *pHex)
		return ERR_MEMORY_ALLOCATION;	

	int tmp = 0;
	for (int i=0;i<nByteLen;i++)
	{
		tmp = (int)(pByte[i]) / 16;
		(*pHex)[i*2] = (char)(tmp+((tmp > 9)?'a'-10:'0'));

		tmp = (int)(pByte[i]) % 16;
		(*pHex)[i*2+1] = (char)(tmp+((tmp > 9)?'a'-10:'0'));
	}

	(*pHex)[nByteLen * 2] = (char)'\0';
	*nHexLen = nByteLen*2;

	return ERR_SUCCESS;
}

int MD5File( const char* pFilePath, char** ppOutput, int* nOutputLen )
{
	if( (NULL == pFilePath) || (NULL == ppOutput) || (NULL == nOutputLen))
		return ERR_PARAMETER_INVALID;

	unsigned char Output[16] = { 0 };
	int nRet = md5_file(pFilePath, Output);
	if(0 != nRet)		
	{
		CLOG log;
		log.Write("MD5File md5_file error! nRet=%d", nRet);
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pFilePath：%s", pFilePath);

		return ERR_OTHER;
	}

	return ByteToHex(Output, 16, ppOutput, nOutputLen);
}

int StringEncryptQomolangma( char* pInBuf, int nInBufLen, char** ppOutBuf, int* nOutBufLen )
{
	if( (NULL == pInBuf) || (nInBufLen <= 0) ||(NULL == ppOutBuf) || (NULL == nOutBufLen))
		return ERR_PARAMETER_INVALID;

	int nLen = nInBufLen;

	char* pTmpBuf = new char[nLen+1];
	if( NULL == pTmpBuf)
		return ERR_MEMORY_ALLOCATION;

	memset(pTmpBuf, 0, nLen+1);

	int nRet = ExchangeChar(pInBuf, pTmpBuf, nLen);
	if(-1 == nRet)
	{
		CLOG log;
		log.Write("StringEncryptQomolangma ExchangeChar error! nRet=%d", nRet);
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pInBuf：%s", pInBuf);

		delete[] pTmpBuf;
		pTmpBuf = NULL;
		return nRet;
	}

	char* pExcharBuf = new char[nLen+1];
	if( NULL == pExcharBuf )
	{
		delete[] pTmpBuf;
		pTmpBuf = NULL;
		return ERR_MEMORY_ALLOCATION;
	}

	memset( pExcharBuf, 0, nLen+1 );

	for(int i=0; i<nLen; i++)
	{
		bool bFlag1 = pTmpBuf[i] >= 'a' && pTmpBuf[i] <= 'z';
		bool bFlag2 = pTmpBuf[i] >= 'A' && pTmpBuf[i] <= 'Z';
		bool bFlagNum = pTmpBuf[i] >= '0' && pTmpBuf[i] <= '9';

		if(bFlag1)
		{                              
			(pExcharBuf)[i] = (int)pTmpBuf[i]+3;    
			if( (pExcharBuf)[i] > (int)'z' )
			{        
				(pExcharBuf)[i] -= 26;
			}
		}
		else if(bFlag2)
		{
			(pExcharBuf)[i] = (int)pTmpBuf[i]+3; 
			if( (pExcharBuf)[i] > (int)'Z' )
			{        
				(pExcharBuf)[i] -= 26;
			}
		}
		else if( bFlagNum )                                         
		{
			if( (int)'9' > (int)pTmpBuf[i] )
			{
				(pExcharBuf)[i] = (int)pTmpBuf[i]+1;
			}
			else
			{
				(pExcharBuf)[i] = '0';
			}
		}
		else
		{
			(pExcharBuf)[i] = (int)pTmpBuf[i];
		}
	}

	delete[] pTmpBuf;
	pTmpBuf = NULL;

	char* pBillCryptBuf = BillEncode64( pExcharBuf, nLen );
	if(NULL == pBillCryptBuf)
	{
		CLOG log;
		log.Write("StringEncryptQomolangma BillEncode64 error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pExcharBuf：%s Len:%d", pExcharBuf, nLen);

		delete[] pExcharBuf;
		pExcharBuf = NULL;
		return ERR_OTHER;
	}

	delete[] pExcharBuf;
	pExcharBuf = NULL;

	int nBillCryptBufLen = nInBufLen*2;

#ifdef ANTI_DEBUG
#ifdef WIN32  
	__asm
	{
		rdtsc
			mov ecx, eax
			mov ebx, edx
	}
#endif
#endif

	JY_STATE JY_Instance;
	memset(&JY_Instance, 0, sizeof(JY_Instance));
	JY_Crypt(&JY_Instance, (unsigned char*)JY_key, (unsigned char*)pBillCryptBuf, (nBillCryptBufLen < JY_ENCODE_LEN)? nBillCryptBufLen : JY_ENCODE_LEN);

#ifdef ANTI_DEBUG
#ifdef WIN32
	__asm
	{
		rdtsc
			cmp edx, ebx
			ja __debugger_found
			sub eax, ecx
			cmp eax, 0x9FFFFFFF
			ja __debugger_found
	}
#endif
#endif

#if 1
	*nOutBufLen = nBillCryptBufLen/3*4;
	if(0 != nBillCryptBufLen%3)
		*nOutBufLen = *nOutBufLen+4;
#else
	*nOutBufLen =BASE64_LENGTH(nBillCryptBufLen);
#endif
		*ppOutBuf = new char[*nOutBufLen+10];
	if(NULL == *ppOutBuf)
	{
		delete[] pBillCryptBuf;
		pBillCryptBuf = NULL;
		return ERR_MEMORY_ALLOCATION;
	}
	memset(*ppOutBuf, 0, *nOutBufLen+10);
	base64Encode(pBillCryptBuf, nBillCryptBufLen, *ppOutBuf);

	delete[] pBillCryptBuf;
	pBillCryptBuf = NULL;

	return ERR_SUCCESS;

#ifdef ANTI_DEBUG
#ifdef WIN32

__debugger_found:

#endif
#endif

	return ERR_OTHER;
}

int StringDecryptQomolangma( char* pInBuf, int nInBufLen, char** ppOutBuf, int* nOutBufLen )
{
	if( (NULL == pInBuf) || (nInBufLen <= 0) || (NULL == ppOutBuf) || (NULL == nOutBufLen) )
		return ERR_PARAMETER_INVALID;

	//int nBillCryptBufLen = nInBufLen*3/4;
	int nBillCryptBufLen = BASE64_DECODE_CAPACITY(nInBufLen);
	char* pBillCryptBuf = new char[nBillCryptBufLen+1];
	if(NULL == pBillCryptBuf)
	{
		return ERR_MEMORY_ALLOCATION;
	}
	memset(pBillCryptBuf, 0, nBillCryptBufLen+1);
	nBillCryptBufLen = base64Decode(pInBuf, nInBufLen, pBillCryptBuf);
	if(0 == nBillCryptBufLen)
	{
		CLOG log;
		log.Write("StringDecryptQomolangma base64Decode error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pInBuf：%s Len: %d", pInBuf, nInBufLen);

		delete[] pBillCryptBuf;
		pBillCryptBuf = NULL;
		return ERR_OTHER;
	}

#ifdef ANTI_DEBUG
#ifdef WIN32  
	__asm
	{
		rdtsc
			mov ecx, eax
			mov ebx, edx
	}
#endif
#endif

	JY_STATE JY_Instance;
	memset(&JY_Instance, 0, sizeof(JY_Instance));
	JY_Crypt(&JY_Instance, (unsigned char*)JY_key, (unsigned char*)pBillCryptBuf, (nBillCryptBufLen < JY_ENCODE_LEN)? nBillCryptBufLen : JY_ENCODE_LEN);

#ifdef ANTI_DEBUG
#ifdef WIN32
	__asm
	{
		rdtsc
			cmp edx, ebx
			ja __debugger_found
			sub eax, ecx
			cmp eax, 0x9FFFFFFF
			ja __debugger_found
	}
#endif
#endif

	int nDecBufLen;
	char* pDecBuf = BillDecode( pBillCryptBuf, nBillCryptBufLen, &nDecBufLen );
	if(NULL == pDecBuf)
	{
		CLOG log;
		log.Write("StringDecryptQomolangma BillDecode error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pBillCryptBuf：%s Len: %d", pBillCryptBuf, nBillCryptBufLen);

		delete[] pBillCryptBuf;
		pBillCryptBuf = NULL;
		return ERR_OTHER;
	}

	delete[] pBillCryptBuf;
	pBillCryptBuf = NULL;

	char* pTmpBuf = new char[nDecBufLen+1];
	if( NULL == pTmpBuf)
	{
		delete[] pDecBuf;
		pDecBuf = NULL;
		return ERR_MEMORY_ALLOCATION;
	}

	memset(pTmpBuf, 0, nDecBufLen+1);
	int nRet = ExchangeChar( pDecBuf, pTmpBuf,nDecBufLen );
	if( -1 == nRet)
	{
		CLOG log;
		log.Write("StringDecryptQomolangma ExchangeChar error! nRet=%d", nRet);
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pDecBuf：%s", pDecBuf);

		delete[] pDecBuf;
		pDecBuf = NULL;

		delete[] pTmpBuf;
		pTmpBuf = NULL;

		return nRet;
	}

	delete[] pDecBuf;
	pDecBuf = NULL;

	*ppOutBuf = new char[nDecBufLen+10];
	if( NULL == *ppOutBuf )
	{
		delete[] pTmpBuf;
		pTmpBuf = NULL;

		return ERR_MEMORY_ALLOCATION;
	}

	memset(*ppOutBuf, 0, nDecBufLen+10);

	for( int i=0; i<nDecBufLen; i++ )
	{
		bool flag1 = pTmpBuf[i] >= 'a' && pTmpBuf[i] <= 'z';
		bool flag2 = pTmpBuf[i] >= 'A' && pTmpBuf[i] <= 'Z';
		bool bFlagNum = pTmpBuf[i] >= '0' && pTmpBuf[i] <= '9';

		if(flag1) 
		{                              
			(*ppOutBuf)[i] = (int)pTmpBuf[i] - 3;    
			if( (*ppOutBuf)[i] < (int)'a' )
			{        
				(*ppOutBuf)[i] += 26;
			}
		}
		else if(flag2)
		{
			(*ppOutBuf)[i] = (int)pTmpBuf[i] - 3; 
			if( (*ppOutBuf)[i] < (int)'A' )
			{        
				(*ppOutBuf)[i] += 26;
			}
		}
		else if(bFlagNum)
		{
			if( (int)'0' == (int)pTmpBuf[i] )
			{
				(*ppOutBuf)[i] = '9';
			}
			else
			{	
				(*ppOutBuf)[i] = (int)pTmpBuf[i]-1;
			}
		}
		else    
		{
			(*ppOutBuf)[i] = (int)pTmpBuf[i];
		}
	}

	delete[] pTmpBuf;
	pTmpBuf = NULL;

	*nOutBufLen = nDecBufLen;

	return ERR_SUCCESS;

#ifdef ANTI_DEBUG
#ifdef WIN32

__debugger_found:

#endif
#endif
	return ERR_OTHER;
}

int EncryptByVersion( char* pInBuf, int nInBufLen, char** ppOutBuf, int* nOutBufLen, char* pVersion )
{
	if((NULL == pInBuf) || (nInBufLen <= 0) || (NULL == ppOutBuf) || (NULL == nOutBufLen) || (NULL == pVersion))
		return ERR_PARAMETER_INVALID;

	int nRet = ERR_OTHER;
	if(!strcmp(pVersion, "0001"))
	{
		nRet = StringEncryptQomolangma(pInBuf, nInBufLen, ppOutBuf, nOutBufLen);
		if(ERR_SUCCESS != nRet)
		{
			CLOG log;
			log.Write("EncryptByVersion StringEncryptQomolangma error! nRet=%d", nRet);
			log.Write("File %s, Line %d", __FILE__, __LINE__);
			log.Write("pInBuf：%s Len: %d", pInBuf, nInBufLen);

			return nRet;
		}
	}
	else	
		return ERR_VERSION_INVALID;	

	return nRet;

}

int DecryptByVersion( char* pInBuf, int nInBufLen, char** ppOutBuf, int* nOutBufLen, char* pVersion )
{
	if((NULL == pInBuf) || (nInBufLen <= 0) || (NULL == ppOutBuf) || (NULL == nOutBufLen) || (NULL == pVersion))
		return ERR_PARAMETER_INVALID;

	int nRet = ERR_OTHER;
	if(!strcmp(pVersion, "0001"))
	{
		nRet = StringDecryptQomolangma(pInBuf, nInBufLen, ppOutBuf, nOutBufLen);
		if(ERR_SUCCESS != nRet)
		{
			CLOG log;
			log.Write("DecryptByVersion StringDecryptQomolangma error! nRet=%d", nRet);
			log.Write("File %s, Line %d", __FILE__, __LINE__);
			log.Write("pInBuf：%s Len: %d", pInBuf, nInBufLen);

			return nRet;
		}
	}
	else	
		return ERR_VERSION_INVALID;	

	return nRet;

}

void FreePtr(void* ptr)
{
	if (NULL != ptr)
	{
		delete[] (char*)ptr;
		ptr = NULL;
	}	
}

int GetDRMVersion(char* pVersion)
{
	if(NULL == pVersion)
		return ERR_PARAMETER_INVALID;

	memset(pVersion, 0, VER_LEN+VER_LEN+LEN_ENCRYPT_TYPE_MODE+1);

	memcpy(pVersion, RIGHT_VER, VER_LEN);
	memcpy(pVersion+VER_LEN, KEY_VER, VER_LEN);
	memcpy(pVersion+VER_LEN+VER_LEN, VER_ENCRYPT_TYPE_MODE, LEN_ENCRYPT_TYPE_MODE);

	return ERR_SUCCESS;
}

#ifdef _ENCTOOL
int GenerateKeyAES( char** ppKey, int* nLen)
{
	if((NULL == ppKey) || (NULL == nLen))
		return ERR_PARAMETER_INVALID;

	*nLen = 32;
	int nRet = GenerateAESKey(ppKey);
	if( 0 != nRet)
	{
		CLOG log;
		log.Write("GenerateKeyAES GenerateAESKey error! nRet=%d", nRet);
		log.Write("File %s, Line %d", __FILE__, __LINE__);

		return nRet;
	}

	if(!strcmp(VER_ENCRYPT_TYPE_MODE, "11"))
	{
		memcpy(*ppKey, VER_ENCRYPT_TYPE_MODE, LEN_ENCRYPT_TYPE_MODE);
	}	

	return ERR_SUCCESS;

}

int ProtectKey(char* pKey, int nKeyLen, char** ppEncKey, int* pEncKeyLen)
{
	if((NULL == pKey) || (nKeyLen <= 0) || (NULL == ppEncKey) || (NULL == pEncKeyLen))
		return ERR_PARAMETER_INVALID;

	// 计算Key的Hash值
	char *pKeyHash = NULL;
	int nKeyHashLen = 0;
	Hash256(pKey, nKeyLen, &pKeyHash, &nKeyHashLen);

	// Key和Hash值拼接
	int nKeyBufLen = nKeyLen + nKeyHashLen;
	char *pKeyBuf = new char[nKeyBufLen + 1];
	if(NULL == pKeyBuf)
	{
		FreePtrAES(pKeyHash);
		return ERR_MEMORY_ALLOCATION;
	}
	memset(pKeyBuf, 0, nKeyBufLen + 1);
	memcpy(pKeyBuf, pKey, nKeyLen);
	memcpy(pKeyBuf+nKeyLen, pKeyHash, nKeyHashLen);

	FreePtrAES(pKeyHash);

	// 根据版本号对密钥加密
	char *pEncKeyBuf = NULL;
	int nEncKeyBufLen = 0;
	int nRet = EncryptByVersion(pKeyBuf, nKeyBufLen, &pEncKeyBuf, &nEncKeyBufLen, KEY_VER);
	if(ERR_SUCCESS != nRet)
	{
		CLOG log;
		log.Write("ProtectKey EncryptByVersion encrypt key error! nRet=%d", nRet);
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pKeyBuf：%s Len: %d", pKeyBuf, nKeyBufLen);

		delete []pKeyBuf;
		pKeyBuf = NULL;
		return nRet;
	}

	delete []pKeyBuf;
	pKeyBuf = NULL;

	// 输出
	*pEncKeyLen = VER_LEN + nEncKeyBufLen;
	*ppEncKey = new char[*pEncKeyLen + 1];
	if(NULL == *ppEncKey)
	{
		FreePtr(pEncKeyBuf);
		return ERR_MEMORY_ALLOCATION;
	}
	memset(*ppEncKey, 0, *pEncKeyLen + 1);
	memcpy(*ppEncKey, KEY_VER, VER_LEN);
	memcpy(*ppEncKey+VER_LEN, pEncKeyBuf, nEncKeyBufLen);

	FreePtr(pEncKeyBuf);

	return ERR_SUCCESS;

}
#endif // _ENCTOOL

#ifdef _SERVER
int EncryptCK(char* pInBuf, int nInBufLen, char* pDevIDHash, int nDevIDHashLen, char* pRandomNum, int nRandomNumLen, char** pOutBuf, int* nOutBufLen)
{
	if((NULL == pInBuf) || (nInBufLen <= 0) || (NULL == pDevIDHash) || (nDevIDHashLen <= 0)
		||(NULL == pRandomNum) || (nRandomNumLen <= 0) || (NULL == pOutBuf) || (NULL == nOutBufLen))
		return ERR_PARAMETER_INVALID;

	// 获取内容密钥
	char* pContKeyTag = memstr(pInBuf, nInBufLen, TAG_CONTENT_KEY, LENGTH_LEN);
	if(NULL == pContKeyTag)
	{
		return ERR_OTHER;
	}
	char szCKLen[5] = {0};
	memcpy(szCKLen, pContKeyTag+LENGTH_TAG, LENGTH_LEN);
	int nCKLen = atoi(szCKLen);

	char* pContKeyVal = pContKeyTag+LENGTH_TAG+LENGTH_LEN+VER_LEN;

	// 设备信息和随机数拼接
	char* pHardInfoAndRandVal = new char[nDevIDHashLen+nRandomNumLen+1];
	if(NULL == pHardInfoAndRandVal)
	{
		return ERR_MEMORY_ALLOCATION;
	}
	memset(pHardInfoAndRandVal, 0, nDevIDHashLen+nRandomNumLen+1);
	memcpy(pHardInfoAndRandVal, pDevIDHash, nDevIDHashLen);
	memcpy(pHardInfoAndRandVal+nDevIDHashLen, pRandomNum, nRandomNumLen);

	// 设备信息和随机数Hash
	char* pHI_Hash;
	int nHI_HashLen;
	Hash256(pHardInfoAndRandVal, nDevIDHashLen+nRandomNumLen, &pHI_Hash, &nHI_HashLen);

	delete[] pHardInfoAndRandVal;
	pHardInfoAndRandVal = NULL;

	// 加密内容密钥
	char* pTmpEncryptedCK;
	int nTmpEncryptedCKLen;
	if(!StringEncryptAES(pHI_Hash, pContKeyVal, HASH_LEN, &pTmpEncryptedCK, &nTmpEncryptedCKLen, false))
	{
		CLOG log;
		log.Write("EncryptCK StringEncryptAES encrypt content key error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("HI_HashLen:%d", HASH_LEN);
		for(int i=0; i<HASH_LEN; i++)
			log.Write("pHI_Hash[%d]:%x", i, pHI_Hash[i]);
		log.Write("pContKeyVal:%s", pContKeyVal);

		FreePtrAES(pHI_Hash);
		return ERR_ENCRYPT_DECRYPT;
	}

	FreePtrAES(pHI_Hash);

	// 加密密钥处理
	int nEncCKLen = nCKLen-VER_LEN;
	char* pEncCK = new char[nEncCKLen+1];
	if(NULL == pEncCK)
	{
		FreePtrAES(pTmpEncryptedCK);
		return ERR_MEMORY_ALLOCATION;
	}
	memset(pEncCK, 0, nEncCKLen+1);
	memcpy(pEncCK, pContKeyVal, nEncCKLen);
	memcpy(pEncCK, pTmpEncryptedCK, HASH_LEN);

	FreePtrAES(pTmpEncryptedCK);

	// 输出
	*nOutBufLen = nInBufLen;
	*pOutBuf = new char[*nOutBufLen+1];
	if(NULL == *pOutBuf)
	{
		delete []pEncCK;
		pEncCK = NULL;
		return ERR_MEMORY_ALLOCATION;
	}
	memset(*pOutBuf, 0, *nOutBufLen+1);
	memcpy(*pOutBuf, pInBuf, pContKeyVal-pInBuf);
	memcpy(*pOutBuf+(pContKeyVal-pInBuf), pEncCK, nEncCKLen);
	
	delete[] pEncCK;
	pEncCK = NULL;

	return ERR_SUCCESS;
}

int GenerateRightFile( char* pEbookID, int nEbookIDLen, char* pContKey, int nContKeyLen, char* pDevIDHashCipher, 
					  int nDevIDHashCipherLen, char* pRandomCipher, int nRandomCipherLen, char** ppRightFileBuf, int* nRightFileBufLen)
{
	if((NULL == pEbookID) || (nEbookIDLen <= 0) || (NULL == pContKey) || (nContKeyLen <= 0) 
		|| (NULL == pDevIDHashCipher) || (nDevIDHashCipherLen <= 0) || (NULL == pRandomCipher) || (nRandomCipherLen <= 0)
		|| (NULL == ppRightFileBuf) || (NULL == nRightFileBufLen))
	{
		CLOG log;
		log.Write("GenerateRightFile parameter error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		if(NULL == pEbookID)
			log.Write("pEbookID is NULL!");
		else
			log.Write("pEbookID:%s, nEbookIDLen:%d", pEbookID, nEbookIDLen);

		if(NULL == pContKey)
			log.Write("pContKey is NULL!");
		else
			log.Write("pContKey:%s, nContKeyLen:%d", pContKey, nContKeyLen);

		if(NULL == pDevIDHashCipher)
			log.Write("pDevIDHashCipher is NULL!");
		else
			log.Write("pDevIDHashCipher:%s, nDevIDHashCipherLen:%d", pDevIDHashCipher, nDevIDHashCipherLen);

		if(NULL == pRandomCipher)
			log.Write("pRandomCipher is NULL!");
		else
			log.Write("pRandomCipher:%s, nRandomCipherLen:%d", pRandomCipher, nRandomCipherLen);

		if(NULL == ppRightFileBuf)
			log.Write("ppRightBuf is NULL!");

		return ERR_PARAMETER_INVALID;
	}

	// 根据版本号解密密钥，校验密钥Hash值
	char *pDeContKey = NULL;
	int nDeContKeyLen = 0;
	char pKeyVersion[5] = { 0 };
	memcpy(pKeyVersion, pContKey, VER_LEN);
	int nRet = DecryptByVersion(pContKey+VER_LEN, nContKeyLen-VER_LEN, &pDeContKey, &nDeContKeyLen, pKeyVersion);
	if(ERR_SUCCESS != nRet)		
	{
		CLOG log;
		log.Write("GenerateRightFile DecryptByVersion decyrpt key error! nRet=%d", nRet);
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pContKey：%s Len: %d", pContKey+VER_LEN, nContKeyLen-VER_LEN);

		return nRet;
	}

	if (nDeContKeyLen > 32)
	{
		char *pContKeyHash = NULL;
		int nContKeyHashLen = 0;
		Hash256(pDeContKey, 32, &pContKeyHash, &nContKeyHashLen);
		if(memcmp(pContKeyHash, pDeContKey + 32, HASH_LEN))
		{
			CLOG log;
			log.Write("GenerateRightFile check keyhash error!");
			log.Write("File %s, Line %d", __FILE__, __LINE__);

			FreePtr(pDeContKey);
			FreePtrAES(pContKeyHash);
			return ERR_OTHER;
		}
		FreePtrAES(pContKeyHash);
	}

	FreePtr(pDeContKey);	
	//char *pContKeyHash = NULL;
	//int nContKeyHashLen = 0;
	//Hash256(pDeContKey, 32, &pContKeyHash, &nContKeyHashLen);
	//if(memcmp(pContKeyHash, pDeContKey + 32, HASH_LEN))
	//{
	//	CLOG log;
	//	log.Write("GenerateRightFile check keyhash error!");
	//	log.Write("File %s, Line %d", __FILE__, __LINE__);

	//	FreePtr(pDeContKey);
	//	FreePtrAES(pContKeyHash);
	//	return ERR_OTHER;
	//}

	//FreePtr(pDeContKey);
	//FreePtrAES(pContKeyHash);	

	// 拼接授权文件字符串
	int nTmpBufLen = nEbookIDLen + nContKeyLen + (LENGTH_TAG+LENGTH_LEN)*2;
	char* pTmpBuf = new char[nTmpBufLen + 1];
	if(NULL == pTmpBuf)
	{
		return ERR_MEMORY_ALLOCATION; 
	}
	memset(pTmpBuf, 0, nTmpBufLen + 1);
	char szLength[5] = {0};
	memcpy(pTmpBuf, TAG_EBOOK_ID, LENGTH_TAG);
	sprintf(szLength, "%04d", nEbookIDLen);
	memcpy(pTmpBuf+LENGTH_TAG, szLength, LENGTH_LEN);
	memcpy(pTmpBuf+LENGTH_TAG+LENGTH_LEN, pEbookID, nEbookIDLen);

	memcpy(pTmpBuf+LENGTH_TAG+LENGTH_TAG+nEbookIDLen,  TAG_CONTENT_KEY, LENGTH_TAG);
	sprintf(szLength, "%04d", nContKeyLen);
	memcpy(pTmpBuf+LENGTH_TAG+LENGTH_LEN+nEbookIDLen+LENGTH_TAG, szLength, LENGTH_LEN);
	memcpy(pTmpBuf+LENGTH_TAG+LENGTH_LEN+nEbookIDLen+LENGTH_TAG+LENGTH_LEN, pContKey, nContKeyLen);

	// 根据版本号解密设备信息
	char* pDevIDHash = NULL;
	int nDevIDHashLen = 0;
	nRet = DecryptByVersion(pDevIDHashCipher, nDevIDHashCipherLen, &pDevIDHash, &nDevIDHashLen, RIGHT_VER);
	if(ERR_SUCCESS != nRet)
	{
		CLOG log;
		log.Write("GenerateRightFile DecryptByVersion decrypt deviceid error! nRet=%d", nRet);
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pDevIDHashCipher：%s Len: %d", pDevIDHashCipher, nDevIDHashCipherLen);

		delete[] pTmpBuf;
		pTmpBuf = NULL;
		return nRet;
	}

	// 根据版本号解密随机数
	char* pRandomNum = NULL;
	int nRandomNumLen = 0;
	char pRandomVersion[5] = { 0 };
	memcpy(pRandomVersion, pRandomCipher, VER_LEN);	
	if(!strcmp(pRandomVersion,"0001"))
	{
		nRet = DecryptByVersion(pRandomCipher+VER_LEN, nRandomCipherLen-VER_LEN, &pRandomNum, &nRandomNumLen, pRandomVersion);
		if(ERR_SUCCESS != nRet)
		{
			CLOG log;
			log.Write("GenerateRightFile DecryptByVersion decrypt random error! nRet=%d", nRet);
			log.Write("File %s, Line %d", __FILE__, __LINE__);
			log.Write("pRandomCipher：%s Len: %d", pRandomCipher, nRandomCipherLen);

			delete[] pTmpBuf;
			pTmpBuf = NULL;
			FreePtr(pDevIDHash);
			return nRet;
		}

	}
	else
	{
		nRet = StringDecryptQomolangma(pRandomCipher, nRandomCipherLen, &pRandomNum, &nRandomNumLen);
		if(ERR_SUCCESS != nRet)
		{
			CLOG log;
			log.Write("GenerateRightFile DecryptByVersion decrypt random error! nRet=%d", nRet);
			log.Write("File %s, Line %d", __FILE__, __LINE__);
			log.Write("pRandomCipher：%s Len: %d", pRandomCipher, nRandomCipherLen);

			delete[] pTmpBuf;
			pTmpBuf = NULL;
			FreePtr(pDevIDHash);
			return nRet;
		}

	}

	// 加密内容密钥
	char* pRightBuf = NULL;
	int nRightBufLen = 0;
	nRet = EncryptCK(pTmpBuf, nTmpBufLen, pDevIDHash, nDevIDHashLen, pRandomNum, nRandomNumLen, &pRightBuf, &nRightBufLen);
	if(ERR_SUCCESS != nRet)
	{
		delete[] pTmpBuf;
		pTmpBuf = NULL;
		FreePtr(pDevIDHash);
		FreePtr(pRandomNum);
		return nRet;
	}

	delete[] pTmpBuf;
	pTmpBuf = NULL;
	FreePtr(pDevIDHash);
	FreePtr(pRandomNum);

	// 根据版本号加密授权文件字符串
	char* pEncodeBuf = NULL;
	int nEncodeLen = 0;
	nRet = EncryptByVersion(pRightBuf, nRightBufLen, &pEncodeBuf, &nEncodeLen, RIGHT_VER);
	if(ERR_SUCCESS != nRet)
	{
		CLOG log;
		log.Write("GenerateRightFile EncryptByVersion encrypt rightfile error! nRet=%d", nRet);
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pRightBuf：%s Len: %d", pRightBuf, nRightBufLen);

		delete[] pRightBuf;
		pRightBuf = NULL;
		FreePtr(pEncodeBuf);
		return nRet;
	}	

	// 计算授权文件字符串Hash值并Base64Encode
	char* pHashBuf = NULL;
	int nHashLen = 0;
	Hash256(pRightBuf, nRightBufLen, &pHashBuf, &nHashLen);

	delete[] pRightBuf;
	pRightBuf = NULL;

#if 1
	int nHashBase64Len = nHashLen/3 *4;
	if(0 != nHashLen%3)
		nHashBase64Len = nHashBase64Len+4;
#else
	//nEncStrLen = strlen(pEncStr);
	int nHashBase64Len = BASE64_LENGTH(nHashLen);
#endif
	char* pHashBase64Buf = new char[nHashBase64Len+1];
	if(NULL == pHashBase64Buf)
	{
		FreePtrAES(pHashBuf);
		FreePtr(pEncodeBuf);
		return ERR_MEMORY_ALLOCATION;
	}

	memset(pHashBase64Buf, 0, nHashBase64Len+1);

	base64Encode(pHashBuf, nHashLen, pHashBase64Buf);

	FreePtrAES(pHashBuf);

	// 输出到文件
	*nRightFileBufLen = VER_LEN + nEncodeLen + LENGTH_TAG + LENGTH_LEN + nHashBase64Len;
	*ppRightFileBuf = new char[*nRightFileBufLen+1];
	if(NULL == *ppRightFileBuf)
	{
		FreePtr(pEncodeBuf);
		delete[] pHashBase64Buf;
		pHashBase64Buf = NULL;
		return ERR_MEMORY_ALLOCATION;
	}

	memset(*ppRightFileBuf, 0, *nRightFileBufLen+1);
	memcpy(*ppRightFileBuf, RIGHT_VER, VER_LEN);
	memcpy(*ppRightFileBuf+VER_LEN, pEncodeBuf, nEncodeLen);
	memcpy(*ppRightFileBuf+VER_LEN+nEncodeLen, TAG_RIGHT_HASH, LENGTH_TAG);
	char szHashBase64Len[5] ={0};
	sprintf(szHashBase64Len, "%04d", nHashBase64Len);
	memcpy(*ppRightFileBuf+VER_LEN+nEncodeLen+LENGTH_TAG, szHashBase64Len, LENGTH_LEN);
	memcpy(*ppRightFileBuf+VER_LEN+nEncodeLen+LENGTH_TAG+LENGTH_LEN, pHashBase64Buf, nHashBase64Len);

	FreePtr(pEncodeBuf);
	delete[] pHashBase64Buf;
	pHashBase64Buf = NULL;	

	return ERR_SUCCESS;
}

int GenerateRandom( char** ppRandomCipher, int* pRandomCipherLen )
{
	if((NULL == ppRandomCipher) || (NULL == pRandomCipherLen))
		return ERR_PARAMETER_INVALID;

	char *pRandom = NULL;
	int nRandomLen = 0;

	int nRet = GenerateRandomNum(&pRandom, &nRandomLen);
	if(0 != nRet)
	{
		CLOG log;
		log.Write("GenerateRandom GenerateRandomNum失败! nRet=%d", nRet);
		log.Write("File %s, Line %d", __FILE__, __LINE__);

		return ERR_OTHER;
	}

	char* pCipherRan = NULL;
	int nCipherRanLen = 0;
	nRet = EncryptByVersion(pRandom, nRandomLen, &pCipherRan, &nCipherRanLen, RIGHT_VER);
	if(ERR_SUCCESS != nRet)	
	{
		CLOG log;
		log.Write("GenerateRandom EncryptByVersion encrypt random error! nRet=%d", nRet);
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pRandom：%s Len: %d", pRandom, nRandomLen);

		FreePtrAES(pRandom);

		return nRet;
	}

	FreePtrAES(pRandom);

	*pRandomCipherLen = VER_LEN + nCipherRanLen;
	*ppRandomCipher = new char[*pRandomCipherLen + 1];
	if(NULL == *ppRandomCipher)
	{
		FreePtr(pCipherRan);
		return ERR_MEMORY_ALLOCATION;
	}
	memset(*ppRandomCipher, 0, *pRandomCipherLen + 1);
	memcpy(*ppRandomCipher, RIGHT_VER, VER_LEN);
	memcpy(*ppRandomCipher+VER_LEN, pCipherRan, nCipherRanLen);

	FreePtr(pCipherRan);

	return ERR_SUCCESS;
}
#endif // _SERVER

#ifdef _CLIENT
int DecryptCK(char* pInBuf, int nInBufLen, char* pDevID, int nDevID, char* pRandomNum, int nRandomNumLen, char** pOutBuf, int* nOutBufLen)
{
	if((NULL == pInBuf) || (nInBufLen <= 0 ) || (NULL == pDevID) || (nDevID <= 0) || (NULL == pRandomNum) || (nRandomNumLen <= 0)
		|| (NULL == pOutBuf) || (NULL == nOutBufLen))
		return ERR_PARAMETER_INVALID;

	// 获取内容密钥
	char* pContKeyTag = memstr(pInBuf, nInBufLen, TAG_CONTENT_KEY, LENGTH_TAG);
	if(NULL == pContKeyTag)
		return ERR_OTHER;

	char szCKLen[5] = {0};
	memcpy(szCKLen, pContKeyTag+LENGTH_TAG, LENGTH_LEN);
	int nCKLen = atoi(szCKLen);

	char* pContKeyVal = pContKeyTag+LENGTH_TAG+LENGTH_LEN+VER_LEN;

	// 设备信息和随机数拼接
	char* pHardInfoAndRandVal = new char[nDevID+nRandomNumLen+1];
	if(NULL == pHardInfoAndRandVal)
	{	
		return ERR_MEMORY_ALLOCATION;
	}
	memset(pHardInfoAndRandVal, 0, nDevID+nRandomNumLen+1);
	memcpy(pHardInfoAndRandVal, pDevID, nDevID);
	memcpy(pHardInfoAndRandVal+nDevID, pRandomNum, nRandomNumLen);

	// 设备信息和随机数Hash
	char* pHI_Hash = NULL;
	int nHI_HashLen = 0;
	Hash256(pHardInfoAndRandVal, nDevID+nRandomNumLen, &pHI_Hash, &nHI_HashLen);
	
	delete[] pHardInfoAndRandVal;
	pHardInfoAndRandVal = NULL;

	// 解密内容密钥
	char* pTmpDecryptedCK = NULL;
	int nTmpDecryptedCKLen = 0;
	if(!StringDecryptAES(pHI_Hash, pContKeyVal, HASH_LEN, &pTmpDecryptedCK, &nTmpDecryptedCKLen, false))
	{
		CLOG log;
		log.Write("DecryptCK StringDecryptAES decrpyt content key error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("HI_HashLen:%d", HASH_LEN);
		for(int i=0; i<HASH_LEN; i++)
			log.Write("pHI_Hash[%d]:%x", i, pHI_Hash[i]);
		log.Write("pContKeyVal:%s", pContKeyVal);

		FreePtrAES(pHI_Hash);
		return ERR_ENCRYPT_DECRYPT;
	}

	FreePtrAES(pHI_Hash);
	
	// 解密密钥处理
	int nDecCKLen = nCKLen-VER_LEN;
	char* pDecCK = new char[nDecCKLen+1];
	if(NULL == pDecCK)
	{
		FreePtrAES(pTmpDecryptedCK);
		return ERR_MEMORY_ALLOCATION;
	}
	memset(pDecCK, 0, nDecCKLen+1);
	memcpy(pDecCK, pContKeyVal, nDecCKLen);
	memcpy(pDecCK, pTmpDecryptedCK, HASH_LEN);

	FreePtrAES(pTmpDecryptedCK);

	// 根据版本号解密密钥，校验密钥Hash值
	char* pRawCK = NULL;
	int nRawCKLen = 0;
	char pKeyVersion[5] = { 0 };
	memcpy(pKeyVersion, pContKeyVal - VER_LEN, VER_LEN);
	int nRet = DecryptByVersion(pDecCK, nDecCKLen, &pRawCK, &nRawCKLen, pKeyVersion);
	if(ERR_SUCCESS != nRet)
	{
		CLOG log;
		log.Write("DecryptCK StringDecryptQomolangma decrypt key error! nRet=%d", nRet);
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pDecCK：%s Len: %d", pDecCK, nDecCKLen);

		delete[] pDecCK;
		pDecCK = NULL;
		return nRet;
	}

	char *pRawCKHash = NULL;
	int nRawCKHashLen = 0;
	Hash256(pRawCK, 32, &pRawCKHash, &nRawCKHashLen);

	if(memcmp(pRawCK+32, pRawCKHash, HASH_LEN))
	{
		CLOG log;
		log.Write("DecryptCK check keyhash error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);

		delete[] pDecCK;
		pDecCK = NULL;
		FreePtr(pRawCK);
		FreePtrAES(pRawCKHash);
		return ERR_OTHER;
	}

	delete[] pDecCK;
	pDecCK = NULL;
	FreePtrAES(pRawCKHash);

	// 输出
	*nOutBufLen = pContKeyVal-pInBuf-VER_LEN+32;
	*pOutBuf = new char[*nOutBufLen + 1];
	if(NULL == *pOutBuf)
	{
		FreePtrAES(pRawCK);
		return ERR_MEMORY_ALLOCATION;
	}
	memset(*pOutBuf, 0, *nOutBufLen+1);
	memcpy(*pOutBuf, pInBuf, pContKeyVal-pInBuf-LENGTH_LEN-VER_LEN);
	char szDecCK[5] = {0};
	sprintf(szDecCK, "%04d", 32);
	memcpy(*pOutBuf+(pContKeyVal-pInBuf)-LENGTH_LEN-VER_LEN, szDecCK, LENGTH_LEN);
	memcpy(*pOutBuf+(pContKeyVal-pInBuf)-VER_LEN, pRawCK, 32);	

	FreePtr(pRawCK);

	return ERR_SUCCESS;
}

int AnalyticRightFileBuf(const char* pInBuf, int nInBufLen, char* pDevIDHashCipher, char* pRandomNumCipher, char** ppRight, int* nRightLen)
{
	if((NULL == pInBuf) || (nInBufLen <= 0) || (NULL == pDevIDHashCipher) || (NULL == pRandomNumCipher) 
		|| (NULL == ppRight) || (NULL == nRightLen))
	{
		CLOG log;
		log.Write("AnalyticRightFileBuf parameter error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);

		if(NULL == pInBuf)
			log.Write("pInBuf is NULL!");
		else
			log.Write("pInBuf:%s, nInBufLen:%d", pInBuf, nInBufLen);

		if(NULL == pDevIDHashCipher)
			log.Write("pDevIDHashCipher is NULL!");
		else
			log.Write("pDevIDHashCipher:%s", pDevIDHashCipher);

		if(NULL == pRandomNumCipher)
			log.Write("pRandomNumCipher is NULL!");
		else
			log.Write("pRandomNumCipher:%s", pRandomNumCipher);

		return ERR_PARAMETER_INVALID;
	}

#ifdef ANTI_DEBUG
#ifdef WIN32  
	__asm
	{
		rdtsc
			mov ecx, eax
			mov ebx, edx
	}
#endif
#endif

	// 校验授权文件Hash值
	char* pOriginaHashBase64 = memstr(const_cast<char*>(pInBuf), nInBufLen, TAG_RIGHT_HASH, LENGTH_TAG);
	if(NULL == pOriginaHashBase64)
	{
		CLOG log;
		log.Write("AnalyticRightFileBuf pOriginaHashBase64 is NULL!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);		

		return ERR_OTHER;
	}

	char szHashBase64Len [5] = {0};
	memcpy(szHashBase64Len, pOriginaHashBase64+LENGTH_TAG, LENGTH_LEN);
	int nHashBase64Len = atoi(szHashBase64Len);

	//int nHashOriginaLen = nHashBase64Len/4 *3;
	int nHashOriginaLen = BASE64_DECODE_CAPACITY(nHashBase64Len);
	char* pHashOrigina = new char[nHashOriginaLen+1];
	if(NULL == pHashOrigina)
	{
		return ERR_MEMORY_ALLOCATION;
	}
	memset(pHashOrigina, 0, nHashOriginaLen+1);
	int nHashDecodeLen = base64Decode(pOriginaHashBase64+LENGTH_TAG+LENGTH_LEN, nHashBase64Len, pHashOrigina);
	if(0 == nHashDecodeLen)
	{
		CLOG log;
		log.Write("AnalyticRightFileBuf base64Decode error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pOriginaHashBase64：%s Len: %d", pOriginaHashBase64+LENGTH_TAG+LENGTH_LEN, nHashBase64Len);

		delete []pHashOrigina;
		pHashOrigina = NULL;
		return ERR_OTHER;
	}


#ifdef ANTI_DEBUG
#ifdef WIN32
	__asm
	{
		rdtsc
			cmp edx, ebx
			ja __debugger_found
			sub eax, ecx
			cmp eax, 0x9FFFFFFF
			ja __debugger_found
	}
#endif
#endif

	int nBodyLen = nInBufLen - VER_LEN - LENGTH_TAG - LENGTH_LEN - nHashBase64Len;
	char* pBodyBuf = const_cast<char*>(pInBuf) + VER_LEN;

	char* pTmpRight = NULL;
	int nTmpRightLen = 0;

	// 根据版本号解密授权文件
	char pRightVersion[5] = { 0 };
	memcpy(pRightVersion, pInBuf, VER_LEN);	
	int nRet = DecryptByVersion(pBodyBuf, nBodyLen, &pTmpRight, &nTmpRightLen, pRightVersion);
	if(ERR_SUCCESS != nRet)
	{
		CLOG log;
		log.Write("AnalyticRightFileBuf DecryptByVersion error! nRet=%d", nRet);
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pBodyBuf：%s Len: %d", pBodyBuf, nBodyLen);

		delete []pHashOrigina;
		pHashOrigina = NULL;
		return nRet;
	}

	char* pHashBuf = NULL;
	int nHashLen = 0;
	Hash256(pTmpRight, nTmpRightLen, &pHashBuf, &nHashLen);

	if(memcmp(pHashBuf, pHashOrigina, HASH_LEN))
	{
		CLOG log;
		log.Write("AnalyticRightFileBuf check rightfile hash error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);

		delete []pHashOrigina;
		pHashOrigina = NULL;
		FreePtr(pTmpRight);
		FreePtrAES(pHashBuf);
		return ERR_OTHER;
	}

	delete []pHashOrigina;
	pHashOrigina = NULL;
	FreePtrAES(pHashBuf);

	// 根据版本号解密设备信息
	char* pDevID = NULL;
	int nDevID = 0;
	nRet = DecryptByVersion(pDevIDHashCipher, strlen(pDevIDHashCipher), &pDevID, &nDevID, RIGHT_VER);
	if(ERR_SUCCESS != nRet)		
	{
		CLOG log;
		log.Write("AnalyticRightFileBuf DecryptByVersion decrypt deviceid error! nRet=%d", nRet);
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pDevIDHashCipher：%s Len: %d", pDevIDHashCipher, strlen(pDevIDHashCipher));

		FreePtr(pTmpRight);
		return nRet;
	}

	// 根据版本号解密随机数
	char* pRandomNum = NULL;
	int nRandomNumLen = 0;
	char pRandomVersion[5] = { 0 };
	memcpy(pRandomVersion, pRandomNumCipher, VER_LEN);	
	if(!strcmp(pRandomVersion,"0001"))
	{
		nRet = DecryptByVersion(pRandomNumCipher+VER_LEN, strlen(pRandomNumCipher)-VER_LEN, &pRandomNum, &nRandomNumLen, pRandomVersion);
		if(ERR_SUCCESS != nRet)
		{
			CLOG log;
			log.Write("AnalyticRightFileBuf DecryptByVersion decrypt random error! nRet=%d", nRet);
			log.Write("File %s, Line %d", __FILE__, __LINE__);
			log.Write("pRandomNumCipher：%s Len: %d", pRandomNumCipher, strlen(pRandomNumCipher));

			FreePtr(pTmpRight);
			FreePtr(pDevID);
			return nRet;	
		}		

	}
	else
	{
		nRet = StringDecryptQomolangma(pRandomNumCipher, strlen(pRandomNumCipher), &pRandomNum, &nRandomNumLen);
		if(ERR_SUCCESS != nRet)
		{
			CLOG log;
			log.Write("AnalyticRightFileBuf DecryptByVersion decrypt random error! nRet=%d", nRet);
			log.Write("File %s, Line %d", __FILE__, __LINE__);
			log.Write("pRandomNumCipher：%s Len: %d", pRandomNumCipher, strlen(pRandomNumCipher));

			FreePtr(pTmpRight);
			FreePtr(pDevID);
			return nRet;	
		}
		
	}

	// 解密内容密钥
	nRet = DecryptCK(pTmpRight, nTmpRightLen, pDevID, nDevID, pRandomNum, nRandomNumLen, ppRight, nRightLen);
	if(ERR_SUCCESS != nRet)
	{
		FreePtr(pTmpRight);
		FreePtr(pDevID);
		FreePtr(pRandomNum);
		return nRet;
	}

	FreePtr(pTmpRight);
	FreePtr(pDevID);
	FreePtr(pRandomNum);

	return ERR_SUCCESS;

#ifdef ANTI_DEBUG
#ifdef WIN32

__debugger_found:

#endif
#endif

	return ERR_OTHER;
}

int GetContentKeyBuf(const char* pRightFileBuf, int nRightFileBuflen, char* pDevIDHashCipher, char* pRandomCipher, char** ppKey, int* nKeyLen)
{
	if((NULL == pRightFileBuf ) || (nRightFileBuflen <= 0) || (NULL == pRandomCipher)
		|| (NULL == ppKey) || (NULL == nKeyLen))
	{
		CLOG log;
		log.Write("GetContentKeyBuf parameter error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		if(NULL == pRightFileBuf)
			log.Write("pRightFileBuf is NULL!");
		else
			log.Write("pRightFileBuf:%s, nRightFileBuflen:%d", pRightFileBuf, nRightFileBuflen);

		if(NULL != pDevIDHashCipher)
			log.Write("pDevIDHashCipher:%s", pDevIDHashCipher);

		if(NULL == pRandomCipher)
			log.Write("pRandomCipher is NULL!");
		else
			log.Write("pRandomCipher:%s", pRandomCipher);

		return ERR_PARAMETER_INVALID;
	}

	// Windows PC获取设备信息
#ifdef WIN32

	if(NULL != pDevIDHashCipher)
		return ERR_PARAMETER_INVALID;

	char *pDevID= NULL;
	int nDeviDLen = 0;
	if(GenerateDeviceID(&pDevID, &nDeviDLen))
		return ERR_OTHER;

	pDevIDHashCipher = pDevID;

#endif

	char* pRight = NULL;
	int nRightLen = 0;
	int nRet = AnalyticRightFileBuf(pRightFileBuf, nRightFileBuflen, pDevIDHashCipher, pRandomCipher, &pRight, &nRightLen);
	if(ERR_SUCCESS != nRet)
	{
#ifdef WIN32
		FreePtr(pDevID);
#endif
		return nRet;
	}

#ifdef WIN32
	FreePtr(pDevID);
#endif

#ifdef ANTI_DEBUG
#ifdef WIN32  
	__asm
	{
		rdtsc
			mov ecx, eax
			mov ebx, edx
	}
#endif
#endif

	// 获取EbookID
	char* pEBookID = memstr(pRight, nRightLen, TAG_EBOOK_ID, LENGTH_TAG);
	char pEBookIDLen[LENGTH_LEN+1] = {0};
	memcpy(pEBookIDLen, pEBookID+LENGTH_TAG, LENGTH_LEN);
	int nEBookIDLen = atoi(pEBookIDLen);

	char *pEBID = new char[nEBookIDLen + 1];
	if(NULL == pEBID)
	{
		FreePtr(pRight);
		return ERR_MEMORY_ALLOCATION;
	}
	memset(pEBID, 0, nEBookIDLen+1);
	memcpy(pEBID, pEBookID+LENGTH_TAG+LENGTH_LEN, nEBookIDLen);

	// 获取内容密钥
	char* pDest = memstr(pRight, nRightLen, TAG_CONTENT_KEY, LENGTH_TAG);
	char keyLenBuf[LENGTH_LEN+1] = {0};
	memcpy(keyLenBuf, pDest+LENGTH_TAG, LENGTH_LEN);

	*nKeyLen = atoi(keyLenBuf);

#ifdef ANTI_DEBUG
#ifdef WIN32
	__asm
	{
		rdtsc
			cmp edx, ebx
			ja __debugger_found
			sub eax, ecx
			cmp eax, 0x9FFFFFFF
			ja __debugger_found
	}
#endif
#endif

	*ppKey = new char[*nKeyLen+1];
	if(NULL == *ppKey)
	{
		FreePtr(pRight);
		delete []pEBID;
		pEBID = NULL;

		return ERR_MEMORY_ALLOCATION;
	}
	memset(*ppKey, 0, *nKeyLen+1);
	memcpy(*ppKey, pDest+LENGTH_TAG+LENGTH_LEN, *nKeyLen);

	FreePtr(pRight);
	delete []pEBID;
	pEBID = NULL;

	return ERR_SUCCESS;

#ifdef ANTI_DEBUG
#ifdef WIN32

__debugger_found:

#endif
#endif

	return ERR_OTHER;
}

#ifndef WIN32
int GenerateDeviceID(char* pUUID, int nUUIDLen, char** ppDeviceID, int* pDeviceIDLen)
{
	if( (NULL == pUUID ) || (nUUIDLen <= 0) || (NULL == ppDeviceID) || (NULL == pDeviceIDLen))
		return ERR_PARAMETER_INVALID;

	char *pHashBuf = NULL;
	int nHashLen = 0;
	Hash256(pUUID, nUUIDLen, &pHashBuf, &nHashLen);

	// 对硬件设备信息加密处理
	int nRet = EncryptByVersion(pHashBuf, nHashLen, ppDeviceID, pDeviceIDLen, RIGHT_VER); 
	if (ERR_SUCCESS != nRet)
		FreePtr(*ppDeviceID);	

	FreePtrAES(pHashBuf);
	return nRet;
}
#endif //WIN32

#ifdef WIN32
#pragma comment(lib,"Crypt32.lib")

int ProtectData(char* pOriginalData, char** ppProtectData, int* nProtectDataLen)
{
	if((NULL == pOriginalData) || (NULL == ppProtectData) || (NULL == nProtectDataLen))
		return ERR_PARAMETER_INVALID;

	DATA_BLOB DataIn;
	DATA_BLOB DataOut;

	DataIn.pbData = (BYTE *)pOriginalData;   
	DataIn.cbData = strlen(pOriginalData)+1;

	ZeroMemory(&DataOut,sizeof(DataOut));

	if(!CryptProtectData(
		&DataIn,
		NULL,
		// to be included with the
		// encrypted data. 
		NULL,                               // Optional entropy not used.
		NULL,                               // Reserved.
		NULL,                               // Pass NULL for the 
		// prompt structure.
		CRYPTPROTECT_LOCAL_MACHINE,
		&DataOut))
	{
		*ppProtectData = NULL;
		*nProtectDataLen = 0;
		return GetLastError();
	}

#if 0
	int nOutLen = DataOut.cbData/3 * 4;
	if(0 != DataOut.cbData%3)
		nOutLen += 4;
#else
	int nOutLen = BASE64_LENGTH(DataOut.cbData);
#endif
	*ppProtectData = new char[nOutLen+1];
	if(NULL == *ppProtectData)
	{
		LocalFree(DataOut.pbData);
		return ERR_MEMORY_ALLOCATION;
	}
	memset(*ppProtectData, 0, nOutLen+1);
	*nProtectDataLen = nOutLen;

	base64Encode((char*)DataOut.pbData, DataOut.cbData, *ppProtectData);

	LocalFree(DataOut.pbData);

	return ERR_SUCCESS;	
}

int UnprotectData(char* pProtectData, int nProtectDataLen, char** ppOriginalData)
{
	if((NULL == pProtectData) || (nProtectDataLen <= 0) || (NULL == ppOriginalData))
		return ERR_PARAMETER_INVALID;

	//int nInLen = nProtectDataLen*3 / 4;
	int nInLen = BASE64_DECODE_CAPACITY(nProtectDataLen);
	char* pInPut = new char[nInLen+1];
	if(NULL == pInPut)
	{
		return ERR_MEMORY_ALLOCATION;
	}
	memset(pInPut, 0, nInLen+1);

	nInLen = base64Decode(pProtectData, nProtectDataLen, pInPut);
	if(0 == nInLen)
	{
		CLOG log;
		log.Write("UnprotectData base64Decode error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pProtectData：%s Len: %d", pProtectData, nProtectDataLen);

		delete[] pInPut;
		pInPut = NULL;
		return ERR_OTHER;
	}

	DATA_BLOB DataIn;
	DATA_BLOB DataOut;
	LPWSTR pDescrOut =  NULL;

	DataIn.pbData = (BYTE *)pInPut;   
	DataIn.cbData = nInLen;

	ZeroMemory(&DataOut,sizeof(DataOut));

	if(!CryptUnprotectData(
		&DataIn,
		&pDescrOut,
		NULL,                 // Optional entropy
		NULL,                 // Reserved
		NULL,                 // Here, the optional 
		// prompt structure is not
		// used.
		0,
		&DataOut))
	{
		delete[] pInPut;
		pInPut = NULL;
		*ppOriginalData = NULL;
		return GetLastError();
	}

	delete[] pInPut;
	pInPut = NULL;

	*ppOriginalData = new char[DataOut.cbData+1];
	if(NULL == *ppOriginalData)
	{
		LocalFree(pDescrOut);
		LocalFree(DataOut.pbData);
		return ERR_MEMORY_ALLOCATION;
	}
	memcpy(*ppOriginalData, (char*)(DataOut.pbData), DataOut.cbData);	

	LocalFree(pDescrOut);
	LocalFree(DataOut.pbData);

	return ERR_SUCCESS;
}

#include "LoadExe.h"
int LoadMultiMedia(const char* pRightFileBuf, int nRightFileBuflen, char* pRandomCipher, char* pMultiMediaFileName)
{
	if((NULL == pRightFileBuf) || (nRightFileBuflen <= 0) || (NULL == pRandomCipher) ||(NULL == pMultiMediaFileName))
	{
		return ERR_PARAMETER_INVALID;
	}

	FILE* pOriFile;
	if( 0 != fopen_s(&pOriFile, pMultiMediaFileName, "r"))
	{
		CLOG log;
		log.Write("LoadMultiMedia fopen_s error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pMultiMediaFileName：%s", pMultiMediaFileName);

		return ERR_FILE_RW;
	}
	fclose(pOriFile);

	char* pKey;
	int nKeyLen;
	int nRet = GetContentKeyBuf(pRightFileBuf, nRightFileBuflen, NULL, pRandomCipher, &pKey, &nKeyLen);
	if(ERR_SUCCESS != nRet)
	{
		CLOG log;
		log.Write("LoadMultiMedia GetContentKeyBuf error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pRightFileBuf：%s Len: %d pRandomCipher: %s", pRightFileBuf, nRightFileBuflen, pRandomCipher);

		return nRet;
	}

	TCHAR szTmpFilePath[_MAX_PATH] = {0}; 
	GetTempPath(_MAX_PATH, szTmpFilePath);

	int nTextLen = WideCharToMultiByte( CP_ACP, 0, szTmpFilePath, -1, NULL, 0, NULL, NULL );
	char* pTempFilePath = new char[nTextLen + 1];
	memset((void*)pTempFilePath, 0, sizeof(char) * (nTextLen+1));
	WideCharToMultiByte( CP_ACP, 0, szTmpFilePath, -1, pTempFilePath, nTextLen, NULL, NULL );

	char szUnCompFile[_MAX_PATH] = {0};
	strcat(szUnCompFile, pTempFilePath);
	strcat(szUnCompFile, "mmt.uda"); 

	FILE* pOldFile;
	if( 0 == fopen_s( &pOldFile, szUnCompFile, "r" ) )
	{
		fclose(pOldFile);

		DeleteFileAnsi(szUnCompFile);
	}

	if(!DecryptAndDecompress(pKey, 32, pMultiMediaFileName, szUnCompFile))
	{
		CLOG log;
		log.Write("LoadMultiMedia DecryptAndDecompress error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pMultiMediaFileName：%s szCompFile: %s", pMultiMediaFileName, szUnCompFile);

		FreePtr(pKey);
		delete[] pTempFilePath;
		pTempFilePath = NULL;
		return ERR_ENCRYPT_DECRYPT;
	}

	FreePtr(pKey);
	delete[] pTempFilePath;
	pTempFilePath = NULL;

	DeleteFileAnsi(pMultiMediaFileName);

	return ExeRunInMem(szUnCompFile);

#if 0
	//以下代码是另外一种处理方式，实现嵌入进程运行，暂时未调通
	FILE *fp = fopen(pMultiMediaFileName, "rb");
	if(fp)
	{
		MZHeader mzH;
		PE_Header peH;
		PE_ExtHeader peXH;
		SectionHeader *secHdr;

		if(ReadPEInfo(fp, &mzH, &peH, &peXH, &secHdr))
		{
			int imageSize = CalcTotalImageSize(&mzH, &peH, &peXH, secHdr);
			//printf("Image Size = %X\n", imageSize);

			LPVOID ptrLoc = VirtualAlloc(NULL, imageSize, MEM_COMMIT, PAGE_EXECUTE_READWRITE);
			if(ptrLoc)
			{
				//printf("Memory allocated at %X\n", ptrLoc);
				LoadPE(fp, &mzH, &peH, &peXH, secHdr, ptrLoc);                                                

				DoFork(&mzH, &peH, &peXH, secHdr, ptrLoc, imageSize);                                
			}
			else
			{
				printf("Allocation failed\n");
				return -2;
			}
		}
		else
		{
			fclose(fp);
			return -2;
		}
	}
	else
	{
		printf("\nCannot open the EXE file!\n");
		return -2;
	}

	return 0;
#endif// #if 0
}

#include "HardwareInfo.h"

int GenerateDeviceID(char** ppDeviceID, int* pDeviceIDLen)
{
	if((NULL == ppDeviceID) || (NULL == pDeviceIDLen))
		return ERR_PARAMETER_INVALID;

	char *pDiskInfo = NULL;
	int nDiskInfoLen = 0;
	char *pFormateInfo  = NULL;
	int nFormateInfoLen = 0;
	char szDeviceInfoLen[5] = {0};  // 存储设备信息长度字符串

	if(GetDiskInfo(&pDiskInfo, &nDiskInfoLen))
		return ERR_OTHER;

	sprintf_s(szDeviceInfoLen, "%04d", nDiskInfoLen); // 整型格式化为字符串形式
	nFormateInfoLen = LENGTH_DEV_TAG + LENGTH_LEN + nDiskInfoLen;	// 格式化硬件设备信息的长度
	pFormateInfo = new char[nFormateInfoLen + 1];
	if(NULL == pFormateInfo)
	{
		FreePtr(pDiskInfo);
		return ERR_MEMORY_ALLOCATION;
	}

	memset(pFormateInfo, 0, nFormateInfoLen + 1);int nTempLen = 0;		
	memcpy(pFormateInfo + nTempLen, TAG_DEV_HAD, LENGTH_DEV_TAG); nTempLen += LENGTH_DEV_TAG;
	memcpy(pFormateInfo + nTempLen, szDeviceInfoLen, LENGTH_LEN); nTempLen += LENGTH_LEN;
	memcpy(pFormateInfo + nTempLen, pDiskInfo, nDiskInfoLen);

	// 对格式化硬件设备信息Hash处理
	char *pHashBuf = NULL;
	int nHashLen = 0;
	Hash256(pFormateInfo, nFormateInfoLen, &pHashBuf, &nHashLen);

	// 对硬件设备Hash值加密处理
	int nRet = EncryptByVersion(pHashBuf, nHashLen, ppDeviceID, pDeviceIDLen, RIGHT_VER); 
	if (ERR_SUCCESS != nRet)
		FreePtr(*ppDeviceID);	

	FreePtr(pDiskInfo);	
	FreePtr(pFormateInfo);
	FreePtrAES(pHashBuf);

	return nRet;
}

int DESEncrypt(unsigned char* pKey, int nKeyLen, unsigned char* pInBuf, int nInBufLen, unsigned char** ppOutBuf, int* nOutBufLen, 
			   const int nEncryptMode, bool bPadding)
{
	if((NULL == pKey) || (nKeyLen <= 0) || (NULL == pInBuf) || (nInBufLen <= 0) || (NULL == ppOutBuf) || (NULL == nOutBufLen))
		return ERR_PARAMETER_INVALID;
	
	// 设置密钥
	des_context ctx;
	des_setkey_enc( &ctx, pKey );	

	// 对加密输出分配内存
	int numBlocks = nInBufLen/8;
	int nOutLen = bPadding ? 8 * (numBlocks + 1) : 8 * numBlocks;
	unsigned char *pOutPut = new unsigned char[nOutLen + 1];
	if(NULL == pOutPut)	
		return ERR_MEMORY_ALLOCATION;
	memset(pOutPut, 0, nOutLen+1);

	unsigned char block[8] = { 0 };
	unsigned char *pTemp = pOutPut;
	int padLen = 0;

	switch (nEncryptMode) 
	{
	case 0:
		for (int i = numBlocks; i > 0; i--) 
		{
			des_crypt_ecb( &ctx, pInBuf, pTemp );
			pInBuf += 8;
			pTemp += 8;
		}

		if (bPadding)
		{
			padLen = 8 - (nInBufLen - 8*numBlocks);
			// assert(padLen > 0 && padLen <= 8);
			memcpy(block, pInBuf, 8 - padLen);
			memset(block + 8 - padLen, padLen, padLen);
			des_crypt_ecb( &ctx, block, pTemp );			
		}

		// Base64Encode输出
		*nOutBufLen = nOutLen/3*4;
		if(0 != nOutLen%3)
			*nOutBufLen = *nOutBufLen+4;
		*ppOutBuf = new unsigned char[*nOutBufLen+1];
		if(NULL == *ppOutBuf)
		{
			delete []pOutPut;
			pOutPut = NULL;
			return ERR_MEMORY_ALLOCATION;
		}
		memset(*ppOutBuf, 0, *nOutBufLen+1);
		base64Encode((char*)pOutPut, nOutLen, (char*)*ppOutBuf);

		break;

	case 1:
		/*unsigned char iv[8] = {0};
		memcpy(iv, "00000000", 8);
		nRet = des_crypt_cbc( &ctx, DES_ENCRYPT, nInBufLen, iv, pInBuf, *ppOutBuf );
		if(0 != nRet)
		{
			delete []*ppOutBuf;
			*ppOutBuf = NULL;
			return ERR_ENCRYPT_DECRYPT;
		}*/
		break;
	default:
		return ERR_OTHER;
	}

	delete []pOutPut;
	pOutPut = NULL;

	return ERR_SUCCESS;

}

int DESDecrypt(unsigned char* pKey, int nKeyLen, unsigned char* pInBuf, int nInBufLen, unsigned char** ppOutBuf, int* nOutBufLen, 
			   const int nEncryptMode, bool bPadding)
{
	if((NULL == pKey) || (nKeyLen <= 0) || (NULL == pInBuf) || (nInBufLen <= 0) || (NULL == ppOutBuf) || (NULL == nOutBufLen))
		return ERR_PARAMETER_INVALID;

	// 对输入Base64Decode
	int nInBufBase64DecodeLen= BASE64_DECODE_CAPACITY(nInBufLen);
	char* pInBufBase64Decode = new char[nInBufBase64DecodeLen + 1];
	if(NULL == pInBufBase64Decode)
		return ERR_MEMORY_ALLOCATION;	
	memset(pInBufBase64Decode, 0, nInBufBase64DecodeLen+1);

	nInBufBase64DecodeLen = base64Decode((char*)pInBuf, nInBufLen, pInBufBase64Decode);
	if(0 == nInBufBase64DecodeLen)
	{
		CLOG log;
		log.Write("DESDecrypt base64Decode error!");
		log.Write("File %s, Line %d", __FILE__, __LINE__);
		log.Write("pInBuf：%s, nInBufLen: %d", pInBuf, nInBufLen);

		delete[] pInBufBase64Decode;
		pInBufBase64Decode = NULL;
		return ERR_OTHER;
	}	

	// 设置密钥
	des_context ctx;
	des_setkey_dec( &ctx, pKey );	

	// 对解密输出分配内存
	int nOutPutLen = nInBufBase64DecodeLen + 8;
	unsigned char *pOutPut = new unsigned char[nOutPutLen + 1];
	if(NULL == pOutPut)
	{
		delete[] pInBufBase64Decode;
		pInBufBase64Decode = NULL;
		return ERR_MEMORY_ALLOCATION;
	}
	memset(pOutPut, 0, nOutPutLen + 1);

	unsigned char block[8] = { 0 };
	unsigned char *pInPut = (unsigned char*)pInBufBase64Decode;
	unsigned char *pTemp = pOutPut;	
	int numBlocks = nInBufBase64DecodeLen/8;
	int padLen = 0;

	switch (nEncryptMode) 
	{
	case 0:
		for (int i = numBlocks - 1; i > 0; --i)
		{
			des_crypt_ecb( &ctx, (unsigned char*)pInPut, pTemp);
			pInPut += 8;
			pTemp += 8;
		}
	
		des_crypt_ecb( &ctx, (unsigned char*)pInPut, block );
	
		if (bPadding)
		{
			padLen = block[7];
			if (padLen > 8)
			{
				CLOG log;
				log.Write("DESDecrypt padLen error!");
				log.Write("File %s, Line %d", __FILE__, __LINE__);
				log.Write("padLen：%d", padLen);

				delete[] pInBufBase64Decode;
				pInBufBase64Decode = NULL;

				delete []pOutPut;
				pOutPut = NULL;
				return ERR_ENCRYPT_DECRYPT;
			}

			for (int i = 8 - padLen; i < 8; ++i)
				if (block[i] != padLen)
				{
					CLOG log;
					log.Write("DESDecrypt block padLen error!");
					log.Write("File %s, Line %d", __FILE__, __LINE__);
					log.Write("block[%d]:%d, padLen：%d", i, block[i], padLen);

					delete []pInBufBase64Decode;
					pInBufBase64Decode = NULL;

					delete []pOutPut;
					pOutPut = NULL;
					return ERR_ENCRYPT_DECRYPT;
				}

				memcpy(pTemp, block, 8 - padLen);	
		}
		else
		{
			memcpy(pTemp, block, 8);
		}

		*nOutBufLen = 8 * numBlocks - padLen;
		*ppOutBuf = new unsigned char[*nOutBufLen + 1];
		if(NULL == *ppOutBuf)
		{
			delete []pInBufBase64Decode;
			pInBufBase64Decode = NULL;

			delete []pOutPut;
			pOutPut = NULL;
			return ERR_MEMORY_ALLOCATION;

		}
		memset(*ppOutBuf, 0, *nOutBufLen + 1);
		memcpy(*ppOutBuf, pOutPut, *nOutBufLen);

		break;

	case 1:
		/*unsigned char iv[8] = {0};
		memcpy(iv, "00000000", 8);
		nRet = des_crypt_cbc( &ctx, DES_ENCRYPT, nInBufLen, iv, pInBuf, *ppOutBuf );
		if(0 != nRet)
		{
			delete []*ppOutBuf;
			*ppOutBuf = NULL;
			return ERR_ENCRYPT_DECRYPT;
		}*/
		break;
	default:
		return ERR_OTHER;
	}

	delete[] pInBufBase64Decode;
	pInBufBase64Decode = NULL;

	delete []pOutPut;
	pOutPut = NULL;

	return ERR_SUCCESS;
}
#endif // WIN32
#endif // _CLIENT





