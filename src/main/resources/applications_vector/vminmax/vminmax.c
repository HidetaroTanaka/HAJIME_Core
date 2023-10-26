#include "util.h"

#define size_t long
#define N 48

// #define DEBUG

const unsigned char dataArray0[N] = {0x14, 0x44, 0x68, 0x7, 0x82, 0x55, 0xB2, 0xE0, 0xC3, 0xB9, 0x93, 0x4E, 0xC6, 0x76, 0x50, 0xB6, 0xBF, 0x62, 0x76, 0x38, 0x27, 0x0, 0x8F, 0x58, 0xA1, 0xE5, 0x3, 0x23, 0x6A, 0x38, 0xAD, 0xD1, 0xBA, 0x6C, 0xF8, 0x53, 0x7A, 0xC8, 0xE1, 0xAB, 0x22, 0xB, 0xA3, 0xA4, 0x59, 0x81, 0x25, 0x4};
const unsigned char dataArray1[N] = {0x8E, 0x57, 0x5, 0xAA, 0x2C, 0xA9, 0x35, 0xA6, 0x7F, 0x77, 0x46, 0x28, 0x70, 0x6F, 0xF0, 0xA6, 0x2B, 0x66, 0x22, 0x27, 0xDF, 0xA0, 0xDD, 0x81, 0xBC, 0x89, 0x39, 0xB9, 0xED, 0x4A, 0xC1, 0xD0, 0x56, 0x6, 0x3D, 0x42, 0x27, 0x2F, 0x9D, 0x70, 0xAD, 0x99, 0xDF, 0xA, 0x5C, 0x8E, 0xF4, 0x78};
unsigned char resultArray[N] = {0};
unsigned char answerArray[N] = {0};

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

_Bool verifyResult(const unsigned char* ptr0, const unsigned char* ptr1, size_t n) {
  int i;
  _Bool correct = 1;
  char string[19];
  for(i=0; i<n; i++) {
    if(ptr0[i] != ptr1[i]) {
      printstr("ARRAY NOT CORRECT IN INDEX: ");
      int32ToHex(i, string);
      printstr(string);
      printstr("\n");
#ifdef DEBUG
      printstr("RES: ");
      int32ToHex(ptr0[i], string);
      printstr(string);
      printstr(", ANS: ");
      int32ToHex(ptr1[i], string);
      printstr(string);
      printstr("\n");
#endif
      correct = 0;
    }
  }
  return correct;
}

int main(int argc, char** argv) {
  int vl, avl = N;
  const unsigned char *ptr0 = dataArray0, *ptr1 = dataArray1;
  unsigned char *ptrRes = resultArray;
  printstr("START OF VMINU TEST:\n");
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e8, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl)
    );
    asm volatile ("vle8.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vle8.v v11, (%0)"
    :
    : "r"(ptr1));
    asm volatile ("vminu.vv v10, v10, v11");
    asm volatile ("vse8.v v10, (%0)"
    :
    : "r"(ptrRes));
    ptr0 += vl;
    ptr1 += vl;
    ptrRes += vl;
    avl -= vl;
  }
  int i;
  for(i=0; i<N; i++) {
    if(dataArray0[i] < dataArray1[i]) {
      answerArray[i] = dataArray0[i];
    } else {
      answerArray[i] = dataArray1[i];
    }
  }
  _Bool correct = verifyResult(resultArray, answerArray, N);
  avl = N;
  ptr0 = dataArray0;
  ptr1 = dataArray1;
  ptrRes = resultArray;
  printstr("START OF VMIN TEST:\n");
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e8, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl)
    );
    asm volatile ("vle8.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vle8.v v11, (%0)"
    :
    : "r"(ptr1));
    asm volatile ("vmin.vv v10, v10, v11");
    asm volatile ("vse8.v v10, (%0)"
    :
    : "r"(ptrRes));
    ptr0 += vl;
    ptr1 += vl;
    ptrRes += vl;
    avl -= vl;
  }
  for(i=0; i<N; i++) {
    signed char* a0 = (signed char*)dataArray0;
    signed char* a1 = (signed char*)dataArray1;
    if(a0[i] < a1[i]) {
      answerArray[i] = dataArray0[i];
    } else {
      answerArray[i] = dataArray1[i];
    }
  }
  correct = correct && verifyResult(resultArray, answerArray, N);
  avl = N;
  ptr0 = dataArray0;
  ptr1 = dataArray1;
  ptrRes = resultArray;
  printstr("START OF VMAXU TEST:\n");
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e8, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl)
    );
    asm volatile ("vle8.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vle8.v v11, (%0)"
    :
    : "r"(ptr1));
    asm volatile ("vmaxu.vv v10, v10, v11");
    asm volatile ("vse8.v v10, (%0)"
    :
    : "r"(ptrRes));
    ptr0 += vl;
    ptr1 += vl;
    ptrRes += vl;
    avl -= vl;
  }
  for(i=0; i<N; i++) {
    if(dataArray0[i] < dataArray1[i]) {
      answerArray[i] = dataArray1[i];
    } else {
      answerArray[i] = dataArray0[i];
    }
  }
  correct = correct && verifyResult(resultArray, answerArray, N);
  avl = N;
  ptr0 = dataArray0;
  ptr1 = dataArray1;
  ptrRes = resultArray;
  printstr("START OF VMAX TEST:\n");
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e8, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl)
    );
    asm volatile ("vle8.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vle8.v v11, (%0)"
    :
    : "r"(ptr1));
    asm volatile ("vmax.vv v10, v10, v11");
    asm volatile ("vse8.v v10, (%0)"
    :
    : "r"(ptrRes));
    ptr0 += vl;
    ptr1 += vl;
    ptrRes += vl;
    avl -= vl;
  }
  for(i=0; i<N; i++) {
    signed char* a0 = (signed char*)dataArray0;
    signed char* a1 = (signed char*)dataArray1;
    if(a0[i] < a1[i]) {
      answerArray[i] = dataArray1[i];
    } else {
      answerArray[i] = dataArray0[i];
    }
  }
  correct = correct && verifyResult(resultArray, answerArray, N);
  return !correct;
}