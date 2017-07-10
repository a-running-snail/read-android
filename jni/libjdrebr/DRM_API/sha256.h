#ifndef SHA256_H_INCLUDED
#define SHA256_H_INCLUDED

typedef unsigned char U8;

#ifdef __alpha__
typedef    unsigned int        U32;
#elif defined(__amd64__)
#include <inttypes.h>
typedef uint32_t U32;
#else
typedef unsigned int U32;
#endif


typedef struct 
{
	U32 state[8], length, curlen;
	unsigned char buf[64];
}
hash_state;

void sha_init(hash_state * md);
void sha_process(hash_state * md, unsigned char *buf, int len);
void sha_done(hash_state * md, unsigned char *hash);

#endif //SHA256_H_INCLUDED
