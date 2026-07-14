#include <stdlib.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#define DEBUG 0

/*************************************************************/
/* Opcode definitions.                                       */
/*************************************************************/
#define DEF_STRING       0	
#define DEF_GLOBALS      55
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
#define MOD_i            56

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

/*************************************************************/
/* Sizes of instructions and data. The compiler generates    */
/* VM code assuming certain sizes of basic types, typically  */
/* 32 or 64 bits for ints and floats. We've chosen to go with*/
/* 64 bits (8 bytes) uniformly for all types. It makes life  */
/* easier of addresses and integers are the same type, for   */
/* example.                                                  */               
/*************************************************************/

#define INT_TP                long long
#define FLOAT_TP              double
#define ADDR_TP               INT_TP* // not unsigned long

#define ARG_SIZE              8
#define OP_SIZE               1

#define INT_FORMAT            "%lld"
#define FLOAT_FORMAT          "%lf"

#define DEF_STRING_SIZE       (0)
#define DEF_GLOBALS_SIZE      (0)
#define PROG_BEGIN_SIZE       (OP_SIZE)
#define PROG_END_SIZE         (OP_SIZE)
#define PROC_BEGIN_SIZE       (OP_SIZE+2*ARG_SIZE)
#define PROC_END_SIZE         (OP_SIZE+2*ARG_SIZE)

#define PUSHADDR_SIZE         (OP_SIZE+ARG_SIZE)
#define FIELD_SIZE            (OP_SIZE+ARG_SIZE)
#define INDEX_SIZE            (OP_SIZE+2*ARG_SIZE)

// Size of PLUS_i, PLUS_f, etc
#define BINARITH_SIZE         (OP_SIZE)

// Size of UMINUS_i, etc
#define UNARITH_SIZE          (OP_SIZE)

// Size of EQ_i, EQ_f, etc
#define COND_SIZE             (OP_SIZE+ARG_SIZE)

// Size of PUSHCONST_i, etc
#define PUSHCONST_SIZE        (OP_SIZE+ARG_SIZE)

#define LOAD_SIZE             (OP_SIZE)
#define STORE_i_SIZE          (OP_SIZE)
#define UMINUS_i_SIZE         (OP_SIZE)

#define LOAD_f_SIZE           (OP_SIZE)
#define STORE_f_SIZE          (OP_SIZE)

#define TRUNC_SIZE            (OP_SIZE)
#define FLOAT_SIZE            (OP_SIZE)

#define CALL_SIZE             (OP_SIZE+2*ARG_SIZE)
#define ACTUAL_SIZE           (OP_SIZE)

#define JUMP_SIZE             (OP_SIZE+ARG_SIZE)

#define WRITE_SIZE            (OP_SIZE)
#define READ_SIZE             (OP_SIZE)
#define WRITELN_SIZE          (OP_SIZE)

/*************************************************************/
/* Initialization.                                           */
/*************************************************************/
char* op2string[100];

