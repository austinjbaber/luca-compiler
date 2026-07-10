package cli;

import ast.PROGRAM;
import lexer.Lex;
import lexer.Token;
import parser.Parse;
import sem.Semantics;

import java.io.IOException;

/** Command-line entry point for the Luca compiler front end. */
public final class Lucac {
    private Lucac() {}

    public static void main(String[] args) {
        if (args.length != 2) {
            usage();
            System.exit(2);
        }

        try {
            switch (args[0]) {
                case "lex":
                    lex(args[1]);
                    break;
                case "parse":
                    parse(args[1]);
                    break;
                case "check":
                    check(args[1]);
                    break;
                default:
                    System.err.println("Unknown command: " + args[0]);
                    usage();
                    System.exit(2);
            }
        } catch (IOException error) {
            System.err.println("lucac: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void lex(String source) throws IOException {
        Lex scanner = new Lex(source);
        System.out.println("<block>");
        while (true) {
            Token token = scanner.nextToken();
            System.out.println("   " + token);
            if (token.getKind() == Token.EOF) {
                break;
            }
        }
        System.out.println("</block>");
    }

    private static PROGRAM parseTree(String source) throws IOException {
        return new Parse(new Lex(source)).program();
    }

    private static void parse(String source) throws IOException {
        PROGRAM tree = parseTree(source);
        System.out.println("<block>");
        System.out.print(tree.toString(0));
        System.out.println("</block>");
    }

    private static void check(String source) throws IOException {
        PROGRAM tree = parseTree(source);
        new Semantics(null);
        Semantics.SemanticAnalysis(tree);
    }

    private static void usage() {
        System.err.println("Usage: lucac <lex|parse|check> <input.luc>");
    }
}
