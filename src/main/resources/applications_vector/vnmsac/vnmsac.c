#include "util.h"

#define size_t long
#define N 48

const signed short dataArray0[N] = {-26995, -13957, -10861, 25374, -18032, -21149, 23738, 24548, -28110, -21910, 16774, -10117, 19840, -11921, 12711, -24192, 27586, -20052, -20407, 13650, -15786, 21109, 31998, 18796, -11098, -1692, 1179, -29277, -17643, -26788, -15187, -12884, -8109, -21033, -28781, 21148, -26980, 24605, 14759, 5326, 32568, 14468, -29372, -25205, -3712, 12033, -13808, 15296};
const signed short dataArray1[N] = {30505, 20166, 5646, -22016, -20267, 1851, -16870, -15522, -6478, 28568, -24933, -8558, 28891, 29410, -5672, -25610, 17628, -16913, -3658, -29191, -18246, 7697, 16690, -9899, 5905, -25983, 25695, 6411, -409, 1080, 8789, -23885, -15650, 27052, -8835, -13117, -11213, 12484, 30265, -20895, 29055, -28773, -22596, 15861, -29873, 27504, 21957, -13862};
const signed short dataArray2[N] = {28495, -8577, 25952, 32552, -21237, -11141, 13665, 4943, 18722, -1943, -7490, 23423, 26213, -2937, 12240, -23931, 9720, 32697, 28006, -15275, 24539, -26743, 25687, -5263, 31572, -8462, 17153, -10801, -665, 29112, 31568, -23133, 16087, 29233, 7190, -16073, -22331, -30392, 18281, -2016, -24649, 29272, 19468, 2718, 4672, 12042, 8434, -28150};
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
  const signed short *ptr2 = dataArray2;
  signed short *ptr3 = resultArray;
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
    asm volatile ("vle16.v v12, (%0)"
    :
    : "r"(ptr2));
    // v12 = -(v10 * v11) + v12
    asm volatile ("vnmsac.vv v12, v10, v11");
    asm volatile ("vse16.v v12, (%0)"
    :
    : "r"(ptr3));
    ptr0 += vl;
    ptr1 += vl;
    ptr2 += vl;
    ptr3 += vl;
    avl -= vl;
  }
  int i;
  for(i=0; i<41; i++) {
    answerArray[i] = -(dataArray0[i] * dataArray1[i]) + dataArray2[i];
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
    // v11 = -(0x1919 * v10) + v11
    asm volatile ("vnmsac.vx v11, %0, v10"
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
    answerArray[i] = -(0x1919 * dataArray0[i]) + dataArray1[i];
  }
  for(i=0; i<41; i++) {
    if(resultArray[i] != answerArray[i]) {
      correct = 0;
    }
  }
  return !correct;
}
