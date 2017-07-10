/*
 *   MIRACL Core module - contains initialisation code and general purpose 
 *   utilities
 *   mrcore.c
 *
 *   Copyright (c) 1988-2000 Shamus Software Ltd.
 */

#include "miracl.h"
#include <stdlib.h>
#include <string.h>

/*** Multi-Threaded Support ***/

#ifndef MR_GENERIC_MT

  #ifdef MR_WINDOWS_MT
    #include <windows.h>
    DWORD mr_key;   

    miracl *get_mip()
    {
        return (miracl *)TlsGetValue(mr_key); 
    }

    void mr_init_threading()
    {
        mr_key=TlsAlloc();
    }

    void mr_end_threading()
    {
        TlsFree(mr_key);
    }

  #endif

  #ifdef MR_UNIX_MT
    #include <pthread.h>
    pthread_key_t mr_key;

    miracl *get_mip()
    {
        return (miracl *)pthread_getspecific(mr_key); 
    }

    void mr_init_threading()
    {
        pthread_key_create(&mr_key,(void(*)(void *))NULL);
    }

    void mr_end_threading()
    {
        pthread_key_delete(mr_key);
    }
  #endif

  #ifndef MR_WINDOWS_MT
    #ifndef MR_UNIX_MT
      miracl *mr_mip=NULL;     /* MIRACL's one and only global variable */

      miracl *get_mip()
      {
          return (miracl *)mr_mip; 
      }
    #endif
  #endif

#endif

/* See Advanced Windows by Jeffrey Richter, Chapter 12 for methods for
   creating different instances of this global for each executing thread 
   when using WIndows '95/NT
*/


#ifndef MR_STRIPPED_DOWN
#ifndef MR_NO_STANDARD_IO

static char *names[] =
{"your program","innum","otnum","jack","normalise",
"multiply","divide","incr","decr","premult",
"subdiv","fdsize","egcd","cbase",
"cinnum","cotnum","nroot","power",
"powmod","bigdig","bigrand","nxprime","isprime",
"mirvar","mad","multi_inverse","putdig",
"add","subtract","mirsys","xgcd",
"fpack","dconv","mr_shift","mround","fmul",
"fdiv","fadd","fsub","fcomp","fconv",
"frecip","fpmul","fincr","","ftrunc",
"frand","sftbit","build","logb2","expint",
"fpower","froot","fpi","fexp","flog","fpowf",
"ftan","fatan","fsin","fasin","fcos","facos",
"ftanh","fatanh","fsinh","fasinh","fcosh",
"facosh","flop","gprime","powltr","fft_mult",
"crt_init","crt","otstr","instr","cotstr","cinstr","powmod2",
"prepare_monty","nres","redc","nres_modmult","nres_powmod",
"nres_moddiv","nres_powltr","divisible","remain",
"fmodulo","nres_modadd","nres_modsub","nres_negate",
"ecurve_init","ecurve_add","ecurve_mult",
"epoint_init","epoint_set","epoint_get","nres_powmod2",
"nres_sqroot","sqroot","nres_premult","ecurve_mult2",
"ecurve_sub","trial_division","nxsafeprime","nres_lucas","lucas",
"brick_init","pow_brick","set_user_function",
"nres_powmodn","powmodn","ecurve_multn",
"ebrick_init","mul_brick","epoint_norm","nres_multi_inverse","",
"nres_dotprod","epoint_negate","ecurve_multi_add",
"ecurve2_init","epoint2_init","epoint2_set","epoint2_norm","epoint2_get",
"epoint2_comp","ecurve2_add","epoint2_negate","ecurve2_sub",
"ecurve2_multi_add","ecurve2_mult","ecurve2_multn","ecurve2_mult2",
"ebrick2_init","mul2_brick","prepare_basis","strong_bigrand",
"bytes_to_big","big_to_bytes","set_io_buffer_size",
"epoint_getxyz","epoint_double_add","nres_double_inverse",
"double_inverse"};
/* 0 - 146 (147 in all) */

#endif
#endif


#ifdef MR_NOASM

/* C only versions of muldiv/muldvd/muldvd2/muldvm */
/* Note that mr_large should be twice the size of mr_small */

mr_small muldiv(mr_small a,mr_small b,mr_small c,mr_small m,mr_small *rp)
{
    mr_small q;
    mr_large ldres,p=(mr_large)a*b+c;
    q=(mr_small)(MR_LROUND(p/m));
    *rp=(mr_small)(p-(mr_large)q*m);
    return q;
}

#ifdef MR_FP_ROUNDING

mr_small imuldiv(mr_small a,mr_small b,mr_small c,mr_small m,mr_large im,mr_small *rp)
{
    mr_small q;
    mr_large ldres,p=(mr_large)a*b+c;
    q=(mr_small)MR_LROUND(p*im);
    *rp=(mr_small)(p-(mr_large)q*m);
    return q;
}

#endif

#ifndef MR_NOFULLWIDTH

mr_small muldvm(mr_small a,mr_small c,mr_small m,mr_small *rp)
{
    mr_small q;
    union doubleword dble;
    dble.h[MR_BOT]=c;
    dble.h[MR_TOP]=a;

    q=(mr_small)(dble.d/m);
    *rp=(mr_small)(dble.d-(mr_large)q*m);
    return q;
}

mr_small muldvd(mr_small a,mr_small b,mr_small c,mr_small *rp)
{
    union doubleword dble;
    dble.d=(mr_large)a*b+c;

    *rp=dble.h[MR_BOT];
    return dble.h[MR_TOP];
}

void muldvd2(mr_small a,mr_small b,mr_small *c,mr_small *rp)
{
    union doubleword dble;
    dble.d=(mr_large)a*b+*c+*rp;
    *rp=dble.h[MR_BOT];
    *c=dble.h[MR_TOP];
}

#endif
#endif

#ifdef MR_NOFULLWIDTH

/* no FULLWIDTH working, so supply dummies */

