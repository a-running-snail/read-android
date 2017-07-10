
#ifndef ___B_H
#define ___B_H

#ifdef __cplusplus
extern "C"
{
#endif

void __b(int id);

void __bs(const char *str);
void __di(const char *str,unsigned int dat);
void __bd(const unsigned char *data,unsigned int len);
#define __bi(dat) __di((const char *)#dat,(unsigned int)dat)

#ifdef __cplusplus
extern int ccctest;
}
#endif
#endif
