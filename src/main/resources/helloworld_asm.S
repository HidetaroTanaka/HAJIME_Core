#include "riscv_test.h"
#include "test_macros.h"

#define PUTCHAR(x) \
  li s1, x; \
  sb s1, 0(s0);

RVTEST_RV64U
RVTEST_CODE_BEGIN

  li s0, 0x1000
  PUTCHAR('H')
  PUTCHAR('e')
  PUTCHAR('l')
  PUTCHAR('l')
  PUTCHAR('o')
  PUTCHAR(',')
  PUTCHAR(' ')
  PUTCHAR('W')
  PUTCHAR('o')
  PUTCHAR('r')
  PUTCHAR('l')
  PUTCHAR('d')
  PUTCHAR('!')
  PUTCHAR(0)
  ret

  TEST_PASSFAIL

RVTEST_CODE_END

  .data
RVTEST_DATA_BEGIN

  TEST_DATA

RVTEST_DATA_END