/*

mr_small muldvd(mr_small a,mr_small b,mr_small c,mr_small *rp)
{
    return (mr_small)0;
}

mr_small muldvm(mr_small a,mr_small c,mr_small m,mr_small *rp)
{
    return (mr_small)0;
}

void muldvd2(mr_small a,mr_small b,mr_small *c,mr_small *rp)
{
}

*/

#endif

#ifndef MR_NO_STANDARD_IO

static void mputs(char *s)
{ /* output a string */
    int i=0;
    while (s[i]!=0) fputc((int)s[i++],stdout);
}

#endif

void mr_berror(_MIPD_ int nerr)
{  /*  Big number error routine  */
#ifndef MR_STRIPPED_DOWN
int i;
#endif

#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif

if (mr_mip->ERCON)
{
    mr_mip->ERNUM=nerr;
    return;
}
#ifndef MR_NO_STANDARD_IO

#ifndef MR_STRIPPED_DOWN
mputs("\nMIRACL error from routine ");
if (mr_mip->depth<MR_MAXDEPTH) mputs(names[mr_mip->trace[mr_mip->depth]]);
else                           mputs("???");
fputc('\n',stdout);

for (i=mr_mip->depth-1;i>=0;i--)
{
    mputs("              called from ");
    if (i<MR_MAXDEPTH) mputs(names[mr_mip->trace[i]]);
    else               mputs("???");
    fputc('\n',stdout);
}

switch (nerr)
{
case 1 :
mputs("Number base too big for representation\n");
break;
case 2 :
mputs("Division by zero attempted\n");
break;
case 3 : 
mputs("Overflow - Number too big\n");
break;
case 4 :
mputs("Internal result is negative\n");
break;
case 5 : 
mputs("Input format error\n");
break;
case 6 :
mputs("Illegal number base\n");
break;
case 7 : 
mputs("Illegal parameter usage\n");
break;
case 8 :
mputs("Out of space\n");
break;
case 9 :
mputs("Even root of a negative number\n");
break;
case 10:
mputs("Raising integer to negative power\n");
break;
case 11:
mputs("Attempt to take illegal root\n");
break;
case 12:
mputs("Integer operation attempted on Flash number\n");
break;
case 13:
mputs("Flash overflow\n");
break;
case 14:
mputs("Numbers too big\n");
break;
case 15:
mputs("Log of a non-positive number\n");
break;
case 16:
mputs("Flash to double conversion failure\n");
break;
case 17:
mputs("I/O buffer overflow\n");
break;
case 18:
mputs("MIRACL not initialised - no call to mirsys()\n");
break;
case 19:
mputs("Illegal modulus \n");
break;
case 20:
mputs("No modulus defined\n");
break;
case 21:
mputs("Exponent too big\n");
break;
case 22:
mputs("Number Base must be power of 2\n");
break;
case 23:
mputs("Specified double length type isn't double length\n");
break;
case 24:
mputs("Specified basis is NOT irreducible\n");
break;
case 25:
mputs("Unable to control Floating-point rounding\n");
break;
case 26:
mputs("Base must be binary (MR_ALWAYS_BINARY defined in mirdef.h ?)\n");
break;
case 27:
mputs("No irreducible basis defined\n");
break;
case 28:
mputs("Composite modulus\n");
break;
default:
mputs("Undefined error\n");
break;
}
exit(0);
#else
mputs("MIRACL error\n");
exit(0);
#endif

#endif
}

void mr_track(_MIPDO_ )
{ /* track course of program execution *
   * through the MIRACL routines       */

#ifndef MR_NO_STANDARD_IO
#ifndef MR_STRIPPED_DOWN
    int i;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    for (i=0;i<mr_mip->depth;i++) fputc('-',stdout);
    fputc('>',stdout);
    mputs(names[mr_mip->trace[mr_mip->depth]]);
    fputc('\n',stdout);
#endif
#endif
}

mr_small brand(_MIPDO_ )
{ /* Marsaglia & Zaman random number generator */
    int i,k;
    mr_unsign32 pdiff,t;
    mr_small r;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->lg2b>32)
    { /* underlying type is > 32 bits. Assume <= 64 bits */
        mr_mip->rndptr+=2;
        if (mr_mip->rndptr<NK-1)
        {
            r=(mr_small)mr_mip->ira[mr_mip->rndptr];
            r=mr_shiftbits(r,mr_mip->lg2b-32);
            r+=(mr_small)mr_mip->ira[mr_mip->rndptr+1];
            return r;
        }
    }
    else
    {
        mr_mip->rndptr++;
        if (mr_mip->rndptr<NK) return (mr_small)mr_mip->ira[mr_mip->rndptr];
    }
    mr_mip->rndptr=0;
    for (i=0,k=NK-NJ;i<NK;i++,k++)
    { /* calculate next NK values */
        if (k==NK) k=0;
        t=mr_mip->ira[k];
        pdiff=t - mr_mip->ira[i] - mr_mip->borrow;
        if (pdiff<t) mr_mip->borrow=0;
        if (pdiff>t) mr_mip->borrow=1;
        mr_mip->ira[i]=pdiff; 
    }
    if (mr_mip->lg2b>32)
    { /* double up */
        r=(mr_small)mr_mip->ira[0];
        r=mr_shiftbits(r,mr_mip->lg2b-32);
        r+=(mr_small)mr_mip->ira[1];
        return r;
    }
    else return (mr_small)(mr_mip->ira[0]);
}

void irand(_MIPD_ mr_unsign32 seed)
{ /* initialise random number system */
    int i,in;
    mr_unsign32 t,m=1L;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    mr_mip->borrow=0L;
    mr_mip->rndptr=0;
    mr_mip->ira[0]^=seed;
    for (i=1;i<NK;i++)
    { /* fill initialisation vector */
        in=(NV*i)%NK;
        mr_mip->ira[in]=m; 
        t=m;
        m=seed-m;
        seed=t;
    }
    for (i=0;i<1000;i++) brand(_MIPPO_ ); /* "warm-up" & stir the generator */
}

