	.file	"selection_sort.c"
	.option nopic
	.attribute arch, "rv64i2p1_m2p0_a2p1_f2p2_d2p2_c2p0_v1p0_zicsr2p0_zifencei2p0_zve32f1p0_zve32x1p0_zve64d1p0_zve64f1p0_zve64x1p0_zvl128b1p0_zvl32b1p0_zvl64b1p0"
	.attribute unaligned_access, 0
	.attribute stack_align, 16
	.text
	.align	1
	.globl	selection_sort
	.type	selection_sort, @function
selection_sort:
	li	a5,1
	ble	a1,a5,.L1
	lw	t4,0(a0)
	li	t5,1
.L3:
	addi	t3,a0,4
	mv	a5,t3
	mv	a2,t4
	li	a4,1
	li	a7,0
	j	.L5
.L10:
	addiw	a4,a4,1
	add	a6,a0,t1
	addi	a5,a5,4
	beq	a1,a4,.L9
.L5:
	lw	a3,0(a5)
	slli	t1,a7,2
	mv	a6,a5
	ble	a2,a3,.L10
	mv	a7,a4
	addiw	a4,a4,1
	mv	a2,a3
	addi	a5,a5,4
	bne	a1,a4,.L5
.L9:
	sw	a2,0(a0)
	sw	t4,0(a6)
	addiw	a1,a1,-1
	mv	a0,t3
	beq	a1,t5,.L1
	lw	t4,0(t3)
	j	.L3
.L1:
	ret
	.size	selection_sort, .-selection_sort
	.section	.text.startup,"ax",@progbits
	.align	1
	.globl	main
	.type	main, @function
main:
	addi	sp,sp,-16
	lui	a0,%hi(.LANCHOR0)
	sd	s0,0(sp)
	li	a1,32
	addi	s0,a0,%lo(.LANCHOR0)
	addi	a0,a0,%lo(.LANCHOR0)
	sd	ra,8(sp)
	call	selection_sort
	lw	a4,0(s0)
	addi	a5,s0,4
	addi	a2,s0,128
	li	a0,1
.L13:
	mv	a3,a4
	lw	a4,0(a5)
	addi	a5,a5,4
	ble	a3,a4,.L12
	li	a0,0
.L12:
	bne	a5,a2,.L13
	ld	ra,8(sp)
	ld	s0,0(sp)
	xori	a0,a0,1
	addi	sp,sp,16
	jr	ra
	.size	main, .-main
	.globl	array
	.data
	.align	3
	.set	.LANCHOR0,. + 0
	.type	array, @object
	.size	array, 128
array:
	.word	14
	.word	7
	.word	4
	.word	11
	.word	28
	.word	8
	.word	20
	.word	13
	.word	2
	.word	21
	.word	3
	.word	6
	.word	22
	.word	12
	.word	19
	.word	29
	.word	16
	.word	31
	.word	10
	.word	5
	.word	15
	.word	17
	.word	9
	.word	0
	.word	30
	.word	18
	.word	26
	.word	27
	.word	1
	.word	24
	.word	25
	.word	23
	.ident	"GCC: () 12.2.0"
