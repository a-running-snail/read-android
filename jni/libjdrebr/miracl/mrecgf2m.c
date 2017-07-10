/*
 *   MIRACL routines for arithmetic over GF(2^m), and 
 *   implementation of Elliptic Curve Cryptography over GF(2^m) 
 *   mrecgf2m.c
 *
 *   Curve equation is Y^2 + XY = X^3 + A.X^2 + B
 *   where A is 0 or 1
 *
 *   For algorithms used, see IEEE P1363 Standard, Appendix A
 *   unless otherwise stated.
 * 
 *   The time-critical routines are the multiplication routine multiply2()
 *   and (for AFFINE co-ordinates), the modular inverse routine inverse2() 
 *   and the routines it calls.
 *
 *   No assembly language used.
 *
 *   Copyright (c) 2000-2001 Shamus Software Ltd.
 */

#include <stdlib.h> 
#include "miracl.h"

#ifndef MR_NOFULLWIDTH
                     /* This does not make sense using floating-point! */

#define M1 (MIRACL-1)
#define M2 (MIRACL-2)
#define M3 (MIRACL-3)
#define TOPBIT ((mr_small)1<<M1)
#define SECBIT ((mr_small)1<<M2)
#define THDBIT ((mr_small)1<<M3)
#define M8 (MIRACL-8)

#define KARATSUBA 2

/* This is extremely time-critical, and expensive */

/*
#include <emmintrin.h>

static mr_small mr_mul2(mr_small a,mr_small b,mr_small *r)
{
    __m64 tt[4],xg,rg;
    mr_small q;

    tt[0]=_m_from_int(0);
    tt[1]=_m_from_int(a);
    tt[2]=_m_psllqi(tt[1],1);
    tt[3]=_m_pxor(tt[1],tt[2]);

    rg=tt[b&3]; 
    xg=tt[(b>>2)&3]; rg=_m_pxor(rg,_m_psllqi(xg,2));
    xg=tt[(b>>4)&3]; rg=_m_pxor(rg,_m_psllqi(xg,4));
    xg=tt[(b>>6)&3]; rg=_m_pxor(rg,_m_psllqi(xg,6));
    xg=tt[(b>>8)&3]; rg=_m_pxor(rg,_m_psllqi(xg,8));
    xg=tt[(b>>10)&3]; rg=_m_pxor(rg,_m_psllqi(xg,10));
    xg=tt[(b>>12)&3]; rg=_m_pxor(rg,_m_psllqi(xg,12));
    xg=tt[(b>>14)&3]; rg=_m_pxor(rg,_m_psllqi(xg,14));
    xg=tt[(b>>16)&3]; rg=_m_pxor(rg,_m_psllqi(xg,16));
    xg=tt[(b>>18)&3]; rg=_m_pxor(rg,_m_psllqi(xg,18));
    xg=tt[(b>>20)&3]; rg=_m_pxor(rg,_m_psllqi(xg,20));
    xg=tt[(b>>22)&3]; rg=_m_pxor(rg,_m_psllqi(xg,22));
    xg=tt[(b>>24)&3]; rg=_m_pxor(rg,_m_psllqi(xg,24));
    xg=tt[(b>>26)&3]; rg=_m_pxor(rg,_m_psllqi(xg,26));
    xg=tt[(b>>28)&3]; rg=_m_pxor(rg,_m_psllqi(xg,28));
    xg=tt[(b>>30)]; rg=_m_pxor(rg,_m_psllqi(xg,30));

    *r=_m_to_int(rg);
    q=_m_to_int(_m_psrlqi(rg,32));

    _m_empty();

    return q;
}

*/

/* wouldn't it be nice if instruction sets supported a 
   one cycle "carry-free" multiplication instruction ... */

static mr_small mr_mul2(mr_small a,mr_small b,mr_small *r)
{
    int k;
    mr_small kb,t[16];
    mr_small x,q,p;
    mr_utype tb0,tb1,tb2;


    q=p=(mr_small)0;

/* an unpredictable branch - how stupid of me! 

    if (b<a) {x=a; a=b; b=x;}  

    if (a<16)
    { 
        t[0]=0; t[1]=b;
        p=t[a&1];
        a>>=1;
        x=t[a&1];
        p^=x<<1;
        q^=x>>M1;
        a>>=1;
        x=t[a&1];
        p^=x<<2;
        q^=x>>M2;
        a>>=1;
        x=t[a&1];
        p^=x<<3;
        q^=x>>M3;
        *r=p;
        return q;
    }
*/
    kb=b;

#if MIRACL <= 32
    t[0]=0;               /* small look up table */
    t[3]=t[2]=a<<1;       /* it can overflow.... */
    t[1]=t[2]>>1;
    t[3]^=t[1];

    tb0=(a&TOPBIT);       /* remember top bit    */
    tb0>>=M1;             /* all ones if top bit is one */
#else
    t[0]=0;               /* larger look-up table */
    t[8]=a<<3;
    t[4]=t[8]>>1;
    t[2]=t[4]>>1;
    t[1]=t[2]>>1;
    t[3]=t[5]=t[7]=t[9]=t[11]=t[13]=t[15]=t[1];
    t[3]^=t[2];
    t[5]^=t[4];
    t[9]^=t[8]; 
    t[6]=t[3]<<1;
    t[7]^=t[6];
    t[10]=t[5]<<1;
    t[11]^=t[10];
    t[12]=t[6]<<1;
    t[13]^=t[12];
    t[14]=t[7]<<1;
    t[15]^=t[14];

    tb0=(a&TOPBIT);       /* remember top bits  */
    tb0>>=M1;             /* all bits one, if this bit is set in a */
    tb1=(a&SECBIT)<<1;      
    tb1>>=M1;
    tb2=(a&THDBIT)<<2;
    tb2>>=M1;
#endif

#if MIRACL == 16
#define UNWOUNDM
    p=q=t[b&3];                       q>>=2;
    x=t[(b>>2)&3];  q^=x; p^=(x<<2);  q>>=2;   
    x=t[(b>>4)&3];  q^=x; p^=(x<<4);  q>>=2;   
    x=t[(b>>6)&3];  q^=x; p^=(x<<6);  q>>=2;
    x=t[(b>>8)&3];  q^=x; p^=(x<<8);  q>>=2;
    x=t[(b>>10)&3]; q^=x; p^=(x<<10); q>>=2;
    x=t[(b>>12)&3]; q^=x; p^=(x<<12); q>>=2;
    x=t[(b>>14)];   q^=x; p^=(x<<14); q>>=2;
#endif

#if MIRACL == 32
#define UNWOUNDM
    p=q=t[b&3];                       q>>=2;
    x=t[(b>>2)&3];  q^=x; p^=(x<<2);  q>>=2;   /* 8 ASM 80386 instructions */
    x=t[(b>>4)&3];  q^=x; p^=(x<<4);  q>>=2;   /* but only 4 ARM instructions! */
    x=t[(b>>6)&3];  q^=x; p^=(x<<6);  q>>=2;
    x=t[(b>>8)&3];  q^=x; p^=(x<<8);  q>>=2;
    x=t[(b>>10)&3]; q^=x; p^=(x<<10); q>>=2;
    x=t[(b>>12)&3]; q^=x; p^=(x<<12); q>>=2;
    x=t[(b>>14)&3]; q^=x; p^=(x<<14); q>>=2;
    x=t[(b>>16)&3]; q^=x; p^=(x<<16); q>>=2;
    x=t[(b>>18)&3]; q^=x; p^=(x<<18); q>>=2;
    x=t[(b>>20)&3]; q^=x; p^=(x<<20); q>>=2;
    x=t[(b>>22)&3]; q^=x; p^=(x<<22); q>>=2;
    x=t[(b>>24)&3]; q^=x; p^=(x<<24); q>>=2;
    x=t[(b>>26)&3]; q^=x; p^=(x<<26); q>>=2;
    x=t[(b>>28)&3]; q^=x; p^=(x<<28); q>>=2;
    x=t[(b>>30)];   q^=x; p^=(x<<30); q>>=2;
#endif

#if MIRACL == 64
#define UNWOUNDM
    p=q=t[b&0xf];                       q>>=4;
    x=t[(b>>4)&0xf];  q^=x; p^=(x<<4);  q>>=4;
    x=t[(b>>8)&0xf];  q^=x; p^=(x<<8);  q>>=4;
    x=t[(b>>12)&0xf]; q^=x; p^=(x<<12); q>>=4;
    x=t[(b>>16)&0xf]; q^=x; p^=(x<<16); q>>=4;
    x=t[(b>>20)&0xf]; q^=x; p^=(x<<20); q>>=4;
    x=t[(b>>24)&0xf]; q^=x; p^=(x<<24); q>>=4;
    x=t[(b>>28)&0xf]; q^=x; p^=(x<<28); q>>=4;
    x=t[(b>>32)&0xf]; q^=x; p^=(x<<32); q>>=4;
    x=t[(b>>36)&0xf]; q^=x; p^=(x<<36); q>>=4;
    x=t[(b>>40)&0xf]; q^=x; p^=(x<<40); q>>=4;
    x=t[(b>>44)&0xf]; q^=x; p^=(x<<44); q>>=4;
    x=t[(b>>48)&0xf]; q^=x; p^=(x<<48); q>>=4;
    x=t[(b>>52)&0xf]; q^=x; p^=(x<<52); q>>=4;
    x=t[(b>>56)&0xf]; q^=x; p^=(x<<56); q>>=4;
    x=t[(b>>60)];     q^=x; p^=(x<<60); q>>=4;

#endif

#ifndef UNWOUNDM
    for (k=0;k<MIRACL;k+=8)
    {                 
        q^=(t[b&3]);   
        b>>=2; 
        p>>=2; 
        p|=q<<M2;
        q>>=2;

        q^=(t[b&3]);
        b>>=2;
        p>>=2;
        p|=q<<M2;
        q>>=2;

        q^=(t[b&3]);
        b>>=2;
        p>>=2;
        p|=q<<M2;
        q>>=2;

        q^=(t[b&3]);
        b>>=2;
        p>>=2;
        p|=q<<M2;
        q>>=2;
    }
#endif

#if MIRACL <= 32
    p^=(tb0&(kb<<M1));       /* compensate for top bit */
    q^=(tb0&(kb>>1));        /* don't break pipeline.. */
#else
    p^=(tb0&(kb<<M1));
    q^=(tb0&(kb>>1));
    p^=(tb1&(kb<<M2));
    q^=(tb1&(kb>>2));
    p^=(tb2&(kb<<M3));
    q^=(tb2&(kb>>3));
#endif

    *r=p;
    return q;
}

/*

Now inlined into square2(.) - see below

static mr_small mr_sqr2(mr_small a,mr_small *r)
{ 
    int i;
    mr_small t,q;
    static const mr_small look[16]=
    {0,(mr_small)1<<M8,(mr_small)4<<M8,(mr_small)5<<M8,(mr_small)16<<M8,
    (mr_small)17<<M8,(mr_small)20<<M8,(mr_small)21<<M8,(mr_small)64<<M8,
    (mr_small)65<<M8,(mr_small)68<<M8,(mr_small)69<<M8,(mr_small)80<<M8,
    (mr_small)81<<M8,(mr_small)84<<M8,(mr_small)85<<M8};

	q=0; *r=0;
    
#if MIRACL == 8
#define UNWOUNDS
        t=look[a&0xF];
        a>>=4;
        *r>>=8;
        *r|=t;
        t=look[a&0xF];
        a>>=4;
        q>>=8;
        q|=t;
#endif

#if MIRACL == 16
#define UNWOUNDS
        t=look[a&0xF];
        a>>=4;
        *r>>=8;
        *r|=t;
        t=look[a&0xF];
        a>>=4;
        *r>>=8;
        *r|=t;
        t=look[a&0xF];
        a>>=4;
        q>>=8;
        q|=t;
        t=look[a&0xF];
        a>>=4;
        q>>=8;
        q|=t;
#endif

#if MIRACL == 32
#define UNWOUNDS

        t=look[a&0xF];
        a>>=4;
        *r>>=8;
        *r|=t;
        t=look[a&0xF];
        a>>=4;
        *r>>=8;
        *r|=t;
        t=look[a&0xF];
        a>>=4;
        *r>>=8;
        *r|=t;
        t=look[a&0xF];
        a>>=4;
        *r>>=8;
        *r|=t;
  
		t=look[a&0xF];
        a>>=4;
        q>>=8;
        q|=t;
        t=look[a&0xF];
        a>>=4;
        q>>=8;
        q|=t;
		t=look[a&0xF];
        a>>=4;
        q>>=8;
        q|=t;
        t=look[a&0xF];
        a>>=4;
        q>>=8;
        q|=t;

#endif

#ifndef UNWOUNDS

    for (i=0;i<MIRACL/2;i+=4)
    {
        t=look[a&0xF];
        a>>=4;
        *r>>=8;
        *r|=t;
    }

    for (i=0;i<MIRACL/2;i+=4)
    {
        t=look[a&0xF];
        a>>=4;
        q>>=8;
        q|=t;
    }

#endif

	return q;
}

*/

static int numbits(big x)
{ /* return degree of x */
    mr_small *gx=x->w,bit=TOPBIT;
    int m,k=x->len;
    if (k==0) return 0;
    m=k*MIRACL;
    while (!(gx[k-1]&bit))
    {
        m--;
        bit>>=1;
    }
    return m;
}

int degree2(big x)
{ /* returns -1 for x=0 */
    return (numbits(x)-1);
}

static int zerobits(big x)
{ /* return number of zero bits at the end of x */
    int m,n,k;
    mr_small *gx,lsb,bit=1;
    k=x->len; 
    if (k==0) return (-1);
    gx=x->w;
    for (m=0;m<k;m++)
    {
        if (gx[m]==0) continue;
        n=0;
        lsb=gx[m];
        while (!(bit&lsb))
        {
            n++; 
            bit<<=1;
        }
        break;
    }
    return (MIRACL*m+n);
}