mr_small mr_shiftbits(mr_small x,int n)
{
#ifdef MR_FP
    int i;
    mr_small dres;
    if (n==0) return x;
    if (n>0)
    {
        for (i=0;i<n;i++) x=x+x;
        return x;
    }
    n=-n;
    for (i=0;i<n;i++) x=MR_DIV(x,2.0);
    return x;
#else
    if (n==0) return x;
    if (n>0) x<<=n;
    else x>>=n;
    return x;
#endif

}

mr_small mr_setbase(_MIPD_ mr_small nb)
{  /* set base. Pack as many digits as  *
    * possible into each computer word  */
    mr_small temp;
#ifdef MR_FP
    mr_small dres;
#endif
#ifndef MR_NOFULLWIDTH
    BOOL fits;
    int bits;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    fits=FALSE;
    bits=MIRACL;
    while (bits>1) 
    {
        bits/=2;
        temp=((mr_small)1<<bits);
        if (temp==nb)
        {
            fits=TRUE;
            break;
        }
        if (temp<nb || (bits%2)!=0) break;
    }
    if (fits)
    {
        mr_mip->apbase=nb;
        mr_mip->pack=MIRACL/bits;
        mr_mip->base=0;
        return 0;
    }
#endif
    mr_mip->apbase=nb;
    mr_mip->pack=1;
    mr_mip->base=nb;
    if (mr_mip->base==0) return 0;
    temp=MR_DIV(MAXBASE,nb);
    while (temp>=nb)
    {
        temp=MR_DIV(temp,nb);
        mr_mip->base*=nb;
        mr_mip->pack++;
    }
#ifdef MR_FP_ROUNDING
    mr_mip->inverse_base=mr_invert(mr_mip->base);
    return mr_mip->inverse_base;
#else
    return 0;
#endif
}

#ifdef MR_FLASH

//BOOL fit(big x,big y,int f)
//{ /* returns TRUE if x/y would fit flash format of length f */
//    int n,d;
//    n=(int)(x->len&(MR_OBITS));
//    d=(int)(y->len&(MR_OBITS));
//    if (n==1 && x->w[0]==1) n=0;
//    if (d==1 && y->w[0]==1) d=0;
//    if (n+d<=f) return TRUE;
//    return FALSE;
//}

#endif

int mr_lent(flash x)
{ /* return length of big or flash in words */
    mr_unsign32 lx;
    lx=(x->len&(MR_OBITS));
#ifdef MR_FLASH
    return (int)((lx&(MR_MSK))+((lx>>(MR_BTS))&(MR_MSK)));
#else
    return (int)lx;
#endif
}

void zero(flash x)
{ /* set big/flash number to zero */
    int i,n;
    mr_small *g;
    if (x==NULL) return;
#ifdef MR_FLASH
    n=mr_lent(x);
#else
    n=(x->len&MR_OBITS);
#endif
    g=x->w;

    for (i=0;i<n;i++)
        g[i]=0;

    x->len=0;

}

void convert(_MIPD_ int n ,big x)
{  /*  convert integer n to big number format  */
    int m;
    mr_unsign32 s;
#ifdef MR_FP
    mr_small dres;
#endif
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    zero(x);
    if (n==0) return;
    s=0;
    if (n<0)
    {
        s=MR_MSBIT;
        n=(-n);
    }
    m=0;
    if (mr_mip->base==0)
    {
#ifndef MR_NOFULLWIDTH
#if MR_IBITS > MIRACL
        while (n>0)
        {
            x->w[m++]=(mr_small)(n%((mr_small)1<<(MIRACL)));
            n/=((mr_small)1<<(MIRACL));
        }
#else
        x->w[m++]=(mr_small)n;
#endif
#endif
    }
    else while (n>0)
    {
        x->w[m++]=MR_REMAIN((mr_small)n,mr_mip->base);
        n/=mr_mip->base;
    }
    x->len=(m|s);
}

//void uconvert(_MIPD_ unsigned int n ,big x)
//{  /*  convert integer n to big number format  */
//    int m;
//#ifdef MR_FP
//    mr_small dres;
//#endif
//#ifdef MR_OS_THREADS
//    miracl *mr_mip=get_mip();
//#endif
//    zero(x);
//    if (n==0) return;
//    
//    m=0;
//    if (mr_mip->base==0)
//    {
//#ifndef MR_NOFULLWIDTH
//#if MR_IBITS > MIRACL
//        while (n>0)
//        {
//            x->w[m++]=(mr_small)(n%((mr_small)1<<(MIRACL)));
//            n/=((mr_small)1<<(MIRACL));
//        }
//#else
//        x->w[m++]=(mr_small)n;
//#endif
//#endif
//    }
//    else while (n>0)
//    {
//        x->w[m++]=MR_REMAIN((mr_small)n,mr_mip->base);
//        n/=mr_mip->base;
//    }
//    x->len=(m);
//}

#ifdef mr_dltype

//void dlconv(_MIPD_ mr_dltype n,big x)
//{ /* convert double length integer to big number format - rarely needed */
//    int m;
//    mr_unsign32 s;
//#ifdef MR_FP
//    mr_small dres;
//#endif
//#ifdef MR_OS_THREADS
//    miracl *mr_mip=get_mip();
//#endif
//    zero(x);
//    if (n==0) return;
//    s=0;
//    if (n<0)
//    {
//        s=MR_MSBIT;
//        n=(-n);
//    }
//    m=0;
//    if (mr_mip->base==0)
//    {
//#ifndef MR_NOFULLWIDTH
//        while (n>0)
//        {
//            x->w[m++]=(mr_small)(n%((mr_dltype)1<<(MIRACL)));
//            n/=((mr_dltype)1<<(MIRACL));
//        }
//#endif
//    }    
//    else while (n>0)
//    {
//        x->w[m++]=(mr_small)MR_REMAIN(n,mr_mip->base);
//        n/=mr_mip->base;
//    }
//    x->len=(m|s);
//}

#endif

