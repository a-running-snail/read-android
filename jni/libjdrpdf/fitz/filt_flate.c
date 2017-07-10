#include "fitz-internal.h"

#include <zlib.h>

typedef struct fz_flate_s fz_flate;

struct fz_flate_s
{
	fz_stream *chain;
	z_stream z;
};

static void *zalloc(void *opaque, unsigned int items, unsigned int size)
{
	return fz_malloc_array_no_throw(opaque, items, size);
}

static void zfree(void *opaque, void *ptr)
{
	fz_free(opaque, ptr);
}

static int
read_flated(fz_stream *stm, unsigned char *outbuf, int outlen)
{
	fz_flate *state = stm->state;
	fz_stream *chain = state->chain;
	z_streamp zp = &state->z;
	int code;

	zp->next_out = outbuf;
	zp->avail_out = outlen;

	while (zp->avail_out > 0)
	{
		if (chain->rp == chain->wp)
			fz_fill_buffer(chain);

		zp->next_in = chain->rp;
		zp->avail_in = chain->wp - chain->rp;

		code = inflate(zp, Z_SYNC_FLUSH);

		chain->rp = chain->wp - zp->avail_in;

		if (code == Z_STREAM_END)
		{
			return outlen - zp->avail_out;
		}
		else if (code == Z_BUF_ERROR)
		{
			fz_warn(stm->ctx, "premature end of data in flate filter");
			return outlen - zp->avail_out;
		}
		else if (code == Z_DATA_ERROR && zp->avail_in == 0)
		{
			fz_warn(stm->ctx, "ignoring zlib error: %s", zp->msg);
			return outlen - zp->avail_out;
		}
		else if (code != Z_OK)
		{
			fz_throw(stm->ctx, "zlib error: %s", zp->msg);
		}
	}

	return outlen - zp->avail_out;
}

static void
close_flated(fz_context *ctx, void *state_)
{
	fz_flate *state = (fz_flate *)state_;
	int code;

	code = inflateEnd(&state->z);
	if (code != Z_OK)
		fz_warn(ctx, "zlib error: inflateEnd: %s", state->z.msg);

	fz_close(state->chain);
	fz_free(ctx, state);
}

fz_stream *
fz_open_flated(fz_stream *chain)
{
	fz_flate *state = NULL;
	int code = Z_OK;
	fz_context *ctx = chain->ctx;

	fz_var(code);
	fz_var(state);

	fz_try(ctx)
	{
		state = fz_malloc_struct(ctx, fz_flate);
		state->chain = chain;

		state->z.zalloc = zalloc;
		state->z.zfree = zfree;
		state->z.opaque = ctx;
		state->z.next_in = NULL;
		state->z.avail_in = 0;

		code = inflateInit(&state->z);
		if (code != Z_OK)
			fz_throw(ctx, "zlib error: inflateInit: %s", state->z.msg);
	}
	fz_catch(ctx)
	{
		if (state && code == Z_OK)
			inflateEnd(&state->z);
		fz_free(ctx, state);
		fz_close(chain);
		fz_rethrow(ctx);
	}
	return fz_new_stream(ctx, state, read_flated, close_flated);
}

// 定义返回值
#define ERR_SUCCESS                     0   // 成功
#define ERR_PARAMETER_INVALID           1   // 输入参数错误
#define ERR_MEMORY_ALLOCATION           2   // 内存分配错误
#define ERR_FILE_RW                     3   // 文件读写错误
#define ERR_ENCRYPT_DECRYPT             4   // 加解密错误
#define ERR_VERSION_INVALID             5   // 版本错误
#define ERR_OTHER                      (-1) // 其他错误

#define ENCRYPT 0
#define DECRYPT 1

typedef void *      Cipher;
typedef Cipher *    PCipher;

extern int CreateCipher(PCipher pcipher);
extern int InitCipher(Cipher cipher, int opmode, const unsigned char* key, int key_len);
extern int UpdateCipher(Cipher cipher, const unsigned char* in_data, int in_data_len,
                        unsigned char* out_data,  int* out_data_len);
extern int FinalCipher(Cipher cipher,const unsigned char* in_data, int in_data_len,
                       unsigned char* out_data, int* out_data_len);
extern int DestroyCipher(Cipher cipher);

#define BLOCK_SIZE 16
#define FINAL_BLOCK_SIZE 2*BLOCK_SIZE

typedef struct fz_jddrm_s fz_jddrm;

struct fz_jddrm_s
{
    fz_stream *chain;
    Cipher cipher;
};

int read_encrypted(fz_stream *stm, unsigned char *buf, int len)
{
    fz_jddrm *state = stm->state;
    fz_stream *chain = state->chain;
    Cipher cipher = state->cipher;
    int code;
    
    unsigned char* pbuf = buf;
    int outlen = 0;
    int avail_out = len;
    
    if (chain->error || chain->eof)
        return 0;
    
    while (avail_out > 0)
    {
        outlen = 0;
        
        if (chain->rp == chain->wp)
            fz_fill_buffer(chain);
        
        if (chain->eof && chain->rp == chain->wp)
        {
            unsigned char pTemp[FINAL_BLOCK_SIZE];
            memset(pTemp, 0, FINAL_BLOCK_SIZE);
            code = FinalCipher(cipher, NULL, 0, pTemp, &outlen);
            if (ERR_SUCCESS != code)
                fz_warn(chain->ctx, "jddrm error: FinalCipher : %d", code);
            memcpy(pbuf, pTemp, outlen);
            pbuf += outlen;
            avail_out -= outlen;
            return len - avail_out;
        }
        
        if (chain->wp - chain->rp < avail_out)
        {
            code = UpdateCipher(cipher,  (const unsigned char*) chain->rp, chain->wp-chain->rp, pbuf, &outlen);
            chain->rp = chain->wp;
        }
        else
        {
            code = UpdateCipher(cipher, (const unsigned char*) chain->rp, avail_out, pbuf, &outlen);
            chain->rp += avail_out;
        }
        if (ERR_SUCCESS != code)
            fz_warn(stm->ctx, "jddrm error: UpdateCipher : %d", code);
        pbuf += outlen;
        avail_out -= outlen;
        continue;
    }
    
    return len;
}

void close_encrypted(fz_context *ctx, void *state_)
{
    fz_jddrm *state = (fz_jddrm *)state_;
    int code;
    
    code = DestroyCipher(state->cipher);
    if (ERR_SUCCESS != code)
        fz_warn(ctx, "jddrm error: DestroyCipher: %d", code);
    
    fz_close(state->chain);
    fz_free(ctx, state);
}

fz_stream * fz_open_jddrm(fz_stream *chain, unsigned char *key, int keylen)
{
    fz_jddrm *state = NULL;
    int code = ERR_SUCCESS;
    fz_context *ctx = chain->ctx;
    
    fz_try (ctx)
    {
        state = fz_malloc_struct(ctx, fz_jddrm);
        state->chain = chain;
        code = CreateCipher(&(state->cipher));
        if (ERR_SUCCESS != code)
            fz_warn(ctx, "jddrm error: CreateCipher: %d", code);
        code = InitCipher(state->cipher, DECRYPT, key, keylen);
        if (ERR_SUCCESS != code)
            fz_warn(ctx, "jddrm error: InitCipher: %d", code);
    }
    fz_catch (ctx)
    {
        if (state)
            DestroyCipher(state->cipher);
        fz_free(ctx, state);
        fz_close(chain);
        fz_rethrow(ctx);
    }
    
    return fz_new_stream(ctx, state, read_encrypted, close_encrypted);
}