static void shiftrightbits(big x,int m)
{
    int i,k=x->len;
    int w=m/MIRACL;  /* words */
    int b=m%MIRACL;  /* bits  */
    mr_small *gx=x->w;
    if (k==0 || m==0) return;
    if (w>0)
    {
        for (i=0;i<k-w;i++)
            gx[i]=gx[i+w];
        for (i=k-w;i<k;i++) gx[i]=0;
        x->len-=w;
    }
/* time critical */
    if (b!=0) 
    {
        for (i=0;i<k-w-1;i++)
            gx[i]=(gx[i]>>b)|(gx[i+1]<<(MIRACL-b));   
        gx[k-w-1]>>=b;
        if (gx[k-w-1]==0) x->len--;
    }
}

static void shiftleftbits(big x,int m)
{
    int i,k=x->len;
    mr_small j; 
    int w=m/MIRACL;  /* words */
    int b=m%MIRACL;  /* bits  */
    mr_small *gx=x->w;
    if (k==0 || m==0) return;
    if (w>0)
    {
        for (i=k+w-1;i>=w;i--)
            gx[i]=gx[i-w];
        for (i=w-1;i>=0;i--) gx[i]=0;
        x->len+=w;
    }
/* time critical */
    if (b!=0) 
    {
        j=gx[k+w-1]>>(MIRACL-b);
        if (j!=0)
        {
            x->len++;
            gx[k+w]=j;
        }
        for (i=k+w-1;i>w;i--)
        {
            gx[i]=(gx[i]<<b)|(gx[i-1]>>(MIRACL-b));
        }
        gx[w]<<=b;
    }
}

static void square2(big x,big w)
{ /* w=x*x where x can be NULL so be careful */
    int i,n,m;
    mr_small a,t,r,*gw;

    static const mr_small look[16]=
    {0,(mr_small)1<<M8,(mr_small)4<<M8,(mr_small)5<<M8,(mr_small)16<<M8,
    (mr_small)17<<M8,(mr_small)20<<M8,(mr_small)21<<M8,(mr_small)64<<M8,
    (mr_small)65<<M8,(mr_small)68<<M8,(mr_small)69<<M8,(mr_small)80<<M8,
    (mr_small)81<<M8,(mr_small)84<<M8,(mr_small)85<<M8};

    if (x!=w) copy(x,w);
    n=w->len;
    if (n==0) return;
    m=n+n;
    w->len=m;
    gw=w->w; 

    for (i=n-1;i>=0;i--)
    {
        r=0;
        a=gw[i];

#if MIRACL == 8
#define UNWOUNDS
        t=look[a&0xF];
        a>>=4;
        r>>=8;
        r|=t;
        gw[i+i]=r; r=0;
        t=look[a&0xF];
        a>>=4;
        r>>=8;
        r|=t;
        gw[i+i+1]=r;
#endif

#if MIRACL == 16
#define UNWOUNDS
        t=look[a&0xF];
        a>>=4;
        r>>=8;
        r|=t;
        t=look[a&0xF];
        a>>=4;
        r>>=8;
        r|=t;
        gw[i+i]=r; r=0;
        t=look[a&0xF];
        a>>=4;
        r>>=8;
        r|=t;
        t=look[a&0xF];
        a>>=4;
        r>>=8;
        r|=t;
        gw[i+i+1]=r;
#endif

#if MIRACL == 32
#define UNWOUNDS
        gw[i+i]=(look[a&0xF]>>24)|(look[(a>>4)&0xF]>>16)|(look[(a>>8)&0xF]>>8)|look[(a>>12)&0xF]; 
        gw[i+i+1]=(look[(a>>16)&0xF]>>24)|(look[(a>>20)&0xF]>>16)|(look[(a>>24)&0xF]>>8)|look[(a>>28)];
#endif

#ifndef UNWOUNDS

        for (i=0;i<MIRACL/2;i+=4)
        {
            t=look[a&0xF];
            a>>=4;
            r>>=8;
            r|=t;
        }
        gw[i+i]=r; r=0;

        for (i=0;i<MIRACL/2;i+=4)
        {
            t=look[a&0xF];
            a>>=4;
            r>>=8;
            r|=t;
        }
        gw[i+i+1]=r;

#endif

    }
    if (gw[m-1]==0) 
    {
        w->len--;
        if (gw[m-2]==0)
            mr_lzero(w);
    }
}

/* Use karatsuba to multiply two polynomials with coefficients in GF(2^m) */

void karmul2_poly(_MIPD_ int n,big *t,big *x,big *y,big *z)
{
    int m,nd2,nd,md,md2;                          
    if (n==1) 
    { /* finished */
        modmult2(_MIPP_ *x,*y,*z);
        zero(z[1]);
        return;
    }       
    if (n==2)
    {  /* in-line 2x2 */
        modmult2(_MIPP_ x[0],y[0],z[0]);
        modmult2(_MIPP_ x[1],y[1],z[2]);
        add2(x[0],x[1],t[0]);
        add2(y[0],y[1],t[1]);
        modmult2(_MIPP_ t[0],t[1],z[1]);
        add2(z[1],z[0],z[1]);
        add2(z[1],z[2],z[1]);
        zero(z[3]);
        return;
    }

    if (n==3)
    {
        modmult2(_MIPP_ x[0],y[0],z[0]); 
        modmult2(_MIPP_ x[1],y[1],z[2]); 
        modmult2(_MIPP_ x[2],y[2],z[4]); 
        add2(x[0],x[1],t[0]);
        add2(y[0],y[1],t[1]);
        modmult2(_MIPP_ t[0],t[1],z[1]); 
        add2(z[1],z[0],z[1]);  
        add2(z[1],z[2],z[1]);  
        add2(x[1],x[2],t[0]);
        add2(y[1],y[2],t[1]);
        modmult2(_MIPP_ t[0],t[1],z[3]); 
        add2(z[3],z[2],z[3]);
        add2(z[3],z[4],z[3]); 
        add2(x[0],x[2],t[0]);
        add2(y[0],y[2],t[1]);   
        modmult2(_MIPP_ t[0],t[1],t[0]); 
        add2(z[2],t[0],z[2]);
        add2(z[2],z[0],z[2]);
        add2(z[2],z[4],z[2]); 
        zero(z[5]);
        return;
    }

    if (n%2==0)
    {
        md=nd=n;
        md2=nd2=n/2;
    }
    else
    {
        nd=n+1;
        md=n-1;
        nd2=nd/2; md2=md/2;
    }

    for (m=0;m<nd2;m++)
    {
        copy(x[m],z[m]);
        copy(y[m],z[nd2+m]);
    }
    for (m=0;m<md2;m++)
    { 
        add2(z[m],x[nd2+m],z[m]);
        add2(z[nd2+m],y[nd2+m],z[nd2+m]);
    }

    karmul2_poly(_MIPP_ nd2,&t[nd],z,&z[nd2],t); 

    karmul2_poly(_MIPP_ nd2,&t[nd],x,y,z);  

    for (m=0;m<nd;m++) add2(t[m],z[m],t[m]);

    karmul2_poly(_MIPP_ md2,&t[nd],&x[nd2],&y[nd2],&z[nd]);

    for (m=0;m<md;m++) add2(t[m],z[nd+m],t[m]);
    for (m=0;m<nd;m++) add2(z[nd2+m],t[m],z[nd2+m]);
}

void karmul2_poly_upper(_MIPD_ int n,big *t,big *x,big *y,big *z)
{ /* n is large and even, and upper half of z is known already */
    int m,nd2,nd;
    nd2=n/2; nd=n;

    for (m=0;m<nd2;m++)
    { 
        add2(x[m],x[nd2+m],z[m]);
        add2(y[m],y[nd2+m],z[nd2+m]);
    }

    karmul2_poly(_MIPP_ nd2,&t[nd],z,&z[nd2],t); 

    karmul2_poly(_MIPP_ nd2,&t[nd],x,y,z);   /* only 2 karmuls needed! */

    for (m=0;m<nd;m++) add2(t[m],z[m],t[m]);

    for (m=0;m<nd2;m++) 
    {
        add2(z[nd+m],z[nd+nd2+m],z[nd+m]);
        add2(z[nd+m],t[nd2+m],z[nd+m]);
    }

    for (m=0;m<nd;m++) 
    {
        add2(t[m],z[nd+m],t[m]);
        add2(z[nd2+m],t[m],z[nd2+m]);
    }
}

/* Some in-line karatsuba down at the bottom... */

static void mr_bottom1(mr_small *x,mr_small *y,mr_small *z)
{
    z[1]=mr_mul2(x[0],y[0],&z[0]);
}

static void mr_bottom2(mr_small *x,mr_small *y,mr_small *z)
{
    mr_small q0,r0,q1,r1,q2,r2;

    q0=mr_mul2(x[0],y[0],&r0);
    q1=mr_mul2(x[1],y[1],&r1);
    q2=mr_mul2((mr_small)(x[0]^x[1]),(mr_small)(y[0]^y[1]),&r2);

    z[0]=r0;
    z[1]=q0^r1^r0^r2;
    z[2]=q0^r1^q1^q2;
    z[3]=q1;
}

static void mr_bottom3(mr_small *x,mr_small *y,mr_small *z)
{ /* just 6 mr_muls... */
    mr_small q0,r0,q1,r1,q2,r2;
    mr_small a0,b0,a1,b1,a2,b2;

    q0=mr_mul2(x[0],y[0],&r0);
    q1=mr_mul2(x[1],y[1],&r1);
    q2=mr_mul2(x[2],y[2],&r2);

    a0=mr_mul2((mr_small)(x[0]^x[1]),(mr_small)(y[0]^y[1]),&b0);
    a1=mr_mul2((mr_small)(x[1]^x[2]),(mr_small)(y[1]^y[2]),&b1);
    a2=mr_mul2((mr_small)(x[0]^x[2]),(mr_small)(y[0]^y[2]),&b2);

    b0^=r0^r1;
    a0^=q0^q1;
    b1^=r1^r2;
    a1^=q1^q2;
    b2^=r0^r2;
    a2^=q0^q2;

    z[0]=r0;
    z[1]=q0^b0;
    z[2]=r1^a0^b2;
    z[3]=q1^b1^a2;
    z[4]=r2^a1;
    z[5]=q2;
}

static void mr_bottom4(mr_small *x,mr_small *y,mr_small *z)
{ /* unwound 4x4 karatsuba multiplication - only 9 muls */
    mr_small q0,r0,q1,r1,q2,r2,tx,ty;
    mr_small xx0,yy0,xx1,yy1,t0,t1,t2,t3;

    q0=mr_mul2(x[0],y[0],&r0);
    q1=mr_mul2(x[1],y[1],&r1);

    tx=x[0]^x[1];
    ty=y[0]^y[1];
    q2=mr_mul2(tx,ty,&r2);

    z[0]=r0;
    z[1]=q0^r1^r0^r2;
    z[2]=q0^r1^q1^q2;
    z[3]=q1;

    q0=mr_mul2(x[2],y[2],&r0);

    q1=mr_mul2(x[3],y[3],&r1);

    tx=x[2]^x[3];
    ty=y[2]^y[3];
    q2=mr_mul2(tx,ty,&r2);

    z[4]=r0;
    z[5]=q0^r1^r0^r2;
    z[6]=q0^r1^q1^q2;
    z[7]=q1;

    xx0=x[2]^x[0];
    yy0=y[2]^y[0];
    q0=mr_mul2(xx0,yy0,&r0);
   
    xx1=x[3]^x[1];
    yy1=y[3]^y[1];
    q1=mr_mul2(xx1,yy1,&r1);

    tx=xx0^xx1;
    ty=yy0^yy1;
    q2=mr_mul2(tx,ty,&r2);

    t0=z[0]^z[4]^r0;
    t1=z[1]^z[5]^q0^r1^r0^r2;
    t2=z[2]^z[6]^q0^r1^q1^q2;
    t3=z[3]^z[7]^q1; 

    z[2]^=t0;
    z[3]^=t1;
    z[4]^=t2;
    z[5]^=t3;
}

void karmul2(int n,mr_small *t,mr_small *x,mr_small *y,mr_small *z)
{ /* Karatsuba multiplication - note that n can be odd or even */
    int m,nd2,nd,md,md2;

    if (n<=4)
    {
        if (n==1)
        {
            mr_bottom1(x,y,z);
            return;
        }
        if (n==2)
        {   
            mr_bottom2(x,y,z);
            return;
        }
        if (n==3)
        {   
            mr_bottom3(x,y,z);
            return;
        }
        if (n==4)
        {   
            mr_bottom4(x,y,z);
            return;
        }
    }
    if (n%2==0)
    {
        md=nd=n;
        md2=nd2=n/2;
    }
    else
    {
        nd=n+1;
        md=n-1;
        nd2=nd/2; md2=md/2;
    }

    for (m=0;m<nd2;m++)
    {
        z[m]=x[m];
        z[nd2+m]=y[m];
    }
    for (m=0;m<md2;m++)
    {
        z[m]^=x[nd2+m];
        z[nd2+m]^=y[nd2+m];
    }

    karmul2(nd2,&t[nd],z,&z[nd2],t); 
    karmul2(nd2,&t[nd],x,y,z);  

    for (m=0;m<nd;m++) t[m]^=z[m];

    karmul2(md2,&t[nd],&x[nd2],&y[nd2],&z[nd]);

    for (m=0;m<md;m++) t[m]^=z[nd+m];
    for (m=0;m<nd;m++) z[nd2+m]^=t[m];
}

/* this is time-critical, so use karatsuba here, since addition is cheap *
 * and easy (no carries to worry about...)                               */


