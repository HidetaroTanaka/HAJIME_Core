#!/bin/bash

#riscv64-unknown-elf-gcc -march=rv64i -mabi=lp64 -static -mcmodel=medany -fvisibility=hidden -nostdlib -nostartfiles -I ./headers/ -T link.ld ../../../submodule/riscv-tests/isa/rv64ui/addi.S -o addi.out
riscv64-unknown-elf-gcc -march=rv64i -mabi=lp64 -static -mcmodel=medany -fvisibility=hidden -nostdlib -nostartfiles -I ./headers/ -T link.ld $1 -o test.out
riscv64-unknown-elf-objdump --disassemble-all --disassemble-zeroes --section=.text --section=.text.startup --section=.text.init --section=.data test.out > test.dump