	.data
newline:	.asciiz "\n"
	.text
	.align 2
	.globl main
main:
	li	$s0,5
	li	$s1,10
	slt	$s2,$s0,$s1
	move	$a0,$s2
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
	li	$s0,10
	li	$s1,5
	slt	$s2,$s0,$s1
	move	$a0,$s2
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
	li	$v0,10
	syscall

