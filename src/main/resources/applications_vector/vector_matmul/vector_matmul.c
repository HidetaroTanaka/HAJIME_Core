#include "util.h"

#define size_t long
#define N 32

const signed char array1[32][32] = {{-2, 0, -2, -2, -6, -1, -8, 4, -5, 6, -8, 2, 0, -2, 0, 0, -8, -5, 5, 5, 2, 0, -4, -5, 4, -7, -7, 0, 1, 1, -6, -2},
                                    {-7, -7, 2, -3, 3, -4, -5, -6, -2, -7, 0, -4, -2, -3, -2, 5, -2, 0, 4, 4, -7, -7, -1, -4, -5, -5, -8, -2, -1, -7, 1, -1},
                                    {4, 2, 6, -7, -4, -5, 0, 2, 0, 3, 3, 1, -5, 1, -7, -2, -7, -6, -7, -2, -1, 6, -8, -5, 6, 1, 4, 0, 1, 0, 5, 5},
                                    {6, -8, 1, -6, 4, 4, -6, 3, -6, -5, -1, -7, -1, -7, 0, -7, 5, 0, -4, -6, -6, -1, -2, -5, 5, 4, -7, 4, 0, 6, 3, 4},
                                    {3, -8, -7, 5, 2, -8, 2, 3, 3, -2, 6, 6, -7, 6, 6, 1, 3, -7, -3, 2, 1, -7, -1, -1, -2, -8, 2, -3, -1, -5, 4, -4},
                                    {-1, 2, -6, 6, -4, 1, 3, -2, 5, 5, -4, 3, -1, -6, -6, -8, 1, 2, 1, -1, -1, 1, 1, 3, 6, 3, 0, -5, -7, -2, 1, -2},
                                    {-4, -3, -8, -8, -8, 4, 5, -8, -4, -7, 3, -4, -3, 5, -4, 6, 2, 3, -3, -3, -8, -7, -5, 0, 4, 6, 5, 3, -7, -3, 4, -8},
                                    {3, 4, -5, 3, 0, -4, -7, -3, -1, 0, -3, 1, -3, 2, 4, 2, -4, -7, -7, 4, 1, -5, 4, -6, 3, 3, 6, 0, -1, -4, -7, -7},
                                    {3, -4, 6, 5, -8, -8, 5, -3, -3, 4, -3, 3, 2, -2, -5, -6, -5, -3, -2, 3, 0, -6, -8, -8, -4, -3, 2, -3, 6, -7, -3, -3},
                                    {2, 5, 1, -5, -8, 5, 3, 0, 2, -2, -5, 0, -2, -1, -5, -7, 3, -3, -1, -1, 5, 2, -2, 3, 5, 3, 3, 1, -1, -5, 6, -7},
                                    {4, 2, 6, 3, 0, -5, -1, -6, -1, -3, -1, -8, -4, -8, -7, -1, -6, 6, 4, -4, -7, 4, 6, 2, -6, -5, 2, -7, -4, -4, 0, 5},
                                    {-4, 4, 0, 0, 5, -3, 4, 0, -2, 6, -6, -4, 0, -5, -6, -7, -1, -6, -3, -3, 1, 1, 3, 4, -4, -5, 6, -5, -2, 5, 5, 0},
                                    {-8, -3, -8, 6, -1, 3, -5, 5, -5, -4, -6, -8, -7, -7, 3, -6, -4, -7, 1, 2, -8, -5, 2, -1, 1, -1, -6, 5, -6, 5, 5, -6},
                                    {5, -3, -6, -6, -3, -1, -8, -4, 2, -3, -5, 5, -5, 5, -4, -8, -7, 1, 0, -3, 1, 6, 2, -1, 1, 5, 2, 4, 4, -2, 1, 3},
                                    {-2, 2, -1, -5, -5, 4, -7, 6, -4, 1, 1, -8, 2, 6, -7, -1, 3, -5, -4, 5, -7, 1, -4, 1, -3, 3, -4, 1, 5, 6, -3, 4},
                                    {-6, 0, 3, 2, 4, 6, -8, 4, -1, -7, -4, -3, -4, -2, 3, 1, 0, -1, 2, -8, -5, 3, -7, 6, 5, -7, 2, -1, -8, -1, -4, -5},
                                    {6, -5, 0, -3, 2, 3, -1, -5, -1, 0, -3, 3, -7, -7, -7, -5, 0, -8, -2, 1, -3, 6, -2, -2, -1, -8, -6, 1, 1, 5, 5, 2},
                                    {0, -3, -4, -6, -1, 6, -8, 1, 3, -8, -7, -7, -1, -5, -5, -4, 2, -4, 3, -4, -5, 1, -1, 5, -2, 0, -8, 3, -2, -8, -5, 1},
                                    {-6, -1, 1, 5, 6, -1, -3, -7, 6, 1, 6, 6, -8, -8, -6, -2, 1, -2, 3, 1, -1, 6, 3, -8, 2, -2, 1, 6, -1, 0, -8, -4},
                                    {-5, 5, -1, 4, 4, 2, -8, 6, 0, -8, -3, -5, -1, -5, -7, 5, 4, -4, 5, 5, -3, -6, 1, 3, -4, -5, -8, 6, 3, 2, -4, -6},
                                    {3, 2, 0, -8, 2, 1, -4, 6, 6, 3, 1, -1, 6, -3, -2, 4, -6, -7, -8, -8, -6, -4, -8, 4, 0, 1, 0, -6, 2, -3, 0, -7},
                                    {-6, 3, -4, 1, 1, 0, -4, 4, 6, -8, 4, -8, -2, -8, -6, 0, -6, 3, -8, 6, 1, 3, 0, -4, -4, -6, 2, 0, 4, -7, 6, -1},
                                    {-7, 1, -4, -8, -7, 5, -5, -3, -7, 3, -7, 0, 2, 6, -6, -5, -4, -6, 0, 3, 2, -5, -5, 4, 5, 1, -2, 6, 1, -7, 2, -2},
                                    {4, -5, -5, -1, 3, -4, 6, -6, 6, -4, -1, -3, -3, 5, -8, 6, -4, 3, -3, 3, -8, 4, 4, 4, 0, -4, 3, 2, -6, -1, 5, 4},
                                    {4, -3, -7, 5, 3, 2, 4, 5, 0, 3, -5, -2, 5, 1, -6, -4, 3, 6, -2, 1, 6, 4, -7, -6, -2, -7, 0, -7, -3, -4, -4, -4},
                                    {1, -8, -2, 1, -7, -2, -6, 1, 4, 1, 3, 2, -7, 0, 2, -1, 3, -8, 1, 5, 1, 0, -6, 6, -7, -2, -8, -3, 3, -4, 0, 2},
                                    {1, -3, -3, 2, 4, -6, 1, -4, -7, -7, 0, -1, 4, -7, 0, -3, 3, -2, -5, -8, 5, 3, -4, 5, 3, -5, 4, 0, -3, -5, 1, -3},
                                    {6, 4, -8, -5, 1, -5, -6, 3, -7, -2, 0, 1, -4, 3, -8, 5, 2, -2, 0, 1, -2, -5, 6, -2, -5, 1, -4, -1, 4, 1, -4, -7},
                                    {4, -8, -2, 0, -5, -7, 6, -2, 0, -6, 2, 4, -7, -2, 0, 4, 1, -1, 5, 0, 3, -7, 0, 4, -7, 4, 0, -7, 0, 3, -7, -3},
                                    {3, 1, 3, 3, 3, 6, -5, -7, -7, -6, -8, 5, -1, 1, 3, 5, 1, 2, 3, -2, -5, 1, -1, -1, 4, -4, 6, -4, 5, -3, -2, 5},
                                    {-2, -5, 0, 0, -7, -1, -1, -1, 1, -4, 0, -5, 5, -3, -6, -2, -1, 5, 0, -8, 1, -8, -2, -5, -7, -6, 2, -8, -2, 4, -1, 1},
                                    {-8, 0, 3, -5, -1, -5, 1, -4, 6, -2, -3, 3, -7, -3, 0, -6, 3, -5, 2, -3, -7, -2, -2, 6, -8, 1, -8, -4, 5, 2, 3, 4}};
