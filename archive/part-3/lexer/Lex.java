package lexer;

import java.io.*;
import java.lang.*;

public class Lex {
   Input input;

   // after first error, no recovery (just EOF)
   private boolean halted = false;

   public Lex(String filename) throws IOException {
      input = new Input(filename);
   }

   public Token nextToken () {
      if (halted) {
         return new Token(Token.EOF, input.position(), "");
      }

      while (true) {
         LexTable.position = input.position();
         LexTable.value = new StringBuilder();

         int state = LexTable.S_START;

         while (true) {
            int curr = input.current();
            int clas = classify(curr);
            int next = LexTable.NEXTSTATE[state][clas];
            if (next == -1) break;

            if (LexTable.ADVANCE[state][clas]) {
               if (curr != -1) {
                  LexTable.value.append((char)curr);
                  input.advance();
               }
            }
            state = next;
         }

         if (LexTable.ACCEPT[state]) {
            int kind = LexTable.TOKEN[state];

            if (kind == LexTable.SKIP) continue;

            if (kind == Token.ERROR_UNTERMINATED_STRING ||
                kind == Token.ERROR_REALLIT ||
                kind == Token.ERROR_ILLEGAL_CHARACTER ||
                kind == Token.ERROR_UNTERMINATED_COMMENT ||
                kind == Token.ERROR_UNTERMINATED_CHAR ||
                kind == Token.ERROR_EMPTY_CHAR) {
               halted = true;
               while (input.current() != -1) input.advance(); // get out
               return new Token(kind, LexTable.tokenStart(), "");
            }

            if (kind == Token.EOF) {
               return new Token(Token.EOF, input.position(), "");
            }

            String text = LexTable.tokenValue();

            if (kind == Token.IDENT) {
               int keyword = Token.keyword(text);
               if (keyword != Token.IDENT) {
                  return new Token(keyword, LexTable.tokenStart(), "");
               }
               return new Token(Token.IDENT, LexTable.tokenStart(), text);
            }

            if (kind == Token.INTLIT || kind == Token.REALLIT) {
               return new Token(kind, LexTable.tokenStart(), text);
            }

            if (kind == Token.STRINGLIT) { // get rid of quotes
               String val;
               if (text.length() >= 2 && text.charAt(0) == '"' &&
                   text.charAt(text.length() - 1) == '"') {
                  val = text.substring(1, text.length() - 1);
               } else {
                  val = "";
               }
               return new Token(Token.STRINGLIT, LexTable.tokenStart(), val);
            }

            if (kind == Token.CHARLIT) { // get rid of quotes
               String val;
               if (text.length() == 3 && text.charAt(0) == '\'' &&
                   text.charAt(2) == '\'') {
                  val = text.substring(1, 2);
               } else {
                  val = "";
               }
               return new Token(Token.CHARLIT, LexTable.tokenStart(), val);
            }

            return new Token(kind, LexTable.tokenStart(), "");
         }

         // else not accepting w/ no next state
         int startLine = LexTable.tokenStart();
         if (input.current() == -1){
            return new Token(Token.EOF, input.position(), "");
         } 
         input.advance();
         halted = true;
         while (input.current() != -1) input.advance(); // get out
         return new Token(Token.ERROR_ILLEGAL_CHARACTER, startLine, "");
      }
   }

   // only 0 thru 255
   private static int classify(int c) {
      if (c == '\r') return Char.WHITESPACE;
      if (c < 0) return Char.EOF;
      if (c > 255) return Char.ILLEGAL;
      return Char.classify(c);
   }

   public static void main (String args[]) throws IOException{
      Lex scanner = new Lex(args[0]);
      System.out.print("<block>\n");
      while(true) {
         Token token = scanner.nextToken();
         System.out.print("   " + token.toString() + "\n");
         if (token.kind == Token.EOF) break;
      }
      System.out.print("</block>\n");
   }
}
