HAJIME Core
=======================

**H**ighly **A**d**J**usted **IM**plementation for **E**mbedded Core (I actually took this name from random Visual Novel game)

The core's goal is to provide some RISC-V implementation for embedded environment. It currently supports RV64IM_Zicsr (only multiply of M) and subset of Zve64x.

This repository requires [`riscv-gnu-toolchain`](https://github.com/riscv-collab/riscv-gnu-toolchain) with Vector Extension enabled (`-march=rv64gcv`) to be installed if you want to run your own RISC-V program.

`verilator` and `iverilog` are also needed for some tests.

```bash
$ sudo apt install verilator iverilog
```

### Building this repository
Use sbt (IntelliJ is recommended). And fetch submodules as this:
```bash
$ git clone https://github.com/HidetaroTanaka/HAJIME_Core
$ cd HAJIME_Core
$ git submodule update --init --recursive
```

If above command doesn't work, run these commands to pull `riscv-tests` manually.

```bash
$ cd submodules
# Run this command if `riscv-tests` directory exists.
$ sudo rm -r ./riscv-tests
$ git clone https://github.com/riscv-software-src/riscv-tests
$ cd riscv-tests
$ git submodule update --init --recursive
```

Firtool is also required to generate SystemVerilog file. Reference: https://github.com/chipsalliance/chisel/blob/main/SETUP.md

```bash
$ wget -q -O - https://github.com/llvm/circt/releases/download/firtool-1.38.0/firrtl-bin-ubuntu-20.04.tar.gz | tar -zx
$ sudo mv firtool-1.38.0/bin/firtool /usr/local/bin/
```

### FPGA Synthesis
Nexys4 (specify board as `xc7a100tcsg324-3`) is supported. Just throw everything on `./fpga` to Vivado, and set `fpga.v` as top (NOT `top.v`).

The memory file contains instruction and data memory of `src/main/resources/applications_fpga/vector_matmul/vector_matmul.c`.
