	.data
newline:	.asciiz "\n"
var0:		.word 0
	.text
	.align 2
	.globl main
main:
	li	$s0,1
	sw	$s0,var0
	lw	$s0,var0
	bne	$s0,$zero,L10
	li	$s0,111
	move	$a0,$s0
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
	b	L20
L10:
	li	$s0,222
	move	$a0,$s0
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
L20:
	li	$v0,10
	syscall

