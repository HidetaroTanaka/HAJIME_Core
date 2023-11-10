#include "util.h"

#define size_t long
#define N 48

const unsigned short dataArray0[N] = {2229, 62092, 15469, 61976, 42298, 14258, 32199, 3671, 14659, 21886, 48577, 5578, 9419, 24199, 43704, 47651, 8581, 56751, 58018, 34058, 34614, 40404, 22179, 36496, 19656, 7360, 27344, 46659, 41186, 48565, 1303, 6734, 56548, 6946, 48589, 21968, 3238, 22028, 41376, 62192, 58563, 64122, 5181, 1171, 17047, 16907, 48231, 24561};
const unsigned short dataArray1[N] = {45080, 49819, 7800, 12777, 367, 33341, 17465, 31645, 55700, 10454, 13949, 21583, 31081, 33567, 31757, 58386, 62774, 33507, 31827, 36488, 11728, 39387, 11606, 27362, 23628, 3202, 4426, 19912, 44128, 47231, 8055, 64181, 21755, 12333, 32628, 40953, 34786, 56967, 42973, 42717, 24376, 8516, 14335, 59365, 30060, 8775, 13671, 35151};
signed short resultArray[N] = {0};
signed short answerArray[N] = {0};

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void int32ToHex(int num, char* str);
extern void clearCounters();
extern void printCounters();

// #define DEBUG

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
  int vl, avl = 41;
  const unsigned short *ptr0 = dataArray0;
  const unsigned short *ptr1 = dataArray1;
  unsigned short *ptr2 = resultArray;
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e16, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl));
    // asm volatile ("li t0, 0x1919");
    asm volatile ("vle16.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vle16.v v11, (%0)"
    :
    : "r"(ptr1));
    asm volatile ("vmulhu.vv v12, v10, v11");
    // asm volatile ("vmulh.vx v12, v12, t0");
    asm volatile ("vse16.v v12, (%0)"
    :
    : "r"(ptr2));
    ptr0 += vl;
    ptr1 += vl;
    ptr2 += vl;
    avl -= vl;
  }
  int i;
  for(i=0; i<41; i++) {
    answerArray[i] = (unsigned short)(((unsigned int)dataArray0[i] * (unsigned int)dataArray1[i]) >> 16);
  }

  _Bool correct = verifyResult(resultArray, answerArray, 41);
  ptr0 = dataArray0;
  ptr2 = resultArray;
  avl = 41;
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e16, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl));
    asm volatile ("li t0, 0x4545");
    asm volatile ("vle16.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vmulhu.vx v12, v10, t0");
    asm volatile ("vse16.v v12, (%0)"
    :
    : "r"(ptr2));
    ptr0 += vl;
    ptr2 += vl;
    avl -= vl;
  }
  for(i=0; i<41; i++) {
    answerArray[i] = (unsigned short)(((unsigned int)dataArray0[i] * (unsigned int)0x4545) >> 16);
  }
  correct = correct && verifyResult(resultArray, answerArray, 41);
  return !correct;
}
