#include "DRMLib.h"
#include "jddecompress.h"
#include "string"
#include "__b.h"

jddecompress::jddecompress(const unsigned char *pUseKey,int iKeyLen) :myZStream(0), myLastNextIn(0),myDecomSize(0)
{

	if (CreateCipher(&pdecrypt))
	{
		pdecrypt = 0;
	}
	InitCipher(pdecrypt, DECRYPT, pUseKey, iKeyLen); 


    myZStream = new z_stream;
	memset(myZStream, 0, sizeof(z_stream));
	myZStream->zalloc = (alloc_func)0;
	myZStream->zfree = (free_func)0;
	inflateInit(myZStream);
}

jddecompress::~jddecompress() 
{	
    if (myZStream) 
    {
		inflateEnd(myZStream);
		delete myZStream;
		myZStream = 0;
	}
	if (myLastNextIn)
	{
		delete []myLastNextIn;
		myLastNextIn = NULL;
	}

	myDecomSize = 0;
    if(pdecrypt)
    {
	    DestroyCipher(pdecrypt);
        pdecrypt=NULL;
    }


}
int jddecompress::decompressBuffer(unsigned char *dest, int  *destLen, unsigned char* source, int sourceLen, bool isEnd) 
{
    
    int err;
	int outoffset = 0;
	if (myZStream->avail_in>0)
	{
		myZStream->next_in = (Bytef*)myLastNextIn;
		myZStream->next_out = dest;
		myZStream->avail_out = (uInt)*destLen;
		err = inflate(myZStream, Z_SYNC_FLUSH);
		if (err != Z_STREAM_END)
		{
			outoffset = myZStream->total_out-myDecomSize;
			if (err == Z_NEED_DICT || (err == Z_BUF_ERROR && myZStream->avail_in == 0))
				return Z_DATA_ERROR;
			if (outoffset>=*destLen)
			{
				if (myZStream->avail_in > 0)
				{
					if (myLastNextIn)
					{
						delete []myLastNextIn;
					}
					myLastNextIn = new char[myZStream->avail_in];
					memcpy(myLastNextIn, myZStream->next_in, myZStream->avail_in);
				}
				*destLen = outoffset;
				return err;
			}
		}
	}
	if ((source == NULL) && (sourceLen == 0) && (err == Z_STREAM_END))
	{
		*destLen = myZStream->total_out - myDecomSize;
		return err;
	}
	myZStream->next_in = (Bytef*)source;
	myZStream->avail_in = (uInt)sourceLen;
	/* Check for source > 64K on 16-bit machine: */
	if ((uLong)myZStream->avail_in != sourceLen) return Z_BUF_ERROR;

	myZStream->next_out = dest+outoffset;
	myZStream->avail_out = (uInt)*destLen -outoffset;
	if ((uLong)myZStream->avail_out != (*destLen -outoffset)) return Z_BUF_ERROR;

	err = inflate(myZStream, isEnd ? Z_FINISH : Z_SYNC_FLUSH);
	if (err != Z_STREAM_END) 
    {
		*destLen = myZStream->total_out - myDecomSize;
		if (err == Z_NEED_DICT || (err == Z_BUF_ERROR && myZStream->avail_in == 0))
			return Z_DATA_ERROR;
		if (myZStream->avail_in > 0) 
        {
			if (myLastNextIn)
			{
				delete []myLastNextIn;
			}
			myLastNextIn = new char[myZStream->avail_in];
			memcpy(myLastNextIn, myZStream->next_in, myZStream->avail_in);
		}
		return err;
	}
	*destLen = myZStream->total_out - myDecomSize;

	myDecomSize +=  *destLen;
    return err;
}

int jddecompress::decryptBuffer(bool final, unsigned char* indata, int inlen, unsigned char* outdata, int* outlen) 
{

    if (pdecrypt == 0)
		return -1;

	if (final == 1) 
    {
       if (FinalCipher(pdecrypt, indata, inlen, outdata, outlen))
			return -1;
	} 
    else 
    {
		if (UpdateCipher(pdecrypt, indata, inlen, outdata, outlen))
			return -1;
	}
	return 0;
}

int jddecompress::decrypt (unsigned char* buffer, unsigned int inbufferlen, unsigned char *dest, unsigned int  outbufferlen ,bool bFinal )
{
	unsigned char *inbuffer = new unsigned char[inbufferlen + 100];	
	unsigned char *outbuffer = new unsigned char[inbufferlen*2];
    int olen=0;
		
	memset(inbuffer, 0, inbufferlen + 100);
	memset(outbuffer, 0, inbufferlen*2);
	memcpy(inbuffer, buffer, inbufferlen);
		
	if (decryptBuffer(bFinal, inbuffer, inbufferlen, outbuffer, (int *)&olen))
	{
			delete[] inbuffer;
			delete[] outbuffer;
			return -1;
	}
    

	//LOGI("******decryptBuffer outbufferlen = %d \n", outbufferlen);
    
    int realSize=outbufferlen;
    memset(dest,0,outbufferlen);    
    decompressBuffer(dest, &realSize, outbuffer, olen, bFinal);
	
	delete[] inbuffer;
	delete[] outbuffer;
	return  realSize;
}

jddecompress *jdzip=NULL;

extern "C" void jdCreate(const unsigned char *pUseKey,int len)
{
    jdzip=new jddecompress(pUseKey,len);

}
extern "C" void jdExit()
{
    delete jdzip;
    jdzip=NULL;

}


extern "C"  int decrypt(unsigned char * encryptedData, int encryptedLength, unsigned char *decryptedData,int decryptedLen,int end)
{
    return jdzip->decrypt(encryptedData,encryptedLength,decryptedData,decryptedLen,end);
}
