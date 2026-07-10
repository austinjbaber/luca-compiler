package parser;

import java.io.*;
import java.lang.*;
import lexer.*;

public class Parse {

   Lex scanner;
   Token curr;
   String parseTraceFileName;

   public Parse(Lex s, String t) {
      scanner = s;
      parseTraceFileName = t;
      curr = scanner.nextToken();
      openTraceFile();
   };

   /********************************************************/
   /*                        Tracing                       */
   /********************************************************/
   int level = 0;
   BufferedWriter traceFile;
   boolean traceToStdout = false; // see openTraceFile(), closeTraceFile(), and TRACE()
   StringBuilder traceBuf = new StringBuilder();

   void openTraceFile () {
      if (parseTraceFileName == null) {
         traceToStdout = true;
         traceFile = null;
         return;
      }

      // input validation
      try {
         File file = new File(parseTraceFileName);
         traceFile = new BufferedWriter(new FileWriter(file));
      } catch (IOException e) {
         System.out.print(e.getMessage() + "\n");
         System.exit(0);
      }
   }

   void closeTraceFile(boolean success) {
   // added a flag to indicate whether to output trace or not for syntax errors
      if (traceToStdout) {
         if (success) {
            System.out.print(traceBuf.toString());
            try { System.out.flush(); } catch (Exception e) { /* ignore */ }
         }
         return;
      }

      if (traceFile != null) {
         try {
            traceFile.close();
         } catch (IOException e) {
            System.out.print(e.getMessage() + "\n");
            System.exit(0);
         }
      }
   }

   void TRACE(String x, String e, String y, boolean args) {
      // added logic to build trace string if tracing to stdout
      // otherwise write directly to file
      try {
         StringBuilder sb = new StringBuilder();
         for (int i=0; i<level; i++) {
            sb.append("   ");
         }
         sb.append(x).append(e);
         if (args) {
            sb.append(" token=\"" + Token.token2string(curr.getKind()) + "\"")
              .append(" line=\"" + curr.getPosition() + "\"");
         }
         sb.append(y);

         if (traceToStdout) {
            traceBuf.append(sb.toString()).append("\n");
         } else if (traceFile != null) {
            traceFile.write(sb.toString());
            traceFile.newLine();
         }
      } catch (IOException ex) {
         System.out.print(ex.getMessage() + "\n");
         System.exit(0);
      }
   }

   void ENTER(String e) {
      TRACE("<", e, ">", true);
      level++;
   }

   void EXIT(String e) {
      level--;
      TRACE("</", e, ">", false);
   }

   void MATCH() {
     TRACE("<", "MATCH", "/>", true);
   }

   /********************************************************/
   /*                        Matching                      */
   /********************************************************/
   boolean lookahead(int tok) {
        return curr.getKind() == tok;
   }

   boolean lookahead(TokenSet toks) {
      return toks.member(curr);
   }

   void match(int expected) {
      match(new TokenSet(expected));
   }

   void match(TokenSet expected) {
      if (lookahead(expected)) {
         Token next = scanner.nextToken();
         MATCH();
         curr = next;
      } else
         error(expected);
   }

   void error(TokenSet expected) {
      String err = "<SYNTAX_ERROR " +
                   "pos=\"" + curr.getPosition() + "\" " +
                   "\n   expected=\"" + expected.toString(false) + "\" " +
                   "\n   found=\"" + Token.token2stringPrint(curr.getKind()) + "\"/>";
      closeTraceFile(false);
      System.out.print(err + "\n");
      System.exit(0);
   }

   /********************************************************/
   /*                FIRST and FOLLOW sets                 */
   /********************************************************/
   // declarations
   TokenSet decl_FIRST = new TokenSet(new int[]{
      Token.CONST, Token.VAR, Token.TYPE, Token.PROCEDURE
   });
   TokenSet decl_FOLLOW = new TokenSet(Token.BEGIN);

   // statements
   TokenSet statement_FIRST = new TokenSet(new int[]{
      Token.IDENT, Token.IF, Token.WHILE, Token.REPEAT,
      Token.LOOP, Token.EXIT, Token.WRITE, Token.WRITELN,
      Token.READ
   });

   // expressions
   TokenSet expr_FIRST = new TokenSet(new int[]{
      Token.MINUS, Token.NOT, Token.TRUNC, Token.FLOAT,
      Token.LPAREN, Token.INTLIT, Token.CHARLIT,
      Token.REALLIT, Token.STRINGLIT, Token.IDENT
   });
   TokenSet expr_FOLLOW = new TokenSet(new int[]{
      Token.SEMICOLON, Token.OF, Token.THEN, Token.DO,
      Token.COMMA, Token.RPAREN, Token.RBRACK
   });

