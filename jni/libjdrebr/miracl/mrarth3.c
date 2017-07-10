/*
 *   MIRACL arithmetic routines 3 - simple powers and roots
 *   mrarth3.c
 *
 *   Copyright (c) 1988-2003 Shamus Software Ltd.
 */

#include <stdlib.h> 
#include "miracl.h"
#define mr_abs(x)  ((x)<0? (-(x)) : (x))

//int logb2(_MIPD_ big x)
//{ /* returns number of bits in x */
//    int xl,lg2;
//    mr_small top;
//#ifdef MR_OS_THREADS
//    miracl *mr_mip=get_mip();
//#endif
//    if (mr_mip->ERNUM || size(x)==0) return 0;
//
//    MR_IN(49)
//
//
//#ifndef MR_ALWAYS_BINARY
//    if (mr_mip->base==mr_mip->base2)
//    {
//#endif
//        xl=(int)(x->len&MR_OBITS);
//        lg2=mr_mip->lg2b*(xl-1);
//        top=x->w[xl-1];
//        while (top>=1)
//        {
//            lg2++;
//            top/=2;
//        }
//
//#ifndef MR_ALWAYS_BINARY
//    }
//    else 
//    {
//        copy(x,mr_mip->w0);
//        insign(PLUS,mr_mip->w0);
//        lg2=0;
//        while (mr_mip->w0->len>1)
//        {
//#ifdef MR_FP_ROUNDING
//            mr_sdiv(_MIPP_ mr_mip->w0,mr_mip->base2,mr_invert(mr_mip->base2),mr_mip->w0);
//#else
//            mr_sdiv(_MIPP_ mr_mip->w0,mr_mip->base2,mr_mip->w0);
//#endif
//            lg2+=mr_mip->lg2b;
//        }
//
//        while (mr_mip->w0->w[0]>=1)
//        {
//            lg2++;
//            mr_mip->w0->w[0]/=2;
//        }
//    }
//#endif
//    MR_OUT
//    return lg2;
//}

void expint(_MIPD_ int b,int n,big x)
{ /* sets x=b^n */
    int i,r,p;
    unsigned int bit,un;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;
    convert(_MIPP_ 1,x);
    if (n==0) return;

    MR_IN(50)

    if (n<0)
    {
        mr_berror(_MIPP_ MR_ERR_NEG_POWER);
        MR_OUT
        return;
    }
    if (b==2)
    {
        r=n/mr_mip->lg2b;
        p=n%mr_mip->lg2b;

#ifndef MR_ALWAYS_BINARY
        if (mr_mip->base==mr_mip->base2)
        {
#endif
            mr_shift(_MIPP_ x,r,x);
            x->w[x->len-1]=mr_shiftbits(x->w[x->len-1],p);
#ifndef MR_ALWAYS_BINARY
        }
        else
        {
            for (i=1;i<=r;i++)
                mr_pmul(_MIPP_ x,mr_mip->base2,x);
            mr_pmul(_MIPP_ x,mr_shiftbits((mr_small)1,p),x);
        }
#endif
    }
    else
    {
        bit=1;
        un=(unsigned int)n;
        while (un>=bit) bit<<=1;
        bit>>=1;
        while (bit>0)
        { /* ltr method */
            multiply(_MIPP_ x,x,x);
            if ((bit&un)!=0) premult(_MIPP_ x,b,x);
            bit>>=1;
        }
    }
    MR_OUT
}   

//void sftbit(_MIPD_ big x,int n,big z)
//{ /* shift x by n bits */
//    int m;
//    mr_small sm;
//#ifdef MR_OS_THREADS
//    miracl *mr_mip=get_mip();
//#endif
//    if (mr_mip->ERNUM) return;
//    copy(x,z);
//    if (n==0) return;
//
//    MR_IN(47)
//
//    m=mr_abs(n);
//    sm=mr_shiftbits((mr_small)1,m%mr_mip->lg2b);
//    if (n>0)
//    { /* shift left */
//
//#ifndef MR_ALWAYS_BINARY
//        if (mr_mip->base==mr_mip->base2)
//        {
//#endif
//            mr_shift(_MIPP_ z,n/mr_mip->lg2b,z);
//            mr_pmul(_MIPP_ z,sm,z);
//#ifndef MR_ALWAYS_BINARY
//        }
//        else
//        {
//            expint(_MIPP_ 2,m,mr_mip->w1);
//            multiply(_MIPP_ z,mr_mip->w1,z);
//        }
//#endif
//    }
//    else
//    { /* shift right */
//
//#ifndef MR_ALWAYS_BINARY
//        if (mr_mip->base==mr_mip->base2)
//        {
//#endif
//            mr_shift(_MIPP_ z,n/mr_mip->lg2b,z);
//#ifdef MR_FP_ROUNDING
//            mr_sdiv(_MIPP_ z,sm,mr_invert(sm),z);
//#else
//            mr_sdiv(_MIPP_ z,sm,z);
//#endif
//
//#ifndef MR_ALWAYS_BINARY
//        }
//        else
//        {
//            expint(_MIPP_ 2,m,mr_mip->w1);
//            divide(_MIPP_ z,mr_mip->w1,z);
//        }
//#endif
//    }
//    MR_OUT
//}

