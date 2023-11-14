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
  result = sum;
  int i, answer = 0;
  for(i=0; i<41; i++) {
    answer += dataArray0[i];
  }

  _Bool correct = (result == answer);
  return !correct;
}