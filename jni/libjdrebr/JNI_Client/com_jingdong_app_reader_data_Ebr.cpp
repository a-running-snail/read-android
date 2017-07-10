#include "com_jingdong_app_reader_data_Ebr.h"
#include "DRMLib.h" 
#include <stdio.h>
#include <android/log.h>

/*
 * Class:     com_jingdong_app_reader_data_Ebr
 * Method:    API01
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
//JNIEXPORT jstring JNICALL Java_com_jingdong_app_reader_data_Ebr_API01
JNIEXPORT jstring JNICALL Java_com_jingdong_app_reader_data_DrmTools_API01
  (JNIEnv *env, jobject obj, jstring UUID)
{
	char *pUUID = (char*)env->GetStringUTFChars(UUID, NULL);
	int nUUID =  (int)env->GetStringUTFLength(UUID);
	char *pDeviceID = NULL;
	int nDeviceIDLen = 0;
	int nRet = GenerateDeviceID(pUUID, nUUID, &pDeviceID, &nDeviceIDLen);
	env->ReleaseStringUTFChars(UUID, pUUID);
	if(0 != nRet)
		return NULL;
	
    jstring str = env->NewStringUTF(pDeviceID);
	return str;

}

/*
 * Class:     com_jingdong_app_reader_data_Ebr
 * Method:    API02
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z
 */
//JNIEXPORT jboolean JNICALL Java_com_jingdong_app_reader_data_Ebr_API02
JNIEXPORT jboolean JNICALL Java_com_jingdong_app_reader_data_DrmTools_API02
  (JNIEnv *env, jobject obj, jstring RightFileBuf, jstring DevIDHashCipher, jstring Random, jstring InFileName, jstring OutFileName)
{
	char *pRightFileBuf = (char*)env->GetStringUTFChars(RightFileBuf, NULL);
	int nRightFileBufLen =  (int)env->GetStringUTFLength(RightFileBuf);
	char *pDevIDHashCipher = (char*)env->GetStringUTFChars(DevIDHashCipher, NULL);
	char *pRandom = (char*)env->GetStringUTFChars(Random, NULL);
	char *pInFileName =(char*)env->GetStringUTFChars(InFileName, NULL);
	char *pOutFileName = (char*)env->GetStringUTFChars(OutFileName, NULL);

	char *pKey = NULL;
	int nKeyLen = 0;
	int nRet = GetContentKeyBuf(pRightFileBuf, nRightFileBufLen, pDevIDHashCipher, pRandom, &pKey, &nKeyLen);
	env->ReleaseStringUTFChars(RightFileBuf, pRightFileBuf);
	env->ReleaseStringUTFChars(DevIDHashCipher, pDevIDHashCipher);
	env->ReleaseStringUTFChars(Random, pRandom);
	if(0 != nRet)
	{
		env->ReleaseStringUTFChars(InFileName, pInFileName);
		env->ReleaseStringUTFChars(OutFileName, pOutFileName);
		return false;
	}
//__android_log_print(ANDROID_LOG_INFO, "ProjectName", "Key:%s", pKey);
//__android_log_print(ANDROID_LOG_INFO, "ProjectName", "nKeyLen:%d", nKeyLen);
	bool bRet = FileDecryptAES(pKey, nKeyLen, pInFileName, pOutFileName);		
	env->ReleaseStringUTFChars(InFileName, pInFileName);
	env->ReleaseStringUTFChars(OutFileName, pOutFileName);
	FreePtr(pKey);

	return bRet;

}

