#include "util.h"

#define size_t long
#define N 12

const int array1[12][12] = {{-100, 0, 81, 84, 46, -95, -39, 12, 68, 89, -70, 94},
                           {-40, 50, 110, 91, 75, 51, 85, 23, -110, -103, -104, 46},
                           {-22, 104, 43, 67, -4, 57, -83, -38, -77, -80, -65, 34},
                           {-42, -71, -128, 16, 1, 23, -13, -42, -31, -104, -116, 104},
                           {126, -41, -16, -64, -20, -80, -10, 25, 35, 6, -89, -38},
                           {80, -34, -84, 106, 114, 93, 119, -112, 22, 76, 109, -51},
                           {84, -101, -10, -51, -54, 87, -22, -121, -116, 92, -74, 33},
                           {23, -51, 97, -101, 119, -86, -19, 112, 70, -51, 24, 61},
                           {31, 41, -125, -9, 79, -42, -110, -70, -35, -12, -23, -32},
                           {-106, -104, 81, 51, 4, 74, 105, -71, -12, -114, 104, -40},
                           {-72, -41, -92, -41, 84, -38, -60, 17, 125, -107, -78, -103},
                           {-46, -53, 59, -85, 11, -38, -14, 93, 38, 20, -46, 10}};
const int array2[12][12] = {{-47, 82, -37, -16, -35, 5, -124, 95, -54, 89, -85, 32},
                           {87, -82, -90, -5, -28, -40, 54, 4, -14, 68, 114, -2},
                           {55, -37, -93, -43, -104, 29, 21, 22, 27, 62, 34, 117},
                           {-58, 113, 43, 24, -37, 5, 43, 109, -124, -125, -40, 86},
                           {98, -62, 41, -79, -9, 103, -45, 102, 73, -106, 100, 49},
                           {-58, 25, -50, 62, 125, 115, -80, 67, -49, 2, -30, -70},
                           {39, 123, -30, -123, -8, 26, 67, -30, -68, 32, 15, 104},
                           {-91, 60, 21, -98, 115, -60, 40, -43, 95, -52, 67, -56},
                           {-8, -82, 28, -93, -68, -54, -9, -48, -11, 93, 16, 64},
                           {17, -106, -70, -90, 35, 78, 97, -127, -78, -28, 125, 69},
                           {44, -78, 26, 52, 97, -10, 62, -78, -110, -8, -126, 115},
                           {-124, 18, 54, 41, -8, 26, -11, -128, 20, -100, -33, 1}};
int resultArray[12][12] = {0};
int answerArray[12][12] = {0};

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

/// <summary>
/// vec1を横ベクトル，vec2を縦ベクトルとした内積
/// </summary>
int innerProd(const int* vec1, const int* vec2, int n) {
  int vl, avl = n;
  int sum = 0;
  while(avl != 0) {
    asm volatile ("vsetvli %0, %1, e32, m1, ta, ma"
    : "=r"(vl)
    : "r"(avl));
    asm volatile ("vmv.s.x v3, %0"
    :
    : "r"(sum));
    asm volatile ("vle32.v v1, (%0)"
    :
    : "r"(vec1));
    asm volatile ("vlse32.v v2, (%0), %1"
    :
    : "r"(vec2), "r"(n*sizeof(int)));
    asm volatile ("vmul.vv v1, v1, v2");
    asm volatile ("vredsum.vs v1, v1, v3");
    asm volatile ("vmv.x.s %0, v1"
    : "=r"(sum));
    vec1 += vl;
    vec2 += vl * n;
    avl -= vl;
  }
  return sum;
}

int main(int argc, char** argv) {
  int i, j, k;

  clrCounters();
  for(i=0; i<12; i++) {
    for(j=0; j<12; j++) {
      // resultArray[i][j] = array1[i][*] * array2[*][j]
      resultArray[i][j] = innerProd(&(array1[i][0]), &(array2[0][j]), 12);
    }
  }
  showCounters();

  clrCounters();
  for(i=0; i<12; i++) {
    for(j=0; j<12; j++) {
      int sum = 0;
      for(k=0; k<12; k++) {
        sum += array1[i][k] * array2[k][j];
      }
      answerArray[i][j] = sum;
    }
  }
  showCounters();
  _Bool correct = 1;
  for(i=0; i<12; i++) {
    for(j=0; j<12; j++) {
      correct = correct && (resultArray[i][j] == answerArray[i][j]);
    }
  }
  return !correct;
}