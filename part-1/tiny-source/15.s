	.data
newline:	.asciiz "\n"
	.text
	.align 2
	.globl main
main:
	li	$s0,10
	li	$s1,3
	sub	$s2,$s0,$s1
	li	$s3,2
	sub	$s4,$s2,$s3
	move	$a0,$s4
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
	li	$v0,10
	syscall

