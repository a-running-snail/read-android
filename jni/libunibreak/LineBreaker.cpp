#include <jni.h>
#include "linebreak.h"

extern "C" {

JNIEXPORT void JNICALL Java_com_jingdong_app_reader_epub_paging_LineBreaker_init
	(JNIEnv *, jclass) {
		init_linebreak();
}


JNIEXPORT void JNICALL Java_com_jingdong_app_reader_epub_paging_LineBreaker_setLineBreaksForCharArray
  (JNIEnv * env, jclass clazz, jcharArray data, jint offset, jint length, jstring lang, jbyteArray breaks)
{
	jchar* dataArray = env->GetCharArrayElements(data, 0);
	jbyte* breaksArray = env->GetByteArrayElements(breaks, 0);
	const char *langArray = (lang != 0) ? env->GetStringUTFChars(lang, 0) : 0;

	set_linebreaks_utf16(dataArray + offset, length, langArray, (char*)breaksArray);
	const jchar* start = dataArray + offset;
	const jchar* end = start + length;
	for (const jchar* ptr = start; ptr < end; ++ptr) {
		if (*ptr == (jchar)0xAD) {
			breaksArray[ptr - start] = LINEBREAK_NOBREAK;
		}
	}

	if (lang != 0) {
		env->ReleaseStringUTFChars(lang, langArray);
	}
	env->ReleaseByteArrayElements(breaks, breaksArray, 0);
	env->ReleaseCharArrayElements(data, dataArray, 0);

}


JNIEXPORT void JNICALL Java_com_jingdong_app_reader_epub_paging_LineBreaker_setLineBreaksForString
  (JNIEnv * env, jclass clazz, jstring data, jstring lang, jbyteArray breaks)
{
	const jchar* dataArray = env->GetStringChars(data, 0);
	jbyte* breaksArray = env->GetByteArrayElements(breaks, 0);
	const size_t len = env->GetStringLength(data);
	const char *langArray = (lang != 0) ? env->GetStringUTFChars(lang, 0) : 0;

	set_linebreaks_utf16(dataArray, len, langArray, (char*)breaksArray);

	if (lang != 0) {
  	env->ReleaseStringUTFChars(lang, langArray);
	}
	env->ReleaseByteArrayElements(breaks, breaksArray, 0);
	env->ReleaseStringChars(data, dataArray);

}

}

