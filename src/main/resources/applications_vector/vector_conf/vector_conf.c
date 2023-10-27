#include "util.h"

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void int32ToHex(int num, char* str);
extern void clearCounters();
extern void printCounters();

void printVtypeInfo(long vtype) {
char string[19];
  printstr("vtype: ");
  int64ToHex(vtype, string);
  printstr(string);
  printstr("\nillegal: ");
  if(vtype < 0) {
    printstr("true");
  } else {
    printstr("false");
  }
  printstr("\nreserved: ");
  if((vtype & 0x7FFFFFFFFFFFFF00L) != 0) {
    printstr("true");
  } else {
    printstr("false");
  }
  printstr("\nMask: ");
  if((vtype & 0x0000000000000080L) != 0) {
    printstr("Agnostic");
  } else {
    printstr("Undisturbed");
  }
  printstr("\nTail: ");
  if((vtype & 0x0000000000000040L) != 0) {
    printstr("Agnostic");
  } else {
    printstr("Undisturbed");
  }
  printstr("\nSEW: ");
  int vsew = (int)(8 << ((vtype >> 3) & 0x7));
  if(vsew <= 64 && vsew >= 0) {
    int32ToHex(vsew, string);
    printstr(string);
  } else {
    printstr("Reserved");
  }
  printstr("\nLMUL: ");
  if((vtype & 0x0000000000000007L) == 0) {
    printstr("1");
  } else {
    printstr("illegal for this implementation.");
  }
  printstr("\n");
}

int main(int argc, char** argv) {
  int i;
  char string[19];
  long vl=1, avl = 29, vtype;
  for(i=0; avl != 0; i++) {
    asm volatile ("vsetvli %0, %1, e32, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl));
    asm volatile ("csrr %0, vtype"
    : "=r"(vtype));
    printstr("ITERATION ");
    int32ToHex(i, string);
    printstr(string);
    printstr("\n");
    printstr("vl: ");
    int64ToHex(vl, string);
    printstr(string);
    printstr("\n");
    printVtypeInfo(vtype);
    printstr("\n");
    avl -= vl;
  }
  // illegal vtypei test
  printstr("ILLEGAL VTYPEI TEST:\n");
  asm volatile ("vsetvli zero, %0, e32, m2, ta, ma"
  :
  : "r"(41));
  asm volatile ("csrr %0, vtype"
  : "=r"(vtype));
  printVtypeInfo(vtype);
  asm volatile ("vsetvli zero, %0, e32, mf4, ta, ma"
  :
  : "r"(41));
  asm volatile ("csrr %0, vtype"
  : "=r"(vtype));
  printVtypeInfo(vtype);
  printstr("\n");

  // vsetivli test
  printstr("VSETIVLI TEST:\n");
  asm volatile ("vsetivli %0, 28, e16, m1, tu, ma"
  : "=r"(vl));
  asm volatile ("csrr %0, vtype"
  : "=r"(vtype));
  printstr("vl: ");
  int64ToHex(vl, string);
  printstr(string);
  printstr("\n");
  printVtypeInfo(vtype);
  printstr("\n");

  // vsetvl test
  printstr("VSETVL TEST:\n");
  asm volatile ("vsetvl zero, %0, %1"
  :
  : "r"(19), "r"(0x98));
  asm volatile ("csrr %0, vtype"
  : "=r"(vtype));
  printVtypeInfo(vtype);
  asm volatile ("vsetvl zero, %0, %1"
  :
  : "r"(19), "r"(0x0100000000000000L));
  asm volatile ("csrr %0, vtype"
  : "=r"(vtype));
  printVtypeInfo(vtype);
  asm volatile ("vsetvl zero, %0, %1"
  :
  : "r"(19), "r"(0x5));
  asm volatile ("csrr %0, vtype"
  : "=r"(vtype));
  printVtypeInfo(vtype);
  return 0;
}