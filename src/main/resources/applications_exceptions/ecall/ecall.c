#include "util.h"

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void clearCounters();
extern void printCounters();
extern void exit(int ret);

uintptr_t handle_trap(uintptr_t cause, uintptr_t epc, uintptr_t regs[32]) {
  char epchex[19];
  int64ToHex(epc, epchex);
  if(cause == 11) {
    printstr("SUCCESSFULLY HANDLED ECALL AT PC:");
    printstr(epchex);
    printstr("\n");
    return epc+4;
  } else {
    printstr("COULDN'T HANDLE EXCEPTION\n");
    exit(-1);
  }
}

int main(int argc, char** argv) {
  asm volatile ("ecall");
  printstr("SUCCESSFULLY RETURNED FROM ECALL\n");
  return 0;
}