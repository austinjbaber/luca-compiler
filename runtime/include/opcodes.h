#ifndef LUCA_OPCODES_H
#define LUCA_OPCODES_H

/* Serialized Luca VM opcodes. These numeric values are part of the bytecode
 * format and must remain stable across the compiler and every runtime. */
#define DEF_STRING       0
#define PROG_BEGIN       1
#define PROG_END         2

#define PUSHADDR_local   3
#define PUSHADDR_formal  4
#define PUSHADDR_global  5
#define FIELD            6
#define INDEX            7

#define PLUS_i           8
#define MINUS_i          9
#define MULT_i           10
#define DIV_i            11
#define EQ_i             12
#define GE_i             13
#define GT_i             14
#define LT_i             15
#define LE_i             16
#define NE_i             17
#define PUSHCONST_i      18
#define LOAD_i           19
#define STORE_i          20
#define UMINUS_i         21

#define PLUS_f           22
#define MINUS_f          23
#define MULT_f           24
#define DIV_f            25
#define EQ_f             26
#define GE_f             27
#define GT_f             28
#define LT_f             29
#define LE_f             30
#define NE_f             31
#define PUSHCONST_f      32
#define LOAD_f           33
#define STORE_f          34
#define UMINUS_f         35

#define TRUNC            36
#define FLOAT            37
#define PROC_BEGIN       38
#define PROC_END         39
#define CALL             40
#define JUMP             41
#define ACTUAL_i         42
#define ACTUAL_f         43

#define WRITE_i          44
#define WRITE_f          45
#define WRITE_c          46
#define WRITE_s          47
#define READ_i           48
#define READ_f           49
#define READ_c           50
#define WRITELN          51

#define ISA              52
#define NARROW           53
#define NEW              54
#define DEF_GLOBALS      55
#define MOD_i            56

#define LUCA_OPCODE_COUNT 57

#endif
