	.data
newline:	.asciiz "\n"
	.text
	.align 2
	.globl main
main:
	b	L20
L10:
	li	$s0,1
	move	$a0,$s0
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
	b	L30
L20:
	li	$s0,2
	move	$a0,$s0
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
	b	L10
L30:
	li	$s0,3
	move	$a0,$s0
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
	li	$v0,10
	syscall

