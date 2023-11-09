#include "util.h"

#define size_t long
#define N 48

const signed short dataArray0[N] = {-20077, 25345, 25710, -16233, 13815, -13064, -5572, 7917, -27975, 18375, 31685, 29222, 15554, 31403, -19676, -22725, -16487, 9872, -18497, 27388, 3080, 22533, 5209, 11456, -16847, 32108, 11099, -29570, -12706, 11380, 19405, 16128, 29114, 17078, 7233, -5089, 31950, 28528, 1030, 17699, -12819, -31249, 9337, 3475, 19029, 22473, -10923, -27762};
const signed short dataArray1[N] = {20029, 13614, 22913, -15313, -27960, 29368, -25713, 3275, 4606, 28018, -12499, 23733, 10377, -25287, -15976, 10629, -14324, 21827, 16250, -16667, -24599, 17674, 8347, 4985, 29222, 7735, 18195, -6070, 20128, 9752, 406, 13172, -1571, -5360, 210, -2082, 8623, -13329, -19375, 10875, 5743, 32060, 22867, 31804, 11080, -2510, -30824, 10865};
signed short resultArray[N] = {0};
signed short answerArray[N] = {0};

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void int32ToHex(int num, char* str);
extern void clearCounters();
extern void printCounters();

int main(int argc, char** argv) {
  int vl, avl = 41;
  const signed short *ptr0 = dataArray0;
  const signed short *ptr1 = dataArray1;
  signed short *ptr2 = resultArray;
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e32, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl));
    asm volatile ("li t0, 0x1919");
    asm volatile ("vle16.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vle16.v v11, (%0)"
    :
    : "r"(ptr1));
    asm volatile ("vmulh.vv v12, v10, v11");
    asm volatile ("vmulh.vx v12, v12, t0");
    asm volatile ("vse32.v v12, (%0)"
    :
    : "r"(ptr2));
    ptr0 += vl;
    ptr1 += vl;
    ptr2 += vl;
    avl -= vl;
  }
  int i;
  for(i=0; i<41; i++) {
    answerArray[i] = (signed short)(((signed int)dataArray0[i] * (signed int)dataArray1[i] * (signed int)0x1919) >> 16);
  }

  _Bool correct = 1;
  char string[19];
  for(i=0; i<41; i++) {
    if(resultArray[i] != answerArray[i]) {
      printstr("ARRAY NOT CORRECT IN INDEX: ");
      int32ToHex(i, string);
      printstr(string);
      printstr("\n");
      correct = 0;
    }
  }
  return !correct;
}

