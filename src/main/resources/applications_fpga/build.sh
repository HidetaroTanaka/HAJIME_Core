#!/bin/bash

riscv64-unknown-elf-gcc -I ./headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o printTo7Seg.riscv ./printTo7Seg/printTo7Seg.c ./headers/syscalls.c ./headers/crt.S -static -nostdlib -nostartfiles -T ./headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all printTo7Seg.riscv > printTo7Seg.dump
riscv64-unknown-elf-objcopy --dump-section .sdata=printTo7Seg_sdata.bin printTo7Seg.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=printTo7Seg_text_init.bin printTo7Seg.riscv
riscv64-unknown-elf-objcopy --dump-section .text=printTo7Seg_text.bin printTo7Seg.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=printTo7Seg_text_startup.bin printTo7Seg.riscv
hexdump -v -e '1/8 "%016x" "\n"' printTo7Seg_sdata.bin > printTo7Seg_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' printTo7Seg_text_init.bin > printTo7Seg_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' printTo7Seg_text.bin > printTo7Seg_text.temp
hexdump -v -e '1/4 "%08x" "\n"' printTo7Seg_text_startup.bin > printTo7Seg_text_startup.temp
cat printTo7Seg_sdata.temp > printTo7Seg_data.mem
cat printTo7Seg_text_init.temp printTo7Seg_text.temp printTo7Seg_text_startup.temp > printTo7Seg_inst.mem

riscv64-unknown-elf-gcc -I ./headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vector_matmul.riscv ./vector_matmul/vector_matmul.c ./headers/syscalls.c ./headers/crt.S -static -nostdlib -nostartfiles -T ./headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vector_matmul.riscv > vector_matmul.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vector_matmul_rodata.bin vector_matmul.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vector_matmul_sdata.bin vector_matmul.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vector_matmul_text_init.bin vector_matmul.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vector_matmul_text.bin vector_matmul.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vector_matmul_text_startup.bin vector_matmul.riscv
#hexdump -v -e '1/8 "%016x" "\n"' vector_matmul_rodata.bin > vector_matmul_rodata.temp
#hexdump -v -e '1/8 "%016x" "\n"' vector_matmul_sdata.bin > vector_matmul_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_matmul_rodata.bin > vector_matmul_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_matmul_sdata.bin > vector_matmul_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_matmul_text_init.bin > vector_matmul_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_matmul_text.bin > vector_matmul_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_matmul_text_startup.bin > vector_matmul_text_startup.temp
cat vector_matmul_rodata.temp vector_matmul_sdata.temp > vector_matmul_data.mem
cat vector_matmul_text_init.temp vector_matmul_text.temp vector_matmul_text_startup.temp > vector_matmul_inst.mem

rm *.riscv *.bin *.temp