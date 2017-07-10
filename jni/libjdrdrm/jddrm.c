
#include <jni.h>
//extern "C"
#include <android/log.h>
#include <stdio.h>
#include <malloc.h>



#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include "__b.h"
#define  LOG_TAG    "CCLOG"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)




extern void jdCreate(char *pUsrKet,int len);
extern void jdExit();
extern int decrypt(unsigned char * encryptedData, int encryptedLength, unsigned char *decryptedData,int decryptedLen,int end);

JNIEXPORT jint JNICALL Java_com_jingdong_app_reader_epub_paging_DecryptHelper_create
            (JNIEnv * env, jobject  obj)
{
    char *pUseKey;
    pUseKey=(char *)malloc(33);
    strcpy(pUseKey,"11ED290ABC5C2C53D730EE98D62635E4");
    jdCreate(pUseKey,32);
}


JNIEXPORT jint JNICALL Java_com_jingdong_app_reader_epub_paging_DecryptHelper_init 
            (JNIEnv * env, jobject  obj, 
             jstring str_key, jstring str_device, jstring str_random)
{
    
    int len;
    char *pUseKey;
    int iKeyLen=0;

    len = (*env)->GetStringUTFLength(env, str_key);
	char *key = (char*)malloc(len+1);	
	if(key)
	{
		memset(key, 0, len+1);
		const char* utf_str = (*env)->GetStringUTFChars(env, str_key, 0);
		if(utf_str)
		{
			strcpy(key, utf_str);
			(*env)->ReleaseStringUTFChars(env, str_key, utf_str);
		}
		else
		{
			free(key);
			return -1;
		}
	}
	else
	{
		free(key);
		return -1;
	}
	
	len = (*env)->GetStringUTFLength(env, str_device);
	char *device = (char*)malloc(len+1);	
	if(key)
	{
		memset(device, 0, len+1);
		const char* utf_str = (*env)->GetStringUTFChars(env, str_device, 0);
		if(utf_str)
		{
			strcpy(device, utf_str);
			(*env)->ReleaseStringUTFChars(env, str_device, utf_str);
		}
		else
		{
			free(key);
			free(device);
			return -1;
		}
	}
	else
	{
		free(key);
		free(device);
		return -1;
	}
	
	len = (*env)->GetStringUTFLength(env, str_random);
	char *random = (char*)malloc(len+1);	
	if(random)
	{
		memset(random, 0, len+1);
		const char* utf_str = (*env)->GetStringUTFChars(env, str_random, 0);
		if(utf_str)
		{
			strcpy(random, utf_str);
			(*env)->ReleaseStringUTFChars(env, str_random, utf_str);
		}
		else
		{
			free(key);
			free(device);
			free(random);
			return -1;
		}
	}
	else
	{
		free(key);
		free(device);
		free(random);
		return -1;
	}

	int bRet = GetContentKeyBuf(key, strlen(key), device,  (char *)random, &pUseKey, &iKeyLen);
    if (bRet) 
    {
		LOGI("******JDCipherUtil::GetContentKeyBuf false \n");
		return -1;
	}

    /*
    __bs("jddrm-----------------");
    __bs(key);
    __di("len",strlen(key));
    __bs(device);

    __bs(random);
    __bs(pUseKey);
    __di("iKeyLen",iKeyLen);
    */
    jdCreate(pUseKey,iKeyLen);


    
	free(key);
	free(device);
	free(random);
    
    return 0;
}

JNIEXPORT jint JNICALL Java_com_jingdong_app_reader_epub_paging_DecryptHelper_close 
            (JNIEnv * env, jobject  obj)
{
    jdExit();
}





    
JNIEXPORT jint JNICALL Java_com_jingdong_app_reader_epub_paging_DecryptHelper_decrypt 
            (JNIEnv * env, jobject  obj,jbyteArray encryptedData, jint encryptedLength,  jbyteArray  decryptedData,jint decryptedLeng,jint end)

