#include "util.h"

#define size_t long
#define N 48

const signed short dataArray0[N] = {227, 222, 39, 155, 33, 229, 40, 58, 172, 122, 182, 72, 70, 11, 29, 209, 168, 250, 120, 66, 179, 133, 115, 55, 253, 82, 196, 219, 44, 120, 18, 179, 143, 6, 120, 112, 192, 18, 144, 89, 238, 106, 17, 50, 153, 105, 173, 160};
signed short result, answer;

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void int32ToHex(int num, char* str);
extern void clearCounters();
extern void printCounters();

int main(int argc, char** argv) {
  int vl, avl = 41;
  const signed short *ptr0 = dataArray0;
  signed short sum = 0;
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e16, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl));
    // v11[0] = sum;
    asm volatile ("vmv.s.x v11, %0"
    :
    : "r"(sum));
    asm volatile ("vle16.v v10, (%0)"
    :
    : "r"(ptr0));
    // v10[0] = sum + sum(v10)
    asm volatile ("vredsum.vs v10, v10, v11");
    // sum = sum(v10)
    asm volatile ("vmv.x.s %0, v10"
    : "=r"(sum));
    ptr0 += vl;
    avl -= vl;
  }
  int i;
  for(i=0; i<41; i++) {
    answerArray[i] = -(dataArray0[i] * dataArray2[i]) + dataArray1[i];
  }

  _Bool correct = 1;
  for(i=0; i<41; i++) {
    if(resultArray[i] != answerArray[i]) {
      correct = 0;
    }
  }
  avl = 41;
  ptr0 = dataArray0;
  ptr1 = dataArray1;
  ptr3 = resultArray;
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e16, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl));
    asm volatile ("vle16.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vle16.v v11, (%0)"
    :
    : "r"(ptr1));
    // v11 = -(0x1919 * v11) + v10
    asm volatile ("vnmsub.vx v11, %0, v10"
    :
    : "r"(0x1919));
    asm volatile ("vse16.v v11, (%0)"
    :
    : "r"(ptr3));
    ptr0 += vl;
    ptr1 += vl;
    ptr3 += vl;
    avl -= vl;
  }
  for(i=0; i<41; i++) {
    answerArray[i] = -(0x1919 * dataArray1[i]) + dataArray0[i];
  }
  for(i=0; i<41; i++) {
    if(resultArray[i] != answerArray[i]) {
      correct = 0;
    }
  }
  return !correct;
}