	.data
newline:	.asciiz "\n"
	.text
	.align 2
	.globl main
main:
	li	$s0,3
	li	$s1,2
	slt	$s2,$s0,$s1
	bne	$s2,$zero,L10
	li	$s0,44
	move	$a0,$s0
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
	b	L20
L10:
	li	$s0,99
	move	$a0,$s0
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
L20:
	li	$v0,10
	syscall

