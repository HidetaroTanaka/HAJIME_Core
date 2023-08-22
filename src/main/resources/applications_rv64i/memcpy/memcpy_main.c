// See LICENSE for license details.

//**************************************************************************
// Memcpy benchmark
//--------------------------------------------------------------------------
//
// This benchmark tests the memcpy implementation in syscalls.c.
// The input data (and reference data) should be generated using
// the memcpy_gendata.pl perl script and dumped to a file named
// dataset1.h.

#include <string.h>
#include "util.h"

//--------------------------------------------------------------------------
// Input/Reference Data

#include "dataset1.h"

//--------------------------------------------------------------------------
// Main

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void clearCounters();
extern void printCounters();

int main( int argc, char* argv[] )
{
  int results_data[DATA_SIZE];

  // Do the riscv-linux memcpy
  // setStats(1);
  clearCounters();
  memcpy(results_data, input_data, sizeof(int) * DATA_SIZE); //, DATA_SIZE * sizeof(int));
  unsigned long instret, cycle;
  asm volatile("rdinstret %0":"=r" (instret));
  asm volatile("rdcycle %0":"=r" (cycle));
  printCounters();
  // setStats(0);

  // Check the results
  return verify( DATA_SIZE, results_data, input_data );
}
