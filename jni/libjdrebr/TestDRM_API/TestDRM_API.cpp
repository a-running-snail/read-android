// TestDRM_API.cpp : DRM_API接口测试程序
#include "DRMLib.h"
#include <string.h>
#include <stdio.h>
#include <string.h>
#ifdef WIN32
#include "..\public\include\zlib.h"
#else
#include "../public/include/zlib.h"
#endif

#define MMTEST

void TestCipher()
{
	printf("**************************Cipher Test****************************\n");
	const int INBUFFER = 10 * 1024;        // 每次UpdateCipher输入buffe的大小
	char *key = NULL;                      // 加密密钥
	int keylen = 0;
	unsigned char *input = NULL;           // 加密输入
	int inputlen = 0;
	unsigned char *output = NULL;          // 加密输出
	int outputlen = 0;
	unsigned char *inbuffer = NULL;        // UpdateCipher输入buffer
	int inbufferlen = 0;
	unsigned char *outbuffer = NULL;       // UpdateCipher输出buffer
	int outbufferlen = 0;
	unsigned char *outputdecrypt = NULL;   // 解密输出
	int outputdecryptlen = 0;
	unsigned char *finalInbuffer = NULL;   // FinalCipher输入buffer
	int finalInbufferlen = 0;
	unsigned char *finalOutbuffer = NULL;  // FinalCipher输出buffer
	int finalOutbufferlen = 0;
	
	try
	{
		// 生成密钥		
		if(GenerateKeyAES(&key, &keylen))
		{
			printf("GenerateKeyAES failed!\n");
			throw -1;
		}	

		// 读取文件作为加密的输入
		FILE* pFile = fopen("test.dat", "rb");
		if(NULL == pFile)
		{
			printf("open file failed!\n");
			throw -1;
		}
		fseek(pFile, 0, SEEK_END);
		int nFileLen = ftell(pFile);
		fseek(pFile, 0, SEEK_SET);
		inputlen = nFileLen;
		input = new unsigned char[inputlen + 1];	
		fread(input, sizeof(char), nFileLen, pFile);
		fclose(pFile);
		
		// 加密在PC端，性能足够用，不需要分块加密，可以不调用UpdateCipher，Init后直接调用FinalCipher加密
		Cipher pencrypt = NULL;	
		if(CreateCipher(&pencrypt))
		{
			printf("cipher_create failed!\n");
			throw -1;
		}

		if(InitCipher(pencrypt, ENCRYPT, (unsigned char*)key, keylen))
		{
			printf("cipher_init failed!\n");
			throw -1;
		}	

		// output buffer比input大一个block的长度即可。目前算法的block长度为16字节。
		// output buffer不够大的话会返回错误码，再按照nOutputlen申请内存。
		outputlen = inputlen + 16;
		output = new unsigned char[outputlen + 1]; 
		if(FinalCipher(pencrypt, input, inputlen, output, &outputlen))
		{
			printf("cipher_final failed!\n");
			throw -1;
		}

		if(DestroyCipher(pencrypt))
		{
			printf("cipher_destroy failed!\n");
			throw -1;
		}

		// 写入文件
		FILE* pFilew = fopen("Encrptjni.pdf", "wb");
		if(NULL == pFilew)
		{
			printf("open file failed!\n");
			throw -1;
		}

		int numWrite = fwrite(output, sizeof(char), outputlen, pFilew);
		if(numWrite != outputlen)
		{
			throw -1;
		}
		fclose(pFilew);


		// 由于解密数据的客户端性能受限，解密可能需要采用分块方式，根据输入数据buffer的大小，
		// 多次调用UpdateCipher解密, 最后再调用FinalCipher解密	
		Cipher pdecrypt = NULL;
		if(CreateCipher(&pdecrypt))
		{
			printf("cipher_create failed!\n");
			throw -1;
		}

		if(InitCipher(pdecrypt, DECRYPT, (unsigned char *)key, keylen))
		{
			printf("cipher_init failed!\n");
			throw -1;
		}

		// UpdateCipher输入buffer的大小
		inbufferlen = INBUFFER;
		inbuffer = new unsigned char[inbufferlen + 1];
		// UpdateCipher输出buffer的大小，与输入buffer的大小一致
		outbufferlen = inbufferlen;
		outbuffer = new unsigned char[outbufferlen + 1];	
		int nLoop = outputlen/INBUFFER; // 调用UpdateCipher的次数
		// 解密密文后的输出
		outputdecrypt = new unsigned char[outputlen + 1];
		outputdecryptlen = 0;

		// 循环调用UpdateCipher
		for(int i=0; i<nLoop; i++)
		{
			memset(inbuffer, 0, inbufferlen + 1);
			memset(outbuffer,0, outbufferlen + 1);
			memcpy(inbuffer, output + i*inbufferlen, inbufferlen);
			if(UpdateCipher(pdecrypt, inbuffer, inbufferlen, outbuffer, &outbufferlen))
			{
				printf("cipher_update failed!\n");
				throw -1;
			}
			memcpy(outputdecrypt + outputdecryptlen, outbuffer, outbufferlen);		
			outputdecryptlen += outbufferlen;
		}
	
		// 最后解密的输入buffer
		finalInbufferlen = outputlen - nLoop*inbufferlen;
		finalInbuffer = new unsigned char[finalInbufferlen + 1];
		memset(finalInbuffer, 0 ,finalInbufferlen + 1);
		memcpy(finalInbuffer, output + nLoop*inbufferlen, finalInbufferlen);
		// 最后解密的输出buffer,output buffer比input buffer大一个block的长度即可,目前算法的block长度为16字节
		finalOutbufferlen = finalInbufferlen + 16;
		finalOutbuffer = new unsigned char[finalOutbufferlen + 1];
		memset(finalOutbuffer, 0, finalOutbufferlen + 1);

		if(FinalCipher(pdecrypt, finalInbuffer, finalInbufferlen, finalOutbuffer, &finalOutbufferlen))
		{
			printf("cipher_final failed!\n");
			throw -1;
		}
		memcpy(outputdecrypt + outputdecryptlen, finalOutbuffer, finalOutbufferlen);
		outputdecryptlen += finalOutbufferlen;

		if(DestroyCipher(pdecrypt))
		{
			printf("cipher_destroy failed!\n");
			throw -1;
		}

		if(memcmp(input, outputdecrypt, inputlen))
			printf("Cipher test failed!\n");
		else
			printf("Cipher test success!\n");		

		// 释放内存
		FreePtrAES(key);
		FreePtr(input);	
		FreePtr(output);
		FreePtr(inbuffer);
		FreePtr(outbuffer);
		FreePtr(outputdecrypt);
		FreePtr(finalInbuffer);
		FreePtr(finalOutbuffer);

	}

	catch (int)
	{
		FreePtrAES(key);
		FreePtr(input);	
		FreePtr(output);
		FreePtr(inbuffer);
		FreePtr(outbuffer);
		FreePtr(outputdecrypt);
		FreePtr(finalInbuffer);
		FreePtr(finalOutbuffer);
	}
	catch (...)
	{
		FreePtrAES(key);
		FreePtr(input);	
		FreePtr(output);
		FreePtr(inbuffer);
		FreePtr(outbuffer);
		FreePtr(outputdecrypt);
		FreePtr(finalInbuffer);
		FreePtr(finalOutbuffer);
	}
	
}

