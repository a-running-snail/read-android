/*
 *   MIRACL calculate pi - by Gauss-Legendre method
 *   mrpi.c
 *
 *   Copyright (c) 1988-2001 Shamus Software Ltd.
 */


#include "miracl.h"

#ifdef MR_FLASH  

void fpi(_MIPD_ flash pi)
{ /* Calculate pi using Guass-Legendre method */
    int x,nits,op[5];
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;

    MR_IN(53)

    if (mr_mip->pi!=NULL)
    {
        copy(mr_mip->pi,pi);
        mr_mip->EXACT=FALSE;
        MR_OUT
        return;
    }
    mr_mip->pi=mirvar(_MIPP_ 0);
    fconv(_MIPP_ 1,2,mr_mip->pi);
    froot(_MIPP_ mr_mip->pi,2,mr_mip->pi);
    fconv(_MIPP_ 1,1,mr_mip->w11);
    fconv(_MIPP_ 1,4,mr_mip->w12);
    x=1;
    op[0]=0x6C;
    op[1]=1;
    op[4]=0;
    nits=mr_mip->lg2b*mr_mip->nib/4;
    while (x<nits)
    {
        copy(mr_mip->w11,mr_mip->w13);
        op[2]=1;
        op[3]=2;
        flop(_MIPP_ mr_mip->w11,mr_mip->pi,op,mr_mip->w11);
        fmul(_MIPP_ mr_mip->pi,mr_mip->w13,mr_mip->pi);
        froot(_MIPP_ mr_mip->pi,2,mr_mip->pi);
        fsub(_MIPP_ mr_mip->w11,mr_mip->w13,mr_mip->w13);
        fmul(_MIPP_ mr_mip->w13,mr_mip->w13,mr_mip->w13);
        op[3]=1;
        op[2]=(-x);
        flop(_MIPP_ mr_mip->w12,mr_mip->w13,op,mr_mip->w12);  /* w12 = w12 - x.w13 */
        x*=2;
    }
    fadd(_MIPP_ mr_mip->w11,mr_mip->pi,mr_mip->pi);
    fmul(_MIPP_ mr_mip->pi,mr_mip->pi,mr_mip->pi);
    op[0]=0x48;
    op[2]=0;
    op[3]=4;
    flop(_MIPP_ mr_mip->pi,mr_mip->w12,op,mr_mip->pi);   /* pi = pi/(4.w12) */
    if (pi!=NULL) copy(mr_mip->pi,pi);   
    MR_OUT
}

#endif

