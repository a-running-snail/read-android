/*
 *  MIRACL Double to Flash conversion routines - use with care
 *  mrdouble.c
 *
 *  Copyright (c) 1988-2001 Shamus Software Ltd.
 *
 */

#include <math.h>
#include "miracl.h"

#ifdef MR_FLASH

#define mr_abs(x)  ((x)<0? (-(x)) : (x))
#define sign(x) ((x)<0? (-1) : 1)

static int dquot(_MIPD_ big x,int num)
{ /* generate c.f. for a double D */
    int m;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (num==0)
    {
        mr_mip->oldn=(-1);
        if (mr_mip->base==0) mr_mip->db=pow(2.0,(double)MIRACL);
        else mr_mip->db=(double)mr_mip->base;
        if (mr_mip->D<1.0)
        {
            mr_mip->D=(1.0/mr_mip->D);
            return (mr_mip->q=0);
        }
    }
    else if (mr_mip->q<0 || num==mr_mip->oldn) return mr_mip->q;
    mr_mip->oldn=num;
    if (mr_mip->D==0.0) return (mr_mip->q=(-1));
    mr_mip->D=modf(mr_mip->D,&mr_mip->n);  /* n is whole number part */
    m=0;           /* D is fractional part (or guard digits!) */
    zero(x);
    while (mr_mip->n>0.0)
    { /* convert n to big */
        m++;
        if (m>mr_mip->nib) return (mr_mip->q=(-2));
        mr_mip->p=mr_mip->n/mr_mip->db;
        modf(mr_mip->p,&mr_mip->p);
        x->w[m-1]=(mr_small)(mr_mip->n-mr_mip->db*mr_mip->p);
        mr_mip->n=mr_mip->p;
    }
    x->len=m;
    if (mr_mip->D>0.0) mr_mip->D=(1.0/mr_mip->D);
    return (mr_mip->q=size(x));
}

void dconv(_MIPD_ double d,flash w)
{ /* convert double to rounded flash */
    int s;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;

    MR_IN(32)

    zero(w);
    if (d==0.0)
    {
        MR_OUT
        return;
    }
    mr_mip->D=d;
    s=sign(mr_mip->D);
    mr_mip->D=mr_abs(mr_mip->D);
    build(_MIPP_ w,dquot);
    insign(s,w);

    MR_OUT
}
        
double fdsize(_MIPD_ flash w)
{ /* express flash number as double. */
    int i,s,en,ed;
    double n,d,b,BIGGEST;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM || size(w)==0) return (0.0);

    MR_IN(11)

    BIGGEST=pow(2.0,(double)(1<<(MR_EBITS-4)));
    mr_mip->EXACT=FALSE;
    n=0.0;
    d=0.0;
    if (mr_mip->base==0) b=pow(2.0,(double)MIRACL);
    else b=(double)mr_mip->base;
    numer(_MIPP_ w,mr_mip->w1);
    s=exsign(mr_mip->w1);
    insign(PLUS,mr_mip->w1);
    en=(int)mr_mip->w1->len;
    for (i=0;i<en;i++)
        n=(double)mr_mip->w1->w[i]+(n/b);
    denom(_MIPP_ w,mr_mip->w1);
    ed=(int)mr_mip->w1->len;
    for (i=0;i<ed;i++)
        d=(double)mr_mip->w1->w[i]+(d/b);
    n/=d;
    while (en!=ed)
    {
        if (en>ed)
        {
            ed++;
            if (BIGGEST/b<n)
            {
                mr_berror(_MIPP_ MR_ERR_DOUBLE_FAIL);
                MR_OUT
                return (0.0);
            }
            n*=b;
        }
        else
        {
            en++;
            if (n<b/BIGGEST)
            {
                mr_berror(_MIPP_ MR_ERR_DOUBLE_FAIL);
                MR_OUT
                return (0.0);
            }
            n/=b;
        }
    }
    n*=s;
    MR_OUT
    return n;
}

#endif

