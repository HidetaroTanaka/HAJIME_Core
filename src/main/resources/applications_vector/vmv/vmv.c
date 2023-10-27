#include "util.h"

#define size_t long
#define N 48

// #define DEBUG

const unsigned char dataArray0[N] = {0x14, 0x44, 0x68, 0x7, 0x82, 0x55, 0xB2, 0xE0, 0xC3, 0xB9, 0x93, 0x4E, 0xC6, 0x76, 0x50, 0xB6, 0xBF, 0x62, 0x76, 0x38, 0x27, 0x0, 0x8F, 0x58, 0xA1, 0xE5, 0x3, 0x23, 0x6A, 0x38, 0xAD, 0xD1, 0xBA, 0x6C, 0xF8, 0x53, 0x7A, 0xC8, 0xE1, 0xAB, 0x22, 0xB, 0xA3, 0xA4, 0x59, 0x81, 0x25, 0x4};
unsigned char resultArray[N] = {0};
unsigned char answerArray[N] = {0};

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void int32ToHex(int num, char* str);
extern void clearCounters();
extern void printCounters();

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
  const unsigned char *ptr0 = dataArray0;
  unsigned char *ptrRes = resultArray;
  printstr("START OF VMV.V.V TEST:\n");
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e8, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl)
    );
    asm volatile ("vle8.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vmv.v.v v11, v10");
    asm volatile ("vse8.v v11, (%0)"
    :
    : "r"(ptrRes));
    ptr0 += vl;
    ptrRes += vl;
    avl -= vl;
  }
  int i;
  for(i=0; i<N; i++) {
    answerArray[i] = dataArray0[i];
  }
  _Bool correct = verifyResult(resultArray, answerArray, N);
  unsigned int *ptrInt = (unsigned int*)resultArray;
  avl = 12;
  printstr("START OF VMV.V.X TEST:\n");
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e32, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl)
    );
    asm volatile ("vmv.v.x v10, %0"
    :
    : "r"(0x114514));
    asm volatile ("vse32.v v10, (%0)"
    :
    : "r"(ptrInt));
    ptrInt += vl;
    avl -= vl;
  }
  ptrInt = (unsigned int*)answerArray;
  for(i=0; i<12; i++) {
    ptrInt[i] = 0x114514;
  }
  correct = correct && verifyResult(resultArray, answerArray, N);
  return !correct;
}