   TokenSet addop_FIRST = new TokenSet(new int[]{
      Token.PLUS, Token.MINUS
   });
   TokenSet mulop_FIRST = new TokenSet(new int[]{
      Token.STAR, Token.SLASH, Token.PERCENT
   });
   TokenSet logicop_FIRST = new TokenSet(new int[]{
      Token.AND, Token.OR
   });
   TokenSet relop_FIRST = new TokenSet(new int[]{
      Token.LT, Token.LE, Token.EQ,
      Token.NE, Token.GE, Token.GT
   });

   // follow sets for designator, expression, and right paren/bracket
   TokenSet designator_prime_FIRST = new TokenSet(new int[]{
      Token.LBRACK, Token.PERIOD
   });

   TokenSet expr_prime_FOLLOW = expr_FOLLOW;
   TokenSet term_prime_FOLLOW = addop_FIRST.union(expr_FOLLOW);
   TokenSet logical_prime_FOLLOW = mulop_FIRST.union(term_prime_FOLLOW);
   TokenSet relational_prime_FOLLOW = logicop_FIRST.union(logical_prime_FOLLOW);

   TokenSet designator_prime_FOLLOW = new TokenSet(new int[]{
      Token.COLONEQ, Token.LPAREN
   }).union(relop_FIRST).union(logicop_FIRST)
     .union(mulop_FIRST).union(addop_FIRST).union(expr_FOLLOW);

   TokenSet rparen_FOLLOW = new TokenSet(Token.RPAREN);
   TokenSet rbrack_FOLLOW = new TokenSet(Token.RBRACK);

   /********************************************************/
   /*                      Program                         */
   /********************************************************/
   public void program () {
      ENTER("PROGRAM");
      match(Token.PROGRAM);
      match(Token.IDENT);
      match(Token.SEMICOLON);
      decls();
      block();
      match(Token.PERIOD);
      match(Token.EOF);
      EXIT("PROGRAM");
      closeTraceFile(true);
   }

   /********************************************************/
   /*                     Declarations                     */
   /********************************************************/

   // <decl_list> ::= <declaration> ';' <decl_list> | epsilon
   void decls() {
      ENTER("decls");
      if (lookahead(decl_FIRST)) {
         declaration();
         match(Token.SEMICOLON);
         decls();
      } else if (lookahead(decl_FOLLOW)) {
         // epsilon
      } else {
         error(decl_FIRST.union(decl_FOLLOW));
      }
      EXIT("decls");
   }

   void declaration() {
      ENTER("declaration");
      if (lookahead(Token.CONST)) {
         constDecl();
      } else if (lookahead(Token.VAR)) {
         varDecl();
      } else if (lookahead(Token.TYPE)) {
         typeDecl();
      } else if (lookahead(Token.PROCEDURE)) {
         procDecl();
      } else {
         error(decl_FIRST);
      }
      EXIT("declaration");
   }

   void constDecl() {
      ENTER("const");
      match(Token.CONST);
      match(Token.IDENT);
      match(Token.COLON);
      match(Token.IDENT);
      match(Token.EQ);
      expr();
      EXIT("const");
   }

   void varDecl() {
      ENTER("var");
      match(Token.VAR);
      match(Token.IDENT);
      match(Token.COLON);
      match(Token.IDENT);
      EXIT("var");
   }

   void typeDecl() {
      ENTER("type");
      match(Token.TYPE);
      match(Token.IDENT);
      match(Token.EQ);
      if (lookahead(Token.ARRAY)) {
         match(Token.ARRAY);
         expr();
         match(Token.OF);
         match(Token.IDENT);
      } else if (lookahead(Token.RECORD)) {
         match(Token.RECORD);
         match(Token.LBRACK);
         fieldListOpt();
         match(Token.RBRACK);
      } else {
         error(new TokenSet(new int[]{Token.ARRAY, Token.RECORD}));
      }
      EXIT("type");
   }

   void fieldListOpt() {
      ENTER("field_list_opt");
      if (lookahead(Token.IDENT)) {
         fieldList();
      } else if (lookahead(rbrack_FOLLOW)) {
         // epsilon
      } else {
         error(new TokenSet(Token.IDENT).union(rbrack_FOLLOW));
      }
      EXIT("field_list_opt");
   }

   void fieldList() {
      ENTER("field_list");
      field();
      if (lookahead(Token.SEMICOLON)) {
         match(Token.SEMICOLON);
         fieldList();
      }
      EXIT("field_list");
   }

   void field() {
      ENTER("field");
      match(Token.IDENT);
      match(Token.COLON);
      match(Token.IDENT);
      EXIT("field");
   }

   void procDecl() {
      ENTER("procedure");
      match(Token.PROCEDURE);
      match(Token.IDENT);
      match(Token.LPAREN);
      formalListOpt();
      match(Token.RPAREN);
      match(Token.SEMICOLON);
      //decls();
      block();
      EXIT("procedure");
   }

