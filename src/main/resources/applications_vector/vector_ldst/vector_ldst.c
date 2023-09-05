#include "util.h"

char array0[32] = {5, 8, 19, 31, 14, 18, 21, 3, 23, 26, 28, 4, 27, 1, 30, 9, 24, 13, 25, 7, 17, 12, 6, 29, 15, 11, 0, 2, 10, 22, 20, 16};
char array1[32] = {0};

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void int32ToHex(int num, char* str);
extern void clearCounters();
extern void printCounters();

int main(int argc, char** argv) {
  int i;
  char string[19];
  long vl=1, avl = 32, vtype;
  char *vec0_ptr, *vec1_ptr;
  vec0_ptr = array0;
  vec1_ptr = array1;
  for(i=0; avl != 0; i++) {
    asm volatile ("vsetvli %0, %1, e8, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl));
    asm volatile ("vle8.v v0, (%0)"
    :
    : "r"(array0));
    asm volatile ("vse8.v v0, (%0)"
    :
    : "r"(array1));
    avl -= vl;
    vec0_ptr += vl;
    vec1_ptr += vl;
  }

  _Bool correct = 1;
  _Bool correct_iteration = 1;
  for(i=0; i<32; i++) {
    correct_iteration = (array0[i] == array1[i]);
    if(!correct_iteration) {
      printstr("ARRAY NOT MATCH IN INDEX: ");
      int32ToHex(i, string);
      printstr(string);
      printstr("\n");
      correct = 0;
    }
  }
  return !correct;
}