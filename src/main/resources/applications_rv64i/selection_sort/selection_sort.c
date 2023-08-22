// #define X86

#ifdef X86
#include <stdio.h>
#else
#include "util.h"
#endif

#define DATA_SIZE 32

int array[32] = {14, 7, 4, 11, 28, 8, 20, 13, 2, 21, 3, 6, 22, 12, 19, 29, 16, 31, 10, 5, 15, 17, 9, 0, 30, 18, 26, 27, 1, 24, 25, 23};

void selection_sort(int* array, int length) {
  // if length <= 1, return
  if(length <= 1) return;
  // find index of the smallest element
  int idx = 0, i;
  for(i=1; i<length; i++) {
    if(array[i] < array[idx]) {
      idx = i;
    }
  }
  // swap first element and index element
  int tmp = array[0];
  array[0] = array[idx];
  array[idx] = tmp;
  // call selection_sort recursively for array[1, length-1]
  selection_sort(array+1, length-1);
  return;
}

extern void printstr(char* str);
extern void int32ToHex(int num, char* str);
extern void int64ToHex(long num, char* str);
extern void clearCounters();
extern void printCounters();

int main(int argc, char** argv) {
  clearCounters();
  selection_sort(array, DATA_SIZE);
  unsigned long instret, cycle;
  printCounters();

  char string[19];
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