void TestDRMAPI()
{
	printf("******************************DRMAPI Test**************************\n");
	const char *RIGHTFILE = "Right.txt";
	int nRet = -1;
	char *pKey = NULL;
	int nKeyLen = 0;
	char *pEncContKey = NULL;
	int nEncContKeyLen = 0;
	char *pDeviceID = NULL;
	int nDeviceIDLen = 0;
	char *pRightBuf = NULL;
	int nRightBufLen = 0;
	char *pContKey = NULL;
	int nContKeyLen = 0;
	char *pProtectData = NULL;
	int nProtectDataLen = 0;
	char *pProtectOri = NULL;
	int nProtectOriLen = 0;
	unsigned char *pOutPut = NULL;
	int nOutPutLen = 0;
	unsigned char *pOutOri = NULL;
	int nOutOriLen = 0;
	char *pRandom = NULL;
	int nRandomLen = 0;
	char* pRightFileBuf = NULL;
	int nRightFileBufLen = 0;
	char *pMD5 = NULL;
	int nMD5Len = 0;

	try
	{
		// 生成密钥	
		nRet = GenerateKeyAES(&pKey, &nKeyLen);
		if(ERR_SUCCESS != nRet)
		{
			printf("GenerateKeyAES failed!\n");
			throw nRet;
		}
		else
			printf("GenerateKeyAES success!\n");	
		//char *pKey = "1146C84969ED0C2A21342130ACAF87FB";
		//int nKeyLen = 32;

		// 对密钥加密
		nRet = ProtectKey((char*)pKey, nKeyLen, &pEncContKey, &nEncContKeyLen);
		if(ERR_SUCCESS != nRet)
		{
			printf("ProtectKey AESKey failed!\n");
			throw nRet;	
		}

		// 密钥转换
		/*char *pEncContKey1 = NULL;
		int nEncContKeyLen1 = 0;
		nRet = StringEncryptQomolangma((char*)pKey, nKeyLen, &pEncContKey1, &nEncContKeyLen1)
		if(ERR_SUCCESS != nRet)
		{
			printf("ProtectKey AESKey failed!\n");
			throw nRet;	
		}

		nRet = TmpTransformContKey(pEncContKey1, nEncContKeyLen1, &pEncContKey, &nEncContKeyLen)
		if(ERR_SUCCESS != nRet)
		{
			printf("TmpTransformContKey failed!\n");
			throw nRet;	
		}

		FreePtr(pEncContKey1);*/

		// 生成DeviceID	
#ifdef WIN32
		nRet = GenerateDeviceID(&pDeviceID, &nDeviceIDLen);
		if(ERR_SUCCESS != nRet)
		{
			printf("GenerateDeviceID failed!\n");
			throw nRet;
		}
		else
		{
			printf("GenerateDeviceID success!\n");	
			printf("DeviceID:%s\n", pDeviceID);
		}
#else
		char *pUUID = "123456789012345";
		nRet = GenerateDeviceID(pUUID, strlen(pUUID), &pDeviceID, &nDeviceIDLen);
		if(ERR_SUCCESS != nRet)
		{
			printf("GenerateDeviceID failed!\n");
			throw nRet;
		}
		else
			printf("GenerateDeviceID success!\n");	
#endif

		// 生成随机数
		nRet = GenerateRandom(&pRandom, &nRandomLen);
		if(ERR_SUCCESS != nRet)
		{
			printf("GenerateRandom failed!\n");
			throw nRet;
		}	
		else
		{
			printf("Random:%s\n", pRandom);
			printf("GenerateRandom test Success\n");
		}
		//char *pRandom = "0001f3FW09ZNCnyfc/AnbU2JNC03enlaa1o4WnBaOHp5Wm8=";
		//char *pRandom = "++++----";

#ifdef WIN32
		// 对随机数保护
		nRet = ProtectData(pRandom, &pProtectData, &nProtectDataLen);
		if(ERR_SUCCESS != nRet)
		{
			printf("ProtectData failed!\n");
			throw nRet;

		}
		nRet = UnprotectData(pProtectData, nProtectDataLen, &pProtectOri);
		if(ERR_SUCCESS != nRet)
		{
			printf("UnprotectData failed!\n");
			throw nRet;
		}
		if(memcmp(pRandom, pProtectOri, strlen(pRandom)))
		{
			printf("ProtectData test failed!\n");
			throw nRet;
		}
		else
			printf("ProtectData test success!\n");
#endif

		// 获取当前版本号
		char pVersion[11] = {0};
		nRet = GetDRMVersion(pVersion);
		if(ERR_SUCCESS != nRet)
		{
			printf("GetDRMVersion failed!\n");
			throw nRet;
		}
		else
			printf("DRMVersion:%s\n", pVersion);

		// 生成授权文件
		char *pEbookID = "30005983";
		nRet = (GenerateRightFile((char*)pEbookID, strlen(pEbookID), (char*)pEncContKey, strlen(pEncContKey), 
			pDeviceID, strlen(pDeviceID), pRandom, strlen(pRandom), &pRightFileBuf, &nRightFileBufLen));
		if(ERR_SUCCESS != nRet)
		{			
			printf("GenerateRightFile failed!\n");
			throw nRet;
		}
		else		
			printf("GenerateRightFile success!\n");	 
		
		// 解析授权文件获取内容密钥
#ifdef WIN32
		nRet = GetContentKeyBuf(pRightFileBuf, nRightFileBufLen, NULL, pRandom, &pContKey, &nContKeyLen);
		if(ERR_SUCCESS != nRet)
		{
			printf("GetContentKey failed!\n");
			throw nRet;
		}
		else	
			printf("GetContentKey success!\n");	
#else
		nRet = GetContentKeyBuf(pRightFileBuf, nRightFileBufLen, pDeviceID, pRandom, &pContKey, &nContKeyLen);
		if(ERR_SUCCESS != nRet)
		{
			printf("GetContentKey failed!\n");
			throw nRet;
		}
		else	
			printf("GetContentKey success!\n");	
#endif

		if(memcmp(pKey, pContKey, nContKeyLen))
		{
			printf("GetContentKey test failed!\n");
			throw nRet;
		}
		else
			printf("All Rights test success!\n\n");  

#ifdef WIN32
#ifdef MMTEST
		// 读原文件
		FILE* pSourceFile = fopen("mmt.exe", "rb");
		if(NULL == pSourceFile)
		{
			printf("open file failed!\n");
			throw -1;
		}
		fseek(pSourceFile, 0, SEEK_END);
		int nSourceLen = ftell(pSourceFile);
		fseek(pSourceFile, 0, SEEK_SET);		
		unsigned char *pSource = new unsigned char[nSourceLen + 1];	
		fread(pSource, sizeof(char), nSourceLen, pSourceFile);
		fclose(pSourceFile);

		unsigned char *pComp = new unsigned char[nSourceLen];
		int nCompLen = nSourceLen;
		nRet = compress2(pComp, (uLongf *)&nCompLen, pSource, (uLongf)nSourceLen, Z_DEFAULT_COMPRESSION);
		if(0 != nRet)
		{
			printf("Compress file failed!\n");
			delete []pSource;
			pSource = NULL;
			throw nRet;
		}

		delete []pSource;
		pSource = NULL;

		// 写入压缩文件
		FILE* pCompFile = fopen("comp.dat", "wb");
		if(NULL == pCompFile)
		{
			printf("open file failed!\n");
			delete []pComp;
			pComp = NULL;
			throw -1;
		}
		fwrite(pComp, sizeof(char), nCompLen, pCompFile);
		fclose(pCompFile);

		delete []pComp;
		pComp = NULL;

		char* pMultiMediaFileName = "mmt.jdb";

		// 加密压缩文件
		if (!FileEncryptAES(pKey, nKeyLen, "comp.dat", pMultiMediaFileName))
		{	
			printf("FileEncryptAES compfile failed!\n");
			throw -1;
		}	

		if(LoadMultiMedia(pRightFileBuf, nRightFileBufLen, pRandom, pMultiMediaFileName))
		{
			printf("LoadMultiMedia failed!\n");
			throw nRet;
		}
		else	
			printf("LoadMultiMedia success!\n");
#endif  // MMTEST

		// DES加解密测试
		unsigned char *pDESKey = (unsigned char *)"12345678";
		unsigned char *pInput = (unsigned char *)"1234567890";
		nRet = DESEncrypt(pDESKey, strlen((char*)pDESKey), pInput, strlen((char*)pInput), &pOutPut, &nOutPutLen);
		if(ERR_SUCCESS != nRet)
		{
			printf("DESEncrypt failed!");
			throw nRet;
		}

		nRet = DESDecrypt(pDESKey, strlen((char*)pDESKey), pOutPut, nOutPutLen, &pOutOri, &nOutOriLen);
		if(ERR_SUCCESS != nRet)
		{
			printf("DESDecrypt failed!");
			throw nRet;
		}

		if(memcmp(pInput, pOutOri, nOutOriLen))
		{
			printf("DESEncrypt test failed!\n");
			throw nRet;
		}
		else
			printf("DESEncrypt test success!\n\n");
		
#endif

		if (!FileEncryptAES(pKey, nKeyLen, "testfile.txt", "testfileencrypt.jdb"))
		{
			printf("FileEncryptAES failed!\n");
			throw -1;
		}
		else
			printf("FileEncryptAES success!\n");

		if (!FileDecryptAES(pKey, nKeyLen, "testfileencrypt.jdb", "testfileencrypt.txt"))
		{
			printf("FileDecryptAES failed!\n");
			throw -1;
		}
		else
			printf("FileDecryptAES success!\n");

		nRet = MD5File("testfile.txt", &pMD5, &nMD5Len);
		if(ERR_SUCCESS != nRet)
		{
			printf("\nMD5File failed!");
			throw nRet;
		}
		else
		{
			printf("\nMD5:%s\n", pMD5);
			printf("MD5File test success!\n");		
		}

		// 释放内存
		FreePtrAES(pKey);
		FreePtr(pEncContKey);
		FreePtr(pDeviceID);
		FreePtr(pRightBuf);
		FreePtr(pContKey);
		FreePtr(pProtectData);
		FreePtr(pProtectOri);
		FreePtr(pOutPut);
		FreePtr(pOutOri);
		FreePtr(pRandom);
		FreePtr(pRightFileBuf);
		FreePtr(pMD5);
		
	}

	catch (int)
	{
		FreePtrAES(pKey);
		FreePtr(pEncContKey);
		FreePtr(pDeviceID);
		FreePtr(pRightBuf);
		FreePtr(pContKey);
		FreePtr(pProtectData);
		FreePtr(pProtectOri);
		FreePtr(pOutPut);
		FreePtr(pOutOri);
		FreePtr(pRandom);
		FreePtr(pRightFileBuf);
		FreePtr(pMD5);

	}
	catch (...)
	{
		FreePtrAES(pKey);
		FreePtr(pEncContKey);
		FreePtr(pDeviceID);
		FreePtr(pRightBuf);
		FreePtr(pContKey);
		FreePtr(pProtectData);
		FreePtr(pProtectOri);
		FreePtr(pOutPut);
		FreePtr(pOutOri);
		FreePtr(pRandom);
		FreePtr(pRightFileBuf);
		FreePtr(pMD5);
	}	

}

int main(int argc, char* argv[])
{	
	DWORD dwOldTime = GetTickCount();	
	TestDRMAPI();
	TestCipher();	
	DWORD dwTimeElapsed = GetTickCount()-dwOldTime;
	printf("TimeElapsed:%dms\n",dwTimeElapsed);
		
	return 0;
}



