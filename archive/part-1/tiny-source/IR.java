/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

import java.lang.*;
import java.io.*;

public class IR {

   public static final int ADD    = 0;	
   public static final int LOAD   = 1;	
   public static final int STORE  = 2;	
   public static final int PUSH   = 3;	
   public static final int PRINT  = 4;	
   public static final int PRINTLN= 5;	
   public static final int HEADER = 6;	
   public static final int EXIT   = 7;
   public static final int MUL    = 8;
   public static final int SUB    = 9;
   public static final int LT     = 10;
   public static final int LABEL  = 11;
   public static final int BRA    = 12;
   public static final int BRNE0  = 13;
   public static final int MAGIC  = 42;

   // Read an IR program from file. There is one integer
   // code per line.
   public static int[] read(String filename) {
	int[] code = new int[100];
        int pc = 0;
	try {
           BufferedReader str = new BufferedReader(new FileReader(filename));
           while(true)
              code[pc++] = Integer.parseInt(str.readLine());
        } catch (Exception e) {
	}
        return code;
    }

    // Write an IR program to standard out, one integer code
    // per line.
    public static void write(int code[], int pc) {
	for(int i=0; i<pc; i++) 
	    System.out.println(code[i]);
    }

}