//void lgconv(_MIPD_ long n,big x)
//{ /* convert long integer to big number format - rarely needed */
//    int m;
//    mr_unsign32 s;
//#ifdef MR_FP
//    mr_small dres;
//#endif
//#ifdef MR_OS_THREADS
//    miracl *mr_mip=get_mip();
//#endif
//    zero(x);
//    if (n==0) return;
//    s=0;
//    if (n<0)
//    {
//        s=MR_MSBIT;
//        n=(-n);
//    }
//    m=0;
//    if (mr_mip->base==0)
//    {
//#ifndef MR_NOFULLWIDTH
//#if MR_LBITS > MIRACL
//        while (n>0)
//        {
//            x->w[m++]=(mr_small)(n%(1L<<(MIRACL)));
//            n/=(1L<<(MIRACL));
//        }
//#else
//        x->w[m++]=(mr_small)n;
//#endif
//#endif
//    }    
//    else while (n>0)
//    {
//        x->w[m++]=MR_REMAIN(n,mr_mip->base);
//        n/=mr_mip->base;
//    }
//    x->len=(m|s);
//}

flash mirvar(_MIPD_ int iv)
{ /* initialize big/flash number */
    flash x;
    int align;
    char *ptr;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
 
    if (mr_mip->ERNUM) return NULL;
    MR_IN(23);  

    if (!(mr_mip->active))
    {
        mr_berror(_MIPP_ MR_ERR_NO_MIRSYS);
        MR_OUT  
        return NULL;
    }

/* OK, now I control alignment.... */

/* Allocate space for big, the length, the pointer, and the array */
/* Do it all in one memory allocation - this is quicker */
/* Ensure that the array has correct alignment */

    x=(big)mr_alloc(_MIPP_ mr_mip->size,1);
    if (x==NULL)
    {
        MR_OUT 
        return x;
    }
    
    ptr=(char *)&x->w;
    align=(unsigned long)(ptr+sizeof(mr_small *))%sizeof(mr_small);   

    x->w=(mr_small *)(ptr+sizeof(mr_small *)+sizeof(mr_small)-align);   

    if (iv!=0) convert(_MIPP_ iv,x);
    MR_OUT 
    return x;
}

flash mirvar_mem(_MIPD_ char *mem,int index)
{ /* initialize big/flash number from pre-allocated memory */
    flash x;
    int align;
    char *ptr;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
 
    if (mr_mip->ERNUM) return NULL;

    x=(big)&mem[mr_mip->size*index];
    ptr=(char *)&x->w;
    align=(unsigned long)(ptr+sizeof(mr_small *))%sizeof(mr_small);   
    x->w=(mr_small *)(ptr+sizeof(mr_small *)+sizeof(mr_small)-align);   

    return x;
}

//void set_user_function(_MIPD_ BOOL (*user)(void))
//{
//#ifdef MR_OS_THREADS
//    miracl *mr_mip=get_mip();
//#endif
//    if (mr_mip->ERNUM) return;
//
//    MR_IN(111)
//
//    if (!(mr_mip->active))
//    {
//        mr_berror(_MIPP_ MR_ERR_NO_MIRSYS);
//        MR_OUT
//        return;
//    }
//
//    mr_mip->user=user;
//
//    MR_OUT
//}

void set_io_buffer_size(_MIPD_ int len)
{
    int i;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (len<0) return;
    MR_IN(142)
    for (i=0;i<mr_mip->IOBSIZ;i++) mr_mip->IOBUFF[i]=0;
    mr_free(mr_mip->IOBUFF);
    if (len==0) 
    {
        MR_OUT
        return;
    }
    mr_mip->IOBSIZ=len;
    mr_mip->IOBUFF=(char *)mr_alloc(_MIPP_ len+1,1);
    mr_mip->IOBUFF[0]='\0';
    MR_OUT
}

