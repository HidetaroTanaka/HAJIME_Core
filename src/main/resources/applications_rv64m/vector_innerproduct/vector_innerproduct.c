#include "util.h"

int array0[16] = {27, -82, -35, 61, -126, -47, -8, -104, -59, -79, 76, 17, 79, -86, -75, -55};
int array1[16] = {-94, -114, -108, 40, -120, 24, 116, -86, 8, 77, 74, 62, 98, -122, -78, 38};

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void clearCounters();
extern void printCounters();

int main(int argc, char** argv) {
  clearCounters();
  int i, ans=0;
  for(i=0; i<16; i++) {
    ans += array0[i] * array1[i];
  }
  printCounters();
  char hexString[19];
  int64ToHex(ans, hexString);
  printstr(hexString);
  printstr("\n");
  return !(ans == 0xDF43);
}
