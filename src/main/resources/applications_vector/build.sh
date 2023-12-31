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

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vsub.riscv ./vsub/vsub.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vsub.riscv > vsub.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vsub_rodata.bin vsub.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vsub_rodata_str1_8.bin vsub.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vsub_sdata.bin vsub.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vsub_text_init.bin vsub.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vsub_text.bin vsub.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vsub_text_startup.bin vsub.riscv
hexdump -v -e '1/4 "%08x" "\n"' vsub_rodata.bin > vsub_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vsub_rodata_str1_8.bin > vsub_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vsub_sdata.bin > vsub_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vsub_text_init.bin > vsub_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vsub_text.bin > vsub_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vsub_text_startup.bin > vsub_text_startup.temp
cat vsub_rodata.temp vsub_rodata_str1_8.temp vsub_sdata.temp > vsub_data.hex
cat vsub_text_init.temp vsub_text.temp vsub_text_startup.temp > vsub_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmadc.riscv ./vmadc/vmadc.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmadc.riscv > vmadc.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmadc_rodata.bin vmadc.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmadc_rodata_str1_8.bin vmadc.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmadc_sdata.bin vmadc.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmadc_text_init.bin vmadc.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmadc_text.bin vmadc.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmadc_text_startup.bin vmadc.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmadc_rodata.bin > vmadc_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmadc_rodata_str1_8.bin > vmadc_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmadc_sdata.bin > vmadc_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmadc_text_init.bin > vmadc_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmadc_text.bin > vmadc_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmadc_text_startup.bin > vmadc_text_startup.temp
cat vmadc_rodata.temp vmadc_rodata_str1_8.temp vmadc_sdata.temp > vmadc_data.hex
cat vmadc_text_init.temp vmadc_text.temp vmadc_text_startup.temp > vmadc_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmsbc.riscv ./vmsbc/vmsbc.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmsbc.riscv > vmsbc.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmsbc_rodata.bin vmsbc.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmsbc_rodata_str1_8.bin vmsbc.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmsbc_sdata.bin vmsbc.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmsbc_text_init.bin vmsbc.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmsbc_text.bin vmsbc.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmsbc_text_startup.bin vmsbc.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmsbc_rodata.bin > vmsbc_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsbc_rodata_str1_8.bin > vmsbc_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsbc_sdata.bin > vmsbc_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsbc_text_init.bin > vmsbc_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsbc_text.bin > vmsbc_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsbc_text_startup.bin > vmsbc_text_startup.temp
cat vmsbc_rodata.temp vmsbc_rodata_str1_8.temp vmsbc_sdata.temp > vmsbc_data.hex
cat vmsbc_text_init.temp vmsbc_text.temp vmsbc_text_startup.temp > vmsbc_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vand.riscv ./vand/vand.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vand.riscv > vand.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vand_rodata.bin vand.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vand_rodata_str1_8.bin vand.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vand_sdata.bin vand.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vand_text_init.bin vand.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vand_text.bin vand.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vand_text_startup.bin vand.riscv
hexdump -v -e '1/4 "%08x" "\n"' vand_rodata.bin > vand_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vand_rodata_str1_8.bin > vand_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vand_sdata.bin > vand_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vand_text_init.bin > vand_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vand_text.bin > vand_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vand_text_startup.bin > vand_text_startup.temp
cat vand_rodata.temp vand_rodata_str1_8.temp vand_sdata.temp > vand_data.hex
cat vand_text_init.temp vand_text.temp vand_text_startup.temp > vand_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmseq.riscv ./vmseq/vmseq.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmseq.riscv > vmseq.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmseq_rodata.bin vmseq.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmseq_rodata_str1_8.bin vmseq.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmseq_sdata.bin vmseq.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmseq_text_init.bin vmseq.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmseq_text.bin vmseq.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmseq_text_startup.bin vmseq.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmseq_rodata.bin > vmseq_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmseq_rodata_str1_8.bin > vmseq_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmseq_sdata.bin > vmseq_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmseq_text_init.bin > vmseq_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmseq_text.bin > vmseq_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmseq_text_startup.bin > vmseq_text_startup.temp
cat vmseq_rodata.temp vmseq_rodata_str1_8.temp vmseq_sdata.temp > vmseq_data.hex
cat vmseq_text_init.temp vmseq_text.temp vmseq_text_startup.temp > vmseq_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmslt.riscv ./vmslt/vmslt.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmslt.riscv > vmslt.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmslt_rodata.bin vmslt.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmslt_rodata_str1_8.bin vmslt.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmslt_sdata.bin vmslt.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmslt_text_init.bin vmslt.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmslt_text.bin vmslt.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmslt_text_startup.bin vmslt.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmslt_rodata.bin > vmslt_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmslt_rodata_str1_8.bin > vmslt_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmslt_sdata.bin > vmslt_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmslt_text_init.bin > vmslt_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmslt_text.bin > vmslt_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmslt_text_startup.bin > vmslt_text_startup.temp
cat vmslt_rodata.temp vmslt_rodata_str1_8.temp vmslt_sdata.temp > vmslt_data.hex
cat vmslt_text_init.temp vmslt_text.temp vmslt_text_startup.temp > vmslt_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmsle.riscv ./vmsle/vmsle.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmsle.riscv > vmsle.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmsle_rodata.bin vmsle.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmsle_rodata_str1_8.bin vmsle.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmsle_sdata.bin vmsle.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmsle_text_init.bin vmsle.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmsle_text.bin vmsle.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmsle_text_startup.bin vmsle.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmsle_rodata.bin > vmsle_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsle_rodata_str1_8.bin > vmsle_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsle_sdata.bin > vmsle_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsle_text_init.bin > vmsle_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsle_text.bin > vmsle_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsle_text_startup.bin > vmsle_text_startup.temp
cat vmsle_rodata.temp vmsle_rodata_str1_8.temp vmsle_sdata.temp > vmsle_data.hex
cat vmsle_text_init.temp vmsle_text.temp vmsle_text_startup.temp > vmsle_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmsgt.riscv ./vmsgt/vmsgt.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmsgt.riscv > vmsgt.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmsgt_rodata.bin vmsgt.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmsgt_rodata_str1_8.bin vmsgt.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmsgt_sdata.bin vmsgt.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmsgt_text_init.bin vmsgt.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmsgt_text.bin vmsgt.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmsgt_text_startup.bin vmsgt.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmsgt_rodata.bin > vmsgt_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsgt_rodata_str1_8.bin > vmsgt_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsgt_sdata.bin > vmsgt_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsgt_text_init.bin > vmsgt_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsgt_text.bin > vmsgt_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmsgt_text_startup.bin > vmsgt_text_startup.temp
cat vmsgt_rodata.temp vmsgt_rodata_str1_8.temp vmsgt_sdata.temp > vmsgt_data.hex
cat vmsgt_text_init.temp vmsgt_text.temp vmsgt_text_startup.temp > vmsgt_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmand.riscv ./vmand/vmand.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmand.riscv > vmand.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmand_rodata.bin vmand.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmand_rodata_str1_8.bin vmand.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmand_sdata.bin vmand.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmand_text_init.bin vmand.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmand_text.bin vmand.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmand_text_startup.bin vmand.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmand_rodata.bin > vmand_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmand_rodata_str1_8.bin > vmand_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmand_sdata.bin > vmand_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmand_text_init.bin > vmand_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmand_text.bin > vmand_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmand_text_startup.bin > vmand_text_startup.temp
cat vmand_rodata.temp vmand_rodata_str1_8.temp vmand_sdata.temp > vmand_data.hex
cat vmand_text_init.temp vmand_text.temp vmand_text_startup.temp > vmand_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmor.riscv ./vmor/vmor.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmor.riscv > vmor.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmor_rodata.bin vmor.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmor_rodata_str1_8.bin vmor.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmor_sdata.bin vmor.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmor_text_init.bin vmor.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmor_text.bin vmor.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmor_text_startup.bin vmor.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmor_rodata.bin > vmor_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmor_rodata_str1_8.bin > vmor_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmor_sdata.bin > vmor_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmor_text_init.bin > vmor_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmor_text.bin > vmor_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmor_text_startup.bin > vmor_text_startup.temp
cat vmor_rodata.temp vmor_rodata_str1_8.temp vmor_sdata.temp > vmor_data.hex
cat vmor_text_init.temp vmor_text.temp vmor_text_startup.temp > vmor_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmxor.riscv ./vmxor/vmxor.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmxor.riscv > vmxor.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmxor_rodata.bin vmxor.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmxor_rodata_str1_8.bin vmxor.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmxor_sdata.bin vmxor.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmxor_text_init.bin vmxor.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmxor_text.bin vmxor.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmxor_text_startup.bin vmxor.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmxor_rodata.bin > vmxor_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmxor_rodata_str1_8.bin > vmxor_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmxor_sdata.bin > vmxor_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmxor_text_init.bin > vmxor_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmxor_text.bin > vmxor_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmxor_text_startup.bin > vmxor_text_startup.temp
cat vmxor_rodata.temp vmxor_rodata_str1_8.temp vmxor_sdata.temp > vmxor_data.hex
cat vmxor_text_init.temp vmxor_text.temp vmxor_text_startup.temp > vmxor_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vminmax.riscv ./vminmax/vminmax.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vminmax.riscv > vminmax.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vminmax_rodata.bin vminmax.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vminmax_rodata_str1_8.bin vminmax.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vminmax_sdata.bin vminmax.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vminmax_text_init.bin vminmax.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vminmax_text.bin vminmax.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vminmax_text_startup.bin vminmax.riscv
hexdump -v -e '1/4 "%08x" "\n"' vminmax_rodata.bin > vminmax_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vminmax_rodata_str1_8.bin > vminmax_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vminmax_sdata.bin > vminmax_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vminmax_text_init.bin > vminmax_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vminmax_text.bin > vminmax_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vminmax_text_startup.bin > vminmax_text_startup.temp
cat vminmax_rodata.temp vminmax_rodata_str1_8.temp vminmax_sdata.temp > vminmax_data.hex
cat vminmax_text_init.temp vminmax_text.temp vminmax_text_startup.temp > vminmax_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmerge.riscv ./vmerge/vmerge.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmerge.riscv > vmerge.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmerge_rodata.bin vmerge.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmerge_rodata_str1_8.bin vmerge.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmerge_sdata.bin vmerge.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmerge_text_init.bin vmerge.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmerge_text.bin vmerge.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmerge_text_startup.bin vmerge.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmerge_rodata.bin > vmerge_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmerge_rodata_str1_8.bin > vmerge_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmerge_sdata.bin > vmerge_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmerge_text_init.bin > vmerge_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmerge_text.bin > vmerge_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmerge_text_startup.bin > vmerge_text_startup.temp
cat vmerge_rodata.temp vmerge_rodata_str1_8.temp vmerge_sdata.temp > vmerge_data.hex
cat vmerge_text_init.temp vmerge_text.temp vmerge_text_startup.temp > vmerge_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmv.riscv ./vmv/vmv.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmv.riscv > vmv.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmv_rodata.bin vmv.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmv_rodata_str1_8.bin vmv.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmv_sdata.bin vmv.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmv_text_init.bin vmv.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmv_text.bin vmv.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmv_text_startup.bin vmv.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmv_rodata.bin > vmv_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmv_rodata_str1_8.bin > vmv_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmv_sdata.bin > vmv_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmv_text_init.bin > vmv_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmv_text.bin > vmv_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmv_text_startup.bin > vmv_text_startup.temp
cat vmv_rodata.temp vmv_rodata_str1_8.temp vmv_sdata.temp > vmv_data.hex
cat vmv_text_init.temp vmv_text.temp vmv_text_startup.temp > vmv_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vector_median.riscv ./vector_median/median.c ./vector_median/vector_median.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vector_median.riscv > vector_median.dump
riscv64-unknown-elf-objcopy --dump-section .data=vector_median_data.bin vector_median.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata=vector_median_rodata.bin vector_median.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vector_median_rodata_str1_8.bin vector_median.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vector_median_sdata.bin vector_median.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vector_median_text_init.bin vector_median.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vector_median_text.bin vector_median.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vector_median_text_startup.bin vector_median.riscv
hexdump -v -e '1/4 "%08x" "\n"' vector_median_data.bin > vector_median_data.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_median_rodata.bin > vector_median_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_median_rodata_str1_8.bin > vector_median_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_median_sdata.bin > vector_median_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_median_text_init.bin > vector_median_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_median_text.bin > vector_median_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_median_text_startup.bin > vector_median_text_startup.temp
cat vector_median_data.temp vector_median_rodata.temp vector_median_rodata_str1_8.temp vector_median_sdata.temp > vector_median_data.hex
cat vector_median_text_init.temp vector_median_text.temp vector_median_text_startup.temp > vector_median_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmul.riscv ./vmul/vmul.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmul.riscv > vmul.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmul_rodata.bin vmul.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmul_rodata_str1_8.bin vmul.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmul_sdata.bin vmul.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmul_text_init.bin vmul.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmul_text.bin vmul.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmul_text_startup.bin vmul.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmul_rodata.bin > vmul_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmul_rodata_str1_8.bin > vmul_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmul_sdata.bin > vmul_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmul_text_init.bin > vmul_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmul_text.bin > vmul_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmul_text_startup.bin > vmul_text_startup.temp
cat vmul_rodata.temp vmul_rodata_str1_8.temp vmul_sdata.temp > vmul_data.hex
cat vmul_text_init.temp vmul_text.temp vmul_text_startup.temp > vmul_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmulh.riscv ./vmulh/vmulh.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmulh.riscv > vmulh.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmulh_rodata.bin vmulh.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmulh_rodata_str1_8.bin vmulh.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmulh_sdata.bin vmulh.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmulh_text_init.bin vmulh.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmulh_text.bin vmulh.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmulh_text_startup.bin vmulh.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmulh_rodata.bin > vmulh_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulh_rodata_str1_8.bin > vmulh_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulh_sdata.bin > vmulh_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulh_text_init.bin > vmulh_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulh_text.bin > vmulh_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulh_text_startup.bin > vmulh_text_startup.temp
cat vmulh_rodata.temp vmulh_rodata_str1_8.temp vmulh_sdata.temp > vmulh_data.hex
cat vmulh_text_init.temp vmulh_text.temp vmulh_text_startup.temp > vmulh_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmulhu.riscv ./vmulhu/vmulhu.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmulhu.riscv > vmulhu.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmulhu_rodata.bin vmulhu.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmulhu_rodata_str1_8.bin vmulhu.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmulhu_sdata.bin vmulhu.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmulhu_text_init.bin vmulhu.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmulhu_text.bin vmulhu.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmulhu_text_startup.bin vmulhu.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmulhu_rodata.bin > vmulhu_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulhu_rodata_str1_8.bin > vmulhu_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulhu_sdata.bin > vmulhu_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulhu_text_init.bin > vmulhu_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulhu_text.bin > vmulhu_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulhu_text_startup.bin > vmulhu_text_startup.temp
cat vmulhu_rodata.temp vmulhu_rodata_str1_8.temp vmulhu_sdata.temp > vmulhu_data.hex
cat vmulhu_text_init.temp vmulhu_text.temp vmulhu_text_startup.temp > vmulhu_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmulhsu.riscv ./vmulhsu/vmulhsu.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmulhsu.riscv > vmulhsu.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmulhsu_rodata.bin vmulhsu.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmulhsu_rodata_str1_8.bin vmulhsu.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmulhsu_sdata.bin vmulhsu.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmulhsu_text_init.bin vmulhsu.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmulhsu_text.bin vmulhsu.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmulhsu_text_startup.bin vmulhsu.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmulhsu_rodata.bin > vmulhsu_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulhsu_rodata_str1_8.bin > vmulhsu_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulhsu_sdata.bin > vmulhsu_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulhsu_text_init.bin > vmulhsu_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulhsu_text.bin > vmulhsu_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmulhsu_text_startup.bin > vmulhsu_text_startup.temp
cat vmulhsu_rodata.temp vmulhsu_rodata_str1_8.temp vmulhsu_sdata.temp > vmulhsu_data.hex
cat vmulhsu_text_init.temp vmulhsu_text.temp vmulhsu_text_startup.temp > vmulhsu_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmacc.riscv ./vmacc/vmacc.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmacc.riscv > vmacc.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmacc_rodata.bin vmacc.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmacc_rodata_str1_8.bin vmacc.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmacc_sdata.bin vmacc.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmacc_text_init.bin vmacc.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmacc_text.bin vmacc.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmacc_text_startup.bin vmacc.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmacc_rodata.bin > vmacc_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmacc_rodata_str1_8.bin > vmacc_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmacc_sdata.bin > vmacc_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmacc_text_init.bin > vmacc_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmacc_text.bin > vmacc_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmacc_text_startup.bin > vmacc_text_startup.temp
cat vmacc_rodata.temp vmacc_rodata_str1_8.temp vmacc_sdata.temp > vmacc_data.hex
cat vmacc_text_init.temp vmacc_text.temp vmacc_text_startup.temp > vmacc_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vnmsac.riscv ./vnmsac/vnmsac.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vnmsac.riscv > vnmsac.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vnmsac_rodata.bin vnmsac.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vnmsac_rodata_str1_8.bin vnmsac.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vnmsac_sdata.bin vnmsac.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vnmsac_text_init.bin vnmsac.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vnmsac_text.bin vnmsac.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vnmsac_text_startup.bin vnmsac.riscv
hexdump -v -e '1/4 "%08x" "\n"' vnmsac_rodata.bin > vnmsac_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vnmsac_rodata_str1_8.bin > vnmsac_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vnmsac_sdata.bin > vnmsac_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vnmsac_text_init.bin > vnmsac_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vnmsac_text.bin > vnmsac_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vnmsac_text_startup.bin > vnmsac_text_startup.temp
cat vnmsac_rodata.temp vnmsac_rodata_str1_8.temp vnmsac_sdata.temp > vnmsac_data.hex
cat vnmsac_text_init.temp vnmsac_text.temp vnmsac_text_startup.temp > vnmsac_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vmadd.riscv ./vmadd/vmadd.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vmadd.riscv > vmadd.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vmadd_rodata.bin vmadd.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vmadd_rodata_str1_8.bin vmadd.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vmadd_sdata.bin vmadd.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vmadd_text_init.bin vmadd.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vmadd_text.bin vmadd.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vmadd_text_startup.bin vmadd.riscv
hexdump -v -e '1/4 "%08x" "\n"' vmadd_rodata.bin > vmadd_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmadd_rodata_str1_8.bin > vmadd_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vmadd_sdata.bin > vmadd_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vmadd_text_init.bin > vmadd_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vmadd_text.bin > vmadd_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vmadd_text_startup.bin > vmadd_text_startup.temp
cat vmadd_rodata.temp vmadd_rodata_str1_8.temp vmadd_sdata.temp > vmadd_data.hex
cat vmadd_text_init.temp vmadd_text.temp vmadd_text_startup.temp > vmadd_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vnmsub.riscv ./vnmsub/vnmsub.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vnmsub.riscv > vnmsub.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vnmsub_rodata.bin vnmsub.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vnmsub_rodata_str1_8.bin vnmsub.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vnmsub_sdata.bin vnmsub.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vnmsub_text_init.bin vnmsub.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vnmsub_text.bin vnmsub.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vnmsub_text_startup.bin vnmsub.riscv
hexdump -v -e '1/4 "%08x" "\n"' vnmsub_rodata.bin > vnmsub_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vnmsub_rodata_str1_8.bin > vnmsub_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vnmsub_sdata.bin > vnmsub_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vnmsub_text_init.bin > vnmsub_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vnmsub_text.bin > vnmsub_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vnmsub_text_startup.bin > vnmsub_text_startup.temp
cat vnmsub_rodata.temp vnmsub_rodata_str1_8.temp vnmsub_sdata.temp > vnmsub_data.hex
cat vnmsub_text_init.temp vnmsub_text.temp vnmsub_text_startup.temp > vnmsub_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vredsum.riscv ./vredsum/vredsum.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vredsum.riscv > vredsum.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vredsum_rodata.bin vredsum.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vredsum_rodata_str1_8.bin vredsum.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vredsum_sdata.bin vredsum.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vredsum_text_init.bin vredsum.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vredsum_text.bin vredsum.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vredsum_text_startup.bin vredsum.riscv
hexdump -v -e '1/4 "%08x" "\n"' vredsum_rodata.bin > vredsum_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vredsum_rodata_str1_8.bin > vredsum_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vredsum_sdata.bin > vredsum_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vredsum_text_init.bin > vredsum_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vredsum_text.bin > vredsum_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vredsum_text_startup.bin > vredsum_text_startup.temp
cat vredsum_rodata.temp vredsum_rodata_str1_8.temp vredsum_sdata.temp > vredsum_data.hex
cat vredsum_text_init.temp vredsum_text.temp vredsum_text_startup.temp > vredsum_inst.hex

