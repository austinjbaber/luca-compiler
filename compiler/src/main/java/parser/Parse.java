package parser;

import ast.*;
import java.io.*;
import java.lang.*;
import lexer.*;

public class Parse {

   Lex scanner;
   Token curr;
   // String parseTraceFileName;

   public Parse(Lex s) {
      scanner = s;
      // parseTraceFileName = t;
      curr = scanner.nextToken();
      // openTraceFile();
   };

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
         curr = scanner.nextToken();
      } else
         error(expected);
   }

   Token consume(int expected) {
      Token t = curr;
      match(expected);
      return t;
   }

   Token consume(TokenSet expected) {
      Token t = curr;
      match(expected);
      return t;
   }

   void error(TokenSet expected) {
      // report first syntax error and stop with no recovery
      String err = "<SYNTAX_ERROR " +
                   "pos=\"" + curr.getPosition() + "\" " +
                   "\n   expected=\"" + expected.toString(false) + "\" " +
                   "\n   found=\"" + Token.token2stringPrint(curr.getKind()) + "\"/>";
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
   TokenSet statseq_FOLLOW = new TokenSet(new int[]{
      Token.END, Token.ELSE, Token.ENDIF, Token.UNTIL,
      Token.ENDDO, Token.ENDLOOP
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
   TokenSet relational_op_FIRST = relop_FIRST.union(new TokenSet(Token.ISA));

   // follow sets for designator, expression, and right paren/bracket
   TokenSet designator_prime_FIRST = new TokenSet(new int[]{
      Token.LBRACK, Token.PERIOD
   });

   // follow checks allow epsilon and reject bad trailing tokens after a valid prefix
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
   public PROGRAM program () {
      Token programToken = consume(Token.PROGRAM);
      Token ident = consume(Token.IDENT);
      consume(Token.SEMICOLON);
      DECLS decls = decls();
      consume(Token.BEGIN);
      STATS stats = statseq();
      consume(Token.END);
      consume(Token.PERIOD);
      consume(Token.EOF);
      return new PROGRAM(ident.getValue(), decls, stats, programToken.getPosition());
   }

   /********************************************************/
   /*                     Declarations                     */
   /********************************************************/

   // <decl_list> ::= <declaration> ';' <decl_list> | epsilon
   DECLS decls() {
      if (lookahead(decl_FIRST)) {
         DECLARATION left = declaration();
         Token semi = consume(Token.SEMICOLON);
         DECLS right = decls();
         return new DECLS(left, right, semi.getPosition());
      } else if (lookahead(decl_FOLLOW)) {
         return new DECLNULL(curr.getPosition());
      } else {
         error(decl_FIRST.union(decl_FOLLOW));
         return new DECLNULL(curr.getPosition());
      }
   }

   DECLARATION declaration() {
      if (lookahead(Token.CONST)) {
         return constDecl();
      } else if (lookahead(Token.VAR)) {
         return varDecl();
      } else if (lookahead(Token.TYPE)) {
         return typeDecl();
      } else if (lookahead(Token.PROCEDURE)) {
         return procDecl();
      } else {
         error(decl_FIRST);
         return new VARDECL("", "", curr.getPosition());
      }
   }

   CONSTDECL constDecl() {
      Token tok = consume(Token.CONST);
      Token ident = consume(Token.IDENT);
      consume(Token.COLON);
      Token type = consume(Token.IDENT);
      consume(Token.EQ);
      EXPRESSION expr = expr();
      return new CONSTDECL(ident.getValue(), type.getValue(), expr, tok.getPosition());
   }

   VARDECL varDecl() {
      Token tok = consume(Token.VAR);
      Token ident = consume(Token.IDENT);
      consume(Token.COLON);
      Token type = consume(Token.IDENT);
      return new VARDECL(ident.getValue(), type.getValue(), tok.getPosition());
   }

   DECLARATION typeDecl() {
      Token tok = consume(Token.TYPE);
      Token ident = consume(Token.IDENT);
      consume(Token.EQ);
      if (lookahead(Token.ARRAY)) {
         consume(Token.ARRAY);
         EXPRESSION count = expr();
         consume(Token.OF);
         Token elemType = consume(Token.IDENT);
         return new ARRAYDECL(ident.getValue(), count, elemType.getValue(), tok.getPosition());
      } else if (lookahead(Token.RECORD)) {
         consume(Token.RECORD);
         consume(Token.LBRACK);
         DECLS fields = fieldListOpt();
         consume(Token.RBRACK);
         return new RECORDDECL(ident.getValue(), fields, tok.getPosition());
      } else {
         error(new TokenSet(new int[]{Token.ARRAY, Token.RECORD}));
         return new VARDECL("", "", curr.getPosition());
      }
   }

   DECLS fieldListOpt() {
      if (lookahead(Token.IDENT)) {
         return fieldList();
      } else if (lookahead(rbrack_FOLLOW)) {
         return new DECLNULL(curr.getPosition());
      } else {
         error(new TokenSet(Token.IDENT).union(rbrack_FOLLOW));
         return new DECLNULL(curr.getPosition());
      }
   }

   DECLS fieldList() {
      DECLARATION left = field();
      if (lookahead(Token.SEMICOLON)) {
         Token semi = consume(Token.SEMICOLON);
         DECLS right = fieldList();
         return new DECLS(left, right, semi.getPosition());
      }
      // no semicolon means this is the final field in the list
      return new DECLS(left, new DECLNULL(curr.getPosition()), left.position);
   }

   FIELDDECL field() {
      Token ident = consume(Token.IDENT);
      consume(Token.COLON);
      Token type = consume(Token.IDENT);
      return new FIELDDECL(ident.getValue(), type.getValue(), ident.getPosition());
   }

   PROCDECL procDecl() {
      Token tok = consume(Token.PROCEDURE);
      Token ident = consume(Token.IDENT);
      consume(Token.LPAREN);
      DECLS formals = formalListOpt();
      consume(Token.RPAREN);
      consume(Token.SEMICOLON);
      DECLS decls = decls();
      consume(Token.BEGIN);
      STATS stats = statseq();
      consume(Token.END);
      return new PROCDECL(ident.getValue(), formals, decls, stats, tok.getPosition());
   }

   DECLS formalListOpt() {
      if (lookahead(Token.VAR) || lookahead(Token.IDENT)) {
         return formalList();
      } else if (lookahead(rparen_FOLLOW)) {
         return new DECLNULL(curr.getPosition());
      } else {
         error(new TokenSet(new int[]{Token.VAR, Token.IDENT}).union(rparen_FOLLOW));
         return new DECLNULL(curr.getPosition());
      }
   }

   DECLS formalList() {
      DECLARATION left = formalParam();
      if (lookahead(Token.SEMICOLON)) {
         Token semi = consume(Token.SEMICOLON);
         DECLS right = formalList();
         return new DECLS(left, right, semi.getPosition());
      }
      // no semicolon means this is the last formal parameter
      return new DECLS(left, new DECLNULL(curr.getPosition()), left.position);
   }

   FORMALDECL formalParam() {
      if (lookahead(Token.VAR)) {
         Token tok = consume(Token.VAR);
         Token ident = consume(Token.IDENT);
         consume(Token.COLON);
         Token type = consume(Token.IDENT);
         return new FORMALDECL(ident.getValue(), type.getValue(), "VAR", tok.getPosition());
      } else if (lookahead(Token.IDENT)) {
         Token ident = consume(Token.IDENT);
         consume(Token.COLON);
         Token type = consume(Token.IDENT);
         return new FORMALDECL(ident.getValue(), type.getValue(), "VAL", ident.getPosition());
      } else {
         error(new TokenSet(new int[]{Token.VAR, Token.IDENT}));
         return new FORMALDECL("", "", "VAL", curr.getPosition());
      }
   }

   /********************************************************/
   /*                      Statements                      */
   /********************************************************/

   // <stat_seq> ::= <statement> ';' <stat_seq> | epsilon
   STATS statseq() {
      if (lookahead(statement_FIRST)) {
         STATEMENT left = statement();
         Token semi = consume(Token.SEMICOLON);
         STATS right = statseq();
         return new STATS(left, right, semi.getPosition());
      } else if (lookahead(statseq_FOLLOW)) {
         return new STATNULL(curr.getPosition());
      } else {
         error(statement_FIRST.union(statseq_FOLLOW));
         return new STATNULL(curr.getPosition());
      }
   }

   STATEMENT statement() {
      if (lookahead(Token.IDENT)) {
         DESIGNATOR des = designator();
         // IDENT-led statements are disambiguated by one token: ':=' assignment vs '(' call.
         if (lookahead(Token.COLONEQ)) {
            consume(Token.COLONEQ);
            EXPRESSION rhs = expr();
            return new ASSIGN(des, rhs, des.position);
         } else if (lookahead(Token.LPAREN)) {
            consume(Token.LPAREN);
            ACTUAL actuals = actualListOpt();
            consume(Token.RPAREN);
            return new PROCCALL(des, actuals, des.position);
         } else {
            error(new TokenSet(new int[]{Token.COLONEQ, Token.LPAREN}));
            return new EXIT(curr.getPosition());
         }
      } else if (lookahead(Token.IF)) {
         return ifStmt();
      } else if (lookahead(Token.WHILE)) {
         return whileStmt();
      } else if (lookahead(Token.REPEAT)) {
         return repeatStmt();
      } else if (lookahead(Token.LOOP)) {
         return loopStmt();
      } else if (lookahead(Token.EXIT)) {
         Token tok = consume(Token.EXIT);
         return new EXIT(tok.getPosition());
      } else if (lookahead(Token.WRITE)) {
         Token tok = consume(Token.WRITE);
         EXPRESSION expr = expr();
         return new WRITE(expr, tok.getPosition());
      } else if (lookahead(Token.WRITELN)) {
         Token tok = consume(Token.WRITELN);
         return new WRITELN(tok.getPosition());
      } else if (lookahead(Token.READ)) {
         Token tok = consume(Token.READ);
         DESIGNATOR des = designator();
         return new READ(des, tok.getPosition());
      } else {
         error(statement_FIRST);
         return new EXIT(curr.getPosition());
      }
   }

   STATEMENT ifStmt() {
      Token tok = consume(Token.IF);
      EXPRESSION expr = expr();
      consume(Token.THEN);
      STATS thenPart = statseq();
      if (lookahead(Token.ELSE)) {
         consume(Token.ELSE);
         STATS elsePart = statseq();
         consume(Token.ENDIF);
         return new IF2(expr, thenPart, elsePart, tok.getPosition());
      } else {
         consume(Token.ENDIF);
         return new IF1(expr, thenPart, tok.getPosition());
      }
   }

   WHILE whileStmt() {
      Token tok = consume(Token.WHILE);
      EXPRESSION expr = expr();
      consume(Token.DO);
      STATS stats = statseq();
      consume(Token.ENDDO);
      return new WHILE(expr, stats, tok.getPosition());
   }

   REPEAT repeatStmt() {
      Token tok = consume(Token.REPEAT);
      STATS stats = statseq();
      consume(Token.UNTIL);
      EXPRESSION expr = expr();
      return new REPEAT(expr, stats, tok.getPosition());
   }

   LOOP loopStmt() {
      Token tok = consume(Token.LOOP);
      STATS stats = statseq();
      consume(Token.ENDLOOP);
      return new LOOP(stats, tok.getPosition());
   }

   ACTUAL actualListOpt() {
      if (lookahead(expr_FIRST)) {
         return actualList();
      } else if (lookahead(rparen_FOLLOW)) {
         return new ACTUALNULL(curr.getPosition());
      } else {
         error(expr_FIRST.union(rparen_FOLLOW));
         return new ACTUALNULL(curr.getPosition());
      }
   }

   ACTUAL actualList() {
      int pos = curr.getPosition();
      EXPRESSION expr = expr();
      if (lookahead(Token.COMMA)) {
         consume(Token.COMMA);
         return new ACTUAL(expr, actualList(), pos);
      } else if (lookahead(rparen_FOLLOW)) {
         // FOLLOW('actual_list') is epsilon tail after final argument
         return new ACTUAL(expr, new ACTUALNULL(curr.getPosition()), pos);
      } else {
         error(new TokenSet(Token.COMMA).union(rparen_FOLLOW));
         return new ACTUAL(expr, new ACTUALNULL(curr.getPosition()), pos);
      }
   }

   /********************************************************/
   /*                     Expressions                      */
   /********************************************************/

   EXPRESSION expr() {
      EXPRESSION left = term();
      // left associativity for addop
      while (lookahead(addop_FIRST)) {
         Token op = consume(addop_FIRST);
         EXPRESSION right = term();
         left = new BINARY(op.getKind(), left, right, op.getPosition());
      }
      if (!lookahead(expr_prime_FOLLOW)) {
         error(addop_FIRST.union(expr_prime_FOLLOW));
      }
      return left;
   }

   EXPRESSION term() {
      EXPRESSION left = logical();
      // left associativity for mulop
      while (lookahead(mulop_FIRST)) {
         Token op = consume(mulop_FIRST);
         EXPRESSION right = logical();
         left = new BINARY(op.getKind(), left, right, op.getPosition());
      }
      if (!lookahead(term_prime_FOLLOW)) {
         error(mulop_FIRST.union(term_prime_FOLLOW));
      }
      return left;
   }

   EXPRESSION logical() {
      EXPRESSION left = relational();
      // logicop parsed above relational and below multiplicative/additive
      while (lookahead(logicop_FIRST)) {
         Token op = consume(logicop_FIRST);
         EXPRESSION right = relational();
         left = new BINARY(op.getKind(), left, right, op.getPosition());
      }
      if (!lookahead(logical_prime_FOLLOW)) {
         error(logicop_FIRST.union(logical_prime_FOLLOW));
      }
      return left;
   }

   EXPRESSION relational() {
      EXPRESSION left = factor();
      while (lookahead(relational_op_FIRST)) {
         if (lookahead(Token.ISA)) {
            // 'isa' expects a type identifier, not a full expression RHS.
            Token isa = consume(Token.ISA);
            Token type = consume(Token.IDENT);
            left = new ISA(isa.getKind(), left, type.getValue(), isa.getPosition());
         } else {
            Token op = consume(relop_FIRST);
            EXPRESSION right = factor();
            left = new BINARY(op.getKind(), left, right, op.getPosition());
         }
      }
      if (!lookahead(relational_prime_FOLLOW)) {
         error(relational_op_FIRST.union(relational_prime_FOLLOW));
      }
      return left;
   }

   EXPRESSION factor() {
      if (lookahead(Token.MINUS)) {
         Token tok = consume(Token.MINUS);
         return new UNARY(tok.getKind(), factor(), tok.getPosition());
      } else if (lookahead(Token.NOT)) {
         Token tok = consume(Token.NOT);
         return new UNARY(tok.getKind(), factor(), tok.getPosition());
      } else if (lookahead(Token.TRUNC)) {
         Token tok = consume(Token.TRUNC);
         return new UNARY(tok.getKind(), factor(), tok.getPosition());
      } else if (lookahead(Token.FLOAT)) {
         Token tok = consume(Token.FLOAT);
         return new UNARY(tok.getKind(), factor(), tok.getPosition());
      } else if (lookahead(Token.LPAREN)) {
         consume(Token.LPAREN);
         EXPRESSION expression = expr();
         consume(Token.RPAREN);
         return expression;
      } else if (lookahead(Token.INTLIT)) {
         Token tok = consume(Token.INTLIT);
         return new INTLIT(tok.getValue(), tok.getPosition());
      } else if (lookahead(Token.CHARLIT)) {
         Token tok = consume(Token.CHARLIT);
         return new CHARLIT(tok.getValue(), tok.getPosition());
      } else if (lookahead(Token.REALLIT)) {
         Token tok = consume(Token.REALLIT);
         return new REALLIT(tok.getValue(), tok.getPosition());
      } else if (lookahead(Token.STRINGLIT)) {
         Token tok = consume(Token.STRINGLIT);
         return new STRINGLIT(tok.getValue(), tok.getPosition());
      } else if (lookahead(Token.IDENT)) {
         return designator();
      } else {
         error(expr_FIRST);
         return new INTLIT("0", curr.getPosition());
      }
   }

   /********************************************************/
   /*                     Designator                       */
   /********************************************************/

   DESIGNATOR designator() {
      Token ident = consume(Token.IDENT);
      DESIGNATOR next = designator_prime();
      return new VARREF(ident.getValue(), next, ident.getPosition());
   }

   DESIGNATOR designator_prime() {
      if (lookahead(Token.LBRACK)) {
         Token tok = consume(Token.LBRACK);
         EXPRESSION index = expr();
         consume(Token.RBRACK);
         DESIGNATOR next = designator_prime();
         return new INDEX(index, next, tok.getPosition());
      } else if (lookahead(Token.PERIOD)) {
         Token tok = consume(Token.PERIOD);
         Token ident = consume(Token.IDENT);
         DESIGNATOR next = designator_prime();
         return new FIELDREF(ident.getValue(), next, tok.getPosition());
      } else if (lookahead(designator_prime_FOLLOW)) {
         // FOLLOW(designator_prime) marks epsilon tail after valid prefix
         return new DESNULL(curr.getPosition());
      } else {
         error(designator_prime_FIRST.union(designator_prime_FOLLOW));
         return new DESNULL(curr.getPosition());
      }
   }

   /********************************************************/
   /*                     Main                             */
   /********************************************************/

   public static void main (String args[]) throws IOException{
      if (args.length != 3) {
         throw new IOException("Usage: luca_parse <input.luc> <output.xml> <output.gv>");
      }

      Lex scanner = new Lex(args[0]);
      Parse parser = new Parse(scanner);
      PROGRAM tree = parser.program();

      try (BufferedWriter xmlFile = new BufferedWriter(new FileWriter(new File(args[1])))) {
         xmlFile.write("<block>\n");
         xmlFile.write(tree.toString(0));
         xmlFile.write("</block>\n");
      }

      Graphviz.clear();
      tree.toGraphviz();
      Graphviz.toFile("AST", args[2]);
   }
}
