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
extern void clearCounters();
extern void printCounters();

int main( int argc, char* argv[] )
{
  int results_data[DATA_SIZE];
  char string[19];
  unsigned long instret, cycle, mhpmcounter3;

  // Do the filter
  // setStats(1);
  asm volatile ("csrw minstret, x0");
  asm volatile ("csrw mcycle, x0");
  asm volatile ("csrw mhpmcounter3, x0");
  median( DATA_SIZE, input_data, results_data );
  asm volatile ("rdinstret %0":"=r"(instret));
  asm volatile ("rdcycle %0":"=r"(cycle));
  asm volatile ("csrr %0, mhpmcounter3":"=r"(mhpmcounter3));

  printstr("CYCLE: ");
  int64ToHex(cycle, string);
  printstr(string);
  printstr("\nINSTRET: ");
  int64ToHex(instret, string);
  printstr(string);
  printstr("\nMHPMCOUNTER3: ");
  int64ToHex(mhpmcounter3, string);
  printstr(string);
  printstr("\n");

  // setStats(0);

  // Check the results
  return verify( DATA_SIZE, results_data, verify_data );
}
