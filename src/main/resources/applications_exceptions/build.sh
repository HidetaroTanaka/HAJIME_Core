#!/bin/bash

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr -mabi=lp64 -o illegal_inst.riscv ./illegal_inst/illegal_inst.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all illegal_inst.riscv > illegal_inst.dump

riscv64-unknown-elf-objcopy --dump-section .rodata=illegal_inst_rodata.bin illegal_inst.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=illegal_inst_rodata_str1_8.bin illegal_inst.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=illegal_inst_sdata.bin illegal_inst.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=illegal_inst_text_init.bin illegal_inst.riscv
riscv64-unknown-elf-objcopy --dump-section .text=illegal_inst_text.bin illegal_inst.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=illegal_inst_text_startup.bin illegal_inst.riscv
hexdump -v -e '1/4 "%08x" "\n"' illegal_inst_rodata.bin > illegal_inst_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' illegal_inst_rodata_str1_8.bin > illegal_inst_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' illegal_inst_sdata.bin > illegal_inst_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' illegal_inst_text_init.bin > illegal_inst_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' illegal_inst_text.bin > illegal_inst_text.temp
hexdump -v -e '1/4 "%08x" "\n"' illegal_inst_text_startup.bin > illegal_inst_text_startup.temp
cat illegal_inst_rodata.temp illegal_inst_rodata_str1_8.temp illegal_inst_sdata.temp > illegal_inst_data.hex
cat illegal_inst_text_init.temp illegal_inst_text.temp illegal_inst_text_startup.temp > illegal_inst_inst.hex

rm *.riscv *.bin *.temp
