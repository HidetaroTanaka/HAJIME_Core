#include "util.h"

#define size_t long
#define N 48

// #define DEBUG

const unsigned char dataArray0[N] = {0x4D, 0xB4, 0xD, 0x2E, 0x1E, 0x4D, 0xB5, 0x70, 0x2E, 0x4A, 0xA, 0x92, 0x69, 0xB8, 0xBF, 0xD, 0x47, 0x4C, 0x4E, 0x34, 0x6E, 0x8C, 0x24, 0xC6, 0x4F, 0x3, 0x93, 0xA9, 0x26, 0x1B, 0x96, 0xC3, 0xA0, 0xD8, 0x53, 0xFD, 0x50, 0xE7, 0xAC, 0x87, 0x98, 0xE6, 0xF6, 0xAB, 0x8E, 0x5C, 0x49, 0x44};
const unsigned char dataArray1[N] = {0xCA, 0x25, 0xD, 0xC9, 0x8F, 0xA1, 0x86, 0x4A, 0x84, 0x4A, 0xD8, 0x32, 0x39, 0x88, 0xC2, 0x88, 0x55, 0x21, 0x6C, 0xFC, 0xCC, 0xC1, 0x2A, 0xB5, 0x4F, 0xEC, 0x2E, 0xCA, 0xAC, 0x3B, 0x84, 0xBD, 0x1C, 0x25, 0x1D, 0x51, 0x90, 0x76, 0x3, 0x4, 0x8D, 0xE6, 0x2C, 0xE8, 0x8E, 0x34, 0x26, 0x3F};
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
  printstr("START OF VMSLEU TEST:\n");
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
    asm volatile ("vmsleu.vv v0, v10, v11");
    // if(v10 < v11) v10 + 5 else nop
    asm volatile ("vadd.vi v10, v10, 0x5, v0.t");
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
    unsigned char temp = dataArray0[i];
    if(temp <= dataArray1[i]) {
      temp += 5;
    }
    answerArray[i] = temp;
  }
  _Bool correct = verifyResult(resultArray, answerArray, N);
  memReset(resultArray, N);
  memReset(answerArray, N);
  ptr0 = dataArray0;
  ptr1 = dataArray1;
  ptrRes = resultArray;
  avl = N;
  printstr("START OF VMSLE TEST:\n");
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
    asm volatile ("vmsle.vv v0, v10, v11");
    // if(v10 < v11) v10 + 5 else nop
    asm volatile ("vadd.vi v10, v10, 0x5, v0.t");
    asm volatile ("vse8.v v10, (%0)"
    :
    : "r"(ptrRes));
    ptr0 += vl;
    ptr1 += vl;
    ptrRes += vl;
    avl -= vl;
  }
  for(i=0; i<N; i++) {
    signed char temp = (signed char)dataArray0[i];
    if(temp <= (signed char)dataArray1[i]) {
      temp += 5;
    }
    *((signed char*)(answerArray+i)) = temp;
  }
  correct = correct && verifyResult(resultArray, answerArray, N);
  return !correct;
}