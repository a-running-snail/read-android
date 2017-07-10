#include "Utility.h"

static char* string_32 = "n5Pr6St7Uv8Wx9YzAb0Cd1Ef2Gh3Jk4M";
static char* string_64 = "AaZzB0bYyCc1XxDdW2wEeVv3FfUuG4g-TtHh5SsIiR6rJjQq7KkPpL8lOoMm9Nn_";

static int BLOCK_LEN = 8;


void JY_Crypt( JY_STATE *s, unsigned char *pKey, unsigned char *pData, int nDataLen )
{ 
	int i, j, k, *m, a;

	s->x = 0;
	s->y = 0;
	m = s->m;

	for( i = 0; i < JY_LENGTH; i++ )
	{
		m[i] = i;
	}

	j = k = 0;

	for( i = 0; i < JY_LENGTH; i++ )
	{
		a = m[i];
		j = (unsigned char)( j + a + pKey[k] );
		m[i] = m[j]; 
		m[j] = a;

		if( ++k >= JY_KEY_LEN ) 
			k = 0;
	}

	int  x, y, *pm, vp, vq;

	x = s->x;
	y = s->y;
	pm = s->m;

	for( i = 0; i < nDataLen; i++ )
	{
		x = (unsigned char)( x + 1 ); 
		vp = pm[x];
		y = (unsigned char)( y + vp );

		pm[x] = vq = pm[y];
		pm[y] = vp;

		pData[i] ^= pm[(unsigned char) ( vp + vq )];
	}

	s->x = x;
	s->y = y;
}

char* BillEncode32( char* pInBuf, int len )
{
	return BillEncode(pInBuf, len, 32);
}

char* BillEncode64 (char* pInBuf, int len ) 
{
	return BillEncode(pInBuf, len, 64);
}

char* BillEncode( char* pInBuf, int len, char choice ) 
{
	if( NULL == pInBuf )
		return NULL;

	char *enc, *pOutBuf;

	if (choice == 0x20) 
		enc = string_32;
	else if (choice == 0x40) 
		enc = string_64;
	else
		return NULL;

	pOutBuf = new char[len*2+1];
	memset(pOutBuf, 0, len*2+1);

	for (int i=0; i<len; i++) 
	{
		unsigned char v = pInBuf[i] + 128;
		unsigned char q = v / choice;
		unsigned char m = v % choice;

		pOutBuf[i*2] = enc[q];
		pOutBuf[i*2 + 1] = enc[m];
	}

	return pOutBuf;
}

char* BillDecode( char* pInBuf, int pInBufLen, int* outlen ) 
{
	if( NULL == pInBuf )
		return NULL;
	
	int nLen = pInBufLen;

	char *dec = NULL;
	int i, choice = 0x20;
	*outlen = 0;

	for (i=0; i<8 && i<nLen; i++) 
	{
		if (*pInBuf == string_32[i])
		{
			dec = string_32;
			break;
		}
	}

	if (dec == NULL) 
	{
		for (i=0; i<4 && i<nLen; i++) 
		{
			if (*pInBuf == string_64[i]) 
			{
				dec = string_64;
				choice = 0x40;
				break;
			}
		}
	}

	if ( NULL == dec ) 
	{
		return NULL;
	}

	char *pOutBuf = new char[nLen/2 + 1];
	memset(pOutBuf, 0, nLen/2 + 1);

	for (i=0; i<nLen; i+=2) 
	{
		int q, m, v;
		char *p = strchr(dec, pInBuf[i]);
		if ( NULL == p )
			break;

		q = p - dec;
		p = strchr(dec, pInBuf[i + 1]);
		if ( NULL == p )
			break;

		m = p - dec;
		v = (choice * q + m) - 128;
		pOutBuf[(*outlen)++] = (unsigned char)v;
	}
	return pOutBuf;
}

int ExchangeChar( char* pInBuf, char* pOutBuf, int nLength)
{
	if(( NULL == pInBuf) || (NULL == pOutBuf) || (0 == nLength))
		return -1;
	
	char* pBlockBuf = new char[BLOCK_LEN+1];
	if( NULL == pBlockBuf )
		return -1;
	
	memset( pBlockBuf, 0, BLOCK_LEN+1 );
	
	int nBlock = nLength / BLOCK_LEN;

	for(int i=0; i<nBlock; i++)
	{
		int nIndex = 0;
		for(int j=1; j<=BLOCK_LEN; j++)
		{
			pBlockBuf[nIndex++] = pInBuf[(i+1)*BLOCK_LEN - j];
		}
		memcpy( pOutBuf + i*BLOCK_LEN, pBlockBuf, BLOCK_LEN );
	}
	
	memcpy( pOutBuf + BLOCK_LEN*nBlock, pInBuf + BLOCK_LEN*nBlock, nLength%BLOCK_LEN );
	
	delete[] pBlockBuf;
	pBlockBuf = NULL;

	return 0;
}

