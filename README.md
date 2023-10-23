HAJIME Core
=======================

**H**ighly **A**d**J**usted **IM**plementation for **E**mbedded Core (I actually took this name from random Visual Novel game)

The core's goal is to provide some RISC-V implementation (SMT and Vector is planned in my imagination) for embedded environment.

This repository requires [`riscv-gnu-toolchain`](https://github.com/riscv-collab/riscv-gnu-toolchain) to be installed if you want to run your own RISC-V program.

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

Firtool is also required to generate SystemVerilog file. Reference: https://github.com/chipsalliance/chisel/blob/main/SETUP.md

```bash
$ wget -q -O - https://github.com/llvm/circt/releases/download/firtool-1.38.0/firrtl-bin-ubuntu-20.04.tar.gz | tar -zx
$ sudo mv firtool-1.38.0/bin/firtool /usr/local/bin/
```