const signed char array2[32][32] = {{-1, -3, 4, -3, 1, 5, -2, -4, -6, 6, 4, -3, -6, -1, 5, 3, 0, 6, -8, 2, -4, -2, -3, -6, -8, 4, 3, -5, 2, -6, 6, 2},
                                    {-4, -3, 2, -1, 2, 3, -2, 0, -2, 1, 4, -2, 3, -6, -7, 5, -8, 6, 1, 6, -4, -2, -3, 3, 1, 1, 5, -1, 4, -4, 1, -5},
                                    {-1, -4, 5, 5, 5, 0, -2, 4, 6, 4, -4, -4, 2, -7, 6, -6, -8, -3, -5, -7, -3, 6, -3, 5, 5, 1, 3, 4, -4, 5, 5, -3},
                                    {5, -7, 5, 6, 5, 4, 2, -5, 0, 0, 3, -8, -4, -6, -7, -5, -3, 3, 4, 2, -8, -2, -5, 5, 2, -7, -8, -1, -2, 6, -6, -5},
                                    {6, -6, -3, -6, 6, 1, 4, 4, -4, 5, -5, -8, 0, -6, -4, 1, 0, -4, -6, 3, -4, -3, 4, 3, 2, -8, -6, 0, -8, -8, 2, 2},
                                    {3, -8, 2, -5, 1, -4, -1, -5, -2, -7, 6, 5, 2, 3, 5, 2, 1, -1, -7, 2, 4, 3, -6, -2, 5, -3, 5, 4, -4, 1, -5, 1},
                                    {0, 3, -1, -3, 0, 2, -5, -7, 2, 6, -6, -5, 2, -1, 6, -1, 5, 4, 0, 4, -6, -8, -5, 3, -4, 1, -1, 3, 2, 3, -6, -5},
                                    {-3, -1, -7, -1, -8, 3, -8, -6, -1, -1, 4, 1, 3, -7, -4, -5, -4, -1, 3, -7, 4, 2, 0, -4, 1, -5, -1, 6, -6, 4, 2, -4},
                                    {6, 1, 5, -8, -2, -6, -2, 1, 2, 3, -3, 4, -1, -8, 5, -2, -5, -2, -4, -5, -1, 3, -3, -1, -3, 1, -3, 2, -4, -5, 1, -5},
                                    {0, 5, -1, -4, -3, -5, 3, 2, -5, 4, -2, 5, -7, 0, -1, 4, -4, -5, 3, 2, 6, -1, -5, 6, 4, -8, -8, 1, -2, -3, 1, -7},
                                    {1, -8, 5, 5, 0, 4, 4, -2, 5, -4, -5, -5, -7, -4, 0, -2, 0, 5, -4, -6, -2, 1, 5, 2, 2, 6, 5, 3, 5, -2, 4, 6},
                                    {0, 6, 1, 3, -6, -7, 4, 6, -7, -6, 6, 5, -8, 3, 6, -4, 3, 2, 4, -5, -4, -4, -8, 3, -4, 4, -6, -4, -2, 4, -3, 6},
                                    {5, -8, -3, -6, -8, 0, -6, 0, -4, 3, 5, -5, 4, -8, -3, 6, 6, -6, 2, 3, 3, -4, 0, -6, -3, -3, 6, 4, -4, 0, -5, 1},
                                    {-8, 5, -1, -1, 1, -8, -6, -3, -5, 0, -3, -6, -4, 2, -7, -4, 5, 3, 2, 6, -4, -5, -3, -6, -5, 5, 2, 2, 5, -6, 4, -6},
                                    {3, -5, -3, 3, 4, 4, -2, 6, 4, -4, 1, -6, -1, -8, -8, -7, -5, -3, -3, -8, 1, -3, -1, -4, -6, 0, 3, -4, 6, 6, 2, -4},
                                    {-6, 0, -8, -3, 5, 2, 1, -2, -8, 6, 1, -7, 0, 3, 1, 0, -1, -8, 3, 5, 3, -2, 1, 1, 0, -5, -2, 5, -6, -4, 2, 4},
                                    {-1, 4, 5, -2, -8, -8, 1, -3, 1, 5, -6, 2, 1, 6, 1, -6, -1, 5, -8, 3, 5, -2, -3, -8, 0, -8, -3, -1, 4, -6, 2, 4},
                                    {3, -7, -2, -3, 4, 5, 6, 6, 3, 0, -5, -7, -6, -8, -4, 6, -6, 2, 5, -6, -6, -7, 4, 3, 1, -4, 1, 6, -5, -2, -2, 2},
                                    {1, 3, -2, -4, 2, 6, -7, 5, 1, -2, -8, -6, -8, -5, -1, 3, -8, -5, 3, -3, 2, -2, -7, 5, -1, 5, -3, 0, -4, 2, -5, 4},
                                    {2, 1, 5, -6, 5, -6, 6, 2, 4, -8, 6, -6, 1, -1, -5, 1, -5, -1, 6, 2, -4, 5, 2, -7, 1, 1, 6, -1, 1, -4, 1, 3},
                                    {1, -5, 6, -2, -1, -7, -8, -6, -7, 6, -5, -6, 0, -5, 5, -5, -6, -5, 2, -2, 2, 2, 2, -8, -4, -4, 1, 4, -1, 5, 5, 2},
                                    {3, 1, 3, 6, 6, -5, 1, 4, 6, -6, -8, 3, -6, 4, 1, 2, -5, -3, 5, -6, -2, 4, -8, 0, 5, 4, -4, -6, 0, -4, -6, 6},
                                    {4, 4, -8, -1, -4, 5, -6, 0, 1, -1, -3, 4, -5, -3, 6, -8, 4, 0, -8, 5, -7, 1, -8, 2, -2, 4, -8, -5, -1, 3, 1, 5},
                                    {0, -7, -4, -1, 3, -7, 3, -4, -1, -1, -8, 6, 1, -4, -7, -4, -3, -6, 3, -1, -4, 3, 6, 2, 5, 2, 5, 5, -4, -2, -4, -7},
                                    {6, 5, -4, -3, -4, 4, 1, -2, -6, 3, 2, -6, 3, 1, -1, 2, -7, 3, -8, -7, -2, -6, 0, -8, 5, 4, -6, -5, 3, -3, 6, -7},
                                    {-4, 5, 2, 1, -2, -5, 2, 5, 4, 6, 5, 3, 4, -8, 3, 0, 5, 2, -7, 0, -6, 6, 4, 5, 6, -5, 0, 5, 1, 1, -4, 0},
                                    {-2, -1, 3, 0, -4, -2, 2, 4, -4, -7, 1, -4, -3, -1, 5, -6, 0, -3, -7, 1, 2, 2, 6, -8, 0, 4, 0, -2, -5, -6, -4, -1},
                                    {2, -5, -3, -3, -2, -1, 3, -7, -5, 2, 1, 5, -8, 0, 4, -3, -2, -3, -5, -8, -8, -3, 4, -8, -1, 6, 2, -8, 3, 3, -1, -2},
                                    {-5, 2, -2, -7, -1, 0, -2, 0, 5, 5, -2, -4, 6, -8, -5, -5, -3, 0, 1, -3, 6, -1, -4, 6, 6, 1, -6, -7, 4, -1, -1, -2},
                                    {3, 1, 3, 4, 0, 3, 0, 0, -6, -7, -1, -1, 6, -1, -6, 1, -5, -6, 1, 2, -6, -7, 2, 1, 6, -7, -4, 3, 0, -3, -3, -1},
                                    {-8, -2, 6, -2, -8, -1, -3, 2, -4, -1, -6, -8, 5, -7, 1, -7, 6, -7, -7, -3, -6, 6, -2, -8, -4, -6, -1, -3, 2, -2, -6, 5},
                                    {-3, 1, 4, 6, -4, 1, -1, -1, -6, 6, -3, -4, 3, 2, -2, -3, -6, -2, 5, -2, 6, -5, 1, -7, -3, -2, -1, 1, 3, -3, 5, 6}};
