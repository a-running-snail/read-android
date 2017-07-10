#ifndef _UTILITY_H
#define _UTILITY_H

#include <stdlib.h>
#include <stdio.h>
#include <time.h>

#ifdef WIN32
#include <windows.h>
#pragma  warning(disable:4996) 
#else
#include <string.h>
#include <stdarg.h>
#endif

typedef struct JY_State
{
	int x, y, m[256];
}JY_STATE;

#define JY_KEY_LEN 32
static char JY_key[JY_KEY_LEN] = \
{
	(char)0xA1,(char)0x25,(char)0x4C,(char)0x67,(char)0x19,(char)0xAB,(char)0xC5,(char)0xEF,\
	(char)0x1A,(char)0x2B,(char)0x3C,(char)0x4D,(char)0x5E,(char)0x6F,(char)0x70,(char)0x89,\
	(char)0x1D,(char)0x22,(char)0x36,(char)0x44,(char)0x5D,(char)0x6C,(char)0x71,(char)0x88,\
	(char)0x9A,(char)0x6A,(char)0xB5,(char)0xCC,(char)0xD8,(char)0xEE,(char)0xFC,(char)0xF0\
};

/* the three BASE64_xx macros are from base64 implementation of gnulib */
/* This uses that the expression (n+(k-1))/k means the smallest
   integer >= n/k, i.e., the ceiling of n/k.  */
#define BASE64_LENGTH(inlen) ((((inlen) + 2) / 3) * 4)
#define BASE64_LENGTH_PLUS_1 (inlen) (BASE64_LENGTH(inlen)+1)
 /* This may allocate a few bytes too much, depending on input,
     but it's not worth the extra CPU time to compute the exact amount.
     The exact amount is 3 * inlen / 4, minus 1 if the input ends
     with "=" and minus another 1 if the input ends with "==".
     Dividing before multiplying avoids the possibility of overflow.  
	 khzou: Deliberately add 1, from 
	 3 * ((inlen) / 4) + 2
	 to:
	 3 * ((inlen) / 4) + 2+1
	 to add space for tailing '\0'
	 */
#define BASE64_DECODE_CAPACITY(inlen)  (3 * ((inlen) / 4) + 2+1)

#define JY_ENCODE_LEN 17
#define JY_LENGTH     256

void JY_Crypt( JY_STATE *s, unsigned char *pKey, unsigned char *pData, int nDataLen );

char* BillEncode32( char* pInBuf, int len ); 

char* BillEncode64 (char* pInBuf, int len ); 

char* BillEncode( char* pInBuf, int len, char choice ); 

char* BillDecode( char* pInBuf, int pInBufLen, int* outlen ); 

int ExchangeChar( char* pInBuf, char* pOutBuf, int nLength );

long FileLength( FILE* fp );

void base64Encode( char* pIn, int nInLen, char* pOut );

int base64Decode( char* pIn, int nInLen, char* pOut );

char* memstr(char* pLongString, int nLongStringLen, char* pSubString, int nSubStringLen);

#if WIN32
void DeleteFileAnsi(const char* pFile);

#include "..\public\include\zlib.h"
extern "C" 
{
#include "p1363.h"
}
bool DecryptAndDecompress(char* szKey, int nKeyLen, char* szInFileName, char* szOutFileName);
#endif

typedef struct log
{
	log()
	{
#ifdef  WIN32
		f = fopen("D:\\jdeblog\\log.txt", "a");
#else
		f = fopen("/tmp/jdeblog/log", "a");
#endif
	}

	~log(){if(f) fclose(f);}

	inline void Write(const char* format, ...)
	{
		if(0 == f)
			return;

		va_list arg;
		int done = 0;

		va_start (arg, format);

		char pLog[1024] = {0};
		int nIndex = 0;

		while( *format != '\0')
		{
			if( *format == '%')
			{
				if( *(format+1) == 'c' )
				{
					char c = (char)va_arg(arg, int);
					pLog[nIndex++] = c;
				} 
				else if( *(format+1) == 'd' || *(format+1) == 'i')
				{
					char store[20];
					int i = va_arg(arg, int);
					char* str = store;
					sprintf(store, "%d", i);
					while( *str != '\0') 
						pLog[nIndex++] = *str++;
				} 
				else if( *(format+1) == 'o')
				{
					char store[20];
					int i = va_arg(arg, int);
					char* str = store;
					sprintf(store, "%o", i);
					while( *str != '\0') 
						pLog[nIndex++] = *str++;
				} 
				else if( *(format+1) == 'x')
				{
					char store[20];
					int i = va_arg(arg, int);
					char* str = store;
					sprintf(store, "%x", i);
					while( *str != '\0') 
						pLog[nIndex++] = *str++;
				} 
				else if( *(format+1) == 's' )
				{
					char* str = va_arg(arg, char*);

					if(0 == str)
						break;

					while( *str != '\0') 
						pLog[nIndex++] = *str++;
				}

				format += 2;
			} 
			else 
			{
				pLog[nIndex++] = *format++;
			}
		}

		va_end (arg);
		char *wday[]={"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
		time_t timep;
		struct tm *p;

		time(&timep);
		p = gmtime(&timep);

		char szLog[1024] = {0};
		sprintf(szLog, "%d-%02d-%02d  %s  %02d:%02d:%02d  **  %s\n", (1900+p->tm_year), (1+p->tm_mon),p->tm_mday, wday[p->tm_wday], p->tm_hour+8, p->tm_min, p->tm_sec, pLog);
		fwrite(szLog, sizeof(char), strlen(szLog), f);

		fflush(f);
	}

	FILE* f;
}CLOG;

#endif //_UTILITY_H
