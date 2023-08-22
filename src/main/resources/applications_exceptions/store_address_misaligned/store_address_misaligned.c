#include "util.h"

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
  if(cause == 6) {
    printstr("SUCCESSFULLY HANDLED STORE ADDRESS MISALIGNED EXCEPTION AT PC:");
    printstr(epchex);
    printstr("\n");
    return epc+4;
  } else {
    printstr("COULDN'T HANDLE EXCEPTION\n");
    exit(-1);
  }
}

int main(int argc, char** argv) {
  // any iteration should not raise load address misaligned exception
  int i;
  char* ptr_char;
  char string[19];
  printstr("START OF STORE ADDRESS MISALIGNED TEST FOR BYTE:\n");
  for(i=0; i<8; i++) {
    ptr_char = (char*)array+i;
    printstr("ITERATION ");
    int32ToHex(i, string);
    printstr(string);
    printstr("\n");
    printstr("ADDRESS: ");
    int64ToHex((long)ptr_char, string);
    printstr(string);
    printstr("\n");
    asm volatile("sb zero, 0(%0)"
    :
    : "r"(ptr_char));
    // make sure program returns here from exception handler
    asm volatile("nop");
    if(handled_exception) {
      printstr("EXCEPTION RAISED\n");
    }
    handled_exception = 0;
  }
  printstr("\n");
  // only the last asm should raise load address misaligned exception
  short* ptr_short;
  printstr("START OF STORE ADDRESS MISALIGNED TEST FOR HALFWORD:\n");
  for(i=0; i<8; i++) {
    ptr_short = (short*)((char*)array+i);
    printstr("ITERATION ");
    int32ToHex(i, string);
    printstr(string);
    printstr("\n");
    printstr("ADDRESS: ");
    int64ToHex((long)ptr_short, string);
    printstr(string);
    printstr("\n");
    asm volatile("sh zero, 0(%0)"
    :
    : "r"(ptr_short));
    // make sure program returns here from exception handler
    asm volatile("nop");
    if(handled_exception) {
      printstr("EXCEPTION RAISED\n");
    }
    handled_exception = 0;
  }
  printstr("\n");
  // only the last three asm should raise load address misaligned exception
  int* ptr_int;
  printstr("START OF STORE ADDRESS MISALIGNED TEST FOR WORD:\n");
  for(i=0; i<8; i++) {
    ptr_int = (int*)((char*)array+i);
    printstr("ITERATION ");
    int32ToHex(i, string);
    printstr(string);
    printstr("\n");
    printstr("ADDRESS: ");
    int64ToHex((long)ptr_int, string);
    printstr(string);
    printstr("\n");
    asm volatile("sw zero, 0(%0)"
    :
    : "r"(ptr_int));
    // make sure program returns here from exception handler
    asm volatile("nop");
    if(handled_exception) {
      printstr("EXCEPTION RAISED\n");
    }
    handled_exception = 0;
  }
  printstr("\n");
  // all iterations except the first one should raise load address misaligned exception
  long* ptr_long;
  printstr("START OF STORE ADDRESS MISALIGNED TEST FOR DOUBLEWORD:\n");
  for(i=0; i<8; i++) {
    ptr_long = (long*)((char*)array+i);
    printstr("ITERATION ");
    int32ToHex(i, string);
    printstr(string);
    printstr("\n");
    printstr("ADDRESS: ");
    int64ToHex((long)ptr_long, string);
    printstr(string);
    printstr("\n");
    asm volatile("sd zero, 0(%0)"
    :
    : "r"(ptr_long));
    // make sure program returns here from exception handler
    asm volatile("nop");
    if(handled_exception) {
      printstr("EXCEPTION RAISED\n");
    }
    handled_exception = 0;
  }
  printstr("\n");
  return 0;
}