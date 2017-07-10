#ifndef _DRM_LIB_H
#define _DRM_LIB_H

typedef void* Cipher;
typedef Cipher* PCipher;

#define ENCRYPT 0
#define DECRYPT 1

// 定义返回值
#define ERR_SUCCESS                     0   // 成功
#define ERR_PARAMETER_INVALID           1   // 输入参数错误
#define ERR_MEMORY_ALLOCATION           2   // 内存分配错误
#define ERR_FILE_RW                     3   // 文件读写错误
#define ERR_ENCRYPT_DECRYPT             4   // 加解密错误
#define ERR_VERSION_INVALID             5   // 版本错误
#define ERR_OTHER                      (-1) // 其他错误


#ifdef WIN32
#ifdef WIN_DLL
#ifdef _EBOOK_DLL
#define EBOOK_DRM_API  __declspec(dllexport)
#else
#define EBOOK_DRM_API  __declspec(dllimport)
#endif //_EBOOK_DLL
#else
#define EBOOK_DRM_API 
#endif //WIN_DLL
#else  //Linux
#define EBOOK_DRM_API 
#endif //WIN32

#ifdef __cplusplus
extern "C" {
#endif

/***************************************************************************************************************************/
//函数：  AES对称加密，对文件全文
//参数：  szKey         [in]  密钥字符串
//        nKeyLen       [in]  密钥长度 
//        szInFileName  [in]  被加密文件的全路径文件名
//		  szOutFileName [in]  加密后生成的全路径文件名
//返回值：bool 成功 true，失败 false
/***************************************************************************************************************************/
EBOOK_DRM_API bool FileEncryptAES( char* szKey, int nKeyLen, char* szInFileName, char* szOutFileName );

/***************************************************************************************************************************/
//函数：  AES对称解密，对文件全文
//参数：  szKey         [in]  密钥字符串
//        nKeyLen       [in]  密钥长度
//        szInFileName  [in]  被解密文件的全路径文件名
//		  szOutFileName [in]  解密后生成的全路径文件名
//返回值：bool 成功 true，失败 false
/***************************************************************************************************************************/
EBOOK_DRM_API bool FileDecryptAES( char* szKey, int nKeyLen, char* szInFileName, char* szOutFileName );

/***************************************************************************************************************************/
//函数：  AES对称加密，对内存中的字符串
//参数：  szKey      [in]  密钥，长度32字节
//        pInput     [in]  被加密字符串指针
//        nInputLen  [in]  被加密字符串长度
//		  pOutput    [out] 加密后字符串指针，内存由内部分配，外部调用函数FreePtrAES释放内存
//        nOutputLen [out] 加密后字符串长度
//        bPadding   [in]  是否使用Padding，默认值为true
//返回值：bool 成功 true，失败 false
/***************************************************************************************************************************/
#ifdef __cplusplus
EBOOK_DRM_API bool StringEncryptAES( char* szKey, char* pInput, int nInputLen, char** pOutput, int* nOutputLen, bool bPadding = true );
#else
EBOOK_DRM_API bool StringEncryptAES( char* szKey, char* pInput, int nInputLen, char** pOutput, int* nOutputLen, bool bPadding);
#endif

/***************************************************************************************************************************/
//函数：  AES对称解密，对内存中的字符串
//参数：  szKey      [in]  密钥，长度32字节
//        pInput     [in]  被解密字符串指针
//        nInputLen  [in]  被解密字符串长度
//		  pOutput    [out] 解密后字符串指针，内存由内部分配，外部调用函数FreePtrAES释放内存
//        nOutputLen [out] 解密后字符串长度
//        bPadding   [in]  是否使用Padding，默认值为true
//返回值：bool 成功 true，失败 false
/***************************************************************************************************************************/
#ifdef __cplusplus
EBOOK_DRM_API bool StringDecryptAES( char* szKey, char* pInput, int nInputLen, char** pOutput, int* nOutputLen, bool bPadding = true );
#else
EBOOK_DRM_API bool StringDecryptAES( char* szKey, char* pInput, int nInputLen, char** pOutput, int* nOutputLen, bool bPadding);
#endif

/***************************************************************************************************************************/
//函数：  释放内存
//参数：  ptr [in] 释放内存指针
//返回值：无
/***************************************************************************************************************************/
EBOOK_DRM_API void FreePtrAES( void* ptr );

/***************************************************************************************************************************/
//函数：  创建加解密句柄
//参数：  pcipher         [out]  加解密句柄
//返回值：int 成功 0，失败 -1
/***************************************************************************************************************************/
EBOOK_DRM_API int CreateCipher(PCipher pcipher);

/***************************************************************************************************************************/
//函数：  初始化加解密
//参数：  cipher          [in]   加解密句柄
//        opmode          [in]   加解密模式，加密为ENCRYPT，解密为DECRYPT
//        key，Key_len    [in]   密钥信息
//返回值：int 成功 0，失败 -1
/***************************************************************************************************************************/
EBOOK_DRM_API int InitCipher(Cipher cipher, int opmode, const unsigned char* key, int key_len);

/***************************************************************************************************************************/
//函数：  更新数据加解密
//参数：  cipher                  [in]    加解密句柄
//        in_data, in_data_len    [in]    输入的数据
//        out_data                [out]   输出的数据,内存由外部申请、释放
//        out_data_len            [in/out]输出数据的长度
//返回值：int 成功 0，失败 -1
/***************************************************************************************************************************/
EBOOK_DRM_API int UpdateCipher(Cipher cipher, const unsigned char* in_data, int in_data_len, 
							   unsigned char* out_data,  int* out_data_len);

/***************************************************************************************************************************/
//函数：  结束加解密,对结尾做相应padding处理
//参数：  cipher                  [in]  加解密句柄
//        in_data, in_data_len    [in]  输入的数据，可以为NULL/0
//        out_data                [out] 输出的数据，内存由外部申请、释放，output buffer比input buffer大一个block的长度即可,
//                                      目前算法的block长度为16字节，内存申请不够则返回错误，再按照out_data_len申请内存
//        out_data_len            [in/out]输出数据的长度
//返回值：int 成功 0，失败 -1
/***************************************************************************************************************************/
EBOOK_DRM_API int FinalCipher(Cipher cipher,const unsigned char* in_data, int in_data_len,
							  unsigned char* out_data, int* out_data_len);

/***************************************************************************************************************************/
//函数：  释放加解密句柄
//参数：  cipher                [in]  加解密句柄
//返回值：int 成功 0，失败 -1
/***************************************************************************************************************************/
EBOOK_DRM_API int DestroyCipher(Cipher cipher);

/***************************************************************************************************************************/
//函数：  散列函数
//参数：  pInput     [in]  输入的字符串指针
//        nInputLen  [in]  输入的字符串长度
//        pOutput    [out] 输出的散列字符串指针，内存由内部分配，使用完毕后外部调用FreePtrAES(void* ptr)释放内存
//        nOutputLen [out] 输出的散列字符串长度
//返回值：void
/***************************************************************************************************************************/
EBOOK_DRM_API void Hash256( char* pInput, int nInputLen, char** pOutput, int* nOutputLen );

/***************************************************************************************************************************/
//函数：  计算文件的MD5值
//参数：  pFilePath  [in]  输入的全路径文件名
//        ppOutput   [out] 输出的散列字符串指针，内存由内部分配，使用完毕后外部调用FreePtr(void* ptr)释放内存
//        nOutputLen [out] 输出的散列字符串长度
//返回值：int 成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int MD5File( const char* pFilePath, char** ppOutput, int* nOutputLen );

/***************************************************************************************************************************/
//函数：  不需要密钥的私有加密方法
//参数：  pInBuf     [in]  待加密的字符串指针
//        nInBufLen  [in]  待加密的字符串长度
//        ppOutBuf   [out] 加密后的字符串指针，内存由内部分配，使用完毕后外部调用FreePtr(void* ptr)释放内存
//        nOutBufLen [out] 加密后字符串长度
//返回值：int 成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int StringEncryptQomolangma( char* pInBuf, int nInBufLen, char** ppOutBuf, int* nOutBufLen );

/***************************************************************************************************************************/
//函数：  不需要密钥的私有解密方法
//参数：  pInBuf     [in]  待解密的字符串指针
//        nInBufLen  [in]  待解密的字符串长度
//        ppOutBuf   [out] 解密后的字符串指针，内存由内部分配，使用完毕后外部调用FreePtr(void* ptr)释放内存
//        nOutBufLen [out] 解密后字符串长度
//返回值：int 成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int StringDecryptQomolangma( char* pInBuf, int nInBufLen, char** ppOutBuf, int* nOutBufLen );

/***************************************************************************************************************************/
//函数：  释放内存
//参数：  ptr [in] 释放内存指针
//返回值：无
/***************************************************************************************************************************/
EBOOK_DRM_API void FreePtr( void* ptr );

/***************************************************************************************************************************/
//函数：  获取DRM版本号，长度为10个字节
//参数：  pVersion      [in/out] 版本号字符串，内存由外部分配、释放。
//返回值：int  成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int  GetDRMVersion(char* pVersion);

#ifdef _ENCTOOL
/***************************************************************************************************************************/
//函数：  生成对称加密密钥
//参数：  ppKey  [out] 生成密钥的指针，内存由内部分配，外部调用函数FreePtrAES释放内存
//        nLen   [out] 生成密钥的长度
//返回值：int 成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int GenerateKeyAES(char** ppKey, int* nLen);

/***************************************************************************************************************************/
//函数：  加密生成的密钥
//        pKey       [in]  待加密的密钥
//        nKeyLen    [in]  待加密的密钥长度
//        ppEncKey   [out] 加密后的密钥，内存由内部分配，使用完毕后外部调用FreePtr(void* ptr)释放内存
//        pEncKeyLen [out] 加密后密钥长度
//返回值：int 成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int ProtectKey(char* pKey, int nKeyLen, char** ppEncKey, int *pEncKeyLen);
#endif // _ENCTOOL

#ifdef _SERVER
/***************************************************************************************************************************/
//函数：  生成授权凭证文件
//参数：  pEbookID            [in]      电子书ID
//        nEbookIDLen         [in]      电子书ID长度
//        pContKey            [in]      内容密钥，直接从数据库中取到已加密的数据
//        nContKeyLen         [in]      内容密钥长度
//        pDevIDHashCipher    [in]      用户设备ID Hash值的密文，由客户端发送过来后，直接存放在数据库中的字符串
//        nDevIDHashCipherLen [in]      用户设备ID Hash值的密文长度
//        pRandomCipher       [in]      用户注册时生成的随机数密文
//        nRandomCipherLen    [in]      用户注册时生成的随机数密文长度
//        ppRightfileBuf      [in/out]  生成授权文件存放的缓冲区
//        nRightFileBufLen    [in/out]  生成授权文件存放的缓冲区长度
//返回值：int  成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int GenerateRightFile(char* pEbookID, int nEbookIDLen, char* pContKey, int nContKeyLen, char* pDevIDHashCipher, 
									int nDevIDHashCipherLen, char* pRandomCipher, int nRandomCipherLen, char** ppRightFileBuf, int* nRightFileBufLen);

