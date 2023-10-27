#include "util.h"

char charArray[48] = {0xA4, 0x3, 0x16, 0x5A, 0x84, 0xBD, 0x7A, 0xC4, 0x41, 0x55, 0x44, 0x6D, 0xE7, 0x3C, 0x0, 0x1B, 0xA7, 0x2A, 0x2C, 0x2B, 0xE4, 0xC7, 0x3, 0x82, 0xB8, 0xAB, 0xA1, 0x90, 0xF3, 0x1B, 0x81, 0x5, 0xE7, 0x6C, 0xA7, 0xD, 0x19, 0x2A, 0xC4, 0x31, 0x98, 0x5, 0xFA, 0xDE, 0x88, 0x1D, 0xA0, 0x95};
short shortArray[24] = {0xB30E, 0x4F13, 0xA981, 0x23C3, 0x74B2, 0x9C41, 0xF59F, 0x8D58, 0x247C, 0x9C11, 0xB567, 0x8834, 0x8BE, 0xD7E3, 0xFB20, 0xBE3E, 0x8A7D, 0xD51E, 0x5CC9, 0x209, 0xA43D, 0xE9E7, 0x1B5D, 0x1987};
int intArray[12] = {0x1E73CDC2, 0xF418BFAF, 0x3E1C9C92, 0x7C3CC907, 0x8F68900A, 0x1F3A91DD, 0x57EDB5D1, 0x5466B951, 0x57D90E87, 0xF2EBE29C, 0xA840A18A, 0x5C08FA94};
long longArray[6] = {0x87E339615D200DF6L, 0xCCBFC8FF1985682AL, 0x4390F0C7FECB0B3L, 0x4484C788D323DA5L, 0x37A8CBA68CE42158L, 0x108A55E8B37ABBFAL};
long targetArray[6] = {0};

extern void printstr(char* str);
extern void int64ToHex(long num, char* str);
extern void int32ToHex(int num, char* str);
extern void clearCounters();
extern void printCounters();

#define size_t long

void* vector_memcpy(void* dest, const void* src, size_t len) {
  size_t vl;
  void* dest_original = dest;
  while(len != 0) {
    asm volatile ("vsetvli %0, %1, e8, m1, ta, ma"
    : "=r"(vl)
    : "r"(len));
    asm volatile ("vle8.v v0, (%0)"
    :
    : "r"(src));
    asm volatile ("vse8.v v0, (%0)"
    :
    : "r"(dest));
    dest += vl;
    src += vl;
    len -= vl;
  }
  return dest_original;
}

_Bool verify_shortArray(const short* ptr0, const short* ptr1, size_t len) {
  int i;
  _Bool correct = 1;
  char string[19];
  for(i=0; i<len; i++) {
    if(ptr0[i] != ptr1[i]) {
      printstr("SHORT ARRAY NOT MATCH IN INDEX: ");
      int32ToHex(i, string);
      printstr(string);
      printstr("\n");
      correct = 0;
    }
  }
  return correct;
}

_Bool verify_intArray(const int* ptr0, const int* ptr1, size_t len) {
  int i;
  _Bool correct = 1;
  char string[19];
  for(i=0; i<len; i++) {
    if(ptr0[i] != ptr1[i]) {
      printstr("INT ARRAY NOT MATCH IN INDEX: ");
      int32ToHex(i, string);
      printstr(string);
      printstr("\n");
      correct = 0;
    }
  }
  return correct;
}

_Bool verify_longArray(const long* ptr0, const long* ptr1, size_t len) {
  int i;
  _Bool correct = 1;
  char string[19];
  for(i=0; i<len; i++) {
    if(ptr0[i] != ptr1[i]) {
      printstr("LONG ARRAY NOT MATCH IN INDEX: ");
      int32ToHex(i, string);
      printstr(string);
      printstr("\n");
      correct = 0;
    }
  }
  return correct;
}

int main(int argc, char** argv) {
  int i;
  char string[19];
  vector_memcpy(targetArray, charArray, 48);
  _Bool correct = 1;
  printstr("Verify array of char memcpy:\n");
  for(i=0; i<48; i++) {
    if(charArray[i] != ((char*)targetArray)[i]) {
      printstr("CHAR ARRAY NOT MATCH IN INDEX: ");
      int32ToHex(i, string);
      printstr(string);
      printstr("\n");
      correct = 0;
    }
  }

  vector_memcpy(targetArray, shortArray, 48);
  printstr("Verify array of short memcpy:\n");
  correct = correct && verify_shortArray(shortArray, (short*)targetArray, 24);

  vector_memcpy(targetArray, intArray, 48);
  printstr("Verify array of int memcpy:\n");
  correct = correct && verify_intArray(intArray, (int*)targetArray, 12);

  vector_memcpy(targetArray, longArray, 48);
  printstr("Verify array of long memcpy:\n");
  correct = correct && verify_longArray(longArray, (long*)targetArray, 6);

  printstr("Verify short memcpy with e16:\n");
  size_t vl, len = 24;
  void* dest = targetArray;
  void* src = shortArray;
  while(len != 0) {
    asm volatile ("vsetvli %0, %1, e16, m1, ta, ma"
    : "=r"(vl)
    : "r"(len));
    asm volatile ("vle16.v v0, (%0)"
    :
    : "r"(src));
    asm volatile ("vse16.v v0, (%0)"
    :
    : "r"(dest));
    dest += (vl << 1);
    src += (vl << 1);
    len -= vl;
  }
  correct = correct && verify_shortArray(shortArray, (short*)targetArray, 24);

  printstr("Verify int memcpy with e32:\n");
  len = 12;
  dest = targetArray;
  src = intArray;
  while(len != 0) {
    asm volatile ("vsetvli %0, %1, e32, m1, ta, ma"
    : "=r"(vl)
    : "r"(len));
    asm volatile ("vle32.v v0, (%0)"
    :
    : "r"(src));
    asm volatile ("vse32.v v0, (%0)"
    :
    : "r"(dest));
    dest += (vl << 2);
    src += (vl << 2);
    len -= vl;
  }
  correct = correct && verify_intArray(intArray, (int*)targetArray, 12);

  printstr("Verify long memcpy with e64:\n");
  len = 6;
  dest = targetArray;
  src = longArray;
  while(len != 0) {
    asm volatile ("vsetvli %0, %1, e64, m1, ta, ma"
    : "=r"(vl)
    : "r"(len));
    asm volatile ("vle64.v v0, (%0)"
    :
    : "r"(src));
    asm volatile ("vse64.v v0, (%0)"
    :
    : "r"(dest));
    dest += (vl << 3);
    src += (vl << 3);
    len -= vl;
  }
  correct = correct && verify_longArray(longArray, (long*)targetArray, 6);

  return !correct;
}
