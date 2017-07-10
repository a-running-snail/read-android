#ifndef DRMALGORITHM_H_INCLUDED
#define DRMALGORITHM_H_INCLUDED

typedef void* Cipher;
typedef Cipher* PCipher;
typedef void* Digestor;
typedef Cipher* PDigestor;
typedef void* CipherStream;
typedef CipherStream* PCipherStream;

#ifdef WIN32
typedef int (__cdecl* read_proc)(void* userp, void* buf, int buf_len);
typedef int (__cdecl* write_proc)(void* userp, void* buf, int buf_len);
#else
typedef int (__attribute__((__cdecl__))* read_proc)(void* userp, void* buf, int buf_len);
typedef int (__attribute__((__cdecl__))* write_proc)(void* userp, void* buf, int buf_len);
#endif

typedef int DRM_Err;

#define DRM_Err_Ok                  0
#define DRM_Err_Unkown             -1
#define DRM_Err_Algorithm_Invalid   1   /* transformation参数给出的算法不支持 */
#define DRM_Err_Buffer_Insufficient 2   /* 输出缓冲区的长度不够 */
#define DRM_Err_Parameter_Invalid   3
#define DRM_Err_Key_Length_Invalid  4
#define DRM_Err_IV_Length_Invalid   5
#define DRM_Err_Padding_Invalid   6
#define DRM_Err_Secret_Length_Invalid   7 /* invalid crypted-text length */


#define IN
#define OUT
#define CAN_0 /* 参数可以为NULL/0 */


#ifdef WIN32
#ifdef WIN_DLL
#ifdef _EBOOK_DLL
#define EBOOK_DRM_API extern "C" __declspec(dllexport)
#else
#define EBOOK_DRM_API extern "C" __declspec(dllimport)
#endif //_EBOOK_DLL
#else
#define EBOOK_DRM_API extern "C"
#endif //WIN_DLL
#else  //Linux
#define EBOOK_DRM_API extern "C"
#endif //WIN32

#define OPMODE_ENCRYPT 0
#define OPMODE_DECRYPT 1

/***************************************************************************************************************************/
//说明：  创建加解密句柄。
//参数：  pcipher         [out]  加解密句柄
//        transformation  [in]   transformation: 加密库通常为字符串，格式为 "算法/小模式/padding", 如"DES/CBC/PKCS-5Padding"。
//                               为了防止字符串泄露信息，三部分都用数字代替，第2和第3部分为0或者没有时表示缺省模式。
//                               目前支持:
// "1/0/0": AES ECB模式，padding为PKCS-5
// "1/1/0": AES CBC模式，padding为PKCS-5
//返回值：DRM_Err         错误码
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err 
cipher_create(PCipher pcipher, const char* transformation);

/***************************************************************************************************************************/
//说明：  生成加解密密钥。
//参数：    pcipher         [in]   加解密句柄
//          key             [out]  加解密密钥，内存由外部分配,申请的内存要足够大，否则返回DRM_Err_Buffer_Insufficient错误码，
//                                 再按照keylen申请内存。
//          keylen         [in/out]  加解密密钥长度
//返回值：DRM_Err         错误码
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_generatekey(Cipher cipher, unsigned char* key, IN OUT int *keylen);

/***************************************************************************************************************************/
//说明：  初始化加解密。
//参数：  cipher          [in]   加解密句柄
//        opmode          [in]   加解密模式，加密为OPMODE_ENCRYPT，解密为OPMODE_DECRYPT
//        key，Key_len    [in]   密钥信息
//        IV，IV_len      [in]   初始化向量
//返回值：DRM_Err         错误码
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err 
cipher_init(Cipher cipher, int opmode, 
			const unsigned char* key, int key_len,
			const unsigned char* IV,
			int IV_len);

/***************************************************************************************************************************/
//说明：  更新数据加解密。
//参数：  cipher                  [in]    加解密句柄
//        in_data, in_data_len    [in]    输入的数据
//        out_data                [out]   输出的数据,内存由外部申请
//        out_data_len            [in/out]输出数据的长度
//返回值：DRM_Err                 错误码
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_update(Cipher cipher, const unsigned char* in_data, int in_data_len, 
			  unsigned char* out_data, IN OUT int* out_data_len);

/***************************************************************************************************************************/
//说明：  结束加解密,对结尾做相应padding处理。
//参数：  cipher                  [in]  加解密句柄
//        in_data, in_data_len    [in]  输入的数据，可以为NULL/0
//        out_data                [out] 输出的数据，内存由外部申请，申请的内存要足够大，否则返回DRM_Err_Buffer_Insufficient错误码，
//                                      再按照out_data_len申请内存。
//        out_data_len            [in/out]输出数据的长度
//返回值：DRM_Err                 错误码
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_final(Cipher cipher, CAN_0 const unsigned char* in_data, CAN_0 int in_data_len, 
			 unsigned char* out_data, IN OUT int* out_data_len);

