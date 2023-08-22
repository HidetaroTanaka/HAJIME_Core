#include "util.h"
#include "encoding.h"

unsigned long array[2] = {0, 1};
_Bool handled_exception = 0;

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void int32ToHex(int num, char* str);
extern void clearCounters();
extern void printCounters();
extern void exit(int ret);

uintptr_t handle_trap(uintptr_t cause, uintptr_t epc, uintptr_t regs[32]) {
  handled_exception = 1;
  char epchex[19];
  int64ToHex(epc, epchex);
  if(cause == 7) {
    printstr("SUCCESSFULLY HANDLED STORE ACCESS FAULT EXCEPTION AT PC:");
    printstr(epchex);
    printstr("\n");
    return epc+4;
  } else {
    printstr("COULDN'T HANDLE EXCEPTION");
    printstr("\n");
    exit(-1);
  }
}

int main(int argc, char** argv) {
  printstr("BYTE ACCESS FAULT TEST:\n");
  void* ptr = (void*)0x6000;
  printstr("ADDRESS 0x6000:\n");
  asm volatile("sb zero, 0(%0)"
  :
  : "r"(ptr));
  asm volatile("nop");
  if(handled_exception) {
    printstr("EXCEPTION RAISED\n");
    handled_exception = 0;
  } else {
    printstr("EXCEPTION NOT RAISED\n");
    return 1;
  }

  ptr = (void*)0x5FFF;
  printstr("ADDRESS 0x5FFF:\n");
  asm volatile("sb zero, 0(%0)"
  :
  : "r"(ptr));
  asm volatile("nop");
  if(handled_exception) {
    printstr("EXCEPTION RAISED\n");
    handled_exception = 0;
    return 1;
  } else {
    printstr("EXCEPTION NOT RAISED\n");
  }

  printstr("HALFWORD ACCESS FAULT TEST:\n");
  ptr = (void*)0x5FFF;
  printstr("ADDRESS 0x5FFF:\n");
  asm volatile("sh zero, 0(%0)"
  :
  : "r"(ptr));
  asm volatile("nop");
  if(handled_exception) {
    printstr("EXCEPTION RAISED\n");
    handled_exception = 0;
  } else {
    printstr("EXCEPTION NOT RAISED\n");
    return 1;
  }
  ptr = (void*)0x5FFE;
  printstr("ADDRESS 0x5FFE:\n");
  asm volatile("sh zero, 0(%0)"
  :
  : "r"(ptr));
  asm volatile("nop");
  if(handled_exception) {
    printstr("EXCEPTION RAISED\n");
    handled_exception = 0;
    return 1;
  } else {
    printstr("EXCEPTION NOT RAISED\n");
  }

  printstr("WORD ACCESS FAULT TEST:\n");
  ptr = (void*)0x5FFD;
  printstr("ADDRESS 0x5FFD:\n");
  asm volatile("sw zero, 0(%0)"
  :
  : "r"(ptr));
  asm volatile("nop");
  if(handled_exception) {
    printstr("EXCEPTION RAISED\n");
    handled_exception = 0;
  } else {
    printstr("EXCEPTION NOT RAISED\n");
    return 1;
  }
  ptr = (void*)0x5FFC;
  printstr("ADDRESS 0x5FFC:\n");
  asm volatile("sh zero, 0(%0)"
  :
  : "r"(ptr));
  asm volatile("nop");
  if(handled_exception) {
    printstr("EXCEPTION RAISED\n");
    handled_exception = 0;
    return 1;
  } else {
    printstr("EXCEPTION NOT RAISED\n");
  }

  printstr("DOUBLEWORD ACCESS FAULT TEST:\n");
  ptr = (void*)0x5FF9;
  printstr("ADDRESS 0x5FF9:\n");
  asm volatile("sd zero, 0(%0)"
  :
  : "r"(ptr));
  asm volatile("nop");
  if(handled_exception) {
    printstr("EXCEPTION RAISED\n");
    handled_exception = 0;
  } else {
    printstr("EXCEPTION NOT RAISED\n");
    return 1;
  }
  ptr = (void*)0x5FF8;
  printstr("ADDRESS 0x5FF0:\n");
  asm volatile("sd zero, 0(%0)"
  :
  : "r"(ptr));
  asm volatile("nop");
  if(handled_exception) {
    printstr("EXCEPTION RAISED\n");
    handled_exception = 0;
    return 1;
  } else {
    printstr("EXCEPTION NOT RAISED\n");
  }

  return 0;
}