/*
 *   MIRACL cryptographic strong random number generator 
 *   mrstrong.c
 *
 *   Copyright (c) 1988-2002 Shamus Software Ltd.
 *
 *   Unguessable seed -> SHA -> PRNG internal state -> SHA -> random numbers
 *   Slow - but secure
 *
 *   See ftp://ftp.rsasecurity.com/pub/pdfs/bull-1.pdf for a justification
 */

#include "miracl.h"

static mr_unsign32 sbrand(csprng *rng)
{ /* Marsaglia & Zaman random number generator */
    int i,k;
    mr_unsign32 pdiff,t;
    rng->rndptr++;
    if (rng->rndptr<NK) return rng->ira[rng->rndptr];
    rng->rndptr=0;
    for (i=0,k=NK-NJ;i<NK;i++,k++)
    { /* calculate next NK values */
        if (k==NK) k=0;
        t=rng->ira[k];
        pdiff=t - rng->ira[i] - rng->borrow;
        if (pdiff<t) rng->borrow=0;
        if (pdiff>t) rng->borrow=1;
        rng->ira[i]=pdiff; 
    }
    return rng->ira[0];
}

static void sirand(csprng* rng,mr_unsign32 seed)
{ /* initialise random number system */
  /* modified so that a subsequent call "stirs" in another seed value */
  /* in this way as many seed bits as desired may be used */
    int i,in;
    mr_unsign32 t,m=1L;
    rng->borrow=0L;
    rng->rndptr=0;
    rng->ira[0]^=seed;
    for (i=1;i<NK;i++)
    { /* fill initialisation vector */
        in=(NV*i)%NK;
        rng->ira[in]^=m;      /* note XOR */
        t=m;
        m=seed-m;
        seed=t;
    }
    for (i=0;i<10000;i++) sbrand(rng ); /* "warm-up" & stir the generator */
}

static void fill_pool(csprng *rng)
{ /* hash down output of RNG to re-fill the pool */
    int i;
    sha sh;
    shs_init(&sh);
    for (i=0;i<128;i++) shs_process(&sh,sbrand(rng));
    shs_hash(&sh,rng->pool);
    rng->pool_ptr=0;
}

void strong_init(csprng *rng,int rawlen,char *raw,mr_unsign32 tod)
{ /* initialise from at least 128 byte string of raw  *
   * random (keyboard?) input, and 32-bit time-of-day */
    int i;
    mr_unsign32 hash[MR_HASH_BYTES/4];
    sha sh;
    rng->pool_ptr=0;
    for (i=0;i<NK;i++) rng->ira[i]=0;
    if (rawlen>0)
    {
        shs_init(&sh);
        for (i=0;i<rawlen;i++)
            shs_process(&sh,raw[i]);
        shs_hash(&sh,(char *)hash);

/* initialise PRNG from distilled randomness */

        for (i=0;i<MR_HASH_BYTES/4;i++) sirand(rng,hash[i]);
    }
    sirand(rng,tod);

    fill_pool(rng);
}

void strong_kill(csprng *rng)
{ /* kill internal state */
    int i;
    rng->pool_ptr=rng->rndptr=0;
    for (i=0;i<MR_HASH_BYTES;i++) rng->pool[i]=0;
    for (i=0;i<NK;i++) rng->ira[i]=0;
    rng->borrow=0;
}

/* get random byte */

int strong_rng(csprng *rng)
{ 
    int r;
    r=rng->pool[rng->pool_ptr++];
    if (rng->pool_ptr>=MR_HASH_BYTES) fill_pool(rng);
    return r;
}

void strong_bigrand(_MIPD_ csprng *rng,big w,big x)
{
    unsigned int ran;
    unsigned int ch;

#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;
    MR_IN(139)
    zero(mr_mip->w1);
    do
    {
        if (mr_mip->ERNUM) break;
        ch=(unsigned char)strong_rng(rng);
        ran=ch; 
        premult(_MIPP_ mr_mip->w1,256,mr_mip->w1);
        incr(_MIPP_ mr_mip->w1,(int)ran,mr_mip->w1);
    } while (compare(mr_mip->w1,w)<0);
    divide(_MIPP_ mr_mip->w1,w,w);
    copy(mr_mip->w1,x);
    MR_OUT
}

void strong_bigdig(_MIPD_ csprng *rng,int n,int b,big x)
{ /* generate random number n digits long *
   * to "printable" base b                */
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;

    MR_IN(19)

    if (b<2 || b>256)
    {
        mr_berror(_MIPP_ MR_ERR_BASE_TOO_BIG);
        MR_OUT
        return;
    }

    do
    { /* repeat if x too small */
        expint(_MIPP_ b,n,mr_mip->w2);
        strong_bigrand(_MIPP_ rng,mr_mip->w2,x);
        subdiv(_MIPP_ mr_mip->w2,b,mr_mip->w2);
    } while (!mr_mip->ERNUM && compare(x,mr_mip->w2)<0);

    MR_OUT
}

/* test main program 

#include <stdio.h>
#include "miracl.h"
#include <time.h>

void main()
{
    int i;
    char raw[256],bytes[25];
    big x,w;
    long seed;
    csprng rng;
    miracl *mip=mirsys(200,256);
    x=mirvar(0);
    w=mirvar(0);
    printf("Enter Raw random string= ");
    scanf("%s",raw);
    getchar();
    time(&seed);
    strong_init(&rng,strlen(raw),raw,seed);
    mip->IOBASE=16;
    expint(2,256,w);
    cotnum(w,stdout);
    for (i=0;i<20;i++)
    {
        strong_bigrand(&rng,w,x);
        cotnum(x,stdout);
    }
    printf("\n");
    for (i=0;i<20;i++)
    {
        strong_bigdig(&rng,128,2,x);
        cotnum(x,stdout);
    }
}

*/

