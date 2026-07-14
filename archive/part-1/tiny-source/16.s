	.data
newline:	.asciiz "\n"
	.text
	.align 2
	.globl main
main:
L22:
	b	L22
	li	$s0,1
	li	$s1,2
	slt	$s2,$s0,$s1
	bne	$s2,$zero,L22
	li	$v0,10
	syscall