/***************************************************************************************************************************/
//函数：  生成字符串形式的随机数
//参数：  pRandom      [out] 随机数字符串，内存由内部分配，使用完毕后外部调用FreePtr(void* ptr)释放内存
//参数    pRandomLen   [out] 随机数字符串长度
//返回值：int  成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int  GenerateRandom(char** ppRandom, int* pRandomLen);
#endif // _SERVER

#ifdef _CLIENT
/***************************************************************************************************************************/
//函数：  解析授权凭证文件内容字符串
//参数：  pInBuf           [in]  授权文件内容字符串
//        nInBufLen        [in]  授权文件内容字符串长度
//        pDevIDHashCipher [in]  用户设备哈希值密文
//        pRandomCipher    [in]  用户随机数密文
//        ppRight          [out] TLV格式的授权文件字符串，内存由内部分配，使用完毕后外部调用FreePtr(void* ptr)释放内存
//        nRightLen        [out] 授权文件字符串长度
//返回值：int  成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int AnalyticRightFileBuf(const char* pInBuf, int nInBufLen, char* pDevIDHashCipher, char* pRandomNumCipher, char** ppRight, int* nRightLen);

/***************************************************************************************************************************/
//函数：  从授权文件字符串中获取内容密钥
//参数：  pRightFileBuf    [in]  授权文件内容字符串
//        nRightFileBuflen [in]  授权文件内容字符串长度
//        pDevIDHashCipher [in]  用户设备哈希值密文，PC客户端调用该接口时，该参数应设为NULL
//        pRandomCipher    [in]  用户随机数密文
//        ppKey            [out] 内容密钥，内存由内部分配，使用完毕后外部调用FreePtr(void* ptr)释放内存
//        nKeyLen          [out] 内容密钥的长度
//返回值：int  成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int GetContentKeyBuf(const char* pRightFileBuf, int nRightFileBuflen, char* pDevIDHashCipher, char* pRandomCipher, 
								   char** ppKey, int* nKeyLen);

