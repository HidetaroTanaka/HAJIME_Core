#!/bin/bash

riscv64-unknown-elf-gcc -I ./common -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr -mabi=lp64 -o factorial.riscv ./factorial/factorial.c ./common/syscalls.c ./common/crt.S -static -nostdlib -nostartfiles -T ./common/test.ld
riscv64-unknown-elf-objdump --disassemble-all factorial.riscv > factorial.dump

rm *.riscv *.bin *.temp
