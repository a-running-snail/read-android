#ifndef _HARDWARE_INFO_H
#define _HARDWARE_INFO_H

void CheckIP(void);

int GetLocalHostMacAddr(char* macAddr);

int GetDiskInfo(char** ppDiskInfo, int* pDiskInfoLen);

#endif //_HARDWARE_INFO_H
