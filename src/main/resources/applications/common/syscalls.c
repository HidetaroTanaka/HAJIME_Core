// See LICENSE for license details.

#include <stdint.h>
#include <string.h>
#include <stdarg.h>
#include <stdio.h>
#include <limits.h>
#include <sys/signal.h>
#include "util.h"

#define SYS_write 64

#undef strcmp

#define TOHOST 0x10000000

#define PUTCHAR_TOHOST( ch ) do { *((volatile char*)TOHOST) = ch; } while(0);

// str.length == 11
void int32ToHex(int num, char* str) {
  str[0] = '0'; str[1] = 'x';
  int i;
  char tmp;
  // str[9] ~ str[2]
  for(i=0; i < 8; i++) {
    tmp = num & 0xF;
    if(tmp >= 0x0 && tmp <= 0x9) {
        str[9-i] = '0' + tmp;
    } else {
        str[9-i] = 'A' + (tmp - 0xA);
    }
    num = num >> 4;
  }
  str[10] = '\0';
  return;
}

// str.length == 19 (0x...\0)
void int64ToHex(long num, char* str) {
  str[0] = '0'; str[1] = 'x';
  int i;
  char tmp;
  // str[17] ~ str[2]
  for(i=0; i<16; i++) {
    tmp = num & 0xF;
    if(tmp >= 0x0 && tmp <= 0x9) {
      str[17-i] = '0' + tmp;
    } else {
      str[17-i] = 'A' + (tmp - 0xA);
    }
    num = num >> 4;
  }
  str[18] = '\0';
  return;
}

void printstr(char* str) {
  char* ptr = str;
  while(*ptr != '\0') {
    PUTCHAR_TOHOST(*ptr)
    ptr++;
  }
  return;
}

void __attribute__((noreturn)) exit(int ret) {
  unsigned long cycle, instret;
  asm volatile("rdcycle %0":"=r" (cycle));
  asm volatile("rdinstret %0":"=r" (instret));
  char string[19];
  printstr("Exit code: ");
  int32ToHex(ret, string);
  printstr(string);
  printstr("\ncycle: ");
  int64ToHex(cycle, string);
  printstr(string);
  printstr("\ninstret: ");
  int64ToHex(instret, string);
  printstr(string);

  // guarantee that register a0 holds exit code
  volatile register unsigned long exit_code asm ("a0") = ret;
  PUTCHAR_TOHOST('\0');
_exit:
  goto _exit;
}

uintptr_t __attribute__((weak)) handle_trap(uintptr_t cause, uintptr_t epc, uintptr_t regs[32])
{
  char epchex[19];
  int64ToHex(epc, epchex);
  switch(cause) {
    case 0:
      printstr("INSTRUCTION ADDRESS MISALIGNED at PC: ");
      printstr(epchex);
      break;
    case 1:
      printstr("INSTRUCTION ACCESS FAULT at PC: ");
      printstr(epchex);
      break;
    case 2:
      printstr("ILLEGAL INSTRUCTION at PC: ");
      printstr(epchex);
      break;
    case 4:
      printstr("LOAD ADDRESS MISALIGNED at PC: ");
      printstr(epchex);
      break;
    case 5:
      printstr("LOAD ACCESS FAULT at PC: ");
      printstr(epchex);
      break;
    case 6:
      printstr("STORE ADDRESS MISALIGNED at PC: ");
      printstr(epchex);
      break;
    case 7:
      printstr("STORE ACCESS FAULT at PC: ");
      printstr(epchex);
      break;
    case 11:
      printstr("ECALL FROM M-MODE at PC: ");
      printstr(epchex);
      break;
    default:
      printstr("UNKNOWN EXCEPTION at PC: ");
      printstr(epchex);
      printstr("\nCHECK MCAUSE in RTL");
      break;
  }
  exit(-1);
}

void __attribute__((weak)) thread_entry(int cid, int nc)
{
  // multi-threaded programs override this function.
  // for the case of single-threaded programs, only let core 0 proceed.
  while (cid != 0);
}

int __attribute__((weak)) main(int argc, char** argv)
{
  // single-threaded programs override this function.
  // printstr("Implement main(), foo!\n");
  return -1;
}

void _init(
#ifdef MULTICORE
    int cid, int nc
#else
    void
#endif
    )
{
  // init_tls();
  // thread_entry(cid, nc);

  // only single-threaded programs should ever get here.
  asm volatile ("csrw minstret, x0; csrw mcycle, x0;");
  int ret = main(0, 0);

  exit(ret);
}

void* memcpy(void* dest, const void* src, size_t len)
{
  if ((((uintptr_t)dest | (uintptr_t)src | len) & (sizeof(uintptr_t)-1)) == 0) {
    const uintptr_t* s = src;
    uintptr_t *d = dest;
    uintptr_t *end = dest + len;
    while (d + 8 < end) {
      uintptr_t reg[8] = {s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7]};
      d[0] = reg[0];
      d[1] = reg[1];
      d[2] = reg[2];
      d[3] = reg[3];
      d[4] = reg[4];
      d[5] = reg[5];
      d[6] = reg[6];
      d[7] = reg[7];
      d += 8;
      s += 8;
    }
    while (d < end)
      *d++ = *s++;
  } else {
    const char* s = src;
    char *d = dest;
    while (d < (char*)(dest + len))
      *d++ = *s++;
  }
  return dest;
}

void* memset(void* dest, int byte, size_t len)
{
  if ((((uintptr_t)dest | len) & (sizeof(uintptr_t)-1)) == 0) {
    uintptr_t word = byte & 0xFF;
    word |= word << 8;
    word |= word << 16;
    word |= word << 16 << 16;

    uintptr_t *d = dest;
    while (d < (uintptr_t*)(dest + len))
      *d++ = word;
  } else {
    char *d = dest;
    while (d < (char*)(dest + len))
      *d++ = byte;
  }
  return dest;
}

size_t strlen(const char *s)
{
  const char *p = s;
  while (*p)
    p++;
  return p - s;
}

size_t strnlen(const char *s, size_t n)
{
  const char *p = s;
  while (n-- && *p)
    p++;
  return p - s;
}

int strcmp(const char* s1, const char* s2)
{
  unsigned char c1, c2;

  do {
    c1 = *s1++;
    c2 = *s2++;
  } while (c1 != 0 && c1 == c2);

  return c1 - c2;
}

char* strcpy(char* dest, const char* src)
{
  char* d = dest;
  while ((*d++ = *src++))
    ;
  return dest;
}