void multiply2(_MIPD_ big x,big y,big w)
{
    int i,j,xl,yl,ml;
    mr_small p,q;

#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif

    big w0=mr_mip->w0;

    if (x==NULL || y==NULL)
    {
        zero(w);
        return;
    }
    if (x->len==0 || y->len==0)
    {
        zero(w);
        return;
    }

    xl=x->len;
    yl=y->len;
    zero(w0);

    if (xl>=KARATSUBA && yl>=KARATSUBA)
    { 
        if (xl>yl) ml=xl;
        else       ml=yl;
     
        karmul2(ml,mr_mip->w7->w,x->w,y->w,w0->w);

        mr_mip->w7->len=w0->len=2*ml+1;
        mr_lzero(w0);
        mr_lzero(mr_mip->w7);    
        copy(w0,w);
        return;
    }

    w0->len=xl+yl;
    for (i=0;i<xl;i++)
    { 
        for (j=0;j<yl;j++)
        {
            q=mr_mul2(x->w[i],y->w[j],&p); 
            w0->w[i+j]^=p;
            w0->w[i+j+1]^=q;
        } 
    }
    mr_lzero(w0);
    copy(w0,w);
}

void add2(big x,big y,big z)
{ /* XOR x and y */
    int i,lx,ly,lz,lm;
    mr_small *gx,*gy,*gz;

    if (x==y)
    {
        zero(z);
        return;
    }
    if (y==NULL)
    {
        copy(x,z);
        return;
    }
    else if (x==NULL) 
    {
        copy(y,z);
        return;
    }

    if (x==z)
    {
        gy=y->w; gz=z->w;
        ly=y->len; lz=z->len;
        lm=lz; if (ly>lz) lm=ly;
        for (i=0;i<lm;i++)
            gz[i]^=gy[i];
        z->len=lm;
        if (gz[lm-1]==0) mr_lzero(z);
    }
    else
    {
        gx=x->w; gy=y->w; gz=z->w;
        lx=x->len; ly=y->len; lz=z->len;
        lm=lx; if (ly>lx) lm=ly;

        for (i=0;i<lm;i++)
            gz[i]=gx[i]^gy[i];
        for (i=lm;i<lz;i++)
            gz[i]=0;
        z->len=lm;
        if (gz[lm-1]==0) mr_lzero(z);
    }
}

static void remain2(_MIPD_ big y,big x)
{ /* generic "remainder" program. x%=y */
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    int my=numbits(y);
    int mx=numbits(x);
    while (mx>=my)
    {
        copy(y,mr_mip->w7);
        shiftleftbits(mr_mip->w7,mx-my);
        add2(x,mr_mip->w7,x);    
        mx=numbits(x);
    }
    return;
}

void gcd2(_MIPD_ big x,big y,big g)
{
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (size(y)==0) 
    {
        copy(x,g);
        return;
    }
    copy(x,mr_mip->w1);
    copy(y,mr_mip->w2);
    forever
    {
        remain2(_MIPP_ mr_mip->w2,mr_mip->w1);
        if (size(mr_mip->w1)==0) break;
        copy(mr_mip->w1,mr_mip->w3);
        copy(mr_mip->w2,mr_mip->w1);
        copy(mr_mip->w3,mr_mip->w2);
    }
    copy(mr_mip->w2,g);
}


/* See "Elliptic Curves in Cryptography", Blake, Seroussi & Smart, 
   Cambridge University Press, 1999, page 20, for this fast reduction
   routine - algorithm II.9 */

void reduce2(_MIPD_ big y,big x)
{ /* reduction wrt the trinomial or pentanomial modulus        *
   * Note that this is linear O(n), and thus not time critical */
    int k1,k2,k3,k4,ls1,ls2,ls3,ls4,rs1,rs2,rs3,rs4,i;
    int M,A,B,C;
    int xl;
    mr_small top,*gx,w;

#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif

    if (x!=y) copy(y,x);
    xl=x->len;
    gx=x->w;

    M=mr_mip->M;
    A=mr_mip->AA;
    if (A==0)
    {
        mr_berror(_MIPP_ MR_ERR_NO_BASIS);
        return;
    }
    B=mr_mip->BB;
    C=mr_mip->CC;

 
/* If optimizing agressively it makes sense to make this code specific to a particular field
   For example code like this can be optimized for the case 
   m=163. Note that the general purpose code involves lots of branches - these cause breaks in 
   the pipeline and they are slow. Further loop unrolling would be even faster...
*/
    if (M==163 && A==7 && B==6 && C==3)
    {
        for (i=xl-1;i>=6;i--)
        {
            w=gx[i]; gx[i]=0;
            gx[i-5]^=((w>>3)^(w<<4)^(w<<3)^w);
            gx[i-6]^=(w<<29);              
            gx[i-4]^=((w>>28)^(w>>29));
        }
        top=gx[5]>>3;
        
        gx[0]^=top;
        top<<=3;
        gx[1]^=(top>>28);
        gx[0]^=(top<<4);
        gx[1]^=(top>>29);
        gx[0]^=(top<<3);
        gx[0]^=top;
        gx[5]^=top;
        
        x->len=6; 
        if (gx[5]==0) mr_lzero(x);
        
        return;
    }

    if (M==103 && A==9)
    {
        for (i=xl-1;i>=4;i--)
        {
            w=gx[i]; gx[i]=0;
            gx[i-3]^=((w>>7)^(w<<2));
            gx[i-4]^=(w<<25);
            gx[i-2]^=(w>>30);
        }
        top=gx[3]>>7;
        gx[0]^=top;
        top<<=7;
        gx[1]^=(top>>30);
        gx[0]^=(top<<2);
        gx[3]^=top;
        x->len=4;
        if (gx[3]==0) mr_lzero(x);

        return;
    }

    k1=1+M/MIRACL;       /* words from MSB to LSB */

    if (xl<=k1)
    {
      if (numbits(x)<=M) return;
    }

    rs1=M%MIRACL;
    ls1=MIRACL-rs1;

    if (M-A < MIRACL)
    { /* slow way */
        while (numbits(x)>=M+1)
        {
            copy(mr_mip->modulus,mr_mip->w7);
            shiftleftbits(mr_mip->w7,numbits(x)-M-1);
            add2(x,mr_mip->w7,x);    
        }
        return;
    }

    k2=1+(M-A)/MIRACL;   /* words from MSB to bit */
    rs2=(M-A)%MIRACL;
    ls2=MIRACL-rs2;

    if (B)
    { /* Pentanomial */
        k3=1+(M-B)/MIRACL;
        rs3=(M-B)%MIRACL;
        ls3=MIRACL-rs3;

        k4=1+(M-C)/MIRACL;
        rs4=(M-C)%MIRACL;
        ls4=MIRACL-rs4;
    }
    
    for (i=xl-1;i>=k1;i--)
    {
        w=gx[i]; gx[i]=0;
        if (rs1==0) gx[i-k1+1]^=w;
        else
        {
            gx[i-k1+1]^=(w>>rs1);
            gx[i-k1]^=(w<<ls1);
        }
        if (rs2==0) gx[i-k2+1]^=w;
        else
        {
            gx[i-k2+1]^=(w>>rs2);
            gx[i-k2]^=(w<<ls2);
        }
        if (B)
        {
            if (rs3==0) gx[i-k3+1]^=w;
            else
            {
                gx[i-k3+1]^=(w>>rs3);
                gx[i-k3]^=(w<<ls3);
            }
            if (rs4==0) gx[i-k4+1]^=w;
            else
            {
                gx[i-k4+1]^=(w>>rs4);
                gx[i-k4]^=(w<<ls4);
            }
        }
    }

    top=gx[k1-1]>>rs1;

    if (top!=0)
    {  
        gx[0]^=top;
        top<<=rs1;

        if (rs2==0) gx[k1-k2]^=top;
        else
        {
            gx[k1-k2]^=(top>>rs2);
            if (k1>k2) gx[k1-k2-1]^=(top<<ls2);
        }
        if (B)
        {
            if (rs3==0) gx[k1-k3]^=top;
            else
            {
                gx[k1-k3]^=(top>>rs3);
                if (k1>k3) gx[k1-k3-1]^=(top<<ls3);
            }
            if (rs4==0) gx[k1-k4]^=top;
            else
            {
                gx[k1-k4]^=(top>>rs4);
                if (k1>k4) gx[k1-k4-1]^=(top<<ls4);
            }
        }
        gx[k1-1]^=top;
    }
    x->len=k1; 
    if (gx[k1-1]==0) mr_lzero(x);
}

void incr2(big x,int n,big w)
{ /* increment x by small amount */
    if (x!=w) copy(x,w);
    if (n==0) return;
    if (w->len==0)
    {
        w->len=1;
        w->w[0]=n;
    }
    else
    {
        w->w[0]^=(mr_small)n;
        mr_lzero(w);
    }
}

void modsquare2(_MIPD_ big x,big w)
{ /* w=x*x mod f */
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif

    square2(x,mr_mip->w0);
    reduce2(_MIPP_ mr_mip->w0,mr_mip->w0);
    copy(mr_mip->w0,w);
}

/* Experimental code for GF(2^103) modular multiplication *
 * Inspired by Robert Harley's ECDL code                  */

#ifdef SP103

#ifdef __GNUC__
#include <xmmintrin.h>
#else
#include <emmintrin.h>
#endif