   void formalListOpt() {
      ENTER("formal_list_opt");
      if (lookahead(Token.VAR) || lookahead(Token.IDENT)) {
         formalList();
      } else if (lookahead(rparen_FOLLOW)) {
         // epsilon
      } else {
         error(new TokenSet(new int[]{Token.VAR, Token.IDENT}).union(rparen_FOLLOW));
      }
      EXIT("formal_list_opt");
   }

   void formalList() {
      ENTER("formal_list");
      formalParam();
      if (lookahead(Token.SEMICOLON)) {
         match(Token.SEMICOLON);
         formalList();
      }
      EXIT("formal_list");
   }

   void formalParam() {
      ENTER("formal_param");
      if (lookahead(Token.VAR)) {
         match(Token.VAR);
         match(Token.IDENT);
         match(Token.COLON);
         match(Token.IDENT);
      } else if (lookahead(Token.IDENT)) {
         match(Token.IDENT);
         match(Token.COLON);
         match(Token.IDENT);
      } else {
         error(new TokenSet(new int[]{Token.VAR, Token.IDENT}));
      }
      EXIT("formal_param");
   }

   /********************************************************/
   /*                      Statements                      */
   /********************************************************/

   void block() {
      ENTER("block");
      match(Token.BEGIN);
      statseq();
      match(Token.END);
      EXIT("block");
   }

   // <stat_seq> ::= <statement> ';' <stat_seq> | epsilon
   void statseq() {
      ENTER("statseq");
      // one or more statements
      while (lookahead(statement_FIRST)) {
         statement();
         match(Token.SEMICOLON);
         statseq();
      }
      EXIT("statseq");
   }

   void statement() {
      // this one got a little beefy with multiple lookaheads
      ENTER("statement");
      if (lookahead(Token.IDENT)) {
         designator();
         if (lookahead(Token.COLONEQ)) {
            ENTER("assign");
            match(Token.COLONEQ);
            expr();
            EXIT("assign");
         } else if (lookahead(Token.LPAREN)) {
            ENTER("call");
            match(Token.LPAREN);
            actualListOpt();
            match(Token.RPAREN);
            EXIT("call");
         } else {
            error(new TokenSet(new int[]{Token.COLONEQ, Token.LPAREN}));
         }
      } else if (lookahead(Token.IF)) {
         ifStmt();
      } else if (lookahead(Token.WHILE)) {
         whileStmt();
      } else if (lookahead(Token.REPEAT)) {
         repeatStmt();
      } else if (lookahead(Token.LOOP)) {
         loopStmt();
      } else if (lookahead(Token.EXIT)) {
         ENTER("exit");
         match(Token.EXIT);
         EXIT("exit");
      } else if (lookahead(Token.WRITE)) {
         ENTER("write");
         match(Token.WRITE);
         expr();
         EXIT("write");
      } else if (lookahead(Token.WRITELN)) {
         ENTER("writeln");
         match(Token.WRITELN);
         EXIT("writeln");
      } else if (lookahead(Token.READ)) {
         ENTER("read");
         match(Token.READ);
         designator();
         EXIT("read");
      } else {
         error(statement_FIRST);
      }
      EXIT("statement");
   }

   void ifStmt() {
      ENTER("if");
      match(Token.IF);
      expr();
      match(Token.THEN);
      statseq();
      if (lookahead(Token.ELSE)) {
         match(Token.ELSE);
         statseq();
      }
      match(Token.ENDIF);
      EXIT("if");
   }

   void whileStmt() {
      ENTER("while");
      match(Token.WHILE);
      expr();
      match(Token.DO);
      statseq();
      match(Token.ENDDO);
      EXIT("while");
   }

   void repeatStmt() {
      ENTER("repeat");
      match(Token.REPEAT);
      statseq();
      match(Token.UNTIL);
      expr();
      EXIT("repeat");
   }

   void loopStmt() {
      ENTER("loop");
      match(Token.LOOP);
      statseq();
      match(Token.ENDLOOP);
      EXIT("loop");
   }

   void actualListOpt() {
      ENTER("actual_list_opt");
      if (lookahead(expr_FIRST)) {
         actualList();
      } else if (lookahead(rparen_FOLLOW)) {
         // epsilon
      } else {
         error(expr_FIRST.union(rparen_FOLLOW));
      }
      EXIT("actual_list_opt");
   }

   void actualList() {
      ENTER("actual_list");
      expr();
      if (lookahead(Token.COMMA)) {
         match(Token.COMMA);
         actualList();
      }
      EXIT("actual_list");
   }

   /********************************************************/
   /*                     Expressions                      */
   /********************************************************/