/***************************************************************************************************************************/
//说明：  释放加解密句柄。
//参数：  cipher                 [in]  加解密句柄
//返回值：DRM_Err                 错误码
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_destroy(Cipher cipher);

/***************************************************************************************************************************/
//说明：  创建摘要句柄。
//参数：  pdigestor         [out]  摘要句柄
//        transformation    [in]   transformation: 加密库通常为字符串，格式为 "算法/小模式/padding"
//返回值：DRM_Err           错误码
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
digestor_create(PDigestor pdigestor, const char* transformation);

/***************************************************************************************************************************/
//说明：  更新数据摘要。
//参数：  digestor          [in]      摘要句柄
//        data,data_len     [in]      输入数据
//返回值：DRM_Err           错误码
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
digestor_update(Digestor digestor, const unsigned char* data, int data_len);

/***************************************************************************************************************************/
//说明：  结束摘要。
//参数：  digestor              [in]    摘要句柄
//        data, data_len        [in]    输入的数据，可以为NULL/0
//        digest                [out]   输出的数据
//        digest_len            [in/out]输出数据的长度
//返回值：DRM_Err               错误码
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
digestor_final(Digestor digestor, CAN_0 const unsigned char* data, CAN_0 int data_len, 
			   unsigned char* digest, IN OUT int* digest_len);

/***************************************************************************************************************************/
//说明：  释放摘要句柄。
//参数：  digestor                [in]  摘要句柄
//返回值：DRM_Err                 错误码
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
digestor_destroy(Digestor digestor);

/*
cipher_stream_xx系列函数是为了给解密文件提供一个隐藏解密细节的接口。
**/
/***************************************************************************************************************************/
//说明：  创建流式加解密句柄。
//参数：  pstream         [out]  流式加解密句柄
//        transformation  [in]   transformation: 加密库通常为字符串，格式为 "算法/小模式/padding"
//返回值：DRM_Err         错误码
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_stream_create(PCipherStream pstream, const char* transformation);


/***************************************************************************************************************************/
//说明：  初始化流式加解密。
//参数：  pcipher         [in/out]  加解密句柄
//        opmode          [in]   加解密模式，加密为OPMODE_ENCRYPT，解密为OPMODE_DECRYPT
//        key，Key_len    [in]   密钥信息
//        IV，IV_len      [in]   初始化向量
//        read_ptr        [in]   读文件函数指针
//        read_userp      [in]   读文件的文件句柄
//        write_ptr       [in]   写文件函数指针
//        write_userp     [in]   写文件的文件句柄
//返回值：DRM_Err         错误码
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_stream_init(PCipherStream pstream, int opmode, 
				   const unsigned char* key, int key_len,
				   const unsigned char* IV, 	int IV_len,
				   read_proc read_ptr, void* read_userp,
				   write_proc write_ptr, void* write_userp
				   );

/***************************************************************************************************************************/
//说明：  该函数内部调用read_ptr读取数据，加解密相应处理后调用write_ptr写入。
//参数：  pstream         [in/out]  流式加解密句柄
//返回值：DRM_Err         错误码
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_stream_transform(PCipherStream pstream);

/***************************************************************************************************************************/
//说明：  释放流式加解密句柄。
//参数：  pstream        [in]  流式加解密句柄
//返回值：DRM_Err        错误码
/***************************************************************************************************************************/
EBOOK_DRM_API DRM_Err
cipher_stream_destroy(PCipherStream pstream);

/*流式加解密调用说明
cipher_stream_xx函数需要数据进行加/解密时，从reader_ptr读取：reade_ptr(read_userp, data, data_to_read); 
函数cipher_stream_transform则同时还需要把加/解密后的数据写入：write_ptr(write_userp, data, data_to_write).
reade_proc和write_proc的返回值是实际读写的字节数。当read_ptr没有数据可读时，返回EOF(-1)。
read_userp和write_userp分别作为read_ptr和write_ptr的参数，比如对C库，调用方式为:
  int __cdecl read_file(FILE* fp, void* buf, int buf_len)
  {
      return fread(buf, 1, buf_len, fp);
  }

  int __cdecl write_file(FILE* fp, void* buf, int buf_len)
  {
      return fwrite(buf, 1, buf_len, fp);
  }
  
	CipherStream stream;
	cipher_stream_create(&stream, "1");
	FILE * fp_read, *fp_write;
	cipher_stream_init(stream, OPMODE_DECRYPT, "12345678", 8, read_file, fp_read, write_file, fp_write);
*/

#endif //DRMALGORITHM_H_INCLUDED