void modmult2(_MIPD_ big x,big y,big w)
{
    int i,j;
    mr_small b;

    __m128i t[16];
    __m128i m,r,s,p,q,xe,xo;
    __m64 a3,a2,a1,a0,top;

    if (x==y)
    {
        modsquare2(_MIPP_ x,w);
        return;
    }
    
    if (x->len==0 || y->len==0)
    {
        zero(w);
        return;
    }

    m=_mm_set_epi32(0,0,0xff<<24,0);    /* shifting mask */

/* precompute a small table */

    t[0]=_mm_set1_epi32(0);
    xe=_mm_set_epi32(0,x->w[2],0,x->w[0]);
    xo=_mm_set_epi32(0,x->w[3],0,x->w[1]);
    t[1]=_mm_xor_si128(xe,_mm_slli_si128(xo,4));
    xe=_mm_slli_epi64(xe,1);
    xo=_mm_slli_epi64(xo,1);
    t[2]=_mm_xor_si128(xe,_mm_slli_si128(xo,4));
    t[3]=_mm_xor_si128(t[2],t[1]);
    xe=_mm_slli_epi64(xe,1);
    xo=_mm_slli_epi64(xo,1);
    t[4]=_mm_xor_si128(xe,_mm_slli_si128(xo,4));
    t[5]=_mm_xor_si128(t[4],t[1]);
    t[6]=_mm_xor_si128(t[4],t[2]);
    t[7]=_mm_xor_si128(t[4],t[3]);
    xe=_mm_slli_epi64(xe,1);
    xo=_mm_slli_epi64(xo,1);
    t[8]=_mm_xor_si128(xe,_mm_slli_si128(xo,4));
    t[9]=_mm_xor_si128(t[8],t[1]);
    t[10]=_mm_xor_si128(t[8],t[2]);
    t[11]=_mm_xor_si128(t[8],t[3]);
    t[12]=_mm_xor_si128(t[8],t[4]);
    t[13]=_mm_xor_si128(t[8],t[5]);
    t[14]=_mm_xor_si128(t[8],t[6]);
    t[15]=_mm_xor_si128(t[8],t[7]);

    b=y->w[0];

    i=b&0xf; j=(b>>4)&0xf;    r=t[j]; 
    s=_mm_and_si128(r,m);     r=_mm_slli_epi64(r,4);
    s=_mm_slli_si128(s,1);    s=_mm_srli_epi64(s,4);  /* net shift left 4 */
    r=_mm_xor_si128(r,s);     r=_mm_xor_si128(r,t[i]);    
    p=q=r;                    q=_mm_srli_si128(q,1); 

    i=(b>>8)&0xf; j=(b>>12)&0xf; r=t[j]; 
    s=_mm_and_si128(r,m);    r=_mm_slli_epi64(r,4);
    s=_mm_slli_si128(s,1);   s=_mm_srli_epi64(s,4);
    r=_mm_xor_si128(r,s);    r=_mm_xor_si128(r,t[i]);
    q=_mm_xor_si128(q,r);    r=_mm_slli_si128(r,1); 
    p=_mm_xor_si128(p,r);    q=_mm_srli_si128(q,1);

    i=(b>>16)&0xf; j=(b>>20)&0xf; r=t[j]; 
    s=_mm_and_si128(r,m);    r=_mm_slli_epi64(r,4);
    s=_mm_slli_si128(s,1);   s=_mm_srli_epi64(s,4);
    r=_mm_xor_si128(r,s);    r=_mm_xor_si128(r,t[i]);
    q=_mm_xor_si128(q,r);    r=_mm_slli_si128(r,2); 
    p=_mm_xor_si128(p,r);    q=_mm_srli_si128(q,1);

    i=(b>>24)&0xf; j=(b>>28); r=t[j]; 
    s=_mm_and_si128(r,m);    r=_mm_slli_epi64(r,4);
    s=_mm_slli_si128(s,1);   s=_mm_srli_epi64(s,4);
    r=_mm_xor_si128(r,s);    r=_mm_xor_si128(r,t[i]);
    q=_mm_xor_si128(q,r);    r=_mm_slli_si128(r,3); 
    p=_mm_xor_si128(p,r);    q=_mm_srli_si128(q,1);

    b=y->w[1];

    i=(b)&0xf; j=(b>>4)&0xf; r=t[j]; 
    s=_mm_and_si128(r,m);    r=_mm_slli_epi64(r,4);
    s=_mm_slli_si128(s,1);   s=_mm_srli_epi64(s,4);
    r=_mm_xor_si128(r,s);    r=_mm_xor_si128(r,t[i]);
    q=_mm_xor_si128(q,r);    r=_mm_slli_si128(r,4); 
    p=_mm_xor_si128(p,r);    q=_mm_srli_si128(q,1);

    i=(b>>8)&0xf; j=(b>>12)&0xf; r=t[j]; 
    s=_mm_and_si128(r,m);    r=_mm_slli_epi64(r,4);
    s=_mm_slli_si128(s,1);   s=_mm_srli_epi64(s,4);
    r=_mm_xor_si128(r,s);    r=_mm_xor_si128(r,t[i]);
    q=_mm_xor_si128(q,r);    r=_mm_slli_si128(r,5); 
    p=_mm_xor_si128(p,r);    q=_mm_srli_si128(q,1);

    i=(b>>16)&0xf; j=(b>>20)&0xf; r=t[j]; 
    s=_mm_and_si128(r,m);    r=_mm_slli_epi64(r,4);
    s=_mm_slli_si128(s,1);   s=_mm_srli_epi64(s,4);
    r=_mm_xor_si128(r,s);    r=_mm_xor_si128(r,t[i]);
    q=_mm_xor_si128(q,r);    r=_mm_slli_si128(r,6); 
    p=_mm_xor_si128(p,r);    q=_mm_srli_si128(q,1);

    i=(b>>24)&0xf; j=(b>>28); r=t[j]; 
    s=_mm_and_si128(r,m);    r=_mm_slli_epi64(r,4);
    s=_mm_slli_si128(s,1);   s=_mm_srli_epi64(s,4);
    r=_mm_xor_si128(r,s);    r=_mm_xor_si128(r,t[i]);
    q=_mm_xor_si128(q,r);    r=_mm_slli_si128(r,7); 
    p=_mm_xor_si128(p,r);    q=_mm_srli_si128(q,1);

    b=y->w[2];

    i=(b)&0xf; j=(b>>4)&0xf; r=t[j]; 
    s=_mm_and_si128(r,m);    r=_mm_slli_epi64(r,4);
    s=_mm_slli_si128(s,1);   s=_mm_srli_epi64(s,4);
    r=_mm_xor_si128(r,s);    r=_mm_xor_si128(r,t[i]);
    q=_mm_xor_si128(q,r);    r=_mm_slli_si128(r,8); 
    p=_mm_xor_si128(p,r);    q=_mm_srli_si128(q,1);

    i=(b>>8)&0xf; j=(b>>12)&0xf; r=t[j]; 
    s=_mm_and_si128(r,m);    r=_mm_slli_epi64(r,4);
    s=_mm_slli_si128(s,1);   s=_mm_srli_epi64(s,4);
    r=_mm_xor_si128(r,s);    r=_mm_xor_si128(r,t[i]);
    q=_mm_xor_si128(q,r);    r=_mm_slli_si128(r,9); 
    p=_mm_xor_si128(p,r);    q=_mm_srli_si128(q,1);

    i=(b>>16)&0xf; j=(b>>20)&0xf; r=t[j]; 
    s=_mm_and_si128(r,m);    r=_mm_slli_epi64(r,4);
    s=_mm_slli_si128(s,1);   s=_mm_srli_epi64(s,4);
    r=_mm_xor_si128(r,s);    r=_mm_xor_si128(r,t[i]);
    q=_mm_xor_si128(q,r);    r=_mm_slli_si128(r,10); 
    p=_mm_xor_si128(p,r);    q=_mm_srli_si128(q,1);

    i=(b>>24)&0xf; j=(b>>28); r=t[j]; 
    s=_mm_and_si128(r,m);    r=_mm_slli_epi64(r,4);
    s=_mm_slli_si128(s,1);   s=_mm_srli_epi64(s,4);
    r=_mm_xor_si128(r,s);    r=_mm_xor_si128(r,t[i]);
    q=_mm_xor_si128(q,r);    r=_mm_slli_si128(r,11); 
    p=_mm_xor_si128(p,r);    q=_mm_srli_si128(q,1);

    b=y->w[3];

    i=(b)&0xf; j=(b>>4)&0xf; r=t[j]; 
    s=_mm_and_si128(r,m);    r=_mm_slli_epi64(r,4);
    s=_mm_slli_si128(s,1);   s=_mm_srli_epi64(s,4);
    r=_mm_xor_si128(r,s);    r=_mm_xor_si128(r,t[i]);
    q=_mm_xor_si128(q,r);    r=_mm_slli_si128(r,12); 
    p=_mm_xor_si128(p,r);

    q=_mm_srli_si128(q,4);   /* only 103 bits, so we are done */

/* modular reduction - x^103+x^9+1 */

    a0=_mm_movepi64_pi64(p);
    a1=_mm_movepi64_pi64(_mm_srli_si128(p,8));
    a2=_mm_movepi64_pi64(q);
    a3=_mm_movepi64_pi64(_mm_srli_si128(q,8));

    a2=_m_pxor(a2,_m_psrlqi(a3,39));
    a2=_m_pxor(a2,_m_psrlqi(a3,30));
    a1=_m_pxor(a1,_m_psllqi(a3,25));
    a1=_m_pxor(a1,_m_psllqi(a3,34));

    a1=_m_pxor(a1,_m_psrlqi(a2,39));
    a1=_m_pxor(a1,_m_psrlqi(a2,30));
    a0=_m_pxor(a0,_m_psllqi(a2,25));
    a0=_m_pxor(a0,_m_psllqi(a2,34));

    top=_m_psrlqi(a1,39);
    a0=_m_pxor(a0,top);
    top=_m_psllqi(top,39);
    a0=_m_pxor(a0,_m_psrlqi(top,30));
    a1=_m_pxor(a1,top);

    if (w->len>4) zero(w);
    
    w->w[0]=_m_to_int(a0);
    a0=_m_psrlqi(a0,32);
    w->w[1]=_m_to_int(a0);
    w->w[2]=_m_to_int(a1);
    a1=_m_psrlqi(a1,32);
    w->w[3]=_m_to_int(a1);

    w->len=4;
    if (w->w[3]==0) mr_lzero(w);
    _m_empty();
}

#else

void modmult2(_MIPD_ big x,big y,big w)
{ /* w=x*y mod f */
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif

    if (x==NULL || y==NULL)
    {
        zero(w);
        return;
    }

    if (x==y)
    {
        modsquare2(_MIPP_ x,w);
        return;
    }

    if (y->len==0)
    {
        zero(w);
        return;
    }

    if (y->len==1)
    {
        if (y->w[0]==1)
        {
            copy(x,w);
            return;
        }
    } 
    
    multiply2(_MIPP_ x,y,mr_mip->w0);
    reduce2(_MIPP_ mr_mip->w0,mr_mip->w0);
    copy(mr_mip->w0,w);
}

#endif

/* Will be *much* faster if M,A,(B and C) are all odd */

void sqroot2(_MIPD_ big x,big y)
{ 
    int i,M,A,B,C;
    int m,k,n,h,s,a,aw,ab,bw,bb,cw,cb;
 #if MIRACL != 32
    int mm,j;
 #endif
    mr_small *wk,w,we,wo;
    BOOL slow;
    static const mr_small odds[16]=
    {0,0,1,1,0,0,1,1,2,2,3,3,2,2,3,3};
    static const mr_small evens[16]=
    {0,1,0,1,2,3,2,3,0,1,0,1,2,3,2,3};

#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    M=mr_mip->M;
    A=mr_mip->AA;
    if (A==0)
    {
        mr_berror(_MIPP_ MR_ERR_NO_BASIS);
        return;
    }
    B=mr_mip->BB;
    C=mr_mip->CC;

    slow=FALSE;
    if (B)
    {
        if (M%2!=1 || A%2!=1 || B%2!=1 || C%2!=1) slow=TRUE;
    }
    else
    {
        if (M%2!=1 || A%2!=1) slow=TRUE;
    }

    if (slow)
    {
        copy(x,y);
        for (i=1;i<mr_mip->M;i++)
            modsquare2(_MIPP_ y,y);
        return;
    }

/* M, A (B and C) are all odd - so use fast
   Fong, Hankerson, Lopez and Menezes method */

    if (x==y)
    {
        copy (x,mr_mip->w0);
        wk=mr_mip->w0->w;
    }
    else
    {
        wk=x->w;
    }
    zero(y);

    k=1+(M/MIRACL);
    h=(k+1)/2;

    a=(A+1)/2;
    aw=a/MIRACL;
    ab=a%MIRACL;

    if (B)
    {
        a=(B+1)/2;
        bw=a/MIRACL;
        bb=a%MIRACL;

        a=(C+1)/2;
        cw=a/MIRACL;
        cb=a%MIRACL;
    }

    y->len=k;
    s=h*MIRACL-1-(M-1)/2;

    for (i=0;i<k;i++)
    {
        n=i/2;
        w=wk[i];
       
#if MIRACL == 32
        m=w&0xF;
        we=evens[m];
        wo=odds[m];
        w>>=4;

        m=w&0xF;
        we|=evens[m]<<2;
        wo|=odds[m]<<2;
        w>>=4;

        m=w&0xF;
        we|=evens[m]<<4;
        wo|=odds[m]<<4;
        w>>=4;

        m=w&0xF;
        we|=evens[m]<<6;
        wo|=odds[m]<<6;
        w>>=4;

        m=w&0xF;
        we|=evens[m]<<8;
        wo|=odds[m]<<8;
        w>>=4;

        m=w&0xF;
        we|=evens[m]<<10;
        wo|=odds[m]<<10;
        w>>=4;

        m=w&0xF;
        we|=evens[m]<<12;
        wo|=odds[m]<<12;
        w>>=4;

        m=w;
        we|=evens[m]<<14;
        wo|=odds[m]<<14;

#else
        mm=0; we=wo=0;
        for (j=0;j<MIRACL/4;j++)
        {
            m=w&0xF; 
            we|=(evens[m]<<mm);
            wo|=(odds[m]<<mm);
            mm+=2; w>>=4;
        }
#endif
        i++;
        if (i<k)
        {
            w=wk[i];
#if MIRACL == 32
        m=w&0xF;
        we|=evens[m]<<16;
        wo|=odds[m]<<16;
        w>>=4;

        m=w&0xF;
        we|=evens[m]<<18;
        wo|=odds[m]<<18;
        w>>=4;

        m=w&0xF;
        we|=evens[m]<<20;
        wo|=odds[m]<<20;
        w>>=4;

        m=w&0xF;
        we|=evens[m]<<22;
        wo|=odds[m]<<22;
        w>>=4;

        m=w&0xF;
        we|=evens[m]<<24;
        wo|=odds[m]<<24;
        w>>=4;

        m=w&0xF;
        we|=evens[m]<<26;
        wo|=odds[m]<<26;
        w>>=4;

        m=w&0xF;
        we|=evens[m]<<28;
        wo|=odds[m]<<28;
        w>>=4;

        m=w;
        we|=evens[m]<<30;
        wo|=odds[m]<<30;
#else
            for (j=0;j<MIRACL/4;j++)
            {
                m=w&0xF; 
                we|=(evens[m]<<mm);
                wo|=(odds[m]<<mm);
                mm+=2; w>>=4;
            }
#endif
        }
        y->w[n]^=we; 

        if (s==0) y->w[h+n]=wo;
        else
        {
            y->w[h+n-1]^=wo<<(MIRACL-s); 
            y->w[h+n]^=wo>>s;     /* abutt odd bits to even */
        }
        if (ab==0) y->w[n+aw]^=wo;
        else
        {
            y->w[n+aw]^=wo<<ab; 
            y->w[n+aw+1]^=wo>>(MIRACL-ab);
        }
        if (B)
        {
            if (bb==0) y->w[n+bw]^=wo;
            else
            {
                y->w[n+bw]^=wo<<bb; 
                y->w[n+bw+1]^=wo>>(MIRACL-bb);
            }
            if (cb==0) y->w[n+cw]^=wo;
            else
            {
                y->w[n+cw]^=wo<<cb; 
                y->w[n+cw+1]^=wo>>(MIRACL-cb);
            }
        }
    }

    if (y->w[k-1]==0) mr_lzero(y);
}

void power2(_MIPD_ big x,int m,big w)
{ /* w=x^m mod f. Could be optimised a lot, but not time critical for me */
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    copy(x,mr_mip->w1);
    
    convert(_MIPP_ 1,w);
    forever
    {
        if (m%2!=0)
            modmult2(_MIPP_ w,mr_mip->w1,w);
        m/=2;
        if (m==0) break;
        modsquare2(_MIPP_ mr_mip->w1,mr_mip->w1);
    }
}

/* Euclidean Algorithm */