   void expr() {
      ENTER("expr");
      term();
      expr_prime();
      EXIT("expr");
   }

   void expr_prime() {
      ENTER("expr_prime");
      if (lookahead(Token.PLUS)) {
         match(Token.PLUS);
         term();
         expr_prime();
      } else if (lookahead(Token.MINUS)) {
         match(Token.MINUS);
         term();
         expr_prime();
      } else if (lookahead(expr_prime_FOLLOW)) {
         // epsilon
      } else {
         error(addop_FIRST.union(expr_prime_FOLLOW));
      }
      EXIT("expr_prime");
   }

   void term() {
      ENTER("term");
      logical();
      term_prime();
      EXIT("term");
   }

   void term_prime() {
      ENTER("term_prime");
      if (lookahead(Token.STAR)) {
         match(Token.STAR);
         logical();
         term_prime();
      } else if (lookahead(Token.SLASH)) {
         match(Token.SLASH);
         logical();
         term_prime();
      } else if (lookahead(Token.PERCENT)) {
         match(Token.PERCENT);
         logical();
         term_prime();
      } else if (lookahead(term_prime_FOLLOW)) {
         // epsilon
      } else {
         error(mulop_FIRST.union(term_prime_FOLLOW));
      }
      EXIT("term_prime");
   }

   void logical() {
      ENTER("logical");
      relational();
      logical_prime();
      EXIT("logical");
   }

   void logical_prime() {
      ENTER("logical_prime");
      if (lookahead(Token.AND)) {
         match(Token.AND);
         relational();
         logical_prime();
      } else if (lookahead(Token.OR)) {
         match(Token.OR);
         relational();
         logical_prime();
      } else if (lookahead(logical_prime_FOLLOW)) {
         // epsilon
      } else {
         error(logicop_FIRST.union(logical_prime_FOLLOW));
      }
      EXIT("logical_prime");
   }

   void relational() {
      ENTER("relational");
      factor();
      relational_prime();
      EXIT("relational");
   }

   void relational_prime() {
      ENTER("relational_prime");
      if (lookahead(relop_FIRST)) {
         match(relop_FIRST);
         factor();
         relational_prime();
      } else if (lookahead(relational_prime_FOLLOW)) {
         // epsilon
      } else {
         error(relop_FIRST.union(relational_prime_FOLLOW));
      }
      EXIT("relational_prime");
   }

   void factor() {
      // unary operators need to re‑enter factor after matching operator
      ENTER("factor");
      if (lookahead(Token.MINUS)) {
         match(Token.MINUS);
         factor();
      } else if (lookahead(Token.NOT)) {
         match(Token.NOT);
         factor();
      } else if (lookahead(Token.TRUNC)) {
         match(Token.TRUNC);
         factor();
      } else if (lookahead(Token.FLOAT)) {
         match(Token.FLOAT);
         factor();
      } else if (lookahead(Token.LPAREN)) {
         match(Token.LPAREN);
         expr();
         match(Token.RPAREN);
      } else if (lookahead(Token.INTLIT)) {
         match(Token.INTLIT);
      } else if (lookahead(Token.CHARLIT)) {
         match(Token.CHARLIT);
      } else if (lookahead(Token.REALLIT)) {
         match(Token.REALLIT);
      } else if (lookahead(Token.STRINGLIT)) {
         match(Token.STRINGLIT);
      } else if (lookahead(Token.IDENT)) {
         designator();
      } else {
         error(expr_FIRST);
      }
      EXIT("factor");
   }

   /********************************************************/
   /*                     Designator                       */
   /********************************************************/

   void designator() {
      ENTER("designator");
      match(Token.IDENT);
      designator_prime();
      EXIT("designator");
   }

   void designator_prime() {
      ENTER("designator_prime");
      if (lookahead(Token.LBRACK)) {
         match(Token.LBRACK);
         expr();
         match(Token.RBRACK);
         designator_prime();
      } else if (lookahead(Token.PERIOD)) {
         match(Token.PERIOD);
         match(Token.IDENT);
         designator_prime();
      } else if (lookahead(designator_prime_FOLLOW)) {
         // epsilon
      } else {
         error(designator_prime_FIRST.union(designator_prime_FOLLOW));
      }
      EXIT("designator_prime");
   }

   /********************************************************/
   /*                     Main                             */
   /********************************************************/

   public static void main (String args[]) throws IOException{
      if (args.length < 1 || args[0] == null) {
         throw new IOException("Missing input file");
      }

      // write trace to second argument, else stdout
      String traceFile = null;
      if (args.length >= 2) {
         traceFile = args[1];
      }

      Lex scanner = new Lex(args[0]);
      Parse parser = new Parse(scanner, traceFile);
      parser.program();
   }
}
