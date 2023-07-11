#!/bin/bash

#riscv64-unknown-elf-gcc -march=rv64i -mabi=lp64 -static -mcmodel=medany -fvisibility=hidden -nostdlib -nostartfiles -I ./headers/ -T link.ld ../../../submodule/riscv-tests/isa/rv64ui/addi.S -o addi.out
riscv64-unknown-elf-gcc -march=rv64i -mabi=lp64 -static -mcmodel=medany -fvisibility=hidden -nostdlib -nostartfiles -I ./headers/ -T link.ld $1 -o test.out
riscv64-unknown-elf-objdump --disassemble-all --disassemble-zeroes --section=.text --section=.text.startup --section=.text.init --section=.data test.out > test.dump
riscv64-unknown-elf-objcopy -O binary test.out test.bin
riscv64-unknown-elf-objcopy --dump-section .data=test_data.bin test.out
riscv64-unknown-elf-objcopy --dump-section .text.init=test_inst.bin test.out
hexdump -v -e '1/1 "%02x" "\n"' test_data.bin > testdump_data.hex
hexdump -v -e '1/1 "%02x" "\n"' test_inst.bin > testdump_inst.hex
# hexdump -v -e '1/1 "%02x" "\n"' test.bin > testdump.hex