package lexer;

import java.lang.*;
import java.io.*;

public class Input {
   LineNumberReader str;   // input stream
   int c;

   public Input(String filename) throws IOException {
      str = new LineNumberReader(new FileReader(filename));
      advance();
   }

   int position() {
      return str.getLineNumber()+1;
   }

   int current() {return c;}

   void advance() {
       try {
          c = str.read();
       } catch (Exception e) {
          c= -1;
       }
   }
}