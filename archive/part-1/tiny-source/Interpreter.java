/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

import java.lang.*;
import java.util.HashMap;

public class Interpreter {

   // Evaluation stack.
   static int[] stack = new int[100];
   static int sp = 0;
   static void push (int v) {stack[sp++] = v;}
   static int pop() {return stack[--sp]; }

  static void run (int[] prog) throws Exception {
      // first pass to look for labels and add to map
      HashMap<Integer, Integer> labelToPc = new HashMap<>();
      int pc = 0;
      while (true) {
          int op = prog[pc];
          if (op == IR.LABEL) {
              int lab = prog[pc + 1];
              labelToPc.put(lab, pc); // jump to LABEL instruction
              pc += 2;
          } else if (op == IR.BRA || op == IR.BRNE0 || op == IR.LOAD || op == IR.STORE || op == IR.PUSH) {
              pc += 2;
          } else if (op == IR.HEADER) {
              pc += 3; // similar to the provided code below
          } else if (op == IR.ADD || op == IR.MUL || op == IR.SUB || op == IR.LT || op == IR.PRINT || op == IR.PRINTLN) {
              pc += 1;
          } else if (op == IR.EXIT) {
              break;
          } else {
              System.err.println("Error: Unrecognized instruction: " + op);
              System.exit(1);
          }
      }

      int[] memory=null;
      pc = 0; // reset to execute
      while (true) {
          switch (prog[pc]) {
          case IR.HEADER    : {
             if (prog[pc+1]!=IR.MAGIC) {
                 System.err.println("Wrong magic number.");
                 throw new Exception();
             }
             memory = new int[prog[pc+2]];
             pc+=3; break;
          }
          case IR.ADD    : {
             int right = pop(); int left  = pop(); push(left+right); pc++; break;
          }
          case IR.MUL    : {
             int right = pop(); int left  = pop(); push(left*right); pc++; break;
          }
          case IR.LOAD   : {
             push(memory[(int)prog[pc+1]]); pc+=2; break;
          }
          case IR.STORE  : {
             memory[prog[pc+1]] = pop(); pc+=2; break;
          }
          case IR.PUSH  : {
             push(prog[pc+1]); pc+=2; break;
          }
          case IR.PRINT  : {
             System.out.print(pop()); pc++; break;
          }
          case IR.PRINTLN: {
             System.out.println(); pc++; break;
          }
          case IR.EXIT   : {
            return;
          }
          case IR.SUB    : {
            int right = pop(); int left  = pop(); push(left-right); pc++; break;
          }
          case IR.LT : {
              int right = pop(); int left  = pop();
              if (left < right) push(1); else push(0);
              pc++; break;
          }
          case IR.LABEL: {
              pc +=2; // skip over LABEL
              break;
          }
          case IR.BRA: {
              int label = prog[pc + 1]; // this is the label we want
              Integer target = labelToPc.get(label); // this is where the label is
              if (target == null) {
                  System.err.println("Error: Undefined label" + label);
                  System.exit(1);
              }
              pc = target;
              break;
          }
          case IR.BRNE0: {
              int label = prog[pc + 1]; // this is the label we want
              int condition = pop(); // grab condition
              if (condition != 0) {
                  Integer target = labelToPc.get(label); // this is where the label is
                  if (target == null) {
                      System.err.println("Error: Undefined label" + label);
                      System.exit(1);
                  }
                  pc = target;
              } else {
                      pc +=2; // if ==0, continue
              }
              break;
          }
          default :
              System.err.println("Illegal instruction: " + prog[pc]);
              throw new Exception();
          }
      }
   }

    public static void main(String args[]) throws Exception{
        int[] code = IR.read(args[0]);
        run(code);
    }
}
