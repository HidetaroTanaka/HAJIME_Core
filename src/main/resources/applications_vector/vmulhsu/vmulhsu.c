#include "util.h"

#define size_t long
#define N 48

const signed short dataArray0[N] = {-1100, -15580, -19526, 10821, -10015, 24520, 13245, 27495, 3681, -6259, -13158, 10867, -10373, 4291, -25823, 12893, -6321, 11597, 29479, 32317, -2720, 9528, 5258, -9483, -5490, 22114, 24180, -20097, -26016, 610, 10995, -29894, -9039, -19687, 14840, 7886, 9945, -7046, -11470, -24647, 22616, 16915, 24884, -19698, -496, -11311, -4182, 13068};
const unsigned short dataArray1[N] = {29466, 42658, 21942, 41806, 41950, 10755, 12935, 18044, 7624, 24094, 15436, 17118, 7270, 49825, 37559, 5813, 61107, 39714, 41420, 41623, 59741, 45743, 38191, 46232, 23338, 53158, 3963, 24954, 8427, 1670, 19967, 35686, 61436, 46339, 37534, 6616, 11953, 3519, 45568, 37501, 55213, 2853, 19112, 40323, 55417, 43250, 39883, 33336};
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
  const signed short *ptr0 = dataArray0;
  const unsigned short *ptr1 = dataArray1;
  signed short *ptr2 = resultArray;
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
    asm volatile ("vmulhsu.vv v12, v10, v11");
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
    answerArray[i] = (signed short)(((signed int)dataArray0[i] * (unsigned int)dataArray1[i]) >> 16);
  }

  _Bool correct = verifyResult(resultArray, answerArray, 41);
  ptr0 = dataArray0;
  ptr2 = resultArray;
  avl = 41;
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e16, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl));
    asm volatile ("li t0, 0xBEEF");
    asm volatile ("vle16.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vmulhsu.vx v12, v10, t0");
    asm volatile ("vse16.v v12, (%0)"
    :
    : "r"(ptr2));
    ptr0 += vl;
    ptr2 += vl;
    avl -= vl;
  }
  for(i=0; i<41; i++) {
    answerArray[i] = (signed short)(((signed int)dataArray0[i] * (unsigned int)0xBEEF) >> 16);
  }
  correct = correct && verifyResult(resultArray, answerArray, 41);
  return !correct;
}
