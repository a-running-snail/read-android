
#include <string.h>  
#include <stdio.h>
#include <android/log.h>

#define  LOG_TAG_B    "CCLOG"
#define  CCLOG(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG_B,__VA_ARGS__)
int ccctest=0;
static int br=0;
void __br()
{
	br++;
}

void __b(int id)
{
	CCLOG("br:%d",id);
	if(id==10)
	{
		__br();
	}
}

void __bs(const char *str)
{

	CCLOG("%s",str);

}

void __di(const char *str,unsigned int dat)
{
	CCLOG("%s = %d(0x%x)",str,dat,dat);
}


void __bd(const unsigned char *buf,unsigned int len)
{
    
    char str[0x1000];
    int opi=0;
    int ii=0;
    int l=0;
    int ll=(len/16)*16;
    while(opi<ll)
    {
        
        sprintf(str,"0x%02x,0x%02x,0x%02x,0x%02x,0x%02x,0x%02x,0x%02x,0x%02x,0x%02x,0x%02x,0x%02x,0x%02x,0x%02x,0x%02x,0x%02x,0x%02x",
         buf[opi],
        buf[opi+1],
        buf[opi+2],
        buf[opi+3],
        buf[opi+4],
        buf[opi+5],
        buf[opi+6],
        buf[opi+7],
        buf[opi+8],
        buf[opi+9],
        buf[opi+10],
        buf[opi+11],
        buf[opi+12],
        buf[opi+13],
        buf[opi+14],
        buf[opi+15]
        );
    __bs(str);
    opi=opi+16;

    }
    

}


