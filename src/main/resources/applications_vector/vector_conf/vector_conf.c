#include "util.h"

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void int32ToHex(int num, char* str);
extern void clearCounters();
extern void printCounters();

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
    printstr("vtype: ");
    int64ToHex(vtype, string);
    printstr(string);
    printstr("\n");
    avl -= vl;
  }
  return 0;
}