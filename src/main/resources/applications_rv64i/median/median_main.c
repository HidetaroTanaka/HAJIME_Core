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

  // Do the filter
  // setStats(1);
  clearCounters();
  median( DATA_SIZE, input_data, results_data );
  printCounters();
  // setStats(0);

  // Check the results
  return verify( DATA_SIZE, results_data, verify_data );
}
