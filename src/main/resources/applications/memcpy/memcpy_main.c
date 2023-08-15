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

int main( int argc, char* argv[] )
{
  int results_data[DATA_SIZE];

  // Do the riscv-linux memcpy
  // setStats(1);
  asm volatile ("csrw minstret, x0; csrw mcycle, x0;");
  memcpy(results_data, input_data, sizeof(int) * DATA_SIZE); //, DATA_SIZE * sizeof(int));
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
  printstr("\n");
  // setStats(0);

  // Check the results
  return verify( DATA_SIZE, results_data, input_data );
}
