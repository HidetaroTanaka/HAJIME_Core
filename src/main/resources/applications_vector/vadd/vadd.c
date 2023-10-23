#include "util.h"

#define size_t long
#define N 48

const int dataArray0[N] = {6, -12, 12, -5, 20, 15, -14, 8, 9, -19, 18, 11, 7, -10, -16, -3, 16, -23, -9, 19, 4, 1, -17, -20, 3, 0, -21, -8, -6, -22, 2, -13, -2, 22, 5, -24, -11, 14, -1, 10, -4, 13, 17, 21, -15, -7, -18};
const int dataArray1[N] = {9, -13, -16, -21, -3, -23, 12, -17, -15, 22, 15, -19, 16, 10, -2, 6, -12, 4, 20, 18, -9, -7, 17, -14, 14, 8, -20, -24, -11, -10, 7, 2, 21, -4, 3, -22, 0, -8, 5, -18, -1, 13, -5, 19, 1, -6, 11};
int resultArray[N] = {0};
int answerArray[N] = {0};

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void int32ToHex(int num, char* str);
extern void clearCounters();
extern void printCounters();

int main(int argc, char** argv) {
  int vl, avl = 41;
  const int *ptr0 = dataArray0;
  const int *ptr1 = dataArray1;
  int *ptr2 = resultArray;
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e32, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl));
    asm volatile ("vle32.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vle32.v v11, (%0)"
    :
    : "r"(ptr1));
    asm volatile ("vadd.vv v12, v10, v11");
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
    answerArray[i] = dataArray0[i] + dataArray1[i];
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

