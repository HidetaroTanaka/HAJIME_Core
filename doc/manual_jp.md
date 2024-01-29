# HAJIME CORE マニュアル
著：Hidetaro Tanaka (https://github.com/HidetaroTanaka)

## はじめに
HAJIME Coreとは，**H**ighly **A**d**J**usted **IM**plementation for **E**mbedded Coreの略であるが，名前をHAJIMEにするために適当に並べた英単語のため，何の略かは今後変更される可能性がある．

## リポジトリ構造
- `.github`: workflow用のファイル等
- `fpga`: FPGA実装に必要なファイルが入っている．Vivado 2023.2でNexys4向けにプロジェクトを作成し，全てのファイルを入れて`fpga.v`をトップにすれば良い．
- `project`: 謎．`chisel-template`に入っていたので何かに必要．
- `src`: ソースファイル，テストファイル，テスト用RISC-Vプログラム等が入っている．
- `submodule`: `riscv-tests`がサブモジュールとして入っている．README通りにpullすればよい．

## コア共通仕様

## スカラコアの仕様
WIP

## ベクトルコアの仕様