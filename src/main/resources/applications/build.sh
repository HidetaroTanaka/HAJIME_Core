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

riscv64-unknown-elf-gcc -I ./common -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64i_zicsr -mabi=lp64 -o printInt64.riscv ./printInt64/printInt64.c ./common/syscalls.c ./common/crt.S -static -nostdlib -nostartfiles -T ./common/test.ld

riscv64-unknown-elf-objdump --disassemble-all printInt64.riscv > printInt64.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=printInt64_rodata.bin printInt64.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=printInt64_rodata_str1_8.bin printInt64.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=printInt64_sdata.bin printInt64.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=printInt64_text_init.bin printInt64.riscv
riscv64-unknown-elf-objcopy --dump-section .text=printInt64_text.bin printInt64.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=printInt64_text_startup.bin printInt64.riscv
hexdump -v -e '1/4 "%08x" "\n"' printInt64_rodata.bin > printInt64_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' printInt64_rodata_str1_8.bin > printInt64_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' printInt64_sdata.bin > printInt64_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' printInt64_text_init.bin > printInt64_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' printInt64_text.bin > printInt64_text.temp
hexdump -v -e '1/4 "%08x" "\n"' printInt64_text_startup.bin > printInt64_text_startup.temp
cat printInt64_rodata.temp printInt64_rodata_str1_8.temp printInt64_sdata.temp > printInt64_data.hex
cat printInt64_text_init.temp printInt64_text.temp printInt64_text_startup.temp > printInt64_inst.hex

riscv64-unknown-elf-gcc -I ./common -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64i_zicsr -mabi=lp64 -o selection_sort.riscv ./selection_sort/selection_sort.c ./common/syscalls.c ./common/crt.S -static -nostdlib -nostartfiles -T ./common/test.ld

riscv64-unknown-elf-objdump --disassemble-all selection_sort.riscv > selection_sort.dump
riscv64-unknown-elf-objcopy --dump-section .data=selection_sort_data.bin selection_sort.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata=selection_sort_rodata.bin selection_sort.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=selection_sort_rodata_str1_8.bin selection_sort.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=selection_sort_text_init.bin selection_sort.riscv
riscv64-unknown-elf-objcopy --dump-section .text=selection_sort_text.bin selection_sort.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=selection_sort_text_startup.bin selection_sort.riscv
hexdump -v -e '1/4 "%08x" "\n"' selection_sort_data.bin > selection_sort_data.temp
hexdump -v -e '1/4 "%08x" "\n"' selection_sort_rodata.bin > selection_sort_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' selection_sort_rodata_str1_8.bin > selection_sort_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' selection_sort_text_init.bin > selection_sort_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' selection_sort_text.bin > selection_sort_text.temp
hexdump -v -e '1/4 "%08x" "\n"' selection_sort_text_startup.bin > selection_sort_text_startup.temp
cat selection_sort_data.temp selection_sort_rodata.temp selection_sort_rodata_str1_8.temp > selection_sort_data.hex
cat selection_sort_text_init.temp selection_sort_text.temp selection_sort_text_startup.temp > selection_sort_inst.hex

riscv64-unknown-elf-gcc -I ./common -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64i_zicsr -mabi=lp64 -o memcpy.riscv ./memcpy/memcpy_main.c ./common/syscalls.c ./common/crt.S -static -nostdlib -nostartfiles -T ./common/test.ld

riscv64-unknown-elf-objdump --disassemble-all memcpy.riscv > memcpy.dump
riscv64-unknown-elf-objcopy --dump-section .data=memcpy_data.bin memcpy.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata=memcpy_rodata.bin memcpy.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=memcpy_rodata_str1_8.bin memcpy.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=memcpy_text_init.bin memcpy.riscv
riscv64-unknown-elf-objcopy --dump-section .text=memcpy_text.bin memcpy.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=memcpy_text_startup.bin memcpy.riscv
hexdump -v -e '1/4 "%08x" "\n"' memcpy_data.bin > memcpy_data.temp
hexdump -v -e '1/4 "%08x" "\n"' memcpy_rodata.bin > memcpy_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' memcpy_rodata_str1_8.bin > memcpy_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' memcpy_text_init.bin > memcpy_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' memcpy_text.bin > memcpy_text.temp
hexdump -v -e '1/4 "%08x" "\n"' memcpy_text_startup.bin > memcpy_text_startup.temp
cat memcpy_data.temp memcpy_rodata.temp memcpy_rodata_str1_8.temp > memcpy_data.hex
cat memcpy_text_init.temp memcpy_text.temp memcpy_text_startup.temp > memcpy_inst.hex

riscv64-unknown-elf-gcc -I ./common -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64i_zicsr -mabi=lp64 -o quicksort.riscv ./quicksort/quicksort.c ./common/syscalls.c ./common/crt.S -static -nostdlib -nostartfiles -T ./common/test.ld

riscv64-unknown-elf-objdump --disassemble-all quicksort.riscv > quicksort.dump
riscv64-unknown-elf-objcopy --dump-section .data=quicksort_data.bin quicksort.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata=quicksort_rodata.bin quicksort.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=quicksort_rodata_str1_8.bin quicksort.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=quicksort_text_init.bin quicksort.riscv
riscv64-unknown-elf-objcopy --dump-section .text=quicksort_text.bin quicksort.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=quicksort_text_startup.bin quicksort.riscv
hexdump -v -e '1/4 "%08x" "\n"' quicksort_data.bin > quicksort_data.temp
hexdump -v -e '1/4 "%08x" "\n"' quicksort_rodata.bin > quicksort_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' quicksort_rodata_str1_8.bin > quicksort_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' quicksort_text_init.bin > quicksort_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' quicksort_text.bin > quicksort_text.temp
hexdump -v -e '1/4 "%08x" "\n"' quicksort_text_startup.bin > quicksort_text_startup.temp
cat quicksort_data.temp quicksort_rodata.temp quicksort_rodata_str1_8.temp > quicksort_data.hex
cat quicksort_text_init.temp quicksort_text.temp quicksort_text_startup.temp > quicksort_inst.hex

rm *.riscv *.bin *.temp