signed char resultArray[32][32] = {{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
signed char answerArray[32][32] = {0};

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);

void clrCounters(void) {
  asm volatile ("csrw minstret, x0; csrw mcycle, x0; csrw mhpmcounter3, x0");
}

void showCounters(void) {
  char string[19];
  unsigned long instret, cycle, mhpmcounter3;
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
}

/// @brief 8bit幅の32*32行列乗算
/// @param array1
/// @param array2
/// @param resultArray
void _e8_32x32_matmul(const signed char array1[32][32], const signed char array2[32][32], signed char resultArray[32][32]) {
  asm volatile ("vsetvli zero, %0, e8, m1, ta, ma"::"r"(32));
  int i=0, j=0;
  for(i=0; i<32; i++) {
    // array1の横ベクトルをロード
    // v1 = array1[i][*]
    asm volatile ("vle8.v v1, (%0)"::"r"(&(array1[i][0])));
    asm volatile ("vmv.s.x v4, zero");
    for(j=0; j<32; j++) {
      // array2の縦ベクトルをロード
      // v2 = array2[*][j]
      asm volatile ("vlse8.v v2, (%0), %1"::"r"(&(array2[0][j])), "r"(32));
      asm volatile ("vmul.vv v3, v1, v2");
      asm volatile ("vredsum.vs v3, v3, v4");
      asm volatile ("vmv.x.s %0, v3":"=r"(resultArray[i][j]));
      // resultArray[i*32+j] = tmp;
    }
  }
}

int main(int argc, char** argv) {
  int i, j, k;

  clrCounters();
  _e8_32x32_matmul(array1, array2, resultArray);
  showCounters();

  clrCounters();
  for(i=0; i<32; i++) {
    for(j=0; j<32; j++) {
      signed char sum = 0;
      for(k=0; k<32; k++) {
        signed char temp;
        asm volatile ("mulw %0, %1, %2":"=r"(temp):"r"(array1[i][k]), "r"(array2[k][j]));
        asm volatile ("addw %0, %1, %2":"=r"(sum):"r"(sum), "r"(temp));
        // sum += array1[i][k] * array2[k][j];
      }
      answerArray[i][j] = sum;
    }
  }
  showCounters();
  _Bool correct = 1;
  for(i=0; i<32; i++) {
    for(j=0; j<32; j++) {
      correct = correct && (resultArray[i][j] == answerArray[i][j]);
    }
  }
  return !correct;
}