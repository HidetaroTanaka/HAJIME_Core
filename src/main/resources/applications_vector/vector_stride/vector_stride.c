#include "util.h"

#define size_t long

char charArray0[48] = {0xA4, 0x3, 0x16, 0x5A, 0x84, 0xBD, 0x7A, 0xC4, 0x41, 0x55, 0x44, 0x6D, 0xE7, 0x3C, 0x0, 0x1B, 0xA7, 0x2A, 0x2C, 0x2B, 0xE4, 0xC7, 0x3, 0x82, 0xB8, 0xAB, 0xA1, 0x90, 0xF3, 0x1B, 0x81, 0x5, 0xE7, 0x6C, 0xA7, 0xD, 0x19, 0x2A, 0xC4, 0x31, 0x98, 0x5, 0xFA, 0xDE, 0x88, 0x1D, 0xA0, 0x95};
char charArray1[48] = {0};
char charArray2[48] = {0};
extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void int32ToHex(int num, char* str);
extern void clearCounters();
extern void printCounters();

// TODO: make sure v0 is zero
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

int main(int argc, char** argv) {
  asm volatile ("vsetvli x0, %0, e8, m1, ta, ma"
  :
  : "r"(17));
  asm volatile ("vlse8.v v12, (%0), %1"
  :
  : "r"(charArray0), "r"(2));
  asm volatile ("vsse8.v v12, (%0), %1"
  :
  : "r"(charArray1), "r"(2));
  int i;
  char string[19];
  _Bool correct = 1;
  for(i=0; i<48; i++) {
    // i=0,2,...32 -> charArray0[i]
    // otherwise -> 0
    int correctVal;
    if(((i & 0x1) == 0) && i < 33) {
      correctVal = charArray0[i];
    } else {
      correctVal = 0;
    }
    if(charArray1[i] != correctVal) {
      printstr("ARRAY NOT CORRECT IN INDEX: ");
      int32ToHex(i, string);
      printstr(string);
      printstr("\n");
      correct = 0;
    }
  }
  // reset
  resetMem(charArray1, 48);

  asm volatile ("vsetvli x0, %0, e16, m1, ta, ma"
  :
  : "r"(6));
  asm volatile ("vlse16.v v6, (%0), %1"
  :
  : "r"(charArray0), "r"(3));
  asm volatile ("vse16.v v6, (%0)"
  :
  : "r"(charArray1)
  );
  // expect:
  // {0xA4, 0x3, 0x7A, 0xC4, 0xE7, 0x3C, 0x2C, 0x2B, 0xB8, 0xAB, 0x81, 0x5}
  short *ptr0, *ptr1;
  ptr0 = (short*)charArray2;
  ptr1 = (short*)charArray0;
  for(i=0; i<6; i++) {
    ptr0[i] = ptr1[i*2+i];
  }
  for(i=0; i<48; i++) {
    if(charArray1[i] != charArray2[i]) {
      printstr("ARRAY NOT CORRECT IN INDEX: ");
      int32ToHex(i, string);
      printstr(string);
      printstr("\n");
      correct = 0;
    }
  }

  resetMem(charArray1, 48);
  // zero stride test
  asm volatile ("vsetvli x0, %0, e8, m1, ta, ma"
  :
  : "r"(25));
  asm volatile ("vlse8.v v21, (%0), zero"
  :
  : "r"(charArray0));
  asm volatile ("vse8.v v21, (%0)"
  :
  : "r"(charArray1));
  for(i=0; i<48; i++) {
    int correctVal = (i < 25) ? charArray0[0] : 0;
    if(charArray1[i] != correctVal) {
      printstr("ARRAY NOT CORRECT IN INDEX: ");
      int32ToHex(i, string);
      printstr(string);
      printstr("\n");
      correct = 0;
    }
  }

  resetMem(charArray1, 48);
  resetMem(charArray2, 48);
  // negative stride test
  asm volatile ("vsetvli x0, %0, e8, m1, ta, ma"
  :
  : "r"(9));
  asm volatile ("vlse8.v v29, (%0), %1"
  :
  : "r"(charArray0+47), "r"(-4));
  // v29: 0x95, 0xDE, 0x31, ...
  asm volatile ("vse8.v v29, (%0)"
  :
  : "r"(charArray1));
  for(i=0; i<9; i++) {
    charArray2[i] = charArray0[47-i*4];
  }
  for(i=0; i<48; i++) {
    if(charArray1[i] != charArray2[i]) {
      printstr("ARRAY NOT CORRECT IN INDEX: ");
      int32ToHex(i, string);
      printstr(string);
      printstr("\n");
      correct = 0;
    }
  }
  return !correct;
}