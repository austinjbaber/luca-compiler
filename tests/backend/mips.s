.data
newline: .asciiz "\n"
.align 2
var_1: .space 4
.align 2
var_2: .space 4
.align 2
var_3: .space 4
.align 2
var_4: .space 4
.align 2
var_5: .space 4
var_6: .space 1
.align 2
var_7: .space 16
.align 2
var_8: .space 40008
.align 2
var_9: .space 4
.align 2
var_10: .space 4
.align 2
var_11: .space 4
.text
.globl main
main:
li $t0, 1
sw $t0, var_1
li $t0, 2
sw $t0, var_2
lw $t0, var_1
lw $t1, var_2
add $t2, $t0, $t1
sw $t2, var_3
lw $t0, var_3
sub $t1, $zero, $t0
sw $t1, var_3
lw $t0, var_1
seq $t1, $t0, $zero
sw $t1, var_3
la $t0, var_6
sw $t0, var_4
lw $t0, var_4
lw $t1, var_1
sb $t1, 0($t0)
lw $t0, var_4
lb $t1, 0($t0)
sw $t1, var_5
lw $t0, var_5
lw $t1, var_2
mul $t2, $t0, $t1
sw $t2, var_3
lw $t0, var_5
lw $t1, var_2
div $t0, $t1
mflo $t2
sw $t2, var_3
lw $t0, var_5
lw $t1, var_2
div $t0, $t1
mfhi $t2
sw $t2, var_3
la $t0, var_7
sw $t0, var_4
lw $t0, var_4
lw $t1, var_5
li $t2, 4
mul $t2, $t1, $t2
add $t3, $t0, $t2
sw $t3, var_9
la $t0, var_8
sw $t0, var_4
lw $t0, var_4
addi $t1, $t0, 4
sw $t1, var_10
lw $t0, var_4
li $t2, 40000
add $t1, $t0, $t2
sw $t1, var_11
lw $t0, var_1
lw $t1, var_2
beq $t0, $t1, L10
lw $t0, var_1
lw $t1, var_2
bne $t0, $t1, L10
lw $t0, var_1
lw $t1, var_2
slt $t2, $t0, $t1
bne $t2, $zero, L10
lw $t0, var_1
lw $t1, var_2
slt $t2, $t1, $t0
bne $t2, $zero, L10
lw $t0, var_1
lw $t1, var_2
slt $t2, $t1, $t0
beq $t2, $zero, L10
lw $t0, var_1
lw $t1, var_2
slt $t2, $t0, $t1
beq $t2, $zero, L10
j L20
L10:
j L20
L20:
lw $a0, var_3
li $v0, 1
syscall
li $v0, 4
la $a0, newline
syscall
li $v0, 10
syscall
