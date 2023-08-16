// #define X86

#ifdef X86
#include <stdio.h>
#else
#include "util.h"
#endif

#define DATA_SIZE 32

int array[32] = {14, 7, 4, 11, 28, 8, 20, 13, 2, 21, 3, 6, 22, 12, 19, 29, 16, 31, 10, 5, 15, 17, 9, 0, 30, 18, 26, 27, 1, 24, 25, 23};

void quicksort(int* x, int left, int right) {
  if(left >= right) return;
  int i, j, pivot, temp;
  i = left+1; j = right; pivot = left;
  while(1) {
    while(x[i] < x[pivot]) i++;
    while(x[j] > x[pivot]) j--;
    if(i >= j) break;
    // x[i] >= x[pivot] && x[j] <= x[pivot]
    temp = x[i];
    x[i] = x[j];
    x[j] = temp;
    i++; j--;
  }
  // jとpivotを交換
  temp = x[j];
  x[j] = x[pivot];
  x[pivot] = temp;
  quicksort(x, left, j-1);
  quicksort(x, j+1, right);
}

extern void printstr(char* str);
extern void int32ToHex(int num, char* str);
extern void int64ToHex(long num, char* str);

int main(int argc, char** argv) {
  asm volatile ("csrw minstret, x0; csrw mcycle, x0;");
  quicksort(array, 0, DATA_SIZE-1);
  unsigned long instret, cycle;
  asm volatile("rdinstret %0":"=r" (instret));
  asm volatile("rdcycle %0":"=r" (cycle));
  char string[19];
  printstr("cycle: ");
  int64ToHex(cycle, string);
  printstr(string);
  printstr("\ninstret: ");
  int64ToHex(instret, string);
  printstr(string);

  printstr("\nindex ");
  int32ToHex(0, string);
  printstr(string);
  printstr(": ");
  int32ToHex(array[0], string);
  printstr(string);

  int i;
  _Bool ans_correct = 1;
  for(i=1; i<DATA_SIZE; i++) {
    #ifdef X86
    printf("%d: %d\n", i, array[i]);
    #endif
    printstr("\nindex ");
    int32ToHex(i, string);
    printstr(string);
    printstr(": ");
    int32ToHex(array[i], string);
    printstr(string);
    if(array[i-1] > array[i]) {
      ans_correct = 0;
    }
  }
  printstr("\n");

  return !ans_correct;
}