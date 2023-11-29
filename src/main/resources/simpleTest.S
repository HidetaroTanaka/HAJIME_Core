#include "riscv_test.h"
#include "test_macros.h"

#define PUTCHAR(x) \
  li s1, x; \
  sb s1, 0(s0);

RVTEST_RV64U
RVTEST_CODE_BEGIN

  li s0, 0x10000000
  li s1, 0x114514
  sw s1, 0(s0)
loop:
  j loop

  TEST_PASSFAIL

RVTEST_CODE_END

  .data
RVTEST_DATA_BEGIN

  TEST_DATA

RVTEST_DATA_END