void init() {
      op2string[DEF_STRING     ]  = "DEF_STRING";
      op2string[DEF_GLOBALS    ]  = "DEF_GLOBALS";
      op2string[PROG_BEGIN     ]  = "PROG_BEGIN";
      op2string[PROG_END       ]  = "PROG_END";

      op2string[PUSHADDR_local ]  = "PUSHADDR_local";
      op2string[PUSHADDR_formal]  = "PUSHADDR_formal";
      op2string[PUSHADDR_global]  = "PUSHADDR_global";
      op2string[FIELD          ]  = "FIELD";
      op2string[INDEX          ]  = "INDEX";

      op2string[PLUS_i         ]  = "PLUS_i";
      op2string[MINUS_i        ]  = "MINUS_i";
      op2string[MULT_i         ]  = "MULT_i";
      op2string[DIV_i          ]  = "DIV_i";
      op2string[MOD_i          ]  = "MOD_i";
      op2string[EQ_i           ]  = "EQ_i";
      op2string[GE_i           ]  = "GE_i";
      op2string[GT_i           ]  = "GT_i";
      op2string[LT_i           ]  = "LT_i";
      op2string[LE_i           ]  = "LE_i";
      op2string[NE_i           ]  = "NE_i";
      op2string[PUSHCONST_i    ]  = "PUSHCONST_i";
      op2string[LOAD_i         ]  = "LOAD_i";
      op2string[STORE_i        ]  = "STORE_i";
      op2string[UMINUS_i       ]  = "UMINUS_i";

      op2string[PLUS_f         ]  = "PLUS_f";
      op2string[MINUS_f        ]  = "MINUS_f";
      op2string[MULT_f         ]  = "MULT_f";
      op2string[DIV_f          ]  = "DIV_f";
      op2string[EQ_f           ]  = "EQ_f";
      op2string[GE_f           ]  = "GE_f";
      op2string[GT_f           ]  = "GT_f";
      op2string[LT_f           ]  = "LT_f";
      op2string[LE_f           ]  = "LE_f";
      op2string[NE_f           ]  = "NE_f";
      op2string[PUSHCONST_f    ]  = "PUSHCONST_f";
      op2string[LOAD_f         ]  = "LOAD_f";
      op2string[STORE_f        ]  = "STORE_f";
      op2string[UMINUS_f       ]  = "UMINUS_f";

      op2string[TRUNC          ]  = "TRUNC";
      op2string[FLOAT          ]  = "FLOAT";

      op2string[PROC_BEGIN     ]  = "PROC_BEGIN";
      op2string[PROC_END       ]  = "PROC_END";
      op2string[CALL           ]  = "CALL";
      op2string[ACTUAL_i       ]  = "ACTUAL_i";
      op2string[ACTUAL_f       ]  = "ACTUAL_f";

      op2string[JUMP           ]  = "JUMP";

      op2string[ISA            ]  = "ISA";
      op2string[NARROW         ]  = "NARROW";
      op2string[NEW            ]  = "NEW";

      op2string[WRITE_i        ]  = "WRITE_i";
      op2string[WRITE_f        ]  = "WRITE_f";
      op2string[WRITE_c        ]  = "WRITE_c";
      op2string[WRITE_s        ]  = "WRITE_s";

      op2string[READ_i         ]  = "READ_i";
      op2string[READ_f         ]  = "READ_f";
      op2string[READ_c         ]  = "READ_c";

      op2string[WRITELN        ]  = "WRITELN";
}

/*************************************************************/
/* Read the VM file.                                         */
/* On completion:                                            */
/*    1) prog[] holds the bytecode read from path.           */
/*       While the input program is a pure textfile, the     */
/*       prog array is a binary representation. For example, */
/*       the "PUSHCONST_f 3.14" instruction will add the     */
/*       binary representation of 3.14 somewhere in prog[].  */
/*    2) programPC holds the index in prog[] where           */
/*       "main()" starts, i.e. the index of the PROG_BEGIN   */
/*       instruction.                                        */
/*    3) strings[] holds the constant strings defined in the */
/*       program, i.e. the strings provided in the           */
/*       DEF_STRING instructions.                            */
/*    4) globalSize holds the size of global variables       */
/*       from the DEF_GLOBALS instruction.                   */
/*************************************************************/

int programPC = 0;
char* strings[100];
int globalSize = 0;

#define READ_DEBUG 0

