#!/bin/bash

#riscv64-unknown-elf-gcc -march=rv64i -mabi=lp64 -static -mcmodel=medany -fvisibility=hidden -nostdlib -nostartfiles -I ./headers/ -T link.ld ../../../submodule/riscv-tests/isa/rv64ui/addi.S -o addi.out
riscv64-unknown-elf-gcc -march=rv64i_zicsr -mabi=lp64 -static -mcmodel=medany -fvisibility=hidden -nostdlib -nostartfiles -I ./headers/ -T link.ld ../../../submodule/riscv-tests/isa/rv64ui/$1.S -o $1.out
riscv64-unknown-elf-objdump --disassemble-all --disassemble-zeroes --section=.text --section=.text.startup --section=.text.init --section=.data $1.out > ./rv64ui/$1.dump
riscv64-unknown-elf-objcopy -O binary $1.out $1.bin
riscv64-unknown-elf-objcopy --dump-section .data=$1_data.bin $1.out
riscv64-unknown-elf-objcopy --dump-section .text.init=$1_inst.bin $1.out
hexdump -v -e '1/4 "%08x" "\n"' $1_data.bin > ./rv64ui/$1_data.hex
hexdump -v -e '1/4 "%08x" "\n"' $1_inst.bin > ./rv64ui/$1_inst.hex
# hexdump -v -e '1/1 "%02x" "\n"' $1.bin > $1_dump.hex
rm *.out *.bin