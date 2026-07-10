/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

import java.lang.*;
import java.io.*;

public class Lex {
    LineNumberReader str;   // input stream
    char ch;                // lookahead character
    boolean done = false;   // reached end-of-file

    public Lex(String filename) throws IOException {
        str = new LineNumberReader(new FileReader(filename));
        get();
    }

    int pos() {return str.getLineNumber();}

    // read the next input character
    void get() {
        try {
           int r = str.read();
           ch = (char)r;
           if (r == -1) done=true;
        } catch (Exception e) {
           done=true;
        }
    }

    // We've found the beginning of a literal integer. Continue
    // scanning it and convert the resulting string to an int.
    Token scanNumber() {
        String s = ""; 
        int ival = -1;        
        while ((!done) && Character.isDigit(ch)) {s+=ch; get();}
        try {
           ival = Integer.parseInt(s.toString());
        } catch (NumberFormatException e) {
           System.err.println("not an integer");
        }
        return new Token(Token.INTLIT, pos(), ival);
    }

    // We've found the beginning of an identifier or keyword.
    // Continue scanning until the end is found, check if 
    // the string's a keyword, otherwise return the IDENT token.
    Token scanName()  {
        String ident = "";
        while ((!done) && Character.isLetterOrDigit(ch)) {ident+=ch; get();}
        if (ident.equals("BEGIN"))      return new Token(Token.BEGIN, pos());
        else if (ident.equals("END"))   return new Token(Token.END, pos());
        else if (ident.equals("PRINT")) return new Token(Token.PRINT, pos());
        else if (ident.equals("IF")) return new Token(Token.IF, pos());
        else if (ident.equals("GOTO")) return new Token(Token.GOTO, pos());
        else                            return new Token(Token.IDENT, pos(), ident);
    }

    // Used by the parser to get the next token. EOF will
    // be the last token generated.
    public Token nextToken() {
        while ((!done) && ch <= ' ') get();  // scan over whitespace
        if (done) return new Token(Token.EOF, pos());
        switch (ch) {
            case '+': get(); return new Token(Token.PLUS, pos()); 
            case ';': get(); return new Token(Token.SEMICOLON, pos());
            case '=': get(); return new Token(Token.EQUAL, pos());
            case '-': get(); return new Token(Token.MINUS, pos());
            case '<': get(); return new Token(Token.LT, pos());
            case ':': get(); return new Token(Token.COLON, pos());
            default:  if (Character.isLetter(ch)) return scanName();
                      else if (Character.isDigit(ch)) return scanNumber();
                      else {
                          System.err.println("illegal character " + ch);
                          get(); return new Token(Token.ILLEGAL, pos()); 
                      }
        }
    }

    public static void main (String args[]) throws IOException{
       Lex scanner = new Lex(args[0]);
       while(true) {
          Token token = scanner.nextToken();
          System.out.println(token.toString());
          if (token.kind == Token.EOF) return;
       }
    }
}
