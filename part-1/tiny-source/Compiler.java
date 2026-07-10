/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

import java.io.*;
import java.util.*;

public class Compiler {

    public static void main (String args[]) throws IOException {
        String file = args[0];
        Lex scanner = new Lex(file);       // Lexical Analysis
        Parse parser = new Parse(scanner); // Syntactic Analysis
        Sem sem = new Sem(parser.ast);     // Semantic Analysis
        GenIR ir = new GenIR(sem);         // Intermediate Code Generation
        GenMips mips = new GenMips(ir.code);     // MIPS Code Generation
        System.out.println(mips.code);     // Print generated code
   }

}