miracl *mirsys(int nd,mr_small nb)
{  /*  Initialize MIRACL system to   *
    *  use numbers to base nb, and   *
    *  nd digits or (-nd) bytes long */
    int i;
    mr_small b;
#ifdef MR_FP
    mr_small dres;
#endif
/*** Multi-Threaded support ***/

#ifndef MR_GENERIC_MT

#ifdef MR_WINDOWS_MT
    miracl *mr_mip=mr_first_alloc();
    TlsSetValue(mr_key,mr_mip);
#endif

#ifdef MR_UNIX_MT
    miracl *mr_mip=mr_first_alloc(); 
    pthread_setspecific(mr_key,mr_mip);    
#endif

#ifndef MR_WINDOWS_MT
#ifndef MR_UNIX_MT
    mr_mip=mr_first_alloc();
#endif
#endif
    mr_mip=get_mip();
#else
    miracl *mr_mip=mr_first_alloc();
#endif

    if (mr_mip==NULL) return NULL;
    mr_mip->depth=0;
    mr_mip->trace[0]=0;
    mr_mip->depth++;
    mr_mip->trace[mr_mip->depth]=29;
                    /* digest hardware configuration */

#ifdef MR_NO_STANDARD_IO
    mr_mip->ERCON=TRUE;
#else
    mr_mip->ERCON=FALSE;
#endif
    mr_mip->logN=0;
    mr_mip->degree=0;
    mr_mip->chin.NP=0;
    mr_mip->user=NULL;
    mr_mip->same=FALSE;
    mr_mip->first_one=FALSE;
    mr_mip->debug=FALSE;
	mr_mip->AA=0;

#ifdef MR_NOFULLWIDTH
    if (nb==0)
    {
        mr_berror(_MIPP_ MR_ERR_BAD_BASE);
        mr_mip->depth--;
        return mr_mip;
    }
#endif

#ifndef MR_FP
#ifdef mr_dltype
#ifndef MR_NOFULLWIDTH
    if (sizeof(mr_dltype)<2*sizeof(mr_utype))
    { /* double length type, isn't */
        mr_berror(_MIPP_ MR_ERR_NOT_DOUBLE_LEN);
        mr_mip->depth--;
        return mr_mip;
    }
#endif
#endif
#endif

    if (nb==1 || nb>MAXBASE)
    {
        mr_berror(_MIPP_ MR_ERR_BAD_BASE);
        mr_mip->depth--;
        return mr_mip;
    }

#ifdef MR_FP_ROUNDING
    if (mr_setbase(_MIPP_ nb)==0)
    { /* unable in fact to control FP rounding */
        mr_berror(_MIPP_ MR_ERR_NO_ROUNDING);
        mr_mip->depth--;
        return mr_mip;
    }
#else
    mr_setbase(_MIPP_ nb);
#endif
    b=mr_mip->base;

    mr_mip->lg2b=0;
    mr_mip->base2=1;
    if (b==0)
    {
        mr_mip->lg2b=MIRACL;
        mr_mip->base2=0;
    }
    else while (b>1)
    {
        b=MR_DIV(b,2);
        mr_mip->lg2b++;
        mr_mip->base2*=2;
    }

#ifdef MR_ALWAYS_BINARY
    if (mr_mip->base!=mr_mip->base2) mr_berror(_MIPP_ MR_ERR_NOT_BINARY);
#endif

    if (nd>0)
        mr_mip->nib=(nd-1)/mr_mip->pack+1;
    else
        mr_mip->nib=(mr_mip->lg2b-8*nd-1)/mr_mip->lg2b;
    if (mr_mip->nib<2) mr_mip->nib=2;
    mr_mip->size=sizeof(struct bigtype)+(mr_mip->nib+1)*sizeof(mr_small);   
    if (mr_mip->size%8) mr_mip->size+=(8-(mr_mip->size%8));
                                              /* bodge for itanium */

#ifdef MR_FLASH
    mr_mip->workprec=mr_mip->nib;
    mr_mip->stprec=mr_mip->nib;
    while (mr_mip->stprec>2 && mr_mip->stprec>MR_FLASH/mr_mip->lg2b) 
        mr_mip->stprec=(mr_mip->stprec+1)/2;
    if (mr_mip->stprec<2) mr_mip->stprec=2;
    mr_mip->pi=NULL;
#endif
    mr_mip->check=ON;
    mr_mip->IOBASE=10;   /* defaults */
    mr_mip->ERNUM=0;
    mr_mip->RPOINT=OFF;
    mr_mip->NTRY=6;
    mr_mip->MONTY=ON;
    mr_mip->EXACT=TRUE;
    mr_mip->TRACER=OFF;
    mr_mip->INPLEN=0;
    mr_mip->PRIMES=NULL;
    mr_mip->IOBSIZ=1024;
    mr_mip->IOBUFF=(char *)mr_alloc(_MIPP_ 1025,1);
    mr_mip->IOBUFF[0]='\0';
    mr_mip->qnr=0;
    mr_mip->TWIST=FALSE;

/* quick start for rng. irand(.) should be called first before serious use.. */

    mr_mip->ira[0]=0x55555555;
    mr_mip->ira[1]=0x12345678;

    for (i=2;i<NK;i++) 
        mr_mip->ira[i]=mr_mip->ira[i-1]+mr_mip->ira[i-2]+0x1379BDF1;
    mr_mip->rndptr=NK;
    mr_mip->borrow=0;

    mr_mip->nib=2*mr_mip->nib+1;
#ifdef MR_FLASH
    if (mr_mip->nib!=(mr_mip->nib&(MR_MSK)) || mr_mip->nib > MR_TOOBIG)
#else
    if (mr_mip->nib!=(int)(mr_mip->nib&(MR_OBITS)) || mr_mip->nib>MR_TOOBIG)
#endif
    {
        mr_berror(_MIPP_ MR_ERR_TOO_BIG);
        mr_mip->nib=(mr_mip->nib-1)/2;
        mr_mip->depth--;
        return mr_mip;
    }
    mr_mip->modulus=NULL;
    mr_mip->A=NULL;
    mr_mip->B=NULL;
    mr_mip->C=NULL;

    mr_mip->workspace=memalloc(_MIPP_ 23);  /* grab workspace */

    mr_mip->M=0;
    mr_mip->fin=FALSE;
    mr_mip->fout=FALSE;
    mr_mip->active=ON;
    
    mr_mip->nib=(mr_mip->nib-1)/2;
#ifdef MR_KCM
    mr_mip->big_ndash=NULL;
    mr_mip->ws=mirvar(_MIPP_ 0);
#endif

/* allocate memory for workspace variables */

    mr_mip->w0=mirvar_mem(_MIPP_ mr_mip->workspace,0);  /* double length */
    mr_mip->w1=mirvar_mem(_MIPP_ mr_mip->workspace,2);
    mr_mip->w2=mirvar_mem(_MIPP_ mr_mip->workspace,3);
    mr_mip->w3=mirvar_mem(_MIPP_ mr_mip->workspace,4);
    mr_mip->w4=mirvar_mem(_MIPP_ mr_mip->workspace,5);
    mr_mip->w5=mirvar_mem(_MIPP_ mr_mip->workspace,6);  /* double length */
    mr_mip->w6=mirvar_mem(_MIPP_ mr_mip->workspace,8);  /* double length */
    mr_mip->w7=mirvar_mem(_MIPP_ mr_mip->workspace,10); /* double length */
    mr_mip->w8=mirvar_mem(_MIPP_ mr_mip->workspace,12);
    mr_mip->w9=mirvar_mem(_MIPP_ mr_mip->workspace,13);
    mr_mip->w10=mirvar_mem(_MIPP_ mr_mip->workspace,14);
    mr_mip->w11=mirvar_mem(_MIPP_ mr_mip->workspace,15);
    mr_mip->w12=mirvar_mem(_MIPP_ mr_mip->workspace,16);
    mr_mip->w13=mirvar_mem(_MIPP_ mr_mip->workspace,17);
    mr_mip->w14=mirvar_mem(_MIPP_ mr_mip->workspace,18);
    mr_mip->w15=mirvar_mem(_MIPP_ mr_mip->workspace,19);
    mr_mip->w16=mirvar_mem(_MIPP_ mr_mip->workspace,20);
    mr_mip->w17=mirvar_mem(_MIPP_ mr_mip->workspace,21);
    mr_mip->w18=mirvar_mem(_MIPP_ mr_mip->workspace,22);

    mr_mip->depth--;
    return mr_mip;
} 

