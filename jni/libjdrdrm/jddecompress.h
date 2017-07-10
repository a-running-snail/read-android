#ifndef __JDZIP_H__
#define __JDZIP_H__


#include "zlib.h"
//#include "./jdepubsdk/jebkitapi/JDCipherUtil.h"



class jddecompress 
{
public:
	jddecompress(const  unsigned char *pUseKey,int iKeyLen);

    ~jddecompress();
	int decompressBuffer(unsigned char *dest, int *destLen, unsigned char* source, int sourceLen, bool isEnd);
	z_stream *myZStream;
	char* myLastNextIn;
	int myDecomSize;

    Cipher pdecrypt;


    int decryptBuffer(bool final, unsigned char* indata, int inlen, unsigned char* outdata, int* outlen);

    int  decrypt ( unsigned char* buffer, unsigned int inbufferlen, unsigned char *dest, unsigned int destLen,bool bFinal );
};

#endif
