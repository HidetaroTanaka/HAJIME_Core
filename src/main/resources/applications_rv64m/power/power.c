#include "util.h"

long array0[16] = {-16, -99, -108, -110, 56, 89, 89, -117, -14, -48, -20, 113, -44, 42, -50, 69};
unsigned long array1[16] = {12, 8, 5, 7, 0, 15, 8, 9, 11, 9, 13, 2, 11, 7, 8, 9};
long ans[16] = {0x1000000000000L, 0x20c850694c2aa1L, 0xfffffffc94365400L, 0xffff4ec3e4f2dc80L, 0x1L, 0xdb182554b1fc8769L, 0xdfc4e816401c1L, 0xc6fc07aa270b38abL, 0xfffffc51231b4800L, 0xfffb31d000000000L, 0xfedcf631ac000000L, 0x31e1L, 0xef648659ab400000L, 0x35ad370e80L, 0x2386f26fc100L, 0x7df37c6dfc47a5L};

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void clearCounters();
extern void printCounters();

/// @brief calculates a^b
/// @param a 整数
/// @param b 非負整数
/// @return
long power(long a, unsigned long b) {
  if(b==0) {
    return 1;
  } else {
    long c = power(a, b>>1);
    c = c*c;
    if(b & 0x1 == 1) {
      return c * a;
    } else {
      return c;
    }
  }
}

int main(int argc, char** argv) {
  clearCounters();
  int i;
  long answer;
  char hexString[19];
  for(i=0; i<16; i++) {
    array0[i] = power(array0[i], array1[i]);
  }
  printCounters();
  _Bool correct = 1;
  for(i=0; i<16; i++) {
    answer = array0[i];
    int64ToHex(answer, hexString);
    printstr(hexString);
    printstr("\n");
    correct = correct && (answer == ans[i]);
  }
  return !correct;
}