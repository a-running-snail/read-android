/*
 *   Module to implement Brickell et al's method for fast
 *   computation of x*G mod n, for fixed G and n, using precomputation. 
 *
 *   Elliptic curve version of mrbrick.c
 *
 *   This idea can be used to substantially speed up certain phases 
 *   of the Digital Signature Standard (ECS) for example.
 *
 *   See "Fast Exponentiation with Precomputation"
 *   by E. Brickell et al. in Proceedings Eurocrypt 1992
 *
 *   Copyright (c) 1988-1999 Shamus Software Ltd.
 */

#include <stdlib.h> 
#include "miracl.h"

BOOL ebrick_init(_MIPD_ ebrick *B,big x,big y,big a,big b,big n,int nb)
{ /* Uses Montgomery arithmetic internally              *
   * (x,y) is the fixed base                            *
   * a,b and n are parameters and modulus of the curve  *
   * nb is the maximum number of bits in the multiplier */
    int i,base,best,store,time;
    epoint *w;

#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (nb<2 || mr_mip->ERNUM) return FALSE;

    MR_IN(115)

    best=0;
    for (i=1,base=2;;base*=2,i++)
    { /* try to find best base as power of 2 */
        store=nb/i+1;
        time=store+base-3;  /* no floating point! */
        if (best==0 || time<best) best=time;
        else break; 
    }
    B->base=base;
    B->store=store;
    B->table=mr_alloc(_MIPP_ store,sizeof(epoint *));
    if (B->table==NULL)
    {
        mr_berror(_MIPP_ MR_ERR_OUT_OF_MEMORY);   
        MR_OUT
        return FALSE;
    }
    B->a=mirvar(_MIPP_ 0);
    B->b=mirvar(_MIPP_ 0);
    B->n=mirvar(_MIPP_ 0);
    copy(a,B->a);
    copy(b,B->b);
    copy(n,B->n);

    ecurve_init(_MIPP_ a,b,n,MR_AFFINE);
    w=epoint_init(_MIPPO_ );
    B->table[0]=epoint_init(_MIPPO_ );
    epoint_set(_MIPP_ x,y,0,B->table[0]);

    for (i=1;i<store;i++) 
    { /* calculate look-up table */
        B->table[i]=epoint_init(_MIPPO_ );
        convert(_MIPP_ base,mr_mip->w1);
        ecurve_mult(_MIPP_ mr_mip->w1,B->table[i-1],w);
        epoint_copy(w,B->table[i]);
    }
    epoint_free(w);
    MR_OUT
    return TRUE;
}

void ebrick_end(ebrick *B)
{
    int i;
    for (i=0;i<B->store;i++)
        epoint_free(B->table[i]);
    mirkill(B->n);
    mirkill(B->b);
    mirkill(B->a);
    mr_free(B->table);  
}

int mul_brick(_MIPD_ ebrick *B,big e,big x,big y)
{
    int i,ndig,d;
    int *digits;
    epoint *w,*w1;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif

    if (size(e)<0) mr_berror(_MIPP_ MR_ERR_NEG_POWER);

    MR_IN(116)

    digits=mr_alloc(_MIPP_ B->store,sizeof(int));
    if (digits==NULL)
    {
        mr_berror(_MIPP_ MR_ERR_OUT_OF_MEMORY);
        MR_OUT
        return 0;        
    }

    ecurve_init(_MIPP_ B->a,B->b,B->n,MR_PROJECTIVE);

    copy(e,mr_mip->w1);
    for (ndig=0;size(mr_mip->w1)>0;ndig++)
    { /* break up exponent into digits, using 'base' */
      /* (note base is a power of 2.) This is fast.  */
        digits[ndig]=subdiv(_MIPP_ mr_mip->w1,B->base,mr_mip->w1);
    }

    if (ndig>B->store)
    {
        mr_free(digits);
        mr_berror(_MIPP_ MR_ERR_EXP_TOO_BIG);
        MR_OUT
        return 0;
    }

    w=epoint_init(_MIPPO_ );
    w1=epoint_init(_MIPPO_ );

    for (d=B->base-1;d>0;d--)
    { /* brickell's method */
        for (i=0;i<ndig;i++)
        {
            if (mr_mip->user!=NULL) (*mr_mip->user)();
            if (digits[i]==d) ecurve_add(_MIPP_ B->table[i],w1);
        }
        ecurve_add(_MIPP_ w1,w);
    }
    d=epoint_get(_MIPP_ w,x,y);
    epoint_free(w1);
    epoint_free(w);

    for (i=0;i<ndig;i++) digits[i]=0;
    mr_free(digits);
    MR_OUT
    return d;
}

