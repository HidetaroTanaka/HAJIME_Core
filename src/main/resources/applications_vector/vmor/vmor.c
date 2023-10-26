#include "util.h"

#define size_t long
#define N 48

// #define DEBUG

const unsigned char dataArray0[N] = {0x14, 0x44, 0x68, 0x7, 0x82, 0x55, 0xB2, 0xE0, 0xC3, 0xB9, 0x93, 0x4E, 0xC6, 0x76, 0x50, 0xB6, 0xBF, 0x62, 0x76, 0x38, 0x27, 0x0, 0x8F, 0x58, 0xA1, 0xE5, 0x3, 0x23, 0x6A, 0x38, 0xAD, 0xD1, 0xBA, 0x6C, 0xF8, 0x53, 0x7A, 0xC8, 0xE1, 0xAB, 0x22, 0xB, 0xA3, 0xA4, 0x59, 0x81, 0x25, 0x4};
const unsigned char dataArray1[N] = {0x8E, 0x57, 0x5, 0xAA, 0x2C, 0xA9, 0x35, 0xA6, 0x7F, 0x77, 0x46, 0x28, 0x70, 0x6F, 0xF0, 0xA6, 0x2B, 0x66, 0x22, 0x27, 0xDF, 0xA0, 0xDD, 0x81, 0xBC, 0x89, 0x39, 0xB9, 0xED, 0x4A, 0xC1, 0xD0, 0x56, 0x6, 0x3D, 0x42, 0x27, 0x2F, 0x9D, 0x70, 0xAD, 0x99, 0xDF, 0xA, 0x5C, 0x8E, 0xF4, 0x78};
const unsigned char dataArray2[N] = {0xCE, 0x1F, 0x94, 0x38, 0x74, 0x13, 0x62, 0x16, 0x4E, 0x70, 0xC9, 0xC6, 0xBF, 0xB3, 0xCE, 0xED, 0x6A, 0xE7, 0xD3, 0x3F, 0xC3, 0xAA, 0xC0, 0x3D, 0xF5, 0x89, 0xC5, 0x48, 0x18, 0x13, 0x15, 0x7E, 0x52, 0x97, 0xFE, 0x47, 0x56, 0x9A, 0xAF, 0xAC, 0x71, 0x8F, 0x4E, 0xED, 0xFE, 0x98, 0xA1, 0x2D};
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
  const unsigned char *ptr0 = dataArray0, *ptr1 = dataArray1, *ptr2 = dataArray2;
  unsigned char *ptrRes = resultArray;
  printstr("START OF VMOR TEST:\n");
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e8, m1, ta, mu"
    : "=r"(vl)
    : "r"(avl)
    );
    asm volatile ("vle8.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vle8.v v11, (%0)"
    :
    : "r"(ptr1));
    asm volatile ("vle8.v v12, (%0)"
    :
    : "r"(ptr2));
    asm volatile ("vmsgtu.vv v0, v10, v11");
    asm volatile ("vmsltu.vv v1, v11, v12");
    asm volatile ("vmor.mm v0, v0, v1");
    asm volatile ("vadd.vi v10, v10, 0x5, v0.t");
    asm volatile ("vse8.v v10, (%0)"
    :
    : "r"(ptrRes));
    ptr0 += vl;
    ptr1 += vl;
    ptr2 += vl;
    ptrRes += vl;
    avl -= vl;
  }
  int i;
  for(i=0; i<N; i++) {
    unsigned char temp0 = dataArray0[i];
    unsigned char temp1 = dataArray1[i];
    unsigned char temp2 = dataArray2[i];
    _Bool vs2 = (temp0 > temp1);
    _Bool vs1 = (temp1 < temp2);
    if(vs2 || vs1) {
      temp0 += 5;
    }
    answerArray[i] = temp0;
  }
  _Bool correct = verifyResult(resultArray, answerArray, N);
  memReset(resultArray, N);
  memReset(answerArray, N);
  avl = N;
  ptr0 = dataArray0;
  ptr1 = dataArray1;
  ptr2 = dataArray2;
  ptrRes = resultArray;
  printstr("START OF VMNOR TEST:\n");
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e8, m1, ta, mu"
    : "=r"(vl)
    : "r"(avl)
    );
    asm volatile ("vle8.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vle8.v v11, (%0)"
    :
    : "r"(ptr1));
    asm volatile ("vle8.v v12, (%0)"
    :
    : "r"(ptr2));
    asm volatile ("vmsgtu.vv v0, v10, v11");
    asm volatile ("vmsltu.vv v1, v11, v12");
    asm volatile ("vmnor.mm v0, v0, v1");
    asm volatile ("vadd.vi v10, v10, 0x5, v0.t");
    asm volatile ("vse8.v v10, (%0)"
    :
    : "r"(ptrRes));
    ptr0 += vl;
    ptr1 += vl;
    ptr2 += vl;
    ptrRes += vl;
    avl -= vl;
  }
  for(i=0; i<N; i++) {
    unsigned char temp0 = dataArray0[i];
    unsigned char temp1 = dataArray1[i];
    unsigned char temp2 = dataArray2[i];
    _Bool vs2 = (temp0 > temp1);
    _Bool vs1 = (temp1 < temp2);
    if(!(vs2 || vs1)) {
      temp0 += 5;
    }
    answerArray[i] = temp0;
  }
  correct = correct && verifyResult(resultArray, answerArray, N);
  memReset(resultArray, N);
  memReset(answerArray, N);
  avl = N;
  ptr0 = dataArray0;
  ptr1 = dataArray1;
  ptr2 = dataArray2;
  ptrRes = resultArray;
  printstr("START OF VMORN TEST:\n");
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e8, m1, ta, mu"
    : "=r"(vl)
    : "r"(avl)
    );
    asm volatile ("vle8.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vle8.v v11, (%0)"
    :
    : "r"(ptr1));
    asm volatile ("vle8.v v12, (%0)"
    :
    : "r"(ptr2));
    asm volatile ("vmsgtu.vv v0, v10, v11");
    asm volatile ("vmsltu.vv v1, v11, v12");
    asm volatile ("vmorn.mm v0, v0, v1");
    asm volatile ("vadd.vi v10, v10, 0x5, v0.t");
    asm volatile ("vse8.v v10, (%0)"
    :
    : "r"(ptrRes));
    ptr0 += vl;
    ptr1 += vl;
    ptr2 += vl;
    ptrRes += vl;
    avl -= vl;
  }
  for(i=0; i<N; i++) {
    unsigned char temp0 = dataArray0[i];
    unsigned char temp1 = dataArray1[i];
    unsigned char temp2 = dataArray2[i];
    _Bool vs2 = (temp0 > temp1);
    _Bool vs1 = (temp1 < temp2);
    if(vs2 || !vs1) {
      temp0 += 5;
    }
    answerArray[i] = temp0;
  }
  correct = correct && verifyResult(resultArray, answerArray, N);
  return !correct;
}