void *memalloc(_MIPD_ int num)
{
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    return mr_alloc(_MIPP_ mr_mip->size*num,1);
}

void memkill(_MIPD_ char *mem,int len)
{
    int i;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mem==NULL) return;
    for (i=0;i<len*mr_mip->size;i++) mem[i]=0;
    mr_free(mem);
}

void mirkill(big x)
{ /* kill a big/flash variable, that is set it to zero
     and free its memory */
    if (x==NULL) return;
    zero(x);
    mr_free(x);
}

void mirexit(_MIPDO_ )
{ /* clean up after miracl */
    int i;

#ifdef MR_WINDOWS_MT
    miracl *mr_mip=get_mip();
#endif
#ifdef MR_UNIX_MT
    miracl *mr_mip=get_mip();
#endif
    mr_mip->ERCON=FALSE;
    mr_mip->active=OFF;
    memkill(_MIPP_ mr_mip->workspace,23);

#ifdef MR_FLASH
    if (mr_mip->pi!=NULL) mirkill(mr_mip->pi);
#endif

    for (i=0;i<NK;i++) mr_mip->ira[i]=0L;
    set_io_buffer_size(_MIPP_ 0);
    if (mr_mip->PRIMES!=NULL) mr_free(mr_mip->PRIMES);

#ifdef MR_KCM
    if (mr_mip->big_ndash!=NULL) mirkill(mr_mip->big_ndash); 
    mirkill(mr_mip->ws);
#endif

    if (mr_mip->modulus!=NULL) mirkill(mr_mip->modulus);
    if (mr_mip->A!=NULL) mirkill(mr_mip->A);
    if (mr_mip->B!=NULL) mirkill(mr_mip->B);
    if (mr_mip->C!=NULL) mirkill(mr_mip->C);

    mr_free(mr_mip);

#ifndef MR_GENERIC_MT
#ifndef MR_WINDOWS_MT
#ifndef MR_UNIX_MT
    mr_mip=NULL;
#endif   
#endif   
#endif   

}

int exsign(flash x)
{ /* extract sign of big/flash number */
    if ((x->len&(MR_MSBIT))==0) return PLUS;
    else                        return MINUS;    
}

void insign(int s,flash x)
{  /* assert sign of big/flash number */
    if (x->len==0) return;
    if (s<0) x->len|=MR_MSBIT;
    else     x->len&=MR_OBITS;
}   

void mr_lzero(big x)
{  /*  strip leading zeros from big number  */
    mr_unsign32 s;
    int m;
    s=(x->len&(MR_MSBIT));
    m=(int)(x->len&(MR_OBITS));
    while (m>0 && x->w[m-1]==0)
        m--;
    x->len=m;
    if (m>0) x->len|=s;
}

int getdig(_MIPD_ big x,int i)
{  /* extract a packed digit */
    int k;
    mr_small n;
#ifdef MR_FP
    mr_small dres;
#endif
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    i--;
    n=x->w[i/mr_mip->pack];

    if (mr_mip->pack==1) return (int)n;
    k=i%mr_mip->pack;
    for (i=1;i<=k;i++)
        n=MR_DIV(n,mr_mip->apbase);  
    return (int)MR_REMAIN(n,mr_mip->apbase);
}

int numdig(_MIPD_ big x)
{  /* returns number of digits in x */
    int nd;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif

    if (x->len==0) return 0;

    nd=(int)(x->len&(MR_OBITS))*mr_mip->pack;
    while (getdig(_MIPP_ x,nd)==0)
        nd--;
    return nd;
} 

void putdig(_MIPD_ int n,big x,int i)
{  /* insert a digit into a packed word */
    int j,k,lx;
    mr_small m,p;
    mr_unsign32 s;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;

    MR_IN(26)

    s=(x->len&(MR_MSBIT));
    lx=(int)(x->len&(MR_OBITS));
    m=getdig(_MIPP_ x,i);
    p=n;
    i--;
    j=i/mr_mip->pack;
    k=i%mr_mip->pack;
    for (i=1;i<=k;i++)
    {
        m*=mr_mip->apbase;
        p*=mr_mip->apbase;
    }
    if (j>=mr_mip->nib && (mr_mip->check || j>=2*mr_mip->nib))
    {
        mr_berror(_MIPP_ MR_ERR_OVERFLOW);
        MR_OUT
        return;
    }

    x->w[j]=(x->w[j]-m)+p;
    if (j>=lx) x->len=((j+1)|s);
    mr_lzero(x);
    MR_OUT
}

void copy(flash x,flash y)
{  /* copy x to y: y=x  */
    int i,nx,ny;
    mr_small *gx,*gy;
    if (x==y || y==NULL) return;

    if (x==NULL)
    { 
        zero(y);
        return;
    }

#ifdef MR_FLASH    
    ny=mr_lent(y);
    nx=mr_lent(x);
#else
    ny=(y->len&(MR_OBITS));
    nx=(x->len&(MR_OBITS));
#endif

    gx=x->w;
    gy=y->w;

    for (i=nx;i<ny;i++)
        gy[i]=0;
    for (i=0;i<nx;i++)
        gy[i]=gx[i];
    y->len=x->len;

}

//void negify(flash x,flash y)
//{ /* negate a big/flash variable: y=-x */
//    copy(x,y);
//    if (y->len!=0) y->len^=MR_MSBIT;
//}

//void absol(flash x,flash y)
//{ /* y=abs(x) */
//    copy(x,y);
//    y->len&=MR_OBITS;
//}

BOOL mr_notint(flash x)
{ /* returns TRUE if x is Flash */
#ifdef MR_FLASH
    if ((((x->len&(MR_OBITS))>>(MR_BTS))&(MR_MSK))!=0) return TRUE;
#endif
    return FALSE;
}

