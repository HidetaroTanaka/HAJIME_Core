#include "util.h"

#define size_t long
#define N 48

const signed short dataArray0[N] = {0xC02D, 0x5923, 0x2871, 0xAFF6, 0x880D, 0xE71D, 0xBE2F, 0xF57A, 0x5B71, 0x34C7, 0xE16F, 0xD3B7, 0x1C60, 0x3EE7, 0x8CF4, 0x5234, 0x520F, 0x5B9A, 0xCE6B, 0x2A58, 0xB2A7, 0xEFE8, 0x546E, 0xC4B3, 0xF4E1, 0xBEB7, 0x1FF4, 0x358, 0x5FBC, 0x8726, 0x8A4B, 0x2F49, 0x9362, 0x4C5E, 0xDFD9, 0x1615, 0xC5AD, 0xE4D8, 0x6233, 0xB139, 0x44C8, 0x466E, 0x1906, 0x407D, 0x33E1, 0x6844, 0xA3A7, 0xE5FC};
const signed short dataArray1[N] = {0xA225, 0xA2B1, 0x56F0, 0x3862, 0x6079, 0x2300, 0x3BF0, 0xAE87, 0xF41A, 0x5A87, 0xD476, 0x2F2B, 0x1049, 0xDA35, 0xFE1C, 0xCBE1, 0x4FA7, 0x2AA6, 0xFB7E, 0x923, 0x738D, 0x3C7C, 0x7797, 0x7B0B, 0x7FAE, 0xC46E, 0xBA4C, 0x8001, 0xE40B, 0x590E, 0xD048, 0x294A, 0xD186, 0xEBAD, 0x77BB, 0xB47F, 0x677A, 0xB406, 0x1E40, 0xDBD5, 0xAE23, 0x299A, 0x3F04, 0xA982, 0xC976, 0xDC6D, 0xACE0, 0x5D39};
const signed short dataArray2[N] = {0x40B5, 0x4514, 0xC3E9, 0xD60A, 0x3BFE, 0x563, 0x11B1, 0xB809, 0x3422, 0xCA78, 0xA74F, 0x115F, 0x3F2C, 0x29E4, 0x31F6, 0xCA9B, 0xBCC0, 0xF7, 0xA2B0, 0x7360, 0x5093, 0xB7FA, 0xD5CD, 0xDBE, 0x7A44, 0xBD3, 0xA64E, 0x39A8, 0x520B, 0xFD93, 0x3710, 0x7FE9, 0x96CF, 0x1CD9, 0xE935, 0x7CD7, 0x7929, 0xEEC1, 0x84F6, 0xBEC1, 0xB11B, 0x6754, 0x12D0, 0x1CD5, 0xE89, 0xBB5B, 0xC66F, 0x479B};
signed short resultArray[N] = {0};
signed short answerArray[N] = {0};

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void int32ToHex(int num, char* str);
extern void clearCounters();
extern void printCounters();

int main(int argc, char** argv) {
  int vl, avl = 41;
  const signed short *ptr0 = dataArray0, *ptr1 = dataArray1, *ptr2 = dataArray2;
  signed short *ptrRes = resultArray;
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e16, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl)
    );
    asm volatile ("vle16.v v10, (%0)"
    :
    : "r"(ptr0));
    asm volatile ("vle16.v v11, (%0)"
    :
    : "r"(ptr1));
    asm volatile ("vle16.v v12, (%0)"
    :
    : "r"(ptr2));
    // v0 = borrow_out(dataArray0 - dataArray1)
    asm volatile ("vmsbc.vv v0, v10, v11");
    asm volatile ("vsbc.vxm v13, v12, %0, v0"
    :
    : "r"(0x0721));
    asm volatile ("vse16.v v13, (%0)"
    :
    : "r"(ptrRes));
    ptr0 += vl;
    ptr1 += vl;
    ptr2 += vl;
    ptrRes += vl;
    avl -= vl;
  }
  int i;
  for(i=0; i<41; i++) {
    _Bool borrow_out = (signed int)(dataArray0[i] - dataArray1[i]) < (signed int)0;
    answerArray[i] = dataArray2[i] - (signed short)0x0721 - (signed short)borrow_out;
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