//void power(_MIPD_ big x,long n,big z,big w)
//{ /* raise big number to int power  w=x^n *
//   * (mod z if z and w distinct)          */
//    mr_small norm;
//#ifdef MR_OS_THREADS
//    miracl *mr_mip=get_mip();
//#endif
//    copy(x,mr_mip->w5);
//    zero(w);
//    if(mr_mip->ERNUM || size(mr_mip->w5)==0) return;
//    convert(_MIPP_ 1,w);
//    if (n==0L) return;
//
//    MR_IN(17)
//
//    if (n<0L)
//    {
//        mr_berror(_MIPP_ MR_ERR_NEG_POWER);
//        MR_OUT
//        return;
//    }
//    if (w==z) forever
//    { /* "Russian peasant" exponentiation */
//        if (n%2!=0L) 
//             multiply(_MIPP_ w,mr_mip->w5,w);
//        n/=2L;
//        if (mr_mip->ERNUM || n==0L) break;
//        multiply(_MIPP_ mr_mip->w5,mr_mip->w5,mr_mip->w5);
//    }
//    else
//    { 
//        norm=normalise(_MIPP_ z,z);
//        divide(_MIPP_ mr_mip->w5,z,z);
//        forever
//        {
//            if (mr_mip->user!=NULL) (*mr_mip->user)();
//
//            if (n%2!=0L) mad(_MIPP_ w,mr_mip->w5,mr_mip->w5,z,z,w);
//            n/=2L;
//            if (mr_mip->ERNUM || n==0L) break;
//            mad(_MIPP_ mr_mip->w5,mr_mip->w5,mr_mip->w5,z,z,mr_mip->w5);
//        }
//        if (norm!=1)
//        {
//#ifdef MR_FP_ROUNDING
//            mr_sdiv(_MIPP_ z,norm,mr_invert(norm),z);
//#else
//            mr_sdiv(_MIPP_ z,norm,z);
//#endif
//            divide(_MIPP_ w,z,z);
//        }
//    }
//    MR_OUT
//}

//BOOL nroot(_MIPD_ big x,int n,big w)
//{  /*  extract  lower approximation to nth root   *
//    *  w=x^(1/n) returns TRUE for exact root      *
//    *  uses Newtons method                        */
//    int sx,dif,s,p,d,lg2,lgx,rem;
//    BOOL full;
//#ifdef MR_OS_THREADS
//    miracl *mr_mip=get_mip();
//#endif
//    if (mr_mip->ERNUM) return FALSE;
//    if (size(x)==0 || n==1)
//    {
//        copy(x,w);
//        return TRUE;
//    }
//
//    MR_IN(16)
//
//    if (n<1) mr_berror(_MIPP_ MR_ERR_BAD_ROOT);
//    sx=exsign(x);
//    if (n%2==0 && sx==MINUS) mr_berror(_MIPP_ MR_ERR_NEG_ROOT);
//    if (mr_mip->ERNUM) 
//    {
//        MR_OUT
//        return FALSE;
//    }
//    insign(PLUS,x);
//    lgx=logb2(_MIPP_ x);
//    if (n>=lgx)
//    { /* root must be 1 */
//        insign(sx,x);
//        convert(_MIPP_ sx,w);
//        MR_OUT
//        if (lgx==1) return TRUE;
//        else        return FALSE;
//    }
//    expint(_MIPP_ 2,1+(lgx-1)/n,mr_mip->w2);           /* guess root as 2^(log2(x)/n) */
//    s=(-(((int)x->len-1)/n)*n);
//    mr_shift(_MIPP_ mr_mip->w2,s/n,mr_mip->w2);
//    lg2=logb2(_MIPP_ mr_mip->w2)-1;
//    full=FALSE;
//    if (s==0) full=TRUE;
//    d=0;
//    p=1;
//    while (!mr_mip->ERNUM)
//    { /* Newtons method */
//        copy(mr_mip->w2,mr_mip->w3);
//        mr_shift(_MIPP_ x,s,mr_mip->w4);
//        mr_mip->check=OFF;
//        power(_MIPP_ mr_mip->w2,n-1,mr_mip->w6,mr_mip->w6);
//        mr_mip->check=ON;
//        divide(_MIPP_ mr_mip->w4,mr_mip->w6,mr_mip->w2);
//        rem=size(mr_mip->w4);
//        subtract(_MIPP_ mr_mip->w2,mr_mip->w3,mr_mip->w2);
//        dif=size(mr_mip->w2);
//        subdiv(_MIPP_ mr_mip->w2,n,mr_mip->w2);
//        add(_MIPP_ mr_mip->w2,mr_mip->w3,mr_mip->w2);
//        p*=2;
//        if(p<lg2+d*mr_mip->lg2b) continue;
//        if (full && mr_abs(dif)<n)
//        { /* test for finished */
//            while (dif<0)
//            {
//                rem=0;
//                decr(_MIPP_ mr_mip->w2,1,mr_mip->w2);
//                mr_mip->check=OFF;
//                power(_MIPP_ mr_mip->w2,n,mr_mip->w6,mr_mip->w6);
//                mr_mip->check=ON;
//                dif=compare(x,mr_mip->w6);
//            }
//            copy(mr_mip->w2,w);
//            insign(sx,w);
//            insign(sx,x);
//            MR_OUT
//            if (rem==0 && dif==0) return TRUE;
//            else                  return FALSE;
//        }
//        else
//        { /* adjust precision */
//            d*=2;
//            if (d==0) d=1;
//            s+=d*n;
//            if (s>=0)
//            {
//                d-=s/n;
//                s=0;
//                full=TRUE;
//            }
//            mr_shift(_MIPP_ mr_mip->w2,d,mr_mip->w2);
//        }
//        p/=2;
//    }
//    MR_OUT
//    return FALSE;
//}