void mr_shift(_MIPD_ big x,int n,big w)
{ /* set w=x.(mr_base^n) by shifting */
    mr_unsign32 s;
    int i,bl;
    mr_small *gw=w->w;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;
    copy(x,w);
    if (w->len==0 || n==0) return;
    MR_IN(33)

    if (mr_notint(w)) mr_berror(_MIPP_ MR_ERR_INT_OP);
    s=(w->len&(MR_MSBIT));
    bl=(int)(w->len&(MR_OBITS))+n;
    if (bl<=0)
    {
        zero(w);
        MR_OUT
        return;
    }
    if (bl>mr_mip->nib && mr_mip->check) mr_berror(_MIPP_ MR_ERR_OVERFLOW);
    if (mr_mip->ERNUM)
    {
        MR_OUT
        return;
    }
    if (n>0)
    {
        for (i=bl-1;i>=n;i--)
            gw[i]=gw[i-n];
        for (i=0;i<n;i++)
            gw[i]=0;
    }
    else
    {
        n=(-n);
        for (i=0;i<bl;i++)
            gw[i]=gw[i+n];
        for (i=0;i<n;i++)
            gw[bl+i]=0;
    }
    w->len=(bl|s);
    MR_OUT
}

int size(big x)
{  /*  get size of big number;  convert to *
    *  integer - if possible               */
    int n,m;
    mr_unsign32 s;
    if (x==NULL) return 0;
    s=(x->len&MR_MSBIT);
    m=(int)(x->len&MR_OBITS);
    if (m==0) return 0;
    if (m==1 && x->w[0]<(mr_small)MR_TOOBIG) n=(int)x->w[0];
    else                                     n=MR_TOOBIG;
    if (s==MR_MSBIT) return (-n);
    return n;
}

int compare(big x,big y)
{  /* compare x and y: =1 if x>y  =-1 if x<y *
    *  =0 if x=y                             */
    int m,n,sig;
    mr_unsign32 sx,sy;
    if (x==y) return 0;
    sx=(x->len&MR_MSBIT);
    sy=(y->len&MR_MSBIT);
    if (sx==0) sig=PLUS;
    else       sig=MINUS;
    if (sx!=sy) return sig;
    m=(int)(x->len&MR_OBITS);
    n=(int)(y->len&MR_OBITS);
    if (m>n) return sig;
    if (m<n) return -sig;
    while (m>0)
    { /* check digit by digit */
        m--;  
        if (x->w[m]>y->w[m]) return sig;
        if (x->w[m]<y->w[m]) return -sig;
    }
    return 0;
}

#ifdef MR_FLASH

void fpack(_MIPD_ big n,big d,flash x)
{ /* create floating-slash number x=n/d from *
   * big integer numerator and denominator   */
    mr_unsign32 s;
    int i,ld,ln;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;

    MR_IN(31)

    ld=(int)(d->len&MR_OBITS);
    if (ld==0) mr_berror(_MIPP_ MR_ERR_FLASH_OVERFLOW);
    if (ld==1 && d->w[0]==1) ld=0;
    if (x==d) mr_berror(_MIPP_ MR_ERR_BAD_PARAMETERS);
    if (mr_notint(n) || mr_notint(d)) mr_berror(_MIPP_ MR_ERR_INT_OP);
    s=(n->len&MR_MSBIT);
    ln=(int)(n->len&MR_OBITS);
    if (ln==1 && n->w[0]==1) ln=0;
    if ((ld+ln>mr_mip->nib) && (mr_mip->check || ld+ln>2*mr_mip->nib)) 
        mr_berror(_MIPP_ MR_ERR_FLASH_OVERFLOW);
    if (mr_mip->ERNUM)
    {
       MR_OUT
       return;
    }
    copy(n,x);
    if (n->len==0)
    {
        MR_OUT
        return;
    }
    s^=(d->len&MR_MSBIT);
    if (ld==0)
    {
        if (x->len!=0) x->len|=s;
        MR_OUT
        return;
    }
    for (i=0;i<ld;i++)
        x->w[ln+i]=d->w[i];
    x->len=(s|(ln+((mr_unsign32)ld<<MR_BTS)));
    MR_OUT
}

void numer(_MIPD_ flash x,big y)
{ /* extract numerator of x */
    int i,ln,ld;
    mr_unsign32 s,ly;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;
    if (mr_notint(x))
    {
        s=(x->len&MR_MSBIT);
        ly=(x->len&MR_OBITS);
        ln=(int)(ly&MR_MSK);
        if (ln==0)
        {
            if(s==MR_MSBIT) convert(_MIPP_ (-1),y);
            else            convert(_MIPP_ 1,y);
            return;
        }
        ld=(int)((ly>>MR_BTS)&MR_MSK);
        if (x!=y)
        {
            for (i=0;i<ln;i++) y->w[i]=x->w[i];
            for (i=ln;i<mr_lent(y);i++) y->w[i]=0;
        }
        else for (i=0;i<ld;i++) y->w[ln+i]=0;
        y->len=(ln|s);
    }
    else copy(x,y);
}

void denom(_MIPD_ flash x,big y)
{ /* extract denominator of x */
    int i,ln,ld;
    mr_unsign32 ly;
#ifdef MR_OS_THREADS
    miracl *mr_mip=get_mip();
#endif
    if (mr_mip->ERNUM) return;
    if (!mr_notint(x))
    {
        convert(_MIPP_ 1,y);
        return;
    }
    ly=(x->len&MR_OBITS);
    ln=(int)(ly&MR_MSK);
    ld=(int)((ly>>MR_BTS)&MR_MSK);
    for (i=0;i<ld;i++)
        y->w[i]=x->w[ln+i];
    if (x==y) for (i=0;i<ln;i++) y->w[ld+i]=0;
    else for (i=ld;i<mr_lent(y);i++) y->w[i]=0;
    y->len=ld;
}

#endif

//int igcd(int x,int y)
//{ /* integer GCD, returns GCD of x and y */
//    int r;
//    if (y==0) return x;
//    while ((r=x%y)!=0)
//        x=y,y=r;
//    return y;
//}