#ifndef WIN32
/***************************************************************************************************************************/
//函数：  Android、iOS平台生成硬件设备ID
//参数：  pUUID          [in]   设备的UUID
//        nUUIDLen       [in]   设备UUID的长度
//        ppDeviceID     [out]  生成的硬件设备ID，内存由内部分配，外部调用FreePtr释放内存
//        pDeviceIDLen   [out]  硬件设备ID的长度
//返回值：int 成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int GenerateDeviceID(char* pUUID, int nUUIDLen, char** ppDeviceID, int* pDeviceIDLen);
#endif //ndef WIN32

//PC Windows平台使用的接口
#ifdef WIN32
#include <Windows.h>
/***************************************************************************************************************************/
//函数：  加密随机数使其与当前设备绑定
//参数：  pOriginalData                   [in]   待保护的原始数据
//        pProtectData，nProtectDataLen   [out]  保护后的数据，内存由内部分配，外部调用FreePtr释放内存
//返回值：int 成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int ProtectData(char* pOriginalData, char** ppProtectData, int* nProtectDataLen);

/***************************************************************************************************************************/
//函数：  解密随机数
//参数：  pProtectData，nProtectDataLen    [in]  被保护的数据
//        pOriginalData                    [out] 取消保护后的原始数据，内存由内部分配，外部调用FreePtr释放内存
//返回值：int 成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int UnprotectData(char* pProtectData, int nProtectDataLen, char** ppOriginalData);

