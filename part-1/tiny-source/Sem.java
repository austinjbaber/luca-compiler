/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

import java.io.*;
import java.util.*;

public class Sem {
    AST ast;
    HashSet<Integer> definedLabels = new HashSet<>();
    HashSet<Integer> targetLabels = new HashSet<>();
    public SyTab sytab = new SyTab();

    public Sem (AST ast) {
       this.ast = ast;
       program((PROGRAM) ast);
    }

    // Start walking the AST at the root, PROGRAM, node.
    void program(PROGRAM n) {
        stats(n.stats);
        for (int x : targetLabels) {
            if (!definedLabels.contains(x)) {
                System.err.println("Error: Label " + x + " undefined");
            }
        }
    }

    // Recursively walk a sequence a statements. NULL indicates
    // the end of the sequence.
    void stats(STATSEQ n) {
       if (n instanceof NULL) return;
       stat(n.stat);
       stats(n.next);
    }

    // Walk assignment, print, label, goto, or if statements.
    void stat(STAT n) {
       if (n instanceof ASSIGN) {
           assign((ASSIGN) n);
       } else if (n instanceof PRINT) {
           print((PRINT) n);
       } else if (n instanceof LABEL) {
           int label = ((LABEL) n).label;
           if (definedLabels.contains(label)) {
               System.err.println("Error: Label " + label + " already exists (duplicate labels not allowed)");
           } else {
               definedLabels.add(((LABEL) n).label);
           }
       } else if (n instanceof GOTO) {
           targetLabels.add(((GOTO) n).label);
       } else if (n instanceof IF) {
           expr(((IF) n).condition);
           targetLabels.add(((IF) n).label);
       }
    }

    // Insert the identifier on the left hand side of the
    // assignment statement into the symbol table, if it's
    // not already there.
    void assign(ASSIGN n) {
       sytab.insert(n.ident);
       expr(n.expr);
    }

    // Walk a print statement.
    void print(PRINT n) {
       expr(n.expr);
    }

    // Walk an arithmetic expression.
    void expr(EXPR n) {
       if (n instanceof IDENT)
           ident((IDENT) n);
       else if (n instanceof INTLIT)
           intlit((INTLIT) n);
       else if (n instanceof BINOP)
           binop((BINOP) n);
    }

    // If an identifier in an expression has not been assigned
    // to before it's used, issue an error message.
    void ident(IDENT n) {
       if (sytab.lookup(n.ident) < 0)
          System.err.println("Identifier not declared: " + n.ident);
    }

    // Walk an integer literal.
    void intlit(INTLIT n) {
    }

    // Walk a binary arithmetic operator.
    void binop(BINOP n) {
       expr(n.left);
       expr(n.right);
    }

    public static void main (String args[]) throws IOException{
        Lex scanner = new Lex(args[0]);
        Parse parser = new Parse(scanner);
        Sem sem = new Sem(parser.ast);
    }
}