//int isqrt(int num,int guess)
//{ /* square root of an integer */
//    int sqr;
//    for (;;)
//    { /* Newtons iteration */
//        sqr=guess+(((num/guess)-guess)/2);
//        if (sqr==guess) 
//        {
//            if (sqr*sqr>num) sqr--;
//            return sqr;
//        }
//        guess=sqr;
//    }
//}

//mr_small sgcd(mr_small x,mr_small y)
//{ /* integer GCD, returns GCD of x and y */
//    mr_small r;
//#ifdef MR_FP
//    mr_small dres;
//#endif
//    if (y==(mr_small)0) return x;
//    while ((r=MR_REMAIN(x,y))!=(mr_small)0)
//        x=y,y=r;
//    return y;
//}

/* routines to support sliding-windows exponentiation *
 * in various contexts */

//int mr_testbit(_MIPD_ big x,int n)
//{ /* return value of n-th bit of big */
//#ifdef MR_OS_THREADS
//    miracl *mr_mip=get_mip();
//#endif
//#ifdef MR_FP
//    mr_small m,a,dres; int i;
//    m=mr_shiftbits((mr_small)1,n%mr_mip->lg2b);
//
//    a=x->w[n/mr_mip->lg2b];
//
//    a=MR_DIV(a,m); 
//
//    if ((MR_DIV(a,2.0)*2.0) != a) return 1;
//#else
//    if ((x->w[n/mr_mip->lg2b] & ((mr_small)1<<(n%mr_mip->lg2b))) >0) return 1;
//#endif
//    return 0;
//}

//int mr_window(_MIPD_ big x,int i,int *nbs,int * nzs)
//{ /* returns sliding window value, max. of 5 bits,         *
//   * starting at i-th bit of big x. nbs is number of bits  *
//   * processed, nzs is the number of additional trailing   *
//   * zeros detected. Returns valid bit pattern 1x..x1 with *
//   * no two adjacent 0's. So 10101 will return 21 with     *
//   * nbs=5, nzs=0. 11001 will return 3, with nbs=2, nzs=2, *
//   * having stopped after the first 11..  */
//
//    int j,r,w;
//    w=5;
//
///* check for leading 0 bit */
//
//    *nbs=1;
//    *nzs=0;
//    if (!mr_testbit(_MIPP_ x,i)) return 0;
//
///* adjust window size if not enough bits left */
//   
//    if (i-w+1<0) w=i+1;
//
//    r=1;
//    for (j=i-1;j>i-w;j--)
//    { /* accumulate bits. Abort if two 0's in a row */
//        (*nbs)++;
//        r*=2;
//        if (mr_testbit(_MIPP_ x,j)) r+=1;
//        if (r%4==0)
//        { /* oops - too many zeros - shorten window */
//            r/=4;
//            *nbs-=2;
//            *nzs=2;
//            break;
//        }
//    }
//    if (r%2==0)
//    { /* remove trailing 0 */
//        r/=2;
//        *nzs=1;
//        (*nbs)--;
//    }
//    return r;
//}

//int mr_window2(_MIPD_ big x,big y,int i,int *nbs,int *nzs)
//{ /* two bit window for double exponentiation */
//    int r,w;
//    BOOL a,b,c,d;
//    w=2;
//    *nbs=1;
//    *nzs=0;
//
///* check for two leading 0's */
//    a=mr_testbit(_MIPP_ x,i); b=mr_testbit(_MIPP_ y,i);
//
//    if (!a && !b) return 0;
//    if (i<1) w=1;
//
//    if (a)
//    {
//        if (b) r=3;
//        else   r=2;
//    }
//    else r=1;
//    if (w==1) return r;
//
//    c=mr_testbit(_MIPP_ x,i-1); d=mr_testbit(_MIPP_ y,i-1);
//
//    if (!c && !d) 
//    {
//        *nzs=1;
//        return r;
//    }
//
//    *nbs=2;
//    r*=4;
//    if (c)
//    {
//        if (d) r+=3;
//        else   r+=2;
//    }
//    else r+=1;
//    return r;
//}

//int mr_naf_window(_MIPD_ big x,big x3,int i,int *nbs,int *nzs)
//{ /* returns sliding window value, max of 5 bits           *
//   * starting at i-th bit of x. nbs is number of bits      *
//   * processed. nzs is number of additional trailing       *    
//   * zeros detected. x and x3 (which is 3*x) are           *
//   * combined to produce the NAF (non-adjacent form)       *
//   * So if x=11011(27) and x3 is 1010001, the LSB is       *
//   * ignored and the value 100T0T (32-4-1=27) processed,   *
//   * where T is -1. Note x.P = (3x-x)/2.P. This value will *
//   * return +7, with nbs=4 and nzs=1, having stopped after *
//   * the first 4 bits. Note in an NAF non-zero elements    *
//   * are never side by side, so 10T10T won't happen        *
//   * NOTE: return value n zero or odd, -21 <= n <= +21     */
//
//    int nb,j,r,w;
//    BOOL last;
//    w=5;
//
// /* get first bit */
//    nb=mr_testbit(_MIPP_ x3,i)-mr_testbit(_MIPP_ x,i);
//
//    *nbs=1;
//    *nzs=0;
//    if (nb==0) return 0;
//    last=FALSE;
//    if (i<=w) 
//    {
//        w=i;
//        last=TRUE;
//    }
//
//    if (nb>0) r=1;
//    else      r=(-1);
//
//    for (j=i-1;j>i-w;j--)
//    { /* scan the bits */
//        (*nbs)++;
//        r*=2;
//        nb=mr_testbit(_MIPP_ x3,j)-mr_testbit(_MIPP_ x,j);
//        if (nb==0) continue;
//        if (nb>0) r+=1;
//        if (nb<0) r-=1;
//           
//    } 
//    if (!last && r%2!=0) (*nzs)++;
//    while (r%2==0)
//    { /* remove trailing zeros */
//        r/=2;
//        (*nzs)++;
//        (*nbs)--;
//    }     
//    return r;
//}

//BOOL point_at_infinity(epoint *p)
//{
//    if (p==NULL) return FALSE;
//    if (p->marker==MR_EPOINT_INFINITY) return TRUE;
//    return FALSE;
//}

