	.data
newline:	.asciiz "\n"
	.text
	.align 2
	.globl main
main:
	li	$s0,0
	bne	$s0,$zero,L45
	li	$s0,99
	move	$a0,$s0
	li	$v0,1
	syscall
	la	$a0,newline
	li	$v0,4
	syscall
L45:
	li	$v0,10
	syscall

