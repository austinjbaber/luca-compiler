/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

import java.io.*;

public class Parse {
    Lex scanner;
    Token currentToken;
    public AST ast;

    public Parse (Lex scanner) {
       this.scanner = scanner;
       next();
       ast = parse();
    }

    void next() {
       currentToken = scanner.nextToken();
    }

    boolean lookahead(int tokenKind) {
       return currentToken.kind == tokenKind;
    }

    void match(int tokenKind) {
       if (!lookahead(tokenKind)) {
            System.err.println("Parsing error, line " + currentToken.position);
            System.exit(-1);
        }
        next();
    }

    // Build an AST node for either a variable reference
    // or a literal integer reference.
    EXPR factor() {
        EXPR expr = null;
        if (lookahead(Token.IDENT)) {
           expr = new IDENT(currentToken.ident);
           match(Token.IDENT);
        } else if (lookahead(Token.INTLIT)) {
           expr = new INTLIT(currentToken.value);
           match(Token.INTLIT);
        }
        return expr;
    }

    // Build an AST subtree for an expression.
   EXPR expr() {
      EXPR f = factor();
      int op;
      while (lookahead(Token.PLUS) || lookahead(Token.MINUS) || lookahead(Token.LT)) {
         op = currentToken.kind; // plus, minus or lt
         match(op);
         EXPR e = factor();
         f = new BINOP(op, f, e);
      }
      return f;
   }

    // Build an ASSIGN subtree.
   STAT assign() {
        String ident = currentToken.ident;
        match(Token.IDENT);
        match(Token.EQUAL);
        EXPR e = expr();
        return new ASSIGN(ident, e);
   }

    // Build a PRINT subtree.
    STAT print() {
        match(Token.PRINT);
        EXPR e = expr();
        return new PRINT(e);
    }

    // Build a STATSEQ subtree. The bottom/rightmost
    // subtree will be a NULL node, indicating the
    // end of the statement sequence. 
    STATSEQ stats() {
        STAT stat;
        if (lookahead(Token.IDENT)) {
           stat = assign();
        } else if (lookahead(Token.PRINT)) {
           stat = print();
        } else if (lookahead(Token.IF)) {
            stat = ifStatement();
        } else if (lookahead(Token.GOTO)) {
            stat = goTo();
        } else if (lookahead(Token.INTLIT)) {
            stat = label();
        } else
           return new NULL();
        match(Token.SEMICOLON);
        STATSEQ next = stats();
        return new STATSEQ(stat, next);
    }

    // Build a tree whose root is a PROGRAM node.
    AST parse() {
        match(Token.BEGIN);
        STATSEQ s = stats();
        PROGRAM p = new PROGRAM(s);
        match(Token.END);
        match(Token.EOF);
        return p;
    }

    STAT label() {
        int label = currentToken.value; // grab before match()
        match(Token.INTLIT);
        match(Token.COLON);
        return new LABEL(label);
    }

    STAT goTo() {
        match(Token.GOTO);
        int lab = currentToken.value;
        match(Token.INTLIT);
        return new GOTO(lab);
    }

    STAT ifStatement() {
        match(Token.IF);
        EXPR condition = expr();             // parse until GOTO
        match(Token.GOTO);
        int label = currentToken.value;
        match(Token.INTLIT);
        return new IF(condition, label);       // (or IFGOTO) — you create this AST class
    }


    public static void main (String args[]) throws IOException{
        Lex scanner = new Lex(args[0]);
        Parse parser = new Parse(scanner);
        System.out.println(parser.ast.toString());
    }
}