void read(char* path, char prog[]) {
   // printf("OPEN %s\n", path);
   FILE *fptr = fopen(path, "r");
   if (fptr == NULL) {
      printf("Error opening file %s\n", path);
      exit(-1);
   } else {
      if (READ_DEBUG) printf("OPEN %s\n", path);
   }
   char buf[1000];
   char* pc = prog;
   char *dest[10];
   while(1) {
      char* r = fgets(buf, 1000, fptr);
      //if (READ_DEBUG) printf("READ %s\n", buf);
      if (r == NULL) return;
      int i=0;
        char* token = strtok(buf, " \t\r\n"); // needed for windows line endings
        while (token != NULL && i < 10) {
          if (token[0] == '#') break;
          dest[i] = token;
          i++;
           token = strtok(NULL, " \t\r\n"); // needed for windows line endings
        }

       if (i == 0) continue; // skip blank lines and comment lines

       int op;
       sscanf(dest[0], "%d", &op);

       int relativePC = (int)(pc - prog);

       if (op == PROG_BEGIN) {
          programPC = relativePC;
          if (READ_DEBUG) printf("READ PROG_BEGIN: programPC=%i\n", programPC);
       };
       if (op == PROC_BEGIN) {
          if (READ_DEBUG) printf("READ PROC_BEGIN: PC=%i\n", relativePC);
       };
       if (op == PUSHCONST_f) {
          *pc = op;
          pc += OP_SIZE;
          double arg;
          sscanf(dest[1], "%lf", &arg);
          FLOAT_TP* fltpc = (FLOAT_TP*)(pc);
          *fltpc = arg;
          pc += ARG_SIZE;
          if (READ_DEBUG) printf("READ FLOAT PC=%i; ARG = %lf\n", relativePC, arg);
       } else if (op == DEF_STRING) {
         int idx;
         sscanf(dest[1], "%d", &idx);
             int len = (int)strlen(dest[2]) - 2;
             char* string = malloc(len + 1); // malloc(len) doesn't leave space for /0
             strncpy(string, dest[2]+1, len);
             string[len] = 0;
         strings[idx] = string;
         if (READ_DEBUG) printf("READ DEF_STRING: %i \"%s\"\n", idx, string);
       } else if (op == DEF_GLOBALS) {
         int size;
         sscanf(dest[1], "%d", &size);
         globalSize = size;
         if (READ_DEBUG) printf("READ DEF_GLOBALS: globalSize=%i\n", size);
       } else {
          *pc = op;
          if (READ_DEBUG) printf("READ PC=%i; OP = %d: %s\n", relativePC, op, op2string[op]);
          pc += OP_SIZE;
          for(int j=1; j<i; j++) {
             INT_TP arg;
             sscanf(dest[j], "%lld", &arg);
             ADDR_TP intpc = (ADDR_TP)pc;
             *intpc = arg;
             if (READ_DEBUG) printf("    PC=%i; ARG %i = %lld\n",(int) (pc-prog), j, arg);
             pc += ARG_SIZE;
          }
       }
   }
}

/*************************************************************/
/* The interpreter.                                          */
/*************************************************************/
union stackNode {
   char* a;     // Address
   INT_TP i;    // Integer
   FLOAT_TP f;  // Float
};


#define CALL_STACK_SIZE 4096
#define EVAL_STACK_SIZE 100

void runtime_error(const char* message) {
   printf("<RUNTIME_ERROR pos=\"X\" message=\"%s\"/>\n", message);
}

