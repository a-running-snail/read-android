/*
 *   Module to implement Brickell et al's method for fast
 *   computation of g^x mod n, for fixed g and n, using precomputation. 
 *   This idea can be used to substantially speed up certain phases 
 *   of the Digital Signature Standard (DSS) for example.
 *
 *   See "Fast Exponentiation with Precomputation"
 *   by E. Brickell et al. in Proceedings Eurocrypt 1992
 *
 *   Copyright (c) 1988-1998 Shamus Software Ltd.
 */

#include <stdlib.h> 
#include "miracl.h"

BOOL brick_init(_MIPD_ brick *b,big g,big n,int nb)
{ /* Uses Montgomery arithmetic internally            *
   * g  is the fixed base for exponentiation          *
   * n  is the fixed modulus                          *
   * nb is the maximum number of bits in the exponent */

    int i,base,best,store,time;

#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (nb<2 || mr_mip->ERNUM) return FALSE;

    MR_IN(109)

    best=0;
    for (i=1,base=2;;base*=2,i++)
    { /* try to find best base as power of 2 */
        store=nb/i+1;
        time=store+base-3;  /* no floating point! */
        if (best==0 || time<best) best=time;
        else break; 
    }
    b->base=base;
    b->store=store;
    b->table=mr_alloc(_MIPP_ store,sizeof(big));
    if (b->table==NULL)
    {
        mr_berror(_MIPP_ MR_ERR_OUT_OF_MEMORY);   
        MR_OUT
        return FALSE;
    }

    b->n=mirvar(_MIPP_ 0);
    copy(n,b->n);
    prepare_monty(_MIPP_ n);

    b->table[0]=mirvar(_MIPP_ 0);
    nres(_MIPP_ g,b->table[0]);
    for (i=1;i<store;i++) 
    { /* calculate look-up table */
        b->table[i]=mirvar(_MIPP_ 0);
        convert(_MIPP_ base,mr_mip->w1);
        nres_powmod(_MIPP_ b->table[i-1],mr_mip->w1,b->table[i]);
    }
    MR_OUT
    return TRUE;
}

void brick_end(brick *b)
{
    int i;
    for (i=0;i<b->store;i++)
        mirkill(b->table[i]);
    mirkill(b->n);
    mr_free(b->table);  
}

void pow_brick(_MIPD_ brick *b,big e,big w)
{
    int i,ndig,d;
    int *digits;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif

    if (size(e)<0) mr_berror(_MIPP_ MR_ERR_NEG_POWER);

    MR_IN(110)

    digits=mr_alloc(_MIPP_ b->store,sizeof(int));
    if (digits==NULL)
    {
        mr_berror(_MIPP_ MR_ERR_OUT_OF_MEMORY);   
        MR_OUT
        return;
    }

    prepare_monty(_MIPP_ b->n);
    copy(e,w);
    for (ndig=0;size(w)>0;ndig++)
    { /* break up exponent into digits, using 'base' */
      /* (note base is a power of 2.) This is fast.  */
        digits[ndig]=subdiv(_MIPP_ w,b->base,w);
    }

    if (ndig>b->store)
    {
        mr_free(digits);
        mr_berror(_MIPP_ MR_ERR_EXP_TOO_BIG);
        MR_OUT
        return;
    }
   
    convert(_MIPP_ 1,mr_mip->w1);
    nres(_MIPP_ mr_mip->w1,mr_mip->w1);
    convert(_MIPP_ 1,w);
    nres(_MIPP_ w,w);
    for (d=b->base-1;d>0;d--)
    { /* brickell's method */
        for (i=0;i<ndig;i++)
        {
            if (mr_mip->user!=NULL) (*mr_mip->user)();

            if (digits[i]==d) nres_modmult(_MIPP_ mr_mip->w1,b->table[i],mr_mip->w1);
        }
        nres_modmult(_MIPP_ w,mr_mip->w1,w);
    }
    redc(_MIPP_ w,w);
    for (i=0;i<ndig;i++) digits[i]=0;
    mr_free(digits);
    MR_OUT
}