BOOL inverse2(_MIPD_ big x,big w)
{
    mr_small bit;
    int i,j,n,n3,k,n4,mb,mw;
    big t;
    BOOL newword;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (size(x)==0) return FALSE;
 
    convert(_MIPP_ 1,mr_mip->w1);     
    zero(mr_mip->w2);                 
    copy(x,mr_mip->w3);               
    copy(mr_mip->modulus,mr_mip->w4); 

    n3=numbits(mr_mip->w3);
    n4=mr_mip->M+1;

    while (n3!=1)
    {
        j=n3-n4;
        
        if (j<0)
        { 
            t=mr_mip->w3; mr_mip->w3=mr_mip->w4; mr_mip->w4=t;
            t=mr_mip->w1; mr_mip->w1=mr_mip->w2; mr_mip->w2=t;
            j=-j; n=n3; n3=n4; n4=n;
        }
       
        mw=j/MIRACL; mb=j%MIRACL;
        
        if (n3<MIRACL)
        {
            mr_mip->w3->w[0]^=mr_mip->w4->w[0]<<mb;
            n3--; bit=((mr_small)1<<(n3-1));
            while (!(mr_mip->w3->w[0]&bit))
            {
                n3--;
                bit>>=1;
            }
        }
        else
        {
            k=mr_mip->w3->len;
            if (mb==0)
            {
                for (i=mw;i<k;i++)
                {
                    mr_mip->w3->w[i]^=mr_mip->w4->w[i-mw];
                }
            }
            else
            {
                mr_mip->w3->w[mw]^=mr_mip->w4->w[0]<<mb;
                for (i=mw+1;i<k;i++)
                {
                    mr_mip->w3->w[i]^=( (mr_mip->w4->w[i-mw]<<mb) | (mr_mip->w4->w[i-mw-1]>>(MIRACL-mb)));
                }
            }

            newword=FALSE;
            while (mr_mip->w3->w[k-1]==0) {k--; newword=TRUE;}
            
            if (newword)
            {           
                bit=TOPBIT;
                n3=k*MIRACL;
            }
            else
            {
                n3--;
                bit=((mr_small)1<<(n3-1));
            }
            while (!(mr_mip->w3->w[k-1]&bit))
            {
                n3--;
                bit>>=1;         
            }
            mr_mip->w3->len=k;
        }

        k=mr_mip->w2->len+mw+1;
        if ((int)mr_mip->w1->len>k) k=mr_mip->w1->len;
        
        if (mb==0)
        {
            for (i=mw;i<k;i++)
            {
                mr_mip->w1->w[i]^=mr_mip->w2->w[i-mw];
            }
        }
        else
        {
            mr_mip->w1->w[mw]^=mr_mip->w2->w[0]<<mb;
            for (i=mw+1;i<k;i++)
            {
                mr_mip->w1->w[i]^=((mr_mip->w2->w[i-mw]<<mb) | (mr_mip->w2->w[i-mw-1]>>(MIRACL-mb)));
            }
        }  
        while (mr_mip->w1->w[k-1]==0) k--;
        mr_mip->w1->len=k;
    }

    copy(mr_mip->w1,w);
    return TRUE;
}

/* Schroeppel, Orman, O'Malley, Spatscheck    *
 * "Almost Inverse" algorithm, Crypto '95     *
 * More optimization here and in-lining would *
 * speed up AFFINE mode. I observe that       *
 * pentanomials would be more efficient if C  *
 * were greater                               */

/*
BOOL inverse2(_MIPD_ big x,big w)
{
    mr_small lsw,*gw;
    int i,n,bits,step,n3,n4,k;
    int k1,k2,k3,k4,ls1,ls2,ls3,ls4,rs1,rs2,rs3,rs4;
    int M,A,B,C;
    big t;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (size(x)==0) return FALSE;

    M=mr_mip->M;
    A=mr_mip->AA;
	if (A==0)
	{
        mr_berror(_MIPP_ MR_ERR_NO_BASIS);
        return FALSE;
	}
 
    B=mr_mip->BB;
    C=mr_mip->CC;
    convert(_MIPP_ 1,mr_mip->w1);     
    zero(mr_mip->w2);                 
    copy(x,mr_mip->w3);               
    copy(mr_mip->modulus,mr_mip->w4); 

    bits=zerobits(mr_mip->w3);
    shiftrightbits(mr_mip->w3,bits);
    k=bits;    
    n3=numbits(mr_mip->w3);
    n4=M+1;

    if (n3>1) forever
    {
        if (n3<n4)
        { 
            t=mr_mip->w3; mr_mip->w3=mr_mip->w4; mr_mip->w4=t;
            t=mr_mip->w1; mr_mip->w1=mr_mip->w2; mr_mip->w2=t;
            n=n3; n3=n4; n4=n;
        }
        
        add2(mr_mip->w3,mr_mip->w4,mr_mip->w3); 
 
        add2(mr_mip->w1,mr_mip->w2,mr_mip->w1);

        if (n3==n4) n3=numbits(mr_mip->w3);
        bits=zerobits(mr_mip->w3);
        k+=bits;    
        n3-=bits;
        if (n3==1) break;
        shiftrightbits(mr_mip->w3,bits);
        shiftleftbits(mr_mip->w2,bits);
   }

    copy(mr_mip->w1,w);

    if (k==0) 
    {
        mr_lzero(w);
        return TRUE;
    }
    step=MIRACL;

    if (A<MIRACL) step=A;
    
    k1=1+M/MIRACL;  
    rs1=M%MIRACL;
    ls1=MIRACL-rs1;

    k2=1+A/MIRACL;  
    rs2=A%MIRACL;
    ls2=MIRACL-rs2;

    if (B)
    { 
        if (C<MIRACL) step=C;

        k3=1+B/MIRACL;
        rs3=B%MIRACL;
        ls3=MIRACL-rs3;

        k4=1+C/MIRACL;
        rs4=C%MIRACL;
        ls4=MIRACL-rs4;
    }

    gw=w->w;
    while (k>0)
    {
        if (k>step) n=step;
        else        n=k;
 
        if (n==MIRACL) lsw=gw[0];
        else           lsw=gw[0]&(((mr_small)1<<n)-1);

        w->len=k1;
        if (rs1==0) gw[k1-1]^=lsw;
        else
        {
            w->len++;
            gw[k1]^=(lsw>>ls1);
            gw[k1-1]^=(lsw<<rs1);
        }
        if (rs2==0) gw[k2-1]^=lsw;
        else
        {
            gw[k2]^=(lsw>>ls2);
            gw[k2-1]^=(lsw<<rs2);
        }
        if (B)
        {
            if (rs3==0) gw[k3-1]^=lsw;
            else
            {
                gw[k3]^=(lsw>>ls3);
                gw[k3-1]^=(lsw<<rs3);
            }
            if (rs4==0) gw[k4-1]^=lsw;
            else
            {
                gw[k4]^=(lsw>>ls4);
                gw[k4-1]^=(lsw<<rs4);
            }
        }
        shiftrightbits(w,n);
        k-=n;
    }
    mr_lzero(w);
    return TRUE;
}

*/

BOOL multi_inverse2(_MIPD_ int m,big *x,big *w)
{ /* find w[i]=1/x[i] mod f, for i=0 to m-1 */
    int i;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (m==0) return TRUE;
    if (m<0) return FALSE;

    if (x==w)
    {
        mr_berror(_MIPP_ MR_ERR_BAD_PARAMETERS);
        return FALSE;
    }
    if (m==1)
    {
        inverse2(_MIPP_ x[0],w[0]);
        return TRUE;
    }
    convert(_MIPP_ 1,w[0]);
    copy(x[0],w[1]);
    for (i=2;i<m;i++)
        modmult2(_MIPP_ w[i-1],x[i-1],w[i]);

    modmult2(_MIPP_ w[m-1],x[m-1],mr_mip->w6);
    if (size(mr_mip->w6)==0)
    {
        mr_berror(_MIPP_ MR_ERR_DIV_BY_ZERO);
        return FALSE;
    }

    inverse2(_MIPP_ mr_mip->w6,mr_mip->w6);  /* y=1/y */

    copy(x[m-1],mr_mip->w5);
    modmult2(_MIPP_ w[m-1],mr_mip->w6,w[m-1]);

    for (i=m-2;;i--)
    {
        if (i==0)
        {
            modmult2(_MIPP_ mr_mip->w5,mr_mip->w6,w[0]);
            break;
        }
        modmult2(_MIPP_ w[i],mr_mip->w5,w[i]);
        modmult2(_MIPP_ w[i],mr_mip->w6,w[i]);
        modmult2(_MIPP_ mr_mip->w5,x[i],mr_mip->w5);
    }
    return TRUE;
}

int trace2(_MIPD_ big x)
{
    int i;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    copy(x,mr_mip->w1);
    for (i=1;i<mr_mip->M;i++)
    {
        modsquare2(_MIPP_ mr_mip->w1,mr_mip->w1);
        add2(mr_mip->w1,x,mr_mip->w1);
    }   
    return (mr_mip->w1->w[0]&1);
}

void rand2(_MIPD_ big x)
{ /* random number */
    int i,k;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    zero(x);
    k=1+mr_mip->M/MIRACL;        
    x->len=k;
    for (i=0;i<k;i++) x->w[i]=brand(_MIPPO_ );
    mr_lzero(x);
    reduce2(_MIPP_ x,x);    
}

int parity2(big x)
{ /* return LSB */
   if (x->len==0) return 0;
   return (int)(x->w[0]%2);
}

void halftrace2(_MIPD_ big b,big w)
{
    int i,M;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif

    M=mr_mip->M;
    if (M%2==0) return;
   
    copy(b,mr_mip->w1);
    copy(b,w);
    for (i=1;i<=(M-1)/2;i++)
    { 
        modsquare2(_MIPP_ w,w);
        modsquare2(_MIPP_ w,w);
        add2(w,mr_mip->w1,w);   
    } 
}

BOOL quad2(_MIPD_ big b,big w)
{ /* Solves x^2 + x = b  for a root w  *
   * returns TRUE if a solution exists *
   * the "other" solution is w+1       */
    int i,M;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif

    M=mr_mip->M;
    copy(b,mr_mip->w1);
    if (M%2==1) halftrace2(_MIPP_ b,w); /* M is odd, so its the Half-Trace */

    else
    {
        forever
        {
            rand2(_MIPP_ mr_mip->w2);
            zero(w);
            copy(mr_mip->w2,mr_mip->w3);
            for (i=1;i<M;i++)
            {
                modsquare2(_MIPP_ mr_mip->w3,mr_mip->w3);
                modmult2(_MIPP_ mr_mip->w3,mr_mip->w1,mr_mip->w4);
                modsquare2(_MIPP_ w,w);
                add2(w,mr_mip->w4,w);
                add2(mr_mip->w3,mr_mip->w2,mr_mip->w3);
            }    
            if (size(mr_mip->w3)!=0) break; 
        }
    }
    copy(w,mr_mip->w2);
    modsquare2(_MIPP_ mr_mip->w2,mr_mip->w2);
    add2(mr_mip->w2,w,mr_mip->w2);
    if (compare(mr_mip->w1,mr_mip->w2)==0) return TRUE;
    return FALSE;
}

void gf2m_dotprod(_MIPD_ int n,big *x,big *y,big w)
{ /* dot product - only one reduction! */
    int i;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    mr_mip->check=OFF;
    zero(mr_mip->w5);

    for (i=0;i<n;i++)
    {
        multiply2(_MIPP_ x[i],y[i],mr_mip->w0);
        add2(mr_mip->w5,mr_mip->w0,mr_mip->w5);
    }

    reduce2(_MIPP_ mr_mip->w5,mr_mip->w5);
    copy(mr_mip->w5,w);

    mr_mip->check=ON;
}

BOOL prepare_basis(_MIPD_ int m,int a,int b,int c,BOOL check)
{
    int i,k,sh;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return FALSE;

    if (b==0) c=0;
    if (m==mr_mip->M && a==mr_mip->AA && b==mr_mip->BB && c==mr_mip->CC)
        return TRUE;   /* its already prepared... */

    MR_IN(138)
    if (m <=0 || a<=0 || a>=m || b>=a) 
    {
        mr_berror(_MIPP_ MR_ERR_BAD_MODULUS);
        MR_OUT
        return FALSE;
    }
    
    mr_mip->M=m;
    mr_mip->AA=a;
    mr_mip->BB=0;
    mr_mip->CC=0;
    if (mr_mip->modulus==NULL) mr_mip->modulus=mirvar(_MIPP_ 0);
    else zero(mr_mip->modulus);

    k=1+m/MIRACL;

    if (k>mr_mip->nib)
    {
        mr_berror(_MIPP_ MR_ERR_OVERFLOW);
        MR_OUT
        return FALSE;
    }

    mr_mip->modulus->len=k;
    sh=m%MIRACL;
    mr_mip->modulus->w[k-1]=((mr_small)1<<sh);
    mr_mip->modulus->w[0]^=1;
    mr_mip->modulus->w[a/MIRACL]^=((mr_small)1<<(a%MIRACL));
    if (b!=0)
    {
         mr_mip->BB=b;
         mr_mip->CC=c;
         mr_mip->modulus->w[b/MIRACL]^=((mr_small)1<<(b%MIRACL));
         mr_mip->modulus->w[c/MIRACL]^=((mr_small)1<<(c%MIRACL));
    }

    if (!check)
    {
        MR_OUT
        return TRUE;
    }

/* check for irreducibility of basis */

    zero(mr_mip->w4);
    mr_mip->w4->len=1;
    mr_mip->w4->w[0]=2;       /* f(t) = t */
    for (i=1;i<=m/2;i++)
    {
        modsquare2(_MIPP_ mr_mip->w4,mr_mip->w4);
        incr2(mr_mip->w4,2,mr_mip->w5);
        gcd2(_MIPP_ mr_mip->w5,mr_mip->modulus,mr_mip->w6);
        if (size(mr_mip->w6)!=1)
        {
            mr_berror(_MIPP_ MR_ERR_NOT_IRREDUC);
            MR_OUT
            return FALSE;
        }
    }
                   
    MR_OUT
    return TRUE;
}

/* Initialise with Trinomial or Pentanomial      *
 * t^m  + t^a + 1 OR t^m + t^a +t^b + t^c + 1    *
 * Set b=0 for pentanomial. a2 is usually 0 or 1 *
 * m negative indicates a super-singular curve   */

