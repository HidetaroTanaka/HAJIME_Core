#include "util.h"

#define size_t long
#define N 48

// #define DEBUG

const unsigned char dataArray0[N] = {0xA6, 0xAF, 0xF5, 0x7F, 0xC2, 0x9F, 0xB, 0xE7, 0x69, 0x49, 0x6B, 0x18, 0x3A, 0xED, 0x6, 0xB6, 0xC9, 0x8F, 0x64, 0xF9, 0xE, 0xB3, 0x67, 0x52, 0xE, 0x8B, 0x54, 0x45, 0x81, 0xEB, 0x35, 0xE7, 0x2A, 0x1F, 0x85, 0x23, 0x93, 0x1D, 0x93, 0x71, 0x18, 0x4D, 0xB, 0xDA, 0xB, 0xB7, 0xB2, 0x12};
const unsigned char dataArray1[N] = {0x57, 0x6, 0xB6, 0x91, 0x81, 0xC, 0x13, 0xAD, 0x5E, 0xF0, 0x53, 0x73, 0x66, 0xCE, 0x69, 0xCF, 0xDF, 0xFC, 0x33, 0x19, 0xE0, 0xCC, 0xEC, 0x3F, 0x41, 0x8E, 0x6C, 0xFE, 0xDF, 0xC0, 0xC7, 0x69, 0x6, 0x61, 0xCD, 0x88, 0x5, 0x18, 0x89, 0xBE, 0x99, 0x44, 0xBC, 0xB0, 0xD4, 0xBF, 0x2E, 0x65};
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
  printstr("START OF VMSLTU TEST:\n");
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
    asm volatile ("vmsltu.vv v0, v10, v11");
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
    if(temp < dataArray1[i]) {
      temp += 5;
    }
    answerArray[i] = temp;
  }
  return !verifyResult(resultArray, answerArray, 48);
}