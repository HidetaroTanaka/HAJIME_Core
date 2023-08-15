// See LICENSE for license details.

//**************************************************************************
// Median filter bencmark
//--------------------------------------------------------------------------
//
// This benchmark performs a 1D three element median filter. The
// input data (and reference data) should be generated using the
// median_gendata.pl perl script and dumped to a file named
// dataset1.h.

#include "util.h"

#include "median.h"

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

  // Do the filter
  // setStats(1);
  asm volatile ("csrw minstret, x0; csrw mcycle, x0;");
  median( DATA_SIZE, input_data, results_data );
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
  return verify( DATA_SIZE, results_data, verify_data );
}
