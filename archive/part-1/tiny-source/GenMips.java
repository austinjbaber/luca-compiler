/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

import java.lang.*;

public class GenMips {
    int[] prog;

    public GenMips(int[] prog) {
       this.prog = prog;
       translate();
    }

   // Collection of free registers.
   static String[] regs = {"$s0","$s1","$s2","$s3","$s4","$s5","$s6",
                           "$t0","$t1","$t2","$t3","$t4","$t5","$t6"};
   int nextReg = 0;

   // Return all used registers, and start allocating them from scratch.
   void initRegs() {
      nextReg = 0; 
   }

    // Return the next free register.
   String freeReg() {
      return regs[nextReg++];
   }

   // Register stack.
   String[] stack = new String[100];
   int sp = 0;
   void push (String v) {stack[sp++] = v;}
   String pop() {return stack[--sp]; }

   // The generated assembly code is stored in a string 'code'.
   public String code = "";
   void add(String instr) {
      code += instr + "\n";
   }

   void translate() {
      int pc = 0;
      initRegs();
      while (true) {
      switch (prog[pc]) {
         case IR.HEADER    : {
            add("\t.data");
            add("newline:\t.asciiz \"\\n\"");
            int vars = prog[pc+2];
            for(int i=0; i<vars; i++)
                add("var" + i + ":\t\t.word 0");
            pc+=3;
            add("\t.text"); 
            add("\t.align 2"); 
            add("\t.globl main"); 
            add("main:"); 
            break;
          }   
          case IR.ADD    : {
             String right = pop();              // The register holding the left hand side.
             String left  = pop();              // The register holding the right hand side.
             String res   = freeReg();          // The register to hold the result.
             add("\tadd\t" + res + "," + left + "," + right);
             push(res);
             pc++; break;
          }   
          case IR.LOAD   : {
             String id = "var" + prog[pc+1];    // The variable identifier.
             String reg = freeReg();            // The register to hold variable value.
             push(reg);
             add("\tlw\t" + reg + "," + id);    // Load the variable into the register.
             pc+=2; break;
          }
          case IR.STORE  : {
             String id = "var" + prog[pc+1];    // The variable identifier.
             String reg = pop();                // The register holding the variable value.
             add("\tsw\t" + reg + "," + id);    // Store the register value into the variable.
             pc+=2; 
             initRegs();
             break;
          }
          case IR.PUSH  : {
             int val = prog[pc+1];              // The integer literal value.
             String res   = freeReg();          // The register to hold the result.
             add("\tli\t" + res + "," + val);   // Load the integer into the register.
             push(res);
             pc+=2; break;
          }
          case IR.PRINT  : {
             String reg = pop();                // The register holding the expression value.
             add("\tmove\t$a0," + reg);         // Move into $a0.
             add("\tli\t$v0,1"); 
             add("\tsyscall");                  // Print the value.
             pc++; 
             initRegs();
             break;
          } 
          case IR.PRINTLN: {
             add("\tla\t$a0,newline"); 
             add("\tli\t$v0,4"); 
             add("\tsyscall"); 
             pc++; break;
          } 
          case IR.EXIT   : {
             add("\tli\t$v0,10"); 
             add("\tsyscall"); 
             return;
          }
          case IR.SUB    : {
              String right = pop();              // The register holding the left hand side.
              String left  = pop();              // The register holding the right hand side.
              String res   = freeReg();          // The register to hold the result.
              add("\tsub\t" + res + "," + left + "," + right);
              push(res);
              pc++; break;
          }
          case IR.LT     : {
              String right = pop();              // The register holding the left hand side.
              String left  = pop();              // The register holding the right hand side.
              String res   = freeReg();          // The register to hold the result.
              add("\tslt\t" + res + "," + left + "," + right);
              push(res);
              pc++; break;
          }
          case IR.LABEL  : {
              int label = prog[pc+1];
              add("L" + label + ":"); // L <label> :
              pc += 2;
              initRegs();
              break;
          }
          case IR.BRA    : {
              int label = prog[pc+1];
              add("\tb\tL" + label); // b L <label>
              pc += 2;
              initRegs();
              break;
          }
          case IR.BRNE0  : {
              int label = prog[pc+1];
              String condition = pop(); // condition value
              add("\tbne\t" + condition + ",$zero,L" + label);   // or: bne cond,$zero,L<label>
              pc += 2;
              initRegs();
              break;
          }
          default : 
        }
     }
  }

   public static void main(String args[]) throws Exception{
      int[] code = IR.read(args[0]);
      GenMips mips = new GenMips(code);
      System.out.println(mips.code);
   }
}
