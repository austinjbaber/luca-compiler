package cli;

import ast.PROGRAM;
import codegen.CodeGenerator;
import lexer.Lex;
import lexer.Token;
import parser.Parse;
import sem.Semantics;
import sym.Arch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Command-line entry point for the Luca compiler. */
public final class Lucac {
    private Lucac() {}

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3 ||
            (args.length == 3 && !"compile".equals(args[0]))) {
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
                case "compile":
                    compile(args[1], args.length == 3 ? args[2] : null);
                    break;
                default:
                    System.err.println("Unknown command: " + args[0]);
                    usage();
                    System.exit(2);
            }
        } catch (IOException | IllegalArgumentException error) {
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

    private static void compile(String source, String output) throws IOException {
        // The active VM stores every scalar and address in an eight-byte slot.
        Arch.SetArch("luca-vm");
        PROGRAM tree = parseTree(source);
        new Semantics(null);
        int errorCount = Semantics.SemanticAnalysisWithCount(tree);
        if (errorCount != 0) {
            throw new IOException("semantic analysis failed with " + errorCount + " error(s)");
        }

        String vm = new CodeGenerator().generate(tree);
        if (output == null) {
            System.out.print(vm);
        } else {
            Path path = Paths.get(output);
            Files.write(path, vm.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void usage() {
        System.err.println("Usage: lucac <lex|parse|check> <input.luc>");
        System.err.println("       lucac compile <input.luc> [output.vm]");
    }
}
