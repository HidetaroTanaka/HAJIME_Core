#!/bin/bash

riscv64-unknown-elf-gcc -I ./common -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr -mabi=lp64 -o factorial.riscv ./factorial/factorial.c ./common/syscalls.c ./common/crt.S -static -nostdlib -nostartfiles -T ./common/test.ld
riscv64-unknown-elf-objdump --disassemble-all factorial.riscv > factorial.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=factorial_rodata.bin factorial.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=factorial_rodata_str1_8.bin factorial.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=factorial_sdata.bin factorial.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=factorial_text_init.bin factorial.riscv
riscv64-unknown-elf-objcopy --dump-section .text=factorial_text.bin factorial.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=factorial_text_startup.bin factorial.riscv
hexdump -v -e '1/4 "%08x" "\n"' factorial_rodata.bin > factorial_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' factorial_rodata_str1_8.bin > factorial_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' factorial_sdata.bin > factorial_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' factorial_text_init.bin > factorial_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' factorial_text.bin > factorial_text.temp
hexdump -v -e '1/4 "%08x" "\n"' factorial_text_startup.bin > factorial_text_startup.temp
cat factorial_rodata.temp factorial_rodata_str1_8.temp factorial_sdata.temp > factorial_data.hex
cat factorial_text_init.temp factorial_text.temp factorial_text_startup.temp > factorial_inst.hex

riscv64-unknown-elf-gcc -I ./common -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr -mabi=lp64 -o power.riscv ./power/power.c ./common/syscalls.c ./common/crt.S -static -nostdlib -nostartfiles -T ./common/test.ld
riscv64-unknown-elf-objdump --disassemble-all power.riscv > power.dump
riscv64-unknown-elf-objcopy --dump-section .data=power_data.bin power.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata=power_rodata.bin power.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=power_rodata_str1_8.bin power.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=power_sdata.bin power.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=power_text_init.bin power.riscv
riscv64-unknown-elf-objcopy --dump-section .text=power_text.bin power.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=power_text_startup.bin power.riscv
hexdump -v -e '1/4 "%08x" "\n"' power_data.bin > power_data.temp
hexdump -v -e '1/4 "%08x" "\n"' power_rodata.bin > power_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' power_rodata_str1_8.bin > power_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' power_sdata.bin > power_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' power_text_init.bin > power_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' power_text.bin > power_text.temp
hexdump -v -e '1/4 "%08x" "\n"' power_text_startup.bin > power_text_startup.temp
cat power_data.temp power_rodata.temp power_rodata_str1_8.temp power_sdata.temp > power_data.hex
cat power_text_init.temp power_text.temp power_text_startup.temp > power_inst.hex

riscv64-unknown-elf-gcc -I ./common -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr -mabi=lp64 -o vector_innerproduct.riscv ./vector_innerproduct/vector_innerproduct.c ./common/syscalls.c ./common/crt.S -static -nostdlib -nostartfiles -T ./common/test.ld
riscv64-unknown-elf-objdump --disassemble-all vector_innerproduct.riscv > vector_innerproduct.dump
riscv64-unknown-elf-objcopy --dump-section .data=vector_innerproduct_data.bin vector_innerproduct.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata=vector_innerproduct_rodata.bin vector_innerproduct.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vector_innerproduct_rodata_str1_8.bin vector_innerproduct.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vector_innerproduct_sdata.bin vector_innerproduct.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vector_innerproduct_text_init.bin vector_innerproduct.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vector_innerproduct_text.bin vector_innerproduct.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vector_innerproduct_text_startup.bin vector_innerproduct.riscv
hexdump -v -e '1/4 "%08x" "\n"' vector_innerproduct_data.bin > vector_innerproduct_data.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_innerproduct_rodata.bin > vector_innerproduct_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_innerproduct_rodata_str1_8.bin > vector_innerproduct_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_innerproduct_sdata.bin > vector_innerproduct_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_innerproduct_text_init.bin > vector_innerproduct_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_innerproduct_text.bin > vector_innerproduct_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_innerproduct_text_startup.bin > vector_innerproduct_text_startup.temp
cat vector_innerproduct_data.temp vector_innerproduct_rodata.temp vector_innerproduct_rodata_str1_8.temp vector_innerproduct_sdata.temp > vector_innerproduct_data.hex
cat vector_innerproduct_text_init.temp vector_innerproduct_text.temp vector_innerproduct_text_startup.temp > vector_innerproduct_inst.hex

rm *.riscv *.bin *.temp
