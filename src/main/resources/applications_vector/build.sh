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

rm *.riscv *.bin *.temp