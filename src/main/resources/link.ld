OUTPUT_ARCH( "riscv" )
ENTRY(_start)

SECTIONS
{
  . = 0x00000000;
  .text.init : { *(.text.init) }
  .text : { *(.text) }
  . = ALIGN(0x4000);
  .data : { *(.data) }
  .bss : { *(.bss) }
  . = ALIGN(0x10000000);
  .tohost : { *(.tohost) }
  _end = .;
}