long FileLength(FILE* fp)
{ 
	fseek(fp, 0, SEEK_END);
	long len = ftell(fp);
	fseek(fp, 0, SEEK_SET);

	return len;
}


#include <iostream>
#include <string>
char base64EncodeChars[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
char base64DecodeChars[] =
{
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
	52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, 0, -1, -1,
	-1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
	15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
	-1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
	41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1
};


void base64Encode(char* pIn, int nInLen, char* pOut)
{
	int i=0,index=0;
	unsigned char c1,c2,c3;

	while(i < nInLen)
	{
		c1=*(pIn+i);
		i++;
		if(i == nInLen)
		{
			pOut[index++] = base64EncodeChars[(c1 & 0XFF) >> 2];
			pOut[index++] = base64EncodeChars[(c1 & 0X03) << 4];
			pOut[index++] = '=';
			pOut[index++] = '=';
			break;
		}

		c2=*(pIn+i);
		i++;
		if(i == nInLen)
		{
			pOut[index++] = base64EncodeChars[(c1 & 0XFF) >> 2];
			pOut[index++] = base64EncodeChars[((c1 & 0X3) << 4) | ((c2 & 0XF0) >> 4)];
			pOut[index++] = base64EncodeChars[(c2 & 0X0F) << 2];
			pOut[index++] = '=';
			break;
		}

		c3=*(pIn+i);
		i++;
		pOut[index++] = base64EncodeChars[(c1 & 0XFF) >> 2];
		pOut[index++] = base64EncodeChars[((c1 & 0X03) << 4) | ((c2 & 0XF0) >> 4)];
		pOut[index++] = base64EncodeChars[((c2 & 0X0F) << 2) | ((c3 & 0XC0) >>6)];
		pOut[index++] = base64EncodeChars[c3 & 0X3F];
	}

	pOut[index] = 0;
}

int base64Decode(char* pIn, int nInLen, char* pOut)
{
	int i=0,index=0;
	unsigned char c1,c2,c3,c4;

	if(nInLen < 4)
		return 0;

	for( int j=0; j<nInLen; j++)
	{
		if(pIn[j]<0 || (unsigned char)pIn[j] >= 128 || base64DecodeChars[pIn[j] & 0XFF] == -1)
		{	
			return 0;
		}
	}

	while( i < nInLen )
	{
		c1 = base64DecodeChars[pIn[i++] & 0XFF];
		c2 = base64DecodeChars[pIn[i++] & 0XFF];
		pOut[index++] = (c1 << 2) | ((c2 & 0X30) >> 4);

		c3 = pIn[i++];
		if (c3 == '=')
			break;
		c3 = base64DecodeChars[c3];
		pOut[index++] = ((c2 & 0XF) << 4) | ((c3 & 0X3C) >> 2);

		c4 = pIn[i++];
		if (c4 == '=')
			break;
		c4 = base64DecodeChars[c4];
		pOut[index++] = ((c3 & 0X03) << 6) | c4;
	}

	pOut[index] = 0;

	return index;
}

char* memstr(char* pLongString, int nLongStringLen, char* pSubString, int nSubStringLen)
{
	for(int i=0; i<nLongStringLen-nSubStringLen; i++)
	{
		if(!memcmp(pLongString+i, pSubString, nSubStringLen))
		{
			return pLongString+i;
		}
	}

	return NULL;
}

#if WIN32
void DeleteFileAnsi(const char* pFile)
{
	int unicodeLen = MultiByteToWideChar( CP_ACP, 0, pFile, -1 ,NULL, 0 );  
	wchar_t*  pUnicode;  
	pUnicode = new wchar_t[unicodeLen+1];  
	memset(pUnicode, 0, (unicodeLen+1) * sizeof(wchar_t));  
	MultiByteToWideChar( CP_ACP, 0, pFile, -1, (LPWSTR)pUnicode, unicodeLen );  

	DeleteFile(pUnicode);

	delete[] pUnicode;
}

int Initinflate(z_streamp pstream)
{	
	int err = -1;

	pstream->zalloc = (alloc_func)0;
	pstream->zfree = (free_func)0;

	err = inflateInit(pstream);

	return err;
}

int Updateinflate(z_streamp pstream, Bytef *dest, uLongf *destLen, const Bytef *source, uLong sourceLen)
{
	int err = -1;
	pstream->next_in = (Bytef*)source;
	pstream->avail_in = (uInt)sourceLen;
	/* Check for source > 64K on 16-bit machine: */

	pstream->next_out = dest;
	pstream->avail_out = (uInt)*destLen;

	err = inflate(pstream, Z_NO_FLUSH);
	*destLen = *destLen - pstream->avail_out;

	return err;
}

int Endinflate(z_streamp pstream)
{
	int err = -1;	
	err = inflateEnd(pstream);

	return err;
}

#define nVirtualBlockSize (64 * 1024)
bool AES_CBC_IV0_DECRYPT_DECOMPRESS(octet *k,FILE *ifp,FILE *ofp)
{
	aes a;
	int i = 0;
	int err = -1;
	bool bRet = true;

	//init ctrl struct
	if (!aes_init(&a,MR_CBC,k->len,k->val,NULL)) 
		return false;

	z_stream stream;
	err = Initinflate(&stream);
	if(err)
		return false;

	if (ifp)
	{
		int nLastLen = 0;
		int nLastBlockLen = 0;
		long lInputFileLen = 0;
		char *ptr = 0;
		int nVirtualBlocks = 0;
		long lDeCompLen = 8*nVirtualBlockSize;
		char *ibuf = (char*)malloc(nVirtualBlockSize);
		char *obuf = (char*)malloc(nVirtualBlockSize);
		char *dbuf = (char*)malloc(8*nVirtualBlockSize);

		fseek(ifp, 0, SEEK_END);
		lInputFileLen = ftell(ifp);
		nVirtualBlocks = lInputFileLen / nVirtualBlockSize;
		fseek(ifp, 0, SEEK_SET);
		if (ofp)
			fseek(ofp, 0, SEEK_SET);

		nLastBlockLen = lInputFileLen % nVirtualBlockSize;
		if (!nLastBlockLen)
		{
			--nVirtualBlocks;
			nLastBlockLen = nVirtualBlockSize;
		}

		for (i = nVirtualBlocks; i > 0; --i)
		{
			fread(ibuf, nVirtualBlockSize, 1, ifp);
			
			aes_decrypt(&a, ibuf, nVirtualBlockSize, obuf, FALSE);
			err = Updateinflate(&stream, (Bytef *)dbuf, (uLongf *)&lDeCompLen, (const Bytef *)obuf, (uLong)nVirtualBlockSize);
			if(err)
			{
				free(obuf);
				free(ibuf);
				free(dbuf);
				return false;
			}

			if(ofp)
				fwrite(dbuf, lDeCompLen, 1, ofp);

			memset(dbuf, 0, 8*nVirtualBlockSize);
			lDeCompLen = 8*nVirtualBlockSize;

		}

		fread(ibuf, nLastBlockLen, 1, ifp);
		
		nLastLen = aes_decrypt(&a, ibuf, nLastBlockLen, obuf, TRUE);
		if(nLastLen < 0)
		{
			free(obuf);
			free(ibuf);
			free(dbuf);
			return false;
		}

		err = Updateinflate(&stream,(Bytef *)dbuf, (uLongf *)&lDeCompLen, (const Bytef *)obuf, (uLong)nLastLen);
		if (err != Z_STREAM_END) 
		{
			free(obuf);
			free(ibuf);
			free(dbuf);

			return false;			
		}

		if (ofp)
			fwrite(dbuf, lDeCompLen, 1, ofp);

		err = Endinflate(&stream);
		if(err)
			bRet = false;		
			
		free(obuf);
		free(ibuf);
		free(dbuf);
	}

	return bRet;
}

bool file_decrypt_decompress(char* szKey, int nKeyLen, char* szInput, char* szOutput)
{
	FILE *pInput;
	FILE *pOutput;
	bool bRet = false;

	octet KM, K;
	if( (NULL == szKey ) || (nKeyLen <= 0) || (NULL == szInput) || (NULL == szOutput))
		return false;

	OCTET_INIT(&KM, nKeyLen);
	OCTET_INIT(&K, 64);

	memcpy(KM.val, szKey, nKeyLen);
	KM.len = nKeyLen;

	HASH(SHA256, &KM, &K);
	K.len = 32;

	pInput = fopen(szInput, "rb");
	if (!pInput)
	{
		OCTET_KILL(&KM);
		OCTET_KILL(&K);
		return false;
	}

	pOutput = fopen(szOutput, "w+b");
	if (!pOutput)
	{
		OCTET_KILL(&KM);
		OCTET_KILL(&K);
		fclose(pInput);
		return false;
	}

	bRet = AES_CBC_IV0_DECRYPT_DECOMPRESS(&K, pInput, pOutput);

	OCTET_KILL(&KM);
	OCTET_KILL(&K);

	fclose(pInput);
	fclose(pOutput);

	return bRet;
}

bool DecryptAndDecompress(char* szKey, int nKeyLen, char* szInFileName, char* szOutFileName)
{
	return file_decrypt_decompress(szKey, nKeyLen, szInFileName, szOutFileName);
}

#endif //WIN32
