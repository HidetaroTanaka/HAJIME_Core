#include "util.h"

#define size_t long

const char dataArray[48] = {0xA4, 0x3, 0x16, 0x5A, 0x84, 0xBD, 0x7A, 0xC4, 0x41, 0x55, 0x44, 0x6D, 0xE7, 0x3C, 0x0, 0x1B, 0xA7, 0x2A, 0x2C, 0x2B, 0xE4, 0xC7, 0x3, 0x82, 0xB8, 0xAB, 0xA1, 0x90, 0xF3, 0x1B, 0x81, 0x5, 0xE7, 0x6C, 0xA7, 0xD, 0x19, 0x2A, 0xC4, 0x31, 0x98, 0x5, 0xFA, 0xDE, 0x88, 0x1D, 0xA0, 0x95};
const char indexArray[48] = {-11, -12, 6, 8, 4, -4, -8, 16, -13, 7, 11, 2, 10, 20, -22, 1, -6, -2, -19, -5, -15, -16, 19, -17, 21, 15, -7, 22, -21, 18, 9, 17, -20, -23, -18, 3, -1, -3, -14, -10, 13, 0, -24, 5, 14, -9, 12};
char vecResult[48] = {0};
char ansResult[48] = {0};

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void int32ToHex(int num, char* str);
extern void clearCounters();
extern void printCounters();

void* resetMem(void* dest, size_t len) {
  size_t vl;
  void* originalDest = dest;
  while(len != 0) {
    asm volatile ("vsetvli %0, %1, e8, m1, ta, ma"
    : "=r"(vl)
    : "r"(len));
    asm volatile ("vse8.v v0, (%0)"
    :
    : "r"(dest));
    dest += vl;
    len -= vl;
  }
  return originalDest;
}

_Bool verifyVec(const char* array0, const char* array1, size_t n) {
  _Bool correct = 1;
  char string[19];
  int i;
  for(i=0; i<n; i++) {
    if(array0[i] != array1[i]) {
      printstr("ARRAY NOT CORRECT IN INDEX: ");
      int32ToHex(i, string);
      printstr(string);
      printstr("\n");
      correct = 0;
    }
  }
  // 0 if correct, 1 if wrong
  return !correct;
}

int main(int argc, char** argv) {
  const char* ptr = dataArray + 24;
  // indexed load test
  asm volatile ("vsetvli x0, %0, e8, m1, ta, ma"
  :
  : "r"(28));
  asm volatile("vle8.v v17, (%0)"
  :
  : "r"(indexArray));
  // v8 = (0 until 28).map(i => dataArray[indexArray[i]+24])
  asm volatile ("vloxei8.v v8, (%0), v17"
  :
  : "r"(ptr));
  asm volatile ("vse8.v v8, (%0)"
  :
  : "r"(vecResult));
  int i;
  for(i=0; i<28; i++) {
    ansResult[i] = ptr[(signed char)indexArray[i]];
  }
  _Bool returnVal = verifyVec(vecResult, ansResult, 48);
  // indexed store test
  asm volatile ("vsetvli x0, %0, e8, m1, ta, ma"
  :
  : "r"(32));
  // v24: dataArray[16~47]
  asm volatile ("vle8.v v24, (%0)"
  :
  : "r"(dataArray+16));
  // v15: indexArray[16~47]
  asm volatile ("vle8.v v15, (%0)"
  :
  : "r"(indexArray+16));
  // vecResult[indexArray[16+i]+24] = dataArray[16+i]
  asm volatile ("vsoxei8.v v24, (%0), v15"
  :
  : "r"(vecResult+24));
  for(i=0; i<32; i++) {
    ansResult[(signed char)indexArray[16+i]+24] = dataArray[16+i];
  }
  return returnVal || verifyVec(vecResult, ansResult, 48);
}