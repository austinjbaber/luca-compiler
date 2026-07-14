	.data
newline:	.asciiz "\n"
	.text
	.align 2
	.globl main
main:
	li	$s0,1
	move	$a0,$s0
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
	b	L99
	li	$s0,999
	move	$a0,$s0
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
L99:
	li	$s0,2
	move	$a0,$s0
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
	li	$v0,10
	syscall

