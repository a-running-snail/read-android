#include "DRMLib.h"
#include "Cipher.h"
#include <assert.h>
#include <malloc.h>
/* Header for class Cipher */

jclass cihper_wrapper_cls;
jmethodID cipher_wrapper_setCipher;
jmethodID cipher_wrapper_getCipher;

Cipher cipher_from_java(JNIEnv* env, jobject self)
{
	jclass cihper__cls = env->FindClass("Cipher");
	jfieldID fid = env->GetFieldID(cihper__cls, "cipher", "I");
	return (Cipher)env->GetObjectField(self, fid);
}


JNIEXPORT jobject JNICALL Java_Cipher_getInstance
(JNIEnv * env, jclass cls, jstring transformation)
{
	Cipher cipher;
	const char *str;
	str = env->GetStringUTFChars(transformation, NULL);
	cipher_create(&cipher, str);
	env->ReleaseStringUTFChars(transformation, str);

	jclass cihper__cls = env->FindClass("Cipher");
	jmethodID mid = env->GetMethodID(cls, "<init>", "()V");
	jmethodID mid_update = env->GetMethodID(cls, "update", "([B)[B");
	jbyteArray jb = env->NewByteArray(32);
	// 
	//jobject obj = env->NewObject(cihper__cls, mid, "");
	jobject obj = env->NewObject(cls, mid, "");
	jfieldID fid = env->GetFieldID(cls, "cipher", "I");
	env->SetObjectField(obj, fid, (jobject)cipher);
	return obj;
}


JNIEXPORT jint JNICALL Java_Cipher_init
(JNIEnv * env, jobject self, jint opmode, jbyteArray key, jbyteArray iv)
{
	
		//(obj, fid, (jobject)cipher);

	Cipher cipher = (Cipher)cipher_from_java(env, self);
	jsize key_len = env->GetArrayLength(key);
	jbyte* key_data = env->GetByteArrayElements(key, 0);

	jsize iv_len = env->GetArrayLength(key);
	jbyte* iv_data = env->GetByteArrayElements(iv, 0);

	return cipher_init(cipher, opmode, (unsigned char*)key_data, key_len, (unsigned char*)iv_data, iv_len);
}

JNIEXPORT jbyteArray JNICALL Java_Cipher_update
(JNIEnv * env, jobject self, jbyteArray data)
{
	Cipher cipher = (Cipher)cipher_from_java(env, self);
	jsize in_data_len = env->GetArrayLength(data);
	jbyte* in_data = env->GetByteArrayElements(data, 0);
	int out_data_len = in_data_len + 16;
	char* out_data = (char*)malloc(out_data_len);
	cipher_update(cipher, (unsigned char*)in_data, in_data_len, (unsigned char*)out_data, &out_data_len);
	assert(out_data_len < in_data_len + 16);
	jbyteArray jb = env->NewByteArray(out_data_len);
	env->SetByteArrayRegion(jb, 0, 
		out_data_len, (jbyte *)out_data);
	return jb;
}

/*
* Class:     Cipher
* Method:    doFinal
* Signature: ([B)[B
*/
JNIEXPORT jbyteArray JNICALL Java_Cipher_doFinal
(JNIEnv * env, jobject self, jbyteArray data)
{
	Cipher cipher = (Cipher)cipher_from_java(env, self);
	jsize in_data_len = env->GetArrayLength(data);
	jbyte* in_data = env->GetByteArrayElements(data, 0);
	int out_data_len = in_data_len + 16;
	char* out_data = (char*)malloc(out_data_len);
	cipher_update(cipher, (unsigned char*)in_data, in_data_len, (unsigned char*)out_data, &out_data_len);
	assert(out_data_len < in_data_len + 16);
	jbyteArray jb = env->NewByteArray(out_data_len);
	env->SetByteArrayRegion(jb, 0, 
		out_data_len, (jbyte *)out_data);
	return jb;
}

JNIEXPORT jint JNICALL Java_Cipher_destroy
(JNIEnv * env, jobject self)
{
	Cipher cipher = (Cipher)cipher_from_java(env, self);
	return cipher_destroy(cipher);
}