BOOL ecurve2_init(_MIPD_ int m,int a,int b,int c,big a2,big a6,BOOL check,int type)
{
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    int i;

/* catch some nonsense conditions */

    if (mr_mip->ERNUM) return FALSE;

    mr_mip->SS=FALSE;
    if (m<0)
    { /* its a supersingular curve! */
        mr_mip->SS=TRUE;
        type=MR_AFFINE;     /* always AFFINE */
        m=-m;
        if (size(a2)!=1) return FALSE;
        if (size(a6) >1) return FALSE;
    }
    if (size(a2)<0) return FALSE;
    if (size(a6)<0) return FALSE;
    MR_IN(123)

    if (!prepare_basis(_MIPP_ m,a,b,c,check))
    { /* unable to set the basis */
        MR_OUT
        return FALSE;
    }    

    mr_mip->Asize=size(a2);    
    mr_mip->Bsize=size(a6);

    if (mr_mip->Asize==MR_TOOBIG)
    { 
        if (mr_mip->A==NULL) mr_mip->A=mirvar(_MIPP_ 0);
        copy(a2,mr_mip->A);
    }

    if (mr_mip->Bsize==MR_TOOBIG)
    { 
        if (mr_mip->B==NULL) mr_mip->B=mirvar(_MIPP_ 0);
        copy(a6,mr_mip->B);
    }

 /* Use C to store B^(2^M-2) - required for projective doubling */
    if (!mr_mip->SS)
    {
        if (mr_mip->C==NULL) mr_mip->C=mirvar(_MIPP_ 0);
        copy(a6,mr_mip->C);
        for (i=1;i<m-1;i++) modsquare2(_MIPP_ mr_mip->C,mr_mip->C);
    }

    mr_mip->coord=type;
    MR_OUT
    return TRUE;
}    

epoint* epoint2_init(_MIPDO_ )
{
    epoint *p;
    char *ptr;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return NULL;
    
    MR_IN(124)

/* Create space for whole structure in one heap access */
    if (mr_mip->coord!=MR_AFFINE)
        p=(epoint *)mr_alloc(_MIPP_ sizeof(epoint)+3*mr_mip->size,1);
    else
        p=(epoint *)mr_alloc(_MIPP_ sizeof(epoint)+2*mr_mip->size,1);

    ptr=(char *)p+sizeof(epoint);
    p->X=mirvar_mem(_MIPP_ ptr,0);
    p->Y=mirvar_mem(_MIPP_ ptr,1);
    if (mr_mip->coord!=MR_AFFINE) p->Z=mirvar_mem(_MIPP_ ptr,2);
    else p->Z=NULL;
    p->marker=MR_EPOINT_INFINITY;
    
    MR_OUT

    return p;
}

void epoint2_free(epoint *p)
{ /* clean up point */
    zero(p->X);
    zero(p->Y);
    if (p->marker==MR_EPOINT_GENERAL) zero(p->Z);
    mr_free(p);
}

BOOL epoint2_set(_MIPD_ big x,big y,int cb,epoint *p)
{ /* initialise a point on active ecurve            *
   * if x or y == NULL, set to point at infinity    *
   * if x==y, a y co-ordinate is calculated - if    *
   * possible - and cb suggests LSB 0/1  of y/x     *
   * (which "decompresses" y). Otherwise, check     *
   * validity of given (x,y) point, ignoring cb.    *
   * Returns TRUE for valid point, otherwise FALSE. */
  
    BOOL valid;
   
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return FALSE;

    MR_IN(125)

    if (x==NULL || y==NULL)
    {
        convert(_MIPP_ 1,p->X);
        convert(_MIPP_ 1,p->Y);
        p->marker=MR_EPOINT_INFINITY;
        MR_OUT
        return TRUE;
    }

    valid=FALSE;       

    if (mr_mip->SS)
    { /* Super-singular - calculate x^3+x+B */
        copy (x,p->X);
        modsquare2(_MIPP_ p->X,mr_mip->w5);           /* w5=x^2 */
        modmult2(_MIPP_ mr_mip->w5,p->X,mr_mip->w5);  /* w5=x^3 */
        add2(mr_mip->w5,p->X,mr_mip->w5);             
        incr2(mr_mip->w5,mr_mip->Bsize,mr_mip->w5);  /* w5=x^3+x+B */
        if (x!=y)
        { /* compare with y^2+y */
            copy(y,p->Y);
            modsquare2(_MIPP_ p->Y,mr_mip->w1);
            add2(mr_mip->w1,p->Y,mr_mip->w1);
            if (compare(mr_mip->w1,mr_mip->w5)==0) valid=TRUE;
        }
        else
        { /* no y supplied - calculate one. Solve quadratic */
            valid=quad2(_MIPP_ mr_mip->w5,mr_mip->w5);
            incr2(mr_mip->w5,cb^parity2(mr_mip->w5),p->Y);
        }
    } 
    else
    { /* calculate x^3+Ax^2+B */
        copy(x,p->X);

        modsquare2(_MIPP_ p->X,mr_mip->w6);           /* w6=x^2 */
        modmult2(_MIPP_ mr_mip->w6,p->X,mr_mip->w5);  /* w5=x^3 */

        if (mr_mip->Asize==MR_TOOBIG)
            copy(mr_mip->A,mr_mip->w1);
        else
            convert(_MIPP_ mr_mip->Asize,mr_mip->w1);
        modmult2(_MIPP_ mr_mip->w6,mr_mip->w1,mr_mip->w0);
        add2(mr_mip->w5,mr_mip->w0,mr_mip->w5);

        if (mr_mip->Bsize==MR_TOOBIG)
            add2(mr_mip->w5,mr_mip->B,mr_mip->w5);    /* w5=x^3+Ax^2+B */
        else
            incr2(mr_mip->w5,mr_mip->Bsize,mr_mip->w5); 
        if (x!=y)
        { /* compare with y^2+xy */
            copy(y,p->Y);
            modsquare2(_MIPP_ p->Y,mr_mip->w2);
            modmult2(_MIPP_ p->Y,p->X,mr_mip->w1);
            add2(mr_mip->w1,mr_mip->w2,mr_mip->w1);
            if (compare(mr_mip->w1,mr_mip->w5)==0) valid=TRUE;
        }
        else
        { /* no y supplied - calculate one. Solve quadratic */
            if (size(p->X)==0) 
            {
                if (mr_mip->Bsize==MR_TOOBIG) 
                    copy(mr_mip->B,mr_mip->w1);
                else convert(_MIPP_ mr_mip->Bsize,mr_mip->w1); 

                sqroot2(_MIPP_ mr_mip->w1,p->Y);
                valid=TRUE;
            }
            else
            {
                inverse2(_MIPP_ mr_mip->w6,mr_mip->w6);  /* 1/x^2 */
                modmult2(_MIPP_ mr_mip->w5,mr_mip->w6,mr_mip->w5);
                valid=quad2(_MIPP_ mr_mip->w5,mr_mip->w5);     
                incr2(mr_mip->w5,cb^parity2(mr_mip->w5),mr_mip->w5);
                modmult2(_MIPP_ mr_mip->w5,p->X,p->Y);
            }
        }
    }
    if (valid)
    {
        p->marker=MR_EPOINT_NORMALIZED;
        MR_OUT
        return TRUE;
    }
    MR_OUT
    return FALSE;
}

BOOL epoint2_norm(_MIPD_ epoint *p)
{ /* normalise a point */
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif

    if (mr_mip->coord==MR_AFFINE) return TRUE;
    if (p->marker!=MR_EPOINT_GENERAL) return TRUE;

    if (mr_mip->ERNUM) return FALSE;

    MR_IN(126)

    if (!inverse2(_MIPP_ p->Z,mr_mip->w8))
    {
        MR_OUT
        return FALSE;
    }

    modsquare2(_MIPP_ mr_mip->w8,mr_mip->w1);          /* 1/ZZ */
    modmult2(_MIPP_ p->X,mr_mip->w1,p->X);             /* X/ZZ */
    modmult2(_MIPP_ mr_mip->w1,mr_mip->w8,mr_mip->w1); /* 1/ZZZ */ 
    modmult2(_MIPP_ p->Y,mr_mip->w1,p->Y);             /* Y/ZZZ */
    convert(_MIPP_ 1,p->Z);

    p->marker=MR_EPOINT_NORMALIZED;
    MR_OUT
    return TRUE;
}

void epoint2_getxyz(_MIPD_ epoint* p,big x,big y,big z)
{
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    convert(_MIPP_ 1,mr_mip->w1);
    if (p->marker==MR_EPOINT_INFINITY)
    {
        if (mr_mip->coord==MR_AFFINE)
        { /* (0,0) = O */
            if (x!=NULL) zero(x);
            if (y!=NULL) zero(y);
        }
        if (mr_mip->coord==MR_PROJECTIVE)
        { /* (1,1,0) = O */
            if (x!=NULL) copy(mr_mip->w1,x);
            if (y!=NULL) copy(mr_mip->w1,y);
        }
        if (z!=NULL) zero(z);
        return;
    }
    if (x!=NULL) copy(p->X,x);
    if (y!=NULL) copy(p->Y,y);
    if (mr_mip->coord==MR_AFFINE)
    {
        if (z!=NULL) zero(z);
    }
    if (mr_mip->coord==MR_PROJECTIVE)
    {
        if (z!=NULL)
        {
            if (p->marker!=MR_EPOINT_GENERAL) copy(mr_mip->w1,z);
            else copy(p->Z,z);
        }
    }
    return;
}

int epoint2_get(_MIPD_ epoint* p,big x,big y)
{ /* Get point co-ordinates in affine, normal form       *
   * (converted from projective form). If x==y, supplies *
   * x only. Return value is LSB of y/x (useful for      *
   * point compression)                                  */
    int lsb;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    
    if (p->marker==MR_EPOINT_INFINITY)
    {
        zero(x);
        zero(y);
        return 0;
    }
    if (mr_mip->ERNUM) return 0;

    MR_IN(127)

    epoint2_norm(_MIPP_ p);

    copy(p->X,x);
    copy(p->Y,mr_mip->w5);

    if (x!=y) copy(mr_mip->w5,y);
    if (size(x)==0)
    {
        MR_OUT
        return 0;
    }
    if (mr_mip->SS)
    {
        lsb=parity2(p->Y);
    }
    else
    {
        inverse2(_MIPP_ x,mr_mip->w5);
        modmult2(_MIPP_ mr_mip->w5,p->Y,mr_mip->w5);

        lsb=parity2(mr_mip->w5);
    }
    MR_OUT
    return lsb;
}

static void ecurve2_double(_MIPD_ epoint *p)
{ /* double epoint on active curve */
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif

    if (p->marker==MR_EPOINT_INFINITY)
    { /* 2 times infinity == infinity! */
        return;
    }

    if (mr_mip->coord==MR_AFFINE)
    {
        if (mr_mip->SS)
        { /* super-singular */
            modsquare2(_MIPP_ p->X,p->X);
            incr2(p->X,1,mr_mip->w8);
            modsquare2(_MIPP_ p->X,p->X);
            modsquare2(_MIPP_ p->Y,p->Y);
            modsquare2(_MIPP_ p->Y,p->Y);
            add2(p->Y,p->X,p->Y);   /* y=x^4+y^4   */
            incr2(p->X,1,p->X);     /* x=x^4+1     */
            return;
        }    

        if (size(p->X)==0)
        { /* set to point at infinity */
            epoint2_set(_MIPP_ NULL,NULL,0,p);
            return;
        }
   
        inverse2(_MIPP_ p->X,mr_mip->w8);
  
        modmult2(_MIPP_ mr_mip->w8,p->Y,mr_mip->w8);
        add2(mr_mip->w8,p->X,mr_mip->w8);   /* w8 is slope m */
  
        modsquare2(_MIPP_ mr_mip->w8,mr_mip->w6);  /* w6 =m^2 */
        add2(mr_mip->w6,mr_mip->w8,mr_mip->w1);
        if (mr_mip->Asize==MR_TOOBIG)
            add2(mr_mip->w1,mr_mip->A,mr_mip->w1); 
        else
            incr2(mr_mip->w1,mr_mip->Asize,mr_mip->w1); /* w1 = x3 */

        add2(p->X,mr_mip->w1,mr_mip->w6);
        modmult2(_MIPP_ mr_mip->w6,mr_mip->w8,mr_mip->w6);
        copy(mr_mip->w1,p->X);
        add2(mr_mip->w6,mr_mip->w1,mr_mip->w6);
        add2(p->Y,mr_mip->w6,p->Y);
        return;
    }

    if (size(p->X)==0)
    { /* set to infinity */
        epoint2_set(_MIPP_ NULL,NULL,0,p);
        return;
    }

    if (p->marker!=MR_EPOINT_NORMALIZED)
    {
        modmult2(_MIPP_ p->Y,p->Z,p->Y);             /* t2 = t2 * t3 */
        modsquare2(_MIPP_ p->Z,p->Z);                /* t3 = t3^2 */
        modmult2(_MIPP_ mr_mip->C,p->Z,mr_mip->w4);  /* t4 = c * t3 */ 
        modmult2(_MIPP_ p->Z,p->X,p->Z);             /* t3 = t3 * t1 */
    }
    else
    {
        copy(mr_mip->C,mr_mip->w4);
        copy(p->X,p->Z);
    }
    add2(p->Y,p->Z,p->Y);                 /* t2 = t2 + t3 */
    add2(mr_mip->w4,p->X,mr_mip->w4);     /* t4 = t4 + t1 */
    modsquare2(_MIPP_ mr_mip->w4,mr_mip->w4);    /* t4 = t4^2 */ 
    modsquare2(_MIPP_ mr_mip->w4,mr_mip->w4);    /* t4 = t4^2 */
    modsquare2(_MIPP_ p->X,mr_mip->w1);          /* t1 = t1^2 */
    add2(p->Y,mr_mip->w1,p->Y);           /* t2 = t2 + t1 */
    modmult2(_MIPP_ p->Y,mr_mip->w4,p->Y);       /* t2 = t2 * t4 */
    modsquare2(_MIPP_ mr_mip->w1,mr_mip->w1);    /* t1 = t1^2 */
    modmult2(_MIPP_ mr_mip->w1,p->Z,mr_mip->w1); /* t1 = t1 * t3 */
    add2(p->Y,mr_mip->w1,p->Y);           /* t2 = t2 + t1 */
    copy(mr_mip->w4,p->X);

    p->marker=MR_EPOINT_GENERAL;
}

