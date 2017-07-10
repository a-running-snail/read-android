#include "HardwareInfo.h"
#include <Windows.h>
#include <stdio.h> 

int GetDiskInfo(char** ppDiskInfo, int* pDiskInfoLen)
{
	if((NULL == ppDiskInfo) || (NULL == pDiskInfoLen))
		return -1;	

	*ppDiskInfo = NULL;
	*pDiskInfoLen = 0;

	// 获取系统盘符
	CHAR szSystemDirectory[MAX_PATH + 1] = {0}; 
	CHAR szRootPathName[MAX_PATH + 1] = {0};
	GetSystemDirectoryA(szSystemDirectory, MAX_PATH); 
	szRootPathName[0] = szSystemDirectory[0];
	memcpy(szRootPathName + 1, ":\\", 3);

	DWORD dwVolumeSerialNumber = 0;  		  
	if(GetVolumeInformationA(szRootPathName,                                 
		NULL,                                 
		0,                                
		&dwVolumeSerialNumber,                                  
		NULL,                                  
		NULL,                                
		NULL,                                 
		0))
	{
		*pDiskInfoLen = sizeof(DWORD)*2;
		*ppDiskInfo = new char[*pDiskInfoLen + 1];
		if (NULL == *ppDiskInfo)
			return -2;

		memset(*ppDiskInfo, 0, *pDiskInfoLen + 1);
		sprintf(*ppDiskInfo, "%x", dwVolumeSerialNumber);	
		return 0;
	}

	return -1;
}