void interpret(char prog[]) {
   union stackNode stack[EVAL_STACK_SIZE];
   int sp = 0;
   int pc = programPC;

   char* globalMem = calloc((size_t)globalSize, 1); // malloc(globalSize)

   while(1) {
      int op = prog[pc];
      if (DEBUG) {
         printf("TRACE pc=%i sp=%i : %s\n", pc, sp, op2string[op]);
         for (int i=0; i<sp; i++) {
            printf("   STACK[%i] = [%p,%lld,%lf]\n", i, stack[i].a, stack[i].i, stack[i].f);
         }
      };
      switch (op) {
         /**********************************************/
         /* Main program declaration.                  */
         /**********************************************/
         case PROG_BEGIN: {
            pc += PROG_BEGIN_SIZE;
            break;
         }	
         case PROG_END: {
            free(globalMem);
            return;
         }	
         case PROC_BEGIN: {
            pc += PROC_BEGIN_SIZE;
            break;
         }
         case PROC_END: {
            pc += PROC_END_SIZE;
            break;
         }
         /**********************************************/
         /* Designators.                               */
         /**********************************************/
         case PUSHADDR_local: {
            // procedures not implemented yet, local/formal addresses use globalMem base
            INT_TP localOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            stack[sp].a = localOffset + globalMem;
            pc += PUSHADDR_SIZE;
            sp++;
            break;
         }
         case PUSHADDR_formal: {
            INT_TP formalOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            stack[sp].a = formalOffset + globalMem;
            pc += PUSHADDR_SIZE;
            sp++;
            break;
         }
         case PUSHADDR_global: {
            INT_TP globalOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            stack[sp].a = globalOffset + globalMem;
            pc += PUSHADDR_SIZE;
            sp++; 
            break;
         }
         case FIELD : {
            INT_TP fieldOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            stack[sp-1].a = stack[sp-1].a + fieldOffset;
            pc += FIELD_SIZE;
            break;
         }
         case INDEX: {
            INT_TP elementSize = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            INT_TP elementCount = (*(INT_TP*)(&prog[pc+OP_SIZE+ARG_SIZE]));
            INT_TP index = stack[sp-1].i;
            if (index < 0 || index >= elementCount) {
               runtime_error("Array index out of range");
               free(globalMem);
               return;
            }
            // [baseAddr, idx] -> [baseAddr + elementSize*idx]
            stack[sp-2].a = stack[sp-2].a + (elementSize * index);
            sp--;
            pc += INDEX_SIZE;
            break;
         }
         /**********************************************/
         /* Integer arithmetic.                        */
         /**********************************************/
         case PLUS_i: {
            stack[sp-2].i = stack[sp-2].i + stack[sp-1].i;
            sp--;
            pc += BINARITH_SIZE;
            break;
         }
         case MINUS_i: {
            stack[sp-2].i = stack[sp-2].i - stack[sp-1].i;
            sp--;
            pc += BINARITH_SIZE;
            break;
         }
         case MULT_i: {
            stack[sp-2].i = stack[sp-2].i * stack[sp-1].i;
            sp--;
            pc += BINARITH_SIZE;
            break;
         }
         case DIV_i: {
            if (stack[sp-1].i == 0) {
               runtime_error("Division by zero");
               free(globalMem);
               return;
            }
            stack[sp-2].i = stack[sp-2].i / stack[sp-1].i;
            sp--;
            pc += BINARITH_SIZE;
            break;
         }
         case MOD_i: {
            if (stack[sp-1].i == 0) {
               runtime_error("Division by zero");
               free(globalMem);
               return;
            }
            stack[sp-2].i = stack[sp-2].i % stack[sp-1].i;
            sp--;
            pc += BINARITH_SIZE;
            break;
         }
         case EQ_i: {
            // conditional offsets relative to current pc
            INT_TP branchOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            INT_TP b = stack[sp-1].i;
            INT_TP a = stack[sp-2].i;
            sp -= 2;
            if (a == b) {
               pc += branchOffset;
            } else {
               pc += COND_SIZE;
            }
            break;
         }
         case GE_i: {
            INT_TP branchOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            INT_TP b = stack[sp-1].i;
            INT_TP a = stack[sp-2].i;
            sp -= 2;
            if (a >= b) {
               pc += branchOffset;
            } else {
               pc += COND_SIZE;
            }
            break;
         }
         case GT_i: {
            INT_TP branchOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            INT_TP b = stack[sp-1].i;
            INT_TP a = stack[sp-2].i;
            sp -= 2;
            if (a > b) {
               pc += branchOffset;
            } else {
               pc += COND_SIZE;
            }
            break;
         }
         case LT_i: {
            INT_TP branchOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            INT_TP b = stack[sp-1].i;
            INT_TP a = stack[sp-2].i;
            sp -= 2;
            if (a < b) {
               pc += branchOffset;
            } else {
               pc += COND_SIZE;
            }
            break;
         }
         case LE_i: {
            INT_TP branchOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            INT_TP b = stack[sp-1].i;
            INT_TP a = stack[sp-2].i;
            sp -= 2;
            if (a <= b) {
               pc += branchOffset;
            } else {
               pc += COND_SIZE;
            }
            break;
         }
         case NE_i: {
            INT_TP branchOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            INT_TP b = stack[sp-1].i;
            INT_TP a = stack[sp-2].i;
            sp -= 2;
            if (a != b) {
               pc += branchOffset;
            } else {
               pc += COND_SIZE;
            }
            break;
         }
         case PUSHCONST_i: {
            INT_TP value = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            stack[sp].i = value;
            pc += PUSHCONST_SIZE;
            sp++;
            break;
         }
         case LOAD_i: {
            stack[sp-1].i = (*(INT_TP*)stack[sp-1].a);
            pc += LOAD_SIZE;
            break;
         }
         case STORE_i: {
            // use stack order [address, value], then pop both
            (*(INT_TP*)stack[sp-2].a) = stack[sp-1].i;
            pc += STORE_i_SIZE;
            sp -= 2;
            break;
         }
         case UMINUS_i: {
            stack[sp-1].i = -stack[sp-1].i;
            pc += UMINUS_i_SIZE;
            break;
         }
         /**********************************************/
         /* Float arithmetic.                          */
         /**********************************************/
         case PLUS_f: {
            stack[sp-2].f = stack[sp-2].f + stack[sp-1].f;
            sp--;
            pc += BINARITH_SIZE;
            break;
         }
         case MINUS_f: {
            stack[sp-2].f = stack[sp-2].f - stack[sp-1].f;
            sp--;
            pc += BINARITH_SIZE;
            break;
         }
         case MULT_f: {
            stack[sp-2].f = stack[sp-2].f * stack[sp-1].f;
            sp--;
            pc += BINARITH_SIZE;
            break;
         }
         case DIV_f: {
            if (stack[sp-1].f == 0.0) {
               runtime_error("Division by zero");
               free(globalMem);
               return;
            }
            stack[sp-2].f = stack[sp-2].f / stack[sp-1].f;
            sp--;
            pc += BINARITH_SIZE;
            break;
         }
         case EQ_f: {
            INT_TP branchOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            FLOAT_TP b = stack[sp-1].f;
            FLOAT_TP a = stack[sp-2].f;
            sp -= 2;
            if (a == b) {
               pc += branchOffset;
            } else {
               pc += COND_SIZE;
            }
            break;
         }
         case GE_f: {
            INT_TP branchOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            FLOAT_TP b = stack[sp-1].f;
            FLOAT_TP a = stack[sp-2].f;
            sp -= 2;
            if (a >= b) {
               pc += branchOffset;
            } else {
               pc += COND_SIZE;
            }
            break;
         }
         case GT_f: {
            INT_TP branchOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            FLOAT_TP b = stack[sp-1].f;
            FLOAT_TP a = stack[sp-2].f;
            sp -= 2;
            if (a > b) {
               pc += branchOffset;
            } else {
               pc += COND_SIZE;
            }
            break;
         }
         case LT_f: {
            INT_TP branchOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            FLOAT_TP b = stack[sp-1].f;
            FLOAT_TP a = stack[sp-2].f;
            sp -= 2;
            if (a < b) {
               pc += branchOffset;
            } else {
               pc += COND_SIZE;
            }
            break;
         }
         case LE_f: {
            INT_TP branchOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            FLOAT_TP b = stack[sp-1].f;
            FLOAT_TP a = stack[sp-2].f;
            sp -= 2;
            if (a <= b) {
               pc += branchOffset;
            } else {
               pc += COND_SIZE;
            }
            break;
         }
         case NE_f: {
            INT_TP branchOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            FLOAT_TP b = stack[sp-1].f;
            FLOAT_TP a = stack[sp-2].f;
            sp -= 2;
            if (a != b) {
               pc += branchOffset;
            } else {
               pc += COND_SIZE;
            }
            break;
         }
         case PUSHCONST_f: {
            FLOAT_TP value = (*(FLOAT_TP*)(&prog[pc+OP_SIZE]));
            stack[sp].f = value;
            pc += PUSHCONST_SIZE;
            sp++;
            break;
         }
         case LOAD_f: {
            stack[sp-1].f = (*(FLOAT_TP*)stack[sp-1].a);
            pc += LOAD_f_SIZE;
            break;
         }
         case STORE_f: {
            (*(FLOAT_TP*)stack[sp-2].a) = stack[sp-1].f;
            pc += STORE_f_SIZE;
            sp -= 2;
            break;
         }
         case UMINUS_f: {
            stack[sp-1].f = -stack[sp-1].f;
            pc += UNARITH_SIZE;
            break;
         }
         /**********************************************/
         /* TRUNC/FLOAT.                               */
         /**********************************************/
         case TRUNC: {
             stack[sp-1].i = (INT_TP)stack[sp-1].f;
             pc += TRUNC_SIZE;
             break;
         }
         case FLOAT: {
             stack[sp-1].f = (FLOAT_TP)stack[sp-1].i;
             pc += FLOAT_SIZE;
             break;
         }
         /**********************************************/
         /* PROCEDURE CALL.                            */
         /**********************************************/
         case CALL: { // TODO
            free(globalMem);
            return;
            break;
         }
         case ACTUAL_i: {
            pc += ACTUAL_SIZE;
            break;
         }
         case ACTUAL_f: {
            pc += ACTUAL_SIZE;
            break;
         }
         /**********************************************/
         /* Branches.                                  */
         /**********************************************/
         case JUMP: {
            // backward branches for loops, forward branches for conditionals
            INT_TP jumpOffset = (*(INT_TP*)(&prog[pc+OP_SIZE]));
            pc += jumpOffset;
            break;
         }
         /**********************************************/
         /* IO.                                        */
         /**********************************************/
         case WRITE_i: {
            printf(INT_FORMAT, stack[sp-1].i);
            pc += WRITE_SIZE;
            sp--;
            break;
         }
         case WRITE_f: {
            printf(FLOAT_FORMAT, stack[sp-1].f);
            pc += WRITE_SIZE;
            sp--;
            break;
         }
         case WRITE_c: {
            printf("%c", (char)stack[sp-1].i);
            pc += WRITE_SIZE;
            sp--;
            break;
         }
         case WRITE_s: {
            INT_TP stringIndex = stack[sp-1].i;
            printf("%s", strings[stringIndex]);
            pc += WRITE_SIZE;
            sp--;
            break;
         }
         case READ_i: {
            INT_TP value;
            scanf(INT_FORMAT, &value);
            (*(INT_TP*)stack[sp-1].a) = value;
            pc += READ_SIZE;
            sp--;
            break;
         }
         case READ_f: {
            FLOAT_TP value;
            scanf(FLOAT_FORMAT, &value);
            (*(FLOAT_TP*)stack[sp-1].a) = value;
            pc += READ_SIZE;
            sp--;
            break;
         }
         case READ_c: {
            char value;
            // leading space skips newlines before reading
            scanf(" %c", &value);
            (*(INT_TP*)stack[sp-1].a) = (INT_TP)value;
            pc += READ_SIZE;
            sp--;
            break;
         }
         case WRITELN: {
            printf("\n");
            pc += WRITELN_SIZE;
            break;
         }
         /**********************************************/
         /* OO.  IGNORE.                               */
         /**********************************************/
         case ISA: {
            pc += OP_SIZE;
            break;
         }
         case NARROW: {
            pc += OP_SIZE;
            break;
         }
         case NEW: {
            pc += OP_SIZE;
            break;
         }
      };
   }
}

/*************************************************************/
/* Main.                                                     */
/*************************************************************/
int main(int argc, char**argv) {
   init();
   char prog[10000];
   read(argv[1], prog);
   interpret(prog);
   return 0;
}