static BOOL ecurve2_padd(_MIPD_ epoint *p,epoint *pa)
{ /* primitive add two epoints on the active ecurve pa+=p      *
   * note that if p is normalized, its Z coordinate isn't used */
 
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->coord==MR_AFFINE)
    {
        add2(p->Y,pa->Y,mr_mip->w8);
        add2(p->X,pa->X,mr_mip->w6);
        if (size(mr_mip->w6)==0)
        {  /* divide by zero */
            if (size(mr_mip->w8)==0)
            { /* should have doubled! */
                return FALSE;
            }
            else
            { /* point at infinity */
                epoint2_set(_MIPP_ NULL,NULL,0,pa);
                return TRUE;
            }
        }
        inverse2(_MIPP_ mr_mip->w6,mr_mip->w5);

        modmult2(_MIPP_ mr_mip->w8,mr_mip->w5,mr_mip->w8); /* w8=m */
        modsquare2(_MIPP_ mr_mip->w8,mr_mip->w5);          /* m^2  */

        if (mr_mip->SS)
        {
             add2(pa->X,p->X,pa->X);
             add2(pa->X,mr_mip->w5,pa->X);

             add2(pa->X,p->X,pa->Y);
             modmult2(_MIPP_ pa->Y,mr_mip->w8,pa->Y);
             add2(pa->Y,p->Y,pa->Y);
             incr2(pa->Y,1,pa->Y);
        }
        else
        {
            add2(mr_mip->w5,mr_mip->w8,mr_mip->w5);
            add2(mr_mip->w5,mr_mip->w6,mr_mip->w5);
            if (mr_mip->Asize==MR_TOOBIG)
                add2(mr_mip->w5,mr_mip->A,mr_mip->w5);
            else
                incr2(mr_mip->w5,mr_mip->Asize,mr_mip->w5); /* w5=x3 */
        
            add2(pa->X,mr_mip->w5,mr_mip->w6);
            modmult2(_MIPP_ mr_mip->w6,mr_mip->w8,mr_mip->w6);
            copy(mr_mip->w5,pa->X);
            add2(mr_mip->w6,mr_mip->w5,mr_mip->w6);
            add2(pa->Y,mr_mip->w6,pa->Y);
        }
        pa->marker=MR_EPOINT_NORMALIZED;
        return TRUE;
    }

    if (p->marker!=MR_EPOINT_NORMALIZED)
    {
        modsquare2(_MIPP_ p->Z,mr_mip->w6);           /* t7 = t6^2    */
        modmult2(_MIPP_ pa->X,mr_mip->w6,mr_mip->w1); /* t1 = t1 * t7 */
        modmult2(_MIPP_ mr_mip->w6,p->Z,mr_mip->w6);  /* t7 = t7 * t6 */
        modmult2(_MIPP_ pa->Y,mr_mip->w6,mr_mip->w2); /* t2 = t2 * t7 */ 
    }
    else
    {
        copy(pa->X,mr_mip->w1);
        copy(pa->Y,mr_mip->w2);
    }
    if (pa->marker==MR_EPOINT_NORMALIZED)
        convert(_MIPP_ 1,mr_mip->w6);
    else
        modsquare2(_MIPP_ pa->Z,mr_mip->w6);           /* t7 = t3^2    */
    modmult2(_MIPP_ mr_mip->w6,p->X,mr_mip->w8);       /* t8 = t4 * t7 */
    add2(mr_mip->w1,mr_mip->w8,mr_mip->w1);     /* t1 = t1 + t8 */
    if (pa->marker!=MR_EPOINT_NORMALIZED)
        modmult2(_MIPP_ mr_mip->w6,pa->Z,mr_mip->w6);  /* t7 = t7 * t3 */
    modmult2(_MIPP_ mr_mip->w6,p->Y,mr_mip->w8);       /* t8 = t7 * t5 */
    add2(mr_mip->w2,mr_mip->w8,mr_mip->w2);     /* t2 = t2 + t8 */
    if (size(mr_mip->w1)==0)
    {
        if (size(mr_mip->w2)==0)
        { /* should have doubled! */
            return FALSE;
        }
        else
        { /* point at infinity */
            epoint2_set(_MIPP_ NULL,NULL,0,pa);
            return TRUE;
        }
    }
    modmult2(_MIPP_ p->X,mr_mip->w2,mr_mip->w4);      /* t4 = t2 * t4 */
    if (pa->marker!=MR_EPOINT_NORMALIZED)
        modmult2(_MIPP_ pa->Z,mr_mip->w1,mr_mip->w3);  /* t3 = t3 * t1 */
    else 
        copy(mr_mip->w1,mr_mip->w3);
    modmult2(_MIPP_ p->Y,mr_mip->w3,mr_mip->w5);      /* t5 = t5 * t3 */
    add2(mr_mip->w4,mr_mip->w5,mr_mip->w4);           /* t4 = t4 + t5 */
    modsquare2(_MIPP_ mr_mip->w3,mr_mip->w5);         /* t5 = t3^2    */
    modmult2(_MIPP_ mr_mip->w4,mr_mip->w5,mr_mip->w6); /* t7 = t4 * t5 */

    if (p->marker!=MR_EPOINT_NORMALIZED) 
        modmult2(_MIPP_ mr_mip->w3,p->Z,mr_mip->w3);  /* t3 = t3 * t6 */
    add2(mr_mip->w2,mr_mip->w3,mr_mip->w4);    /* t4 = t2 + t3 */
    modmult2(_MIPP_ mr_mip->w2,mr_mip->w4,mr_mip->w2);/* t2 = t2 * t4 */
    modsquare2(_MIPP_ mr_mip->w1,mr_mip->w5);         /* t5 = t1^2    */
    modmult2(_MIPP_ mr_mip->w1,mr_mip->w5,mr_mip->w1);/* t1 = t1 * t5 */
    if (mr_mip->Asize>0)
    {
        modsquare2(_MIPP_ mr_mip->w3,mr_mip->w8);     /* t8 = t3^2    */
        if (mr_mip->Asize>1)
        {
            if (mr_mip->Asize==MR_TOOBIG)
                copy(mr_mip->A,mr_mip->w5);
            else 
                convert(_MIPP_ mr_mip->Asize,mr_mip->w5);
            modmult2(_MIPP_ mr_mip->w8,mr_mip->w5,mr_mip->w8);
        }
        add2(mr_mip->w1,mr_mip->w8,mr_mip->w1);/* t1 = t1 + t8 */
    }
    add2(mr_mip->w1,mr_mip->w2,pa->X);         /* t1 = t1 + t2 */
    modmult2(_MIPP_ mr_mip->w4,pa->X,mr_mip->w4);/* t4 = t4 * t1 */
    add2(mr_mip->w4,mr_mip->w6,pa->Y);         /* t2 = t4 + t7 */
    copy(mr_mip->w3,pa->Z);

    pa->marker=MR_EPOINT_GENERAL;
    return TRUE;
}

void epoint2_copy(epoint *a,epoint *b)
{   
    if (a==b) return;
    copy(a->X,b->X);
    copy(a->Y,b->Y);
    if (a->marker==MR_EPOINT_GENERAL) copy(a->Z,b->Z);
    b->marker=a->marker;
    return;
}

BOOL epoint2_comp(_MIPD_ epoint *a,epoint *b)
{
    int ia,ib;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return FALSE;
    if (a==b) return TRUE;

    if (a->marker==MR_EPOINT_INFINITY)
    {
        if (b->marker==MR_EPOINT_INFINITY) return TRUE;
        else return FALSE;
    } 
    if (b->marker==MR_EPOINT_INFINITY)
        return FALSE;

    MR_IN(128)

    ia=epoint2_get(_MIPP_ a,mr_mip->w9,mr_mip->w9);
    ib=epoint2_get(_MIPP_ b,mr_mip->w10,mr_mip->w10);

    MR_OUT
    if (ia==ib && compare(mr_mip->w9,mr_mip->w10)==0) return TRUE;
    return FALSE;
}

big ecurve2_add(_MIPD_ epoint *p,epoint *pa)
{  /* pa=pa+p; */
   /* An ephemeral pointe to the line slope is returned *
    * only if curve is super-singular                   */
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return NULL;

    MR_IN(129)

    if (p==pa) 
    {
        ecurve2_double(_MIPP_ pa);
        MR_OUT
        return mr_mip->w8;
    }
    if (pa->marker==MR_EPOINT_INFINITY)
    {
        epoint2_copy(p,pa);
        MR_OUT 
        return NULL;
    }
    if (p->marker==MR_EPOINT_INFINITY) 
    {
        MR_OUT
        return NULL;
    }
    if (!ecurve2_padd(_MIPP_ p,pa)) ecurve2_double(_MIPP_ pa);
    MR_OUT
    return mr_mip->w8;
}

void epoint2_negate(_MIPD_ epoint *p)
{ /* negate a point */
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;
    if (p->marker==MR_EPOINT_INFINITY) return;
    MR_IN(130)
    if (p->marker==MR_EPOINT_GENERAL)
    {
        modmult2(_MIPP_ p->X,p->Z,mr_mip->w1);
        add2(p->Y,mr_mip->w1,p->Y);
    }
    else 
    {
        if (mr_mip->SS)  incr2(p->Y,1,p->Y);
        else             add2(p->Y,p->X,p->Y);
                        
    }
    MR_OUT
}

big ecurve2_sub(_MIPD_ epoint *p,epoint *pa)
{
    big r;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return NULL;

    MR_IN(131)

    if (p==pa)
    {
        epoint2_set(_MIPP_ NULL,NULL,0,pa);
        MR_OUT
        return NULL;
    } 
    if (p->marker==MR_EPOINT_INFINITY) 
    {
        MR_OUT
        return NULL;
    }

    epoint2_negate(_MIPP_ p);
    r=ecurve2_add(_MIPP_ p,pa);
    epoint2_negate(_MIPP_ p);

    MR_OUT
    return r;
}

void ecurve2_multi_add(_MIPD_ int m,epoint **x,epoint **w)
{ /* adds m points together simultaneously, w[i]+=x[i] */
    int i,*flag;
    big *A,*B,*C;
    char *mem;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;

    MR_IN(132)
    if (mr_mip->coord==MR_AFFINE && !mr_mip->SS)
    {
        A=(big *)mr_alloc(_MIPP_ m,sizeof(big));
        B=(big *)mr_alloc(_MIPP_ m,sizeof(big));
        C=(big *)mr_alloc(_MIPP_ m,sizeof(big));
        flag=(int *)mr_alloc(_MIPP_ m,sizeof(int));

        convert(_MIPP_ 1,mr_mip->w3);  /* unity */
        mem=memalloc(_MIPP_ 3*m);

        for (i=0;i<m;i++)
        {
            A[i]=mirvar_mem(_MIPP_ mem,3*i);
            B[i]=mirvar_mem(_MIPP_ mem,3*i+1);
            C[i]=mirvar_mem(_MIPP_ mem,3*i+2);
            flag[i]=0;
            if (compare(x[i]->X,w[i]->X)==0 && compare(x[i]->Y,w[i]->Y)==0)
            { /* doubling */
                if (x[i]->marker==MR_EPOINT_INFINITY || size(x[i]->Y)==0)
                {
                    flag[i]=1;     /* result is infinity */
                    copy(mr_mip->w3,B[i]);
                    continue;
                }
                modsquare2(_MIPP_ x[i]->X,A[i]);
                add2(A[i],x[i]->Y,A[i]);
                copy(x[i]->X,B[i]);
            }
            else
            {
                if (x[i]->marker==MR_EPOINT_INFINITY)
                {
                    flag[i]=2;                    /* w[i] unchanged */
                    copy(mr_mip->w3,B[i]);
                    continue;
                }
                if (w[i]->marker==MR_EPOINT_INFINITY)
                {
                    flag[i]=3;                    /* w[i]=x[i] */
                    copy(mr_mip->w3,B[i]);
                    continue;
                }
                add2(x[i]->X,w[i]->X,B[i]);
                if (size(B[i])==0)
                { /* point at infinity */
                    flag[i]=1;                /* result is infinity */
                    copy(mr_mip->w3,B[i]);
                    continue;
                }
                add2(x[i]->Y,w[i]->Y,A[i]);
            }
        }

        multi_inverse2(_MIPP_ m,B,C); /* one inversion only */
        for (i=0;i<m;i++)
        {
            if (flag[i]==1)
            { /* point at infinity */
                epoint2_set(_MIPP_ NULL,NULL,0,w[i]);
                continue;
            }
            if (flag[i]==2)
            {
                continue;
            }
            if (flag[i]==3)
            {
                epoint2_copy(x[i],w[i]);
                continue;
            }
            modmult2(_MIPP_ A[i],C[i],mr_mip->w8);
            modsquare2(_MIPP_ mr_mip->w8,mr_mip->w6); /* m^2 */
            add2(mr_mip->w6,mr_mip->w8,mr_mip->w6);
            add2(mr_mip->w6,x[i]->X,mr_mip->w6);
            add2(mr_mip->w6,w[i]->X,mr_mip->w6);
            if (mr_mip->Asize==MR_TOOBIG)
                add2(mr_mip->w6,mr_mip->A,mr_mip->w6);
            else
                incr2(mr_mip->w6,mr_mip->Asize,mr_mip->w6);

            add2(w[i]->X,mr_mip->w6,mr_mip->w2);
            modmult2(_MIPP_ mr_mip->w2,mr_mip->w8,mr_mip->w2);
            add2(mr_mip->w2,mr_mip->w6,mr_mip->w2);
            add2(mr_mip->w2,w[i]->Y,w[i]->Y);
            copy(mr_mip->w6,w[i]->X);

            w[i]->marker=MR_EPOINT_GENERAL;

        }
        memkill(_MIPP_ mem,3*m);
        mr_free(flag);
        mr_free(C); mr_free(B); mr_free(A);
    }
    else
    { /* no speed-up for projective coordinates */
        for (i=0;i<m;i++) ecurve2_add(_MIPP_ x[i],w[i]);
    }
    MR_OUT
}

