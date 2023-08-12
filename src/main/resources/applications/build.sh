#!/bin/bash

riscv64-unknown-elf-gcc -I ./common -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64i_zicsr -mabi=lp64 -o median.riscv ./median/median.c ./median/median_main.c ./common/syscalls.c ./common/crt.S -static -nostdlib -nostartfiles -T ./common/test.ld
riscv64-unknown-elf-objdump --disassemble-all median.riscv > median.dump
riscv64-unknown-elf-objcopy --dump-section .data=median_data.bin median.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata=median_rodata.bin median.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=median_rodata_str1_8.bin median.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=median_text_init.bin median.riscv
riscv64-unknown-elf-objcopy --dump-section .text=median_text.bin median.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=median_text_startup.bin median.riscv
hexdump -v -e '1/4 "%08x" "\n"' median_data.bin > median_data.temp
hexdump -v -e '1/4 "%08x" "\n"' median_rodata.bin > median_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' median_rodata_str1_8.bin > median_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' median_text_init.bin > median_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' median_text.bin > median_text.temp
hexdump -v -e '1/4 "%08x" "\n"' median_text_startup.bin > median_text_startup.temp
cat median_text_init.temp median_text.temp median_text_startup.temp > median_inst.hex
cat median_data.temp median_rodata.temp median_rodata_str1_8.temp > median_data.hex

riscv64-unknown-elf-gcc -I ./common -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64i_zicsr -mabi=lp64 -o helloworld.riscv ./helloworld/helloworld.c ./common/syscalls.c ./common/crt.S -static -nostdlib -nostartfiles -T ./common/test.ld

# riscv64-unknown-elf-gcc -I ./common -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64i -mabi=lp64 ./helloworld/helloworld.c ./common/syscalls.c ./common/crt.S -static -nostdlib -nostartfiles -T ./common/test.ld -S

riscv64-unknown-elf-objdump --disassemble-all helloworld.riscv > helloworld.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=helloworld_rodata.bin helloworld.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=helloworld_rodata_str1_8.bin helloworld.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=helloworld_text_init.bin helloworld.riscv
riscv64-unknown-elf-objcopy --dump-section .text=helloworld_text.bin helloworld.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=helloworld_text_startup.bin helloworld.riscv
hexdump -v -e '1/4 "%08x" "\n"' helloworld_rodata.bin > helloworld_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' helloworld_rodata_str1_8.bin > helloworld_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' helloworld_text_init.bin > helloworld_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' helloworld_text.bin > helloworld_text.temp
hexdump -v -e '1/4 "%08x" "\n"' helloworld_text_startup.bin > helloworld_text_startup.temp
cat helloworld_rodata.temp helloworld_rodata_str1_8.temp > helloworld_data.hex
cat helloworld_text_init.temp helloworld_text.temp helloworld_text_startup.temp > helloworld_inst.hex

rm *.riscv *.bin *.temp
