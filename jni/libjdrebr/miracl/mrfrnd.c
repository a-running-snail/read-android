/*
 *   MIRACL flash random number routine 
 *   mrfrnd.c
 *
 *   Copyright (c) 1988-1997 Shamus Software Ltd.
 */

#include "miracl.h"

#ifdef MR_FLASH

void frand(_MIPD_ flash x)
{ /* generates random flash number 0<x<1 */
    int i;
#ifdef MR_FP
    mr_small dres;
#endif
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;

    MR_IN(46)

    zero(mr_mip->w6);
    mr_mip->w6->len=mr_mip->nib;
    for (i=0;i<mr_mip->nib;i++) 
    { /* generate a full width random number */
        if (mr_mip->base==0) mr_mip->w6->w[i]=brand(_MIPPO_ );
        else                 mr_mip->w6->w[i]=MR_REMAIN(brand(_MIPPO_ ),mr_mip->base);
    }
    mr_mip->check=OFF;
    bigrand(_MIPP_ mr_mip->w6,mr_mip->w5);
    mr_mip->check=ON;
    mround(_MIPP_ mr_mip->w5,mr_mip->w6,x);

    MR_OUT
}

#endif