void ecurve2_mult(_MIPD_ big e,epoint *pa,epoint *pt)
{ /* pt=e*pa; */
    int i,j,n,ch,ce,nb,nbs,nzs;
    epoint *p;
    epoint *table[11];
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;

    MR_IN(133)

    if (size(e)==0) 
    { /* multiplied by 0 */
        epoint2_set(_MIPP_ NULL,NULL,0,pt);
        MR_OUT
        return;
    }
    copy(e,mr_mip->w9);
    epoint2_norm(_MIPP_ pa);
    epoint2_copy(pa,pt);

    if (size(mr_mip->w9)<0)
    { /* pt = -pt */
        negify(mr_mip->w9,mr_mip->w9);
        epoint2_negate(_MIPP_ pt);
    }

    if (size(mr_mip->w9)==1)
    { 
        MR_OUT
        return;
    }

    premult(_MIPP_ mr_mip->w9,3,mr_mip->w10);      /* h=3*e */
    p=epoint2_init(_MIPPO_ );
    epoint2_copy(pt,p);

#ifndef MR_ALWAYS_BINARY
    if (mr_mip->base==mr_mip->base2)
    {
#endif

        table[0]=epoint2_init(_MIPPO_ );
        epoint2_copy(p,table[0]);
        ecurve2_double(_MIPP_ p);

        for (i=1;i<=10;i++)
        { /* precomputation */
            table[i]=epoint2_init(_MIPPO_ );
            epoint2_copy(table[i-1],table[i]);
            ecurve2_add(_MIPP_ p,table[i]);
        }

  /* note that normalising this table doesn't really help */
        nb=logb2(_MIPP_ mr_mip->w10);

        for (i=nb-2;i>=1;)
        { /* add/subtract */
            if (mr_mip->user!=NULL) (*mr_mip->user)();
            n=mr_naf_window(_MIPP_ mr_mip->w9,mr_mip->w10,i,&nbs,&nzs);
            for (j=0;j<nbs;j++)
                ecurve2_double(_MIPP_ pt);
            if (n>0) 
                ecurve2_add(_MIPP_ table[n/2],pt);
            if (n<0) 
                 ecurve2_sub(_MIPP_ table[(-n)/2],pt);
            i-=nbs;
            if (nzs)
            {
                for (j=0;j<nzs;j++) ecurve2_double(_MIPP_ pt);
                i-=nzs;
            }
        }
        for (i=10;i>=0;i--) epoint2_free(table[i]);
#ifndef MR_ALWAYS_BINARY
    }
    else
    { 
        expint(_MIPP_ 2,logb2(_MIPP_ mr_mip->w10)-1,mr_mip->w11);
        mr_psub(_MIPP_ mr_mip->w10,mr_mip->w11,mr_mip->w10);
        subdiv(_MIPP_ mr_mip->w11,2,mr_mip->w11);
        while (size(mr_mip->w11) > 1)
        { /* add/subtract method */
            if (mr_mip->user!=NULL) (*mr_mip->user)();

            ecurve2_double(_MIPP_ pt);
            ce=compare(mr_mip->w9,mr_mip->w11); /* e(i)=1? */
            ch=compare(mr_mip->w10,mr_mip->w11); /* h(i)=1? */
            if (ch>=0) 
            {  /* h(i)=1 */
                if (ce<0) ecurve2_add(_MIPP_ p,pt);
                mr_psub(_MIPP_ mr_mip->w10,mr_mip->w11,mr_mip->w10);
            }
            if (ce>=0) 
            {  /* e(i)=1 */
                if (ch<0) ecurve2_sub(_MIPP_ p,pt);
                mr_psub(_MIPP_ mr_mip->w9,mr_mip->w11,mr_mip->w9);  
            }
            subdiv(_MIPP_ mr_mip->w11,2,mr_mip->w11);
        }
    }
#endif
    epoint2_free(p);
    MR_OUT
}

void ecurve2_multn(_MIPD_ int n,big *y,epoint **x,epoint *w)
{ /* pt=e[o]*p[0]+e[1]*p[1]+ .... e[n-1]*p[n-1]   */
    int i,j,k,m,nb,ea;
    epoint **G;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;

    MR_IN(134)

    m=1<<n;
    G=(epoint **)mr_alloc(_MIPP_ m,sizeof(epoint*));

    for (i=0,k=1;i<n;i++)
    {
        for (j=0; j < (1<<i) ;j++)
        {
            G[k]=epoint2_init(_MIPPO_ );
            epoint2_copy(x[i],G[k]);
            if (j!=0) ecurve2_add(_MIPP_ G[j],G[k]);
            k++;
        }
    }

    nb=0;
    for (j=0;j<n;j++) if ((k=logb2(_MIPP_ y[j])) > nb) nb=k;

    epoint2_set(_MIPP_ NULL,NULL,0,w);            /* w=0 */
    
#ifndef MR_ALWAYS_BINARY
    if (mr_mip->base==mr_mip->base2)
    {
#endif
        for (i=nb-1;i>=0;i--)
        {
            if (mr_mip->user!=NULL) (*mr_mip->user)();
            ea=0;
            k=1;
            for (j=0;j<n;j++)
            {
                if (mr_testbit(_MIPP_ y[j],i)) ea+=k;
                k<<=1;
            }
            ecurve2_double(_MIPP_ w);
            if (ea!=0) ecurve2_add(_MIPP_ G[ea],w);
        }    
#ifndef MR_ALWAYS_BINARY
    }
    else mr_berror(_MIPP_ MR_ERR_NOT_SUPPORTED);
#endif

    for (i=1;i<m;i++) epoint2_free(G[i]);
    mr_free(G);
    MR_OUT
}

void ecurve2_mult2(_MIPD_ big e,epoint *p,big ea,epoint *pa,epoint *pt)
{ /* pt=e*p+ea*pa; */
    int e1,h1,e2,h2;
    epoint *p1,*p2,*ps,*pd;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;

    MR_IN(135)

    if (size(e)==0) 
    {
        ecurve2_mult(_MIPP_ ea,pa,pt);
        MR_OUT
        return;
    }

    p2=epoint2_init(_MIPPO_ );
    epoint2_norm(_MIPP_ pa);
    epoint2_copy(pa,p2);
    copy(ea,mr_mip->w9);
    if (size(mr_mip->w9)<0)
    { /* p2 = -p2 */
        negify(mr_mip->w9,mr_mip->w9);
        epoint2_negate(_MIPP_ p2);
    }
    premult(_MIPP_ mr_mip->w9,3,mr_mip->w10);      /* 3*ea */

    p1=epoint2_init(_MIPPO_ );
    epoint2_norm(_MIPP_ p);
    epoint2_copy(p,p1);
    copy(e,mr_mip->w12);
    if (size(mr_mip->w12)<0)
    { /* p1= -p1 */
        negify(mr_mip->w12,mr_mip->w12);
        epoint2_negate(_MIPP_ p1);
    }
    premult(_MIPP_ mr_mip->w12,3,mr_mip->w13);    /* 3*e */

    epoint2_set(_MIPP_ NULL,NULL,0,pt);            /* pt=0 */

    if (compare(mr_mip->w10,mr_mip->w13)>=0)
         expint(_MIPP_ 2,logb2(_MIPP_ mr_mip->w10)-1,mr_mip->w11);
    else expint(_MIPP_ 2,logb2(_MIPP_ mr_mip->w13)-1,mr_mip->w11);

    ps=epoint2_init(_MIPPO_ );
    pd=epoint2_init(_MIPPO_ );

    epoint2_copy(p1,ps);
    ecurve2_add(_MIPP_ p2,ps);                    /* ps=p1+p2 */
    epoint2_copy(p1,pd);
    ecurve2_sub(_MIPP_ p2,pd);                    /* pd=p1-p2 */
    epoint2_norm(_MIPP_ ps);
    epoint2_norm(_MIPP_ pd);
    while (size(mr_mip->w11) > 1)
    { /* add/subtract method */
        if (mr_mip->user!=NULL) (*mr_mip->user)();

        ecurve2_double(_MIPP_ pt);

        e1=h1=e2=h2=0;
        if (compare(mr_mip->w9,mr_mip->w11)>=0)
        { /* e1(i)=1? */
            e2=1;  
            mr_psub(_MIPP_ mr_mip->w9,mr_mip->w11,mr_mip->w9);
        }
        if (compare(mr_mip->w10,mr_mip->w11)>=0)
        { /* h1(i)=1? */
            h2=1;  
            mr_psub(_MIPP_ mr_mip->w10,mr_mip->w11,mr_mip->w10);
        } 
        if (compare(mr_mip->w12,mr_mip->w11)>=0)
        { /* e2(i)=1? */
            e1=1;   
            mr_psub(_MIPP_ mr_mip->w12,mr_mip->w11,mr_mip->w12);
        }
        if (compare(mr_mip->w13,mr_mip->w11)>=0) 
        { /* h2(i)=1? */
            h1=1;  
            mr_psub(_MIPP_ mr_mip->w13,mr_mip->w11,mr_mip->w13);
        }

        if (e1!=h1)
        {
            if (e2==h2)
            {
                if (h1==1) ecurve2_add(_MIPP_ p1,pt);
                else       ecurve2_sub(_MIPP_ p1,pt);
            }
            else
            {
                if (h1==1)
                {
                    if (h2==1) ecurve2_add(_MIPP_ ps,pt);
                    else       ecurve2_add(_MIPP_ pd,pt);
                }
                else
                {
                    if (h2==1) ecurve2_sub(_MIPP_ pd,pt);
                    else       ecurve2_sub(_MIPP_ ps,pt);
                }
            }
        }
        else if (e2!=h2)
        {
            if (h2==1) ecurve2_add(_MIPP_ p2,pt);
            else       ecurve2_sub(_MIPP_ p2,pt);
        }

        subdiv(_MIPP_ mr_mip->w11,2,mr_mip->w11);
    }
    epoint2_free(p1);
    epoint2_free(p2);
    epoint2_free(ps);
    epoint2_free(pd);
    MR_OUT
}

/*   Routines to implement Brickell et al's method for fast
 *   computation of x*G mod n, for fixed G and n, using precomputation. 
 *
 *   Elliptic curve over GF(2^m) version of mrebrick.c
 *
 *   This idea can be used to substantially speed up certain phases 
 *   of the Digital Signature Standard (ECS) for example.
 *
 *   See "Fast Exponentiation with Precomputation"
 *   by E. Brickell et al. in Proceedings Eurocrypt 1992
 */

BOOL ebrick2_init(_MIPD_ ebrick2 *B,big x,big y,big a2,big a6,int m,int a,int b,int c,int nb)
{ /* (x,y) is the fixed base                            *
   * a2 and a6 the parameters of the curve              *
   * m, a, b, c are the m in the 2^m modulus, and a,b,c *
   * are the parameters of the irreducible bases,       *
   * trinomial if b!=0, otherwise pentanomial           *
   * nb is the maximum number of bits in the multiplier */

    int i,base,best,store,time;
    epoint *w;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (nb<2 || mr_mip->ERNUM) return FALSE;

    MR_IN(136)

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
    B->a6=mirvar(_MIPP_ 0);
    copy(a6,B->a6);
    B->a2=mirvar(_MIPP_ 0);
    copy(a2,B->a2);
    B->m=m;
    B->a=a;
    B->b=b;
    B->c=c;   

    if (!ecurve2_init(_MIPP_ m,a,b,c,a2,a6,TRUE,MR_AFFINE))
    {
        MR_OUT
        return FALSE;
    }
    w=epoint2_init(_MIPPO_ );
    B->table[0]=epoint2_init(_MIPPO_ );
    epoint2_set(_MIPP_ x,y,0,B->table[0]);

    for (i=1;i<store;i++) 
    { /* calculate look-up table */
        B->table[i]=epoint2_init(_MIPPO_ );
        convert(_MIPP_ base,mr_mip->w1);
        ecurve2_mult(_MIPP_ mr_mip->w1,B->table[i-1],w);
        epoint2_copy(w,B->table[i]);
    }
    epoint2_free(w);
    MR_OUT
    return TRUE;
}

void ebrick2_end(ebrick2 *B)
{
    int i;
    for (i=0;i<B->store;i++)
        epoint2_free(B->table[i]);
    mirkill(B->a2);
    mirkill(B->a6);
    mr_free(B->table);  
}

int mul2_brick(_MIPD_ ebrick2 *B,big e,big x,big y)
{
    int i,ndig,d;
    int *digits;
    epoint *w,*w1;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif

    if (size(e)<0) mr_berror(_MIPP_ MR_ERR_NEG_POWER);

    MR_IN(137)

    digits=mr_alloc(_MIPP_ B->store,sizeof(int));
    if (digits==NULL)
    {
        mr_berror(_MIPP_ MR_ERR_OUT_OF_MEMORY);
        MR_OUT
        return 0;        
    }

    if (!ecurve2_init(_MIPP_ B->m,B->a,B->b,B->c,B->a2,B->a6,FALSE,MR_PROJECTIVE))
    {
        MR_OUT
        return 0;
    }
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

    w=epoint2_init(_MIPPO_ );
    w1=epoint2_init(_MIPPO_ );

    for (d=B->base-1;d>0;d--)
    { /* brickell's method */
        for (i=0;i<ndig;i++)
        {
            if (mr_mip->user!=NULL) (*mr_mip->user)();
            if (digits[i]==d) ecurve2_add(_MIPP_ B->table[i],w1);
        }
        ecurve2_add(_MIPP_ w1,w);
    }
    d=epoint2_get(_MIPP_ w,x,y);
    epoint2_free(w1);
    epoint2_free(w);

    for (i=0;i<ndig;i++) digits[i]=0;
    mr_free(digits);
    MR_OUT
    return d;
}

#endif

