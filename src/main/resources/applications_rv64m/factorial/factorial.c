#include "util.h"

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void clearCounters();
extern void printCounters();

unsigned long factorial(unsigned long num) {
  // we define 0! to be 1
  if(num <= 1) {
    return 1;
  } else {
    return num * factorial(num-1);
  }
}

int main(int argc, char** argv) {
  clearCounters();
  unsigned long ans = factorial((unsigned long)12);
  printCounters();
  char hexString[19];
  int64ToHex(ans, hexString);
  printstr(hexString);
  return (ans != (unsigned long)0x1C8CFC00);
}