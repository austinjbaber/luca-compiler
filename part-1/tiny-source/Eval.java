/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */
import java.io.*;
import java.util.*;

public class Eval {
    Sem sem;
    int[] memory;        // Variable store.
    ArrayList<STAT> statements; // store STATEQS with label suppport
    HashMap<Integer, Integer> labelToIndex; // map labels to indices

    public Eval (Sem sem) {
       this.sem = sem;
       program((PROGRAM) sem.ast);
    }

    // Start evaluating an AST at the root, PROGRAM, node.
    // We must have performed semantic analysis before
    // the evaluation, so that variables have been assigned
    // identifier numbers. These numbers are used to index
    // 'memory', an array that holds current variable values.
    void program(PROGRAM n) {
       memory = new int[sem.sytab.size()];
       // stats(n.stats); no longer using a LL
        statements = new ArrayList<>();
        labelToIndex = new HashMap<>();
        flatten(n.stats); // traverse stats to fill statements and labelToIndex
//        System.out.println(statements.size());
//        System.out.println(labelToIndex);
        run();
//        System.out.println("done running");

    }

//    void stats(STATSEQ n) {
//       if (n instanceof NULL) return;
//       stat(n.stat);
//       stats(n.next);
//    }
//
//    void stat(STAT n) {
//       if (n instanceof ASSIGN)
//          assign((ASSIGN)n);
//       else if (n instanceof PRINT)
//          print((PRINT)n);
//    }

    // Evaluate the expression, and assign the result to
    // the appropriate variable in 'memory'.
    void assign(ASSIGN n) {
        int v = expr(n.expr);
        memory[sem.sytab.lookup(n.ident)] = v;
    }

    // Evaluate the expression, and print the result.
    void print(PRINT n) {
       int v = expr(n.expr);
       System.out.println(v);
    }

    // Evaluate an expression.
    int expr(EXPR n) {
       if (n instanceof IDENT)
           return ident((IDENT) n);
       else if (n instanceof INTLIT)
           return intlit((INTLIT) n);
       else if (n instanceof BINOP)
           return binop((BINOP) n);
       return -1;
    }

    // Look up the identifier number, and return the current
    // value from the memory cell.
    int ident(IDENT n) {
        return memory[sem.sytab.lookup(n.ident)];
    }

    int intlit(INTLIT n) {
        return n.val;
    }

    // Evaluate an binary arithmetic expression.
   int binop(BINOP n) {
      int l = expr(n.left);
      int r = expr(n.right);
      if (n.OP == Token.PLUS) {
          return l + r;
      } else if (n.OP == Token.MINUS) {
          return l - r;
      } else if (n.OP == Token.LT) {
          if (l < r) return 1; else return 0;
      } else {
          return -1;
      }
    }


    // recursive helper method that turns STATSEQ into an arraylist, and saves
    // label/index data in the meantime
    void flatten(STATSEQ n) {
        if (n instanceof NULL) {
            return;
        }
        int index = statements.size();
        statements.add(n.stat);

        if (n.stat instanceof LABEL) {
            labelToIndex.put(((LABEL)n.stat).label, index);
        }
        flatten(n.next);
    }

    void run() {
        int pc = 0;

        while (pc < statements.size()) {
            STAT s = statements.get(pc);

            if (s instanceof ASSIGN) {
                assign((ASSIGN) s);
                pc++;
            }  else if (s instanceof PRINT) {
                print((PRINT) s);
                pc++;
            } else if (s instanceof LABEL) {
                pc++; // no-op
            } else if (s instanceof GOTO) {
                int label = ((GOTO) s).label;
                Integer target = labelToIndex.get(label);
                if (target == null) {
                    System.err.println("Error: Undefined label in GOTO: " + label);
                    System.exit(1);
                }
                pc = target; // goto label's index
            } else if (s instanceof IF) {
                IF f = (IF) s;
                int cond = expr(f.condition); // evaluate if condition
                if (cond != 0) {
                    Integer target = labelToIndex.get(f.label);
                    if (target == null) {
                        System.err.println("Undefined label in IF GOTO: " + f.label);
                        System.exit(1);
                    }
                    pc = target; // goto label's index
                } else {
                    pc++; // no-op
                }
            } else {
                System.err.println("Unknown STAT type at pc=" + pc + ": " + s.getClass());
            }
        }
    }


    public static void main (String args[]) throws Exception{
       Lex scanner = new Lex(args[0]);
       Parse parser = new Parse(scanner);
       Sem sem = new Sem(parser.ast);
       Eval eval = new Eval(sem);
    }
}


