#include "util.h"

#define size_t long
#define N 36

const unsigned short dataArray0[N] = {0x6FB7, 0x8D00, 0x53C4, 0x3C37, 0x98A7, 0x55F9, 0xEB09, 0x6085, 0x8FA8, 0x8E7B, 0x5EE4, 0x397D, 0xB098, 0x820, 0xEA7E, 0x8600, 0x4171, 0x399B, 0x5131, 0xFDD7, 0x4E0C, 0xEA4D, 0x5199, 0x60A1, 0xAFAD, 0x6654, 0x8785, 0xF84F, 0xB1D2, 0x50AE, 0xE510, 0x91AB, 0xC2D3, 0x281, 0x8D7B, 0xA2C0};
const unsigned short dataArray1[N] = {0x6FB7, 0x8D00, 0x53C4, 0x3C37, 0x98A7, 0x9821, 0xEB09, 0x6085, 0x8FA8, 0x8E7B, 0x5088, 0x397D, 0x18E2, 0x547F, 0xC148, 0x8600, 0x4171, 0xF507, 0x5131, 0xFDD7, 0x4E0C, 0xEA4D, 0x73D0, 0x60A1, 0x387A, 0x6654, 0x8785, 0xFB00, 0x5380, 0x589F, 0x9660, 0x91AB, 0xC2D3, 0x281, 0x8D7B, 0xA2C0};
unsigned short resultArray[N] = {0};
unsigned short answerArray[N] = {0};

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void int32ToHex(int num, char* str);
extern void clearCounters();
extern void printCounters();

void* memReset(void* dest, size_t n) {
  int vl, avl = n;
  void *ptr = dest;
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e8, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl));
    asm volatile ("vand.vi v31, v31, 0");
    asm volatile ("vse8.v v31, (%0)"
    :
    : "r"(ptr));
    ptr += vl;
    avl -= vl;
  }
  return ptr;
}

_Bool verifyResult(const unsigned short* ptr0, const unsigned short* ptr1, size_t n) {
  int i;
  _Bool correct = 1;
  char string[19];
  for(i=0; i<n; i++) {
    if(ptr0[i] != ptr1[i]) {
      printstr("ARRAY NOT CORRECT IN INDEX: ");
      int32ToHex(i, string);
      printstr(string);
      printstr("\n");
      correct = 0;
    }
  }
  return correct;
}

int main(int argc, char** argv) {
  int vl, avl = 36;
  const unsigned short *ptr0 = dataArray0, *ptr1 = dataArray1;
  unsigned short *ptrRes = resultArray;
  printstr("START OF VMSEQ TEST:\n");
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e16, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl)
    );
    asm volatile ("vle16.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vle16.v v11, (%0)"
    :
    : "r"(ptr1));
    asm volatile ("vmseq.vv v0, v10, v11");
    asm volatile ("vse16.v v10, (%0), v0.t"
    :
    : "r"(ptrRes));
    ptr0 += vl;
    ptr1 += vl;
    ptrRes += vl;
    avl -= vl;
  }
  int i;
  for(i=0; i<36; i++) {
    if(dataArray0[i] == dataArray1[i]) {
      answerArray[i] = dataArray0[i];
    }
  }
  _Bool correct = verifyResult(resultArray, answerArray, 36);
  memReset(resultArray, 72);
  memReset(answerArray, 72);
  ptr0 = dataArray0;
  ptr1 = dataArray1;
  ptrRes = resultArray;
  avl = 36;
  printstr("START OF VMSNE TEST:\n");
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e16, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl)
    );
    asm volatile ("vle16.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vle16.v v11, (%0)"
    :
    : "r"(ptr1));
    asm volatile ("vmsne.vv v0, v10, v11");
    asm volatile ("vse16.v v10, (%0), v0.t"
    :
    : "r"(ptrRes));
    ptr0 += vl;
    ptr1 += vl;
    ptrRes += vl;
    avl -= vl;
  }
  for(i=0; i<36; i++) {
    if(dataArray0[i] != dataArray1[i]) {
      answerArray[i] = dataArray0[i];
    }
  }
  correct = correct && verifyResult(resultArray, answerArray, 36);
  return !correct;
}