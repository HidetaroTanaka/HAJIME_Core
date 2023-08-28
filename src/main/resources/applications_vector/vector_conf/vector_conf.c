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
  long vl=1, avl = 32, vtype;
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
  return 0;
}