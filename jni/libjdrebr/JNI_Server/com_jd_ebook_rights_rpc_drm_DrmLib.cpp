#include "com_jd_ebook_rights_rpc_drm_DrmLib.h"
#include "DRMLib.h"

JNIEXPORT jobject JNICALL Java_com_jd_ebook_rights_rpc_drm_DrmLib_GenerateRightFile
(JNIEnv *env, jobject obj, jbyteArray jEbookID, jint nEbookIDLen, jbyteArray jContKey, 
 jint nContKeyLen, jbyteArray jDevIDHash, jint nDevIDHashLen, jbyteArray jRandom,jint nRandomLen)
{
	jbyte* pEbookID = env->GetByteArrayElements(jEbookID, 0);
	jbyte* pContKey = env->GetByteArrayElements(jContKey, 0);
	jbyte* pDevIDHash = env->GetByteArrayElements(jDevIDHash, 0);
	jbyte* pRandom = env->GetByteArrayElements(jRandom, 0);

	char *pRightBuf = NULL;
	int nRightBufLen = 0;

	int nRet = GenerateRightFile((char*)pEbookID, nEbookIDLen, (char*)pContKey, nContKeyLen, (char*)pDevIDHash, nDevIDHashLen, (char*)pRandom, nRandomLen, &pRightBuf, &nRightBufLen);

	jclass objClass = env->FindClass("com/jd/ebook/rights/rpc/drm/DrmObject");
	jfieldID str = env->GetFieldID(objClass, "pString", "[B");
	jfieldID len = env->GetFieldID(objClass, "nStrLen", "I");
	jfieldID result = env->GetFieldID(objClass, "nResult", "I");

	jobject objDrm = env->AllocObject(objClass);   
	if(!nRet)
	{
		jbyteArray jarray = env->NewByteArray(nRightBufLen); 
		if (NULL != jarray) 
		{
			env->SetByteArrayRegion(jarray, 0, nRightBufLen, (jbyte*)pRightBuf); 
			env->SetObjectField(objDrm, str, jarray);
			env->SetIntField(objDrm, len, nRightBufLen);
			env->DeleteLocalRef(jarray);
		}
		else
		{
			env->SetObjectField(objDrm, str, 0);
			env->SetIntField(objDrm, len, 0);
		}  
	}
	else
	{
		env->SetObjectField(objDrm, str, 0);
		env->SetIntField(objDrm, len, 0);
	}

	env->SetIntField(objDrm, result, nRet);

	env->DeleteLocalRef(objClass);

	env->ReleaseByteArrayElements(jEbookID, pEbookID,  0);
	env->ReleaseByteArrayElements(jContKey, pContKey,  0);
	env->ReleaseByteArrayElements(jDevIDHash, pDevIDHash,  0);
	env->ReleaseByteArrayElements(jRandom, pRandom,  0);

	FreePtr(pRightBuf);

	return objDrm;
}

JNIEXPORT jobject JNICALL Java_com_jd_ebook_rights_rpc_drm_DrmLib_GenerateRandom
(JNIEnv *env, jobject obj)
{
	char *pRandomCipher = NULL;
	int nRandomCipherLen = 0;

	int nRet = GenerateRandom(&pRandomCipher, &nRandomCipherLen);

	jclass objClass = env->FindClass("com/jd/ebook/rights/rpc/drm/DrmObject");
	jfieldID str = env->GetFieldID(objClass, "pString", "[B");
	jfieldID len = env->GetFieldID(objClass, "nStrLen", "I");
	jfieldID result = env->GetFieldID(objClass, "nResult", "I");

	jobject objDrm = env->AllocObject(objClass); 

	if(!nRet)
	{
		jbyteArray jarray = env->NewByteArray(nRandomCipherLen); 
		if (NULL != jarray) 
		{
			env->SetByteArrayRegion(jarray, 0, nRandomCipherLen, (jbyte*)pRandomCipher); 
			env->SetObjectField(objDrm, str, jarray);
			env->SetIntField(objDrm, len, nRandomCipherLen);
			env->DeleteLocalRef(jarray);
		}
		else
		{
			env->SetObjectField(objDrm, str, 0);
			env->SetIntField(objDrm, len, 0);
		}  
	}
	else
	{
		env->SetObjectField(objDrm, str, 0);
		env->SetIntField(objDrm, len, 0);
	}

	env->SetIntField(objDrm, result, nRet);

	env->DeleteLocalRef(objClass);
	
	FreePtr(pRandomCipher);

	return objDrm;
}