{
#if 1    
    int len=0;
    jbyte *jsdata = (*env)->GetByteArrayElements(env,encryptedData,0);

    jbyte *jddata = (*env)->GetByteArrayElements(env,decryptedData,0); 
    /*
    unsigned char s[0x100]=
        {
            0x4f,0xd4,0x9c,0x9e,0x3c,0x80,0x5a,0x6b,0x01,0xef,0x9f,0x16,0x6a,0x48,0x2d,0xe9,
            0x66,0xfe,0x52,0xba,0xd7,0xeb,0xc8,0xec,0xc2,0xb1,0x18,0x3d,0xf1,0xa8,0x3e,0xe5,
            0x33,0x95,0x1d,0xbb,0xd1,0xaa,0xdf,0x1a,0xc5,0x20,0x83,0x17,0xa7,0xb7,0xed,0x4b,
            0x62,0xc3,0x70,0xd0,0x86,0x07,0xbd,0x04,0x4f,0x00,0xe7,0xee,0x99,0xb3,0xda,0x79,
            0x88,0xc6,0xc2,0x97,0x78,0x6b,0x23,0xee,0xec,0xd1,0x85,0x1a,0x67,0x36,0x84,0x3b,
            0xa2,0xd1,0xe7,0xc7,0xe5,0x56,0xf8,0x5a,0xe6,0xe1,0x52,0x31,0xf3,0xb3,0x9d,0x74,
            0x64,0x44,0x04,0x13,0xce,0x8e,0x16,0x22,0x49,0x48,0x79,0x57,0xa5,0xc2,0x88,0xcc,
            0xb8,0x07,0xad,0x93,0x53,0xac,0x0b,0x1f,0xc0,0x57,0x6e,0xb5,0xc1,0xbb,0xb5,0x78,
            0x31,0x1b,0x1b,0xc5,0xbc,0xae,0xbf,0x0e,0xbd,0x7d,0x62,0xdd,0x26,0x52,0xa8,0x43,
            0xd1,0x6c,0x70,0xda,0x91,0xd9,0xa2,0xc9,0x2d,0x36,0x28,0x16,0xcc,0x69,0x57,0x27,
            0x09,0xaa,0x89,0xf6,0xed,0x6a,0xac,0xcf,0xd6,0x3f,0xd4,0xa6,0xd6,0xd6,0xd5,0xc6,
            0xa7,0x8a,0xc4,0x04,0xda,0x0b,0xbd,0x4b,0xa6,0xa6,0x9d,0x9d,0x61,0x34,0xaa,0xc4,
            0x6a,0x6e,0xe7,0x46,0xfb,0x81,0x4e,0x48,0x5d,0xa8,0xa5,0x55,0x51,0xb2,0x85,0x3b,
            0x8c,0xb3,0x37,0x35,0x89,0xda,0x9a,0x69,0xda,0x30,0x01,0x33,0x19,0xec,0xb4,0xf2,
            0xf3,0xc9,0xa5,0xb9,0xb2,0xcb,0x1a,0xb2,0x1d,0x9b,0x4b,0x78,0xd4,0x3a,0xca,0x7d,
            0xac,0xdf,0xdf,0x56,0x36,0xc9,0x5f,0x9e,0xc8,0x7c,0x2f,0x9e,0xd9,0xcc,0xcb,0xa0

            };
    
    unsigned char d[0x400];
    //memcpy(jddata,jsdata,encryptedLength);
    
    len=decrypt(s,0x100 ,d,0x400,0);
    */ 
    len=decrypt(jsdata,encryptedLength,jddata,decryptedLeng,end);
    (*env)->ReleaseByteArrayElements(env,encryptedData, jsdata, 0);
    (*env)->ReleaseByteArrayElements(env,decryptedData, jddata, 0);
    return len;
#else
    struct stat st;
    unsigned char *ibuf;
    unsigned char *obuf;
    FILE *fp=fopen("/data/data/com.jingdong.app.reader/lib/libtest.so","rb");
    if (fp==NULL)
    {
        __b(1);
        return 0;
    }
    __b(2);
    stat ("/data/data/com.jingdong.app.reader/lib/libtest.so", &st);
    int size=st.st_size;
    
    ibuf=(unsigned char *)malloc(size+10);
    obuf=(unsigned char *)malloc(size*10);
    int omax=size*10;
    
    fread(ibuf,1,size,fp);
    fclose(fp);
    memset(obuf,0,size*10);
    int iopi=0;
    int oopi=0;
    int ss=0;
    
    while(size>0)
    {
        if(size>0x100)
            ss=0x100;
        else
            ss=size;
        //oopi=decrypt(&ibuf[iopi],ss ,&obuf[oopi],omax-oopi,1);
        oopi=decrypt(&ibuf[iopi],ss ,&obuf[oopi],omax-oopi,!(size>0x100));
        __di("oopi",oopi);
        size=size-ss;
        iopi=iopi+0x100;
    }
    __di("oopi",oopi); 
    __bs("--------------------------------------------------------------------------");
   
    size=oopi;
    char str[0x104];
   
    int opi=0;
    while(size>0)
    {
        if(size>0x100)
            ss=0x100;
        else
            ss=size;
        memcpy(str,&obuf[opi],ss);
        str[ss]=0;
        __bs(str);
        size=size-ss;
        opi=opi+0x100;
    }
    __bs("--------------------------------------------------------------------------");
    return 0;

#endif    
}


