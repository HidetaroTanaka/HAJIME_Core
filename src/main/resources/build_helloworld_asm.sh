#!/bin/bash

#riscv64-unknown-elf-gcc -march=rv64i -mabi=lp64 -static -mcmodel=medany -fvisibility=hidden -nostdlib -nostartfiles -I ./headers/ -T link.ld ../../../submodule/riscv-tests/isa/rv64ui/addi.S -o addi.out
#riscv64-unknown-elf-gcc -march=rv64i -mabi=lp64 -static -mcmodel=medany -fvisibility=hidden -nostdlib -nostartfiles -I ./headers/ -T link.ld helloworld_asm.S -o helloworld_asm.out
#riscv64-unknown-elf-objdump --disassemble-all --disassemble-zeroes --section=.text --section=.text.startup --section=.text.init --section=.data helloworld_asm.out > helloworld_asm.dump
#riscv64-unknown-elf-objcopy -O binary helloworld_asm.out helloworld_asm.bin
#riscv64-unknown-elf-objcopy --dump-section .text.init=helloworld_asm_inst.bin helloworld_asm.out
#hexdump -v -e '1/1 "%02x" "\n"' helloworld_asm_inst.bin > helloworld_asm_inst.hex

riscv64-unknown-elf-gcc -march=rv64i_zicsr -mabi=lp64 -static -mcmodel=medany -fvisibility=hidden -nostdlib -nostartfiles -I ./headers/ -T link.ld simpleTest.S -o simpleTest.out
riscv64-unknown-elf-objdump --disassemble-all --disassemble-zeroes --section=.text --section=.text.startup --section=.text.init --section=.data simpleTest.out > simpleTest.dump
riscv64-unknown-elf-objcopy -O binary simpleTest.out simpleTest.bin
riscv64-unknown-elf-objcopy --dump-section .text.init=simpleTest_inst.bin simpleTest.out
hexdump -v -e '1/4 "%08x" "\n"' simpleTest_inst.bin > simpleTest_inst.hex

rm *.out *.bin