riscv64-unknown-elf-gcc -I ../application_headers -DPREALLOCATE=1 -mcmodel=medany -static -std=gnu99 -O2 -fno-common -fno-builtin-printf -fno-tree-loop-distribute-patterns -march=rv64im_zicsr_zve64x -mabi=lp64 -o vector_matmul.riscv ./vector_matmul/vector_matmul.c ../application_headers/syscalls.c ../application_headers/crt.S -static -nostdlib -nostartfiles -T ../application_headers/test.ld
riscv64-unknown-elf-objdump --disassemble-all vector_matmul.riscv > vector_matmul.dump
riscv64-unknown-elf-objcopy --dump-section .rodata=vector_matmul_rodata.bin vector_matmul.riscv
riscv64-unknown-elf-objcopy --dump-section .rodata.str1.8=vector_matmul_rodata_str1_8.bin vector_matmul.riscv
riscv64-unknown-elf-objcopy --dump-section .sdata=vector_matmul_sdata.bin vector_matmul.riscv
riscv64-unknown-elf-objcopy --dump-section .text.init=vector_matmul_text_init.bin vector_matmul.riscv
riscv64-unknown-elf-objcopy --dump-section .text=vector_matmul_text.bin vector_matmul.riscv
riscv64-unknown-elf-objcopy --dump-section .text.startup=vector_matmul_text_startup.bin vector_matmul.riscv
hexdump -v -e '1/4 "%08x" "\n"' vector_matmul_rodata.bin > vector_matmul_rodata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_matmul_rodata_str1_8.bin > vector_matmul_rodata_str1_8.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_matmul_sdata.bin > vector_matmul_sdata.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_matmul_text_init.bin > vector_matmul_text_init.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_matmul_text.bin > vector_matmul_text.temp
hexdump -v -e '1/4 "%08x" "\n"' vector_matmul_text_startup.bin > vector_matmul_text_startup.temp
cat vector_matmul_rodata.temp vector_matmul_rodata_str1_8.temp vector_matmul_sdata.temp > vector_matmul_data.hex
cat vector_matmul_text_init.temp vector_matmul_text.temp vector_matmul_text_startup.temp > vector_matmul_inst.hex


rm *.riscv *.bin *.temp