riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vector_conf.riscv ./vector_conf/vector_conf.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vector_conf.riscv > vector_conf.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vector_conf_rodata.bin vector_conf.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vector_conf_rodata_str1_8.bin vector_conf.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vector_conf_sdata.bin vector_conf.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vector_conf_text_init.bin vector_conf.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vector_conf_text.bin vector_conf.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vector_conf_text_startup.bin vector_conf.riscv
hexdump -v -e '1/4 "%08x" "\n"' vector_conf_rodata.bin > vector_conf_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_conf_rodata_str1_8.bin > vector_conf_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_conf_sdata.bin > vector_conf_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_conf_text_init.bin > vector_conf_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_conf_text.bin > vector_conf_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_conf_text_startup.bin > vector_conf_text_startup.temp
cat vector_conf_rodata.temp vector_conf_rodata_str1_8.temp vector_conf_sdata.temp > vector_conf_data.hex
cat vector_conf_text_init.temp vector_conf_text.temp vector_conf_text_startup.temp > vector_conf_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vector_ldst.riscv ./vector_ldst/vector_ldst.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vector_ldst.riscv > vector_ldst.dump
riscv64-unknown-elf-objcopy --dump-section .data=vector_ldst_data.bin vector_ldst.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata=vector_ldst_rodata.bin vector_ldst.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vector_ldst_rodata_str1_8.bin vector_ldst.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vector_ldst_sdata.bin vector_ldst.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vector_ldst_bss.bin vector_ldst.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vector_ldst_text_init.bin vector_ldst.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vector_ldst_text.bin vector_ldst.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vector_ldst_text_startup.bin vector_ldst.riscv
hexdump -v -e '1/4 "%08x" "\n"' vector_ldst_data.bin > vector_ldst_data.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_ldst_rodata.bin > vector_ldst_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_ldst_rodata_str1_8.bin > vector_ldst_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_ldst_sdata.bin > vector_ldst_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_ldst_bss.bin > vector_ldst_bss.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_ldst_text_init.bin > vector_ldst_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_ldst_text.bin > vector_ldst_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_ldst_text_startup.bin > vector_ldst_text_startup.temp
cat vector_ldst_data.temp vector_ldst_rodata.temp vector_ldst_rodata_str1_8.temp vector_ldst_sdata.temp vector_ldst_bss.temp > vector_ldst_data.hex
cat vector_ldst_text_init.temp vector_ldst_text.temp vector_ldst_text_startup.temp > vector_ldst_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vector_memcpy.riscv ./vector_memcpy/vector_memcpy.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vector_memcpy.riscv > vector_memcpy.dump
riscv64-unknown-elf-objcopy --dump-section .data=vector_memcpy_data.bin vector_memcpy.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata=vector_memcpy_rodata.bin vector_memcpy.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vector_memcpy_rodata_str1_8.bin vector_memcpy.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vector_memcpy_sdata.bin vector_memcpy.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vector_memcpy_text_init.bin vector_memcpy.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vector_memcpy_text.bin vector_memcpy.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vector_memcpy_text_startup.bin vector_memcpy.riscv
hexdump -v -e '1/4 "%08x" "\n"' vector_memcpy_data.bin > vector_memcpy_data.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_memcpy_rodata.bin > vector_memcpy_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_memcpy_rodata_str1_8.bin > vector_memcpy_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_memcpy_sdata.bin > vector_memcpy_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_memcpy_text_init.bin > vector_memcpy_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_memcpy_text.bin > vector_memcpy_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_memcpy_text_startup.bin > vector_memcpy_text_startup.temp
cat vector_memcpy_data.temp vector_memcpy_rodata.temp vector_memcpy_rodata_str1_8.temp vector_memcpy_sdata.temp > vector_memcpy_data.hex
cat vector_memcpy_text_init.temp vector_memcpy_text.temp vector_memcpy_text_startup.temp > vector_memcpy_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vector_stride.riscv ./vector_stride/vector_stride.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vector_stride.riscv > vector_stride.dump
riscv64-unknown-elf-objcopy --dump-section .data=vector_stride_data.bin vector_stride.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata=vector_stride_rodata.bin vector_stride.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vector_stride_rodata_str1_8.bin vector_stride.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vector_stride_sdata.bin vector_stride.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vector_stride_text_init.bin vector_stride.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vector_stride_text.bin vector_stride.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vector_stride_text_startup.bin vector_stride.riscv
hexdump -v -e '1/4 "%08x" "\n"' vector_stride_data.bin > vector_stride_data.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_stride_rodata.bin > vector_stride_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_stride_rodata_str1_8.bin > vector_stride_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_stride_sdata.bin > vector_stride_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_stride_text_init.bin > vector_stride_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_stride_text.bin > vector_stride_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_stride_text_startup.bin > vector_stride_text_startup.temp
cat vector_stride_data.temp vector_stride_rodata.temp vector_stride_rodata_str1_8.temp vector_stride_sdata.temp > vector_stride_data.hex
cat vector_stride_text_init.temp vector_stride_text.temp vector_stride_text_startup.temp > vector_stride_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vector_index.riscv ./vector_index/vector_index.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vector_index.riscv > vector_index.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vector_index_rodata.bin vector_index.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vector_index_rodata_str1_8.bin vector_index.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vector_index_sdata.bin vector_index.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vector_index_text_init.bin vector_index.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vector_index_text.bin vector_index.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vector_index_text_startup.bin vector_index.riscv
hexdump -v -e '1/4 "%08x" "\n"' vector_index_rodata.bin > vector_index_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_index_rodata_str1_8.bin > vector_index_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_index_sdata.bin > vector_index_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_index_text_init.bin > vector_index_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_index_text.bin > vector_index_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_index_text_startup.bin > vector_index_text_startup.temp
cat vector_index_rodata.temp vector_index_rodata_str1_8.temp vector_index_sdata.temp > vector_index_data.hex
cat vector_index_text_init.temp vector_index_text.temp vector_index_text_startup.temp > vector_index_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vadd.riscv ./vadd/vadd.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vadd.riscv > vadd.dump
riscv64-unknown-elf-objdump --disassemble-all vadd.riscv > vadd.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vadd_rodata.bin vadd.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vadd_rodata_str1_8.bin vadd.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vadd_sdata.bin vadd.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vadd_text_init.bin vadd.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vadd_text.bin vadd.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vadd_text_startup.bin vadd.riscv
hexdump -v -e '1/4 "%08x" "\n"' vadd_rodata.bin > vadd_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vadd_rodata_str1_8.bin > vadd_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vadd_sdata.bin > vadd_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vadd_text_init.bin > vadd_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vadd_text.bin > vadd_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vadd_text_startup.bin > vadd_text_startup.temp
cat vadd_rodata.temp vadd_rodata_str1_8.temp vadd_sdata.temp > vadd_data.hex
cat vadd_text_init.temp vadd_text.temp vadd_text_startup.temp > vadd_inst.hex

rm *.riscv *.bin *.temp