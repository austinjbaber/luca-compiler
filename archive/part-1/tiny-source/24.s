	.data
newline:	.asciiz "\n"
var0:		.word 0
	.text
	.align 2
	.globl main
main:
	li	$s0,0
	sw	$s0,var0
L10:
	lw	$s0,var0
	move	$a0,$s0
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
	lw	$s0,var0
	li	$s1,1
	add	$s2,$s0,$s1
	sw	$s2,var0
	lw	$s0,var0
	li	$s1,4
	slt	$s2,$s0,$s1
	bne	$s2,$zero,L10
	li	$v0,10
	syscall