/***************************************************************************************************************************/
//函数：  生成硬件设备ID
//参数：  ppDeviceID     [out]  生成的硬件设备ID，内存由内部分配，外部调用FreePtr释放内存
//        pDeviceIDLen   [out]  生成的硬件设备ID的长度
//返回值：int 成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int GenerateDeviceID(char** ppDeviceID, int* pDeviceIDLen);

/***************************************************************************************************************************/
//函数：  运行多媒体文件
//参数：  pRightFileBuf       [in]  授权文件字符串
//        nRightFileBuflen    [in]  授权文件字符串长度
//        pRandom             [in]  客户端注册的随机数
//        pMultiMediaFileName [in]  多媒体文件存放的路径
//返回值：int  成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int LoadMultiMedia(const char* pRightFileBuf, int nRightFileBuflen, char* pRandom, char* pMultiMediaFileName);

/***************************************************************************************************************************/
//函数：  DES加密
//参数：  pKey         [in]  密钥
//        nKeyLen      [in]  密钥长度，长度为8个字节
//        pInBuf       [in]  待加密的字符串指针
//        nInBufLen    [in]  待加密的字符串长度
//        ppOutBuf     [out] 加密后的字符串指针，内存由内部分配，使用完毕后外部调用FreePtr(void* ptr)释放内存
//        nOutBufLen   [out] 加密后字符串长度
//        nEncryptMode [in]  DES加解密模式,0为ECB模式，默认值为0
//        bPadding     [in]  是否使用Padding(PKCS5Padding)，默认值为true
//返回值：int 成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int DESEncrypt(unsigned char* pKey, int nKeyLen, unsigned char* pInBuf, int nInBufLen, unsigned char** ppOutBuf, int* nOutBufLen, 
							 const int nEncryptMode = 0, bool bPadding = true );

/***************************************************************************************************************************/
//函数：  DES解密
//参数：  pKey         [in]  密钥
//        nKeyLen      [in]  密钥长度,长度为8个字节
//        pInBuf       [in]  待解密的字符串指针
//        nInBufLen    [in]  待解密的字符串长度
//        ppOutBuf     [out] 解密后的字符串指针，内存由内部分配，使用完毕后外部调用FreePtr(void* ptr)释放内存
//        nOutBufLen   [out] 解密后字符串长度
//        nEncryptMode [in]  DES加解密模式，0为ECB模式，默认值为0
//        bPadding     [in]  是否使用Padding(PKCS5Padding)，默认值为true
//返回值：int 成功 0，失败 非0
/***************************************************************************************************************************/
EBOOK_DRM_API int DESDecrypt(unsigned char* pKey, int nKeyLen, unsigned char* pInBuf, int nInBufLen, unsigned char** ppOutBuf, int* nOutBufLen, 
							 const int nEncryptMode = 0, bool bPadding = true );

#endif //WIN32
#endif // _CLIENT

#ifdef __cplusplus
}
#endif

#endif //_DRM_LIB_H

