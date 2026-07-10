	.data
newline:	.asciiz "\n"
var0:		.word 0
var1:		.word 0
var2:		.word 0
	.text
	.align 2
	.globl main
main:
	li	$s0,5
	sw	$s0,var0
	li	$s0,99
	sw	$s0,var1
	lw	$s0,var1
	lw	$s1,var0
	add	$s2,$s0,$s1
	li	$s3,9
	add	$s4,$s2,$s3
	sw	$s4,var2
	lw	$s0,var2
	move	$a0,$s0
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
	li	$v0,10
	syscall

