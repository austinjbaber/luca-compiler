// package parser;

// import java.lang.*;
// import java.io.*;
// import java.io.BufferedWriter;
// import java.io.File;
// import java.io.FileWriter;

// public class Parse {

//    lexer.Lex scanner; 
//    lexer.Token current;
//    String parseTraceFileName;

//    public Parse(lexer.Lex s, String t) {
//       scanner = s;
//       parseTraceFileName = t;
//       current = scanner.nextToken();
//       openTraceFile();
//    };

//    /********************************************************/
//    /*                        Tracing                       */
//    /********************************************************/
//    int level = 0;
//    BufferedWriter traceFile;

//    void openTraceFile () {
//       if (parseTraceFileName != null) {
//          try {
//             File file = new File(parseTraceFileName);
//             traceFile = new BufferedWriter(new FileWriter(file));
//          } catch (IOException e) {
//             System.out.println(e.getMessage());
//             System.exit(0);
//          }
//       }
//    }

//    void closeTraceFile () {
//       if (traceFile != null) {
//          try {
//             traceFile.close();
//          } catch (IOException e) {
//             System.out.println(e.getMessage());
//             System.exit(0);
//          }
//       }
//    }

//    void TRACE(String x, String e, String y, boolean args) {
//       if (traceFile != null) {
//          try {
//             for(int i=0; i<level; i++) {
//                traceFile.write("   ");
//             };
//             traceFile.write(x + e); 
//             if (args) {
//                traceFile.write( 
//                   " token=\"" + lexer.Token.token2string(current.getKind()) + "\"" +
//                   " line=\"" + current.getPosition() + "\"");
//             };
//             traceFile.write(y); 
//             traceFile.newLine();
//          } catch (IOException ex) {
//             System.out.println(ex.getMessage());
//             System.exit(0);
//          }
//       }
//    }

//    void ENTER(String e) {
//       TRACE("<", e, ">", true);
//       level++;
//    }

//    void EXIT(String e) {
//       level--;
//       TRACE("</", e, ">", false);
//    }

//    void MATCH() {
//      TRACE("<", "MATCH", "/>", true);
//    }

//    /********************************************************/
//    /*                        Matching                      */
//    /********************************************************/
//    boolean lookahead(int tok) {
//         return current.getKind() == tok;
//    }

//    boolean lookahead(lexer.TokenSet toks) {
//       return toks.member(current);
//    }

//    void match(int expected) {
//       match(new lexer.TokenSet(expected));
//    }

//    void match(lexer.TokenSet expected) {
//       if (lookahead(expected)) {
//          lexer.Token next = scanner.nextToken();
//          MATCH();
//          current = next;
//       } else 
//          error(expected);
//    }

//    void error(lexer.TokenSet expected) {
//       String err = "<SYNTAX_ERROR " +
//                    "pos=\"" + current.getPosition() + "\" " +
//                    "\n   expected=\"" + expected.toString(false) + "\" " +
//                    "\n   found=\"" + lexer.Token.token2stringPrint(current.getKind()) + "\"/>";
//       closeTraceFile();
//       System.out.println(err);
//       System.exit(0);
//    }

//    /********************************************************/
//    /*                FIRST and FOLLOW sets                 */
//    /********************************************************/
//    lexer.TokenSet decl_FIRST = new lexer.TokenSet(new int[]{});


//    /********************************************************/
//    /*                      Program                         */
//    /********************************************************/
//    public void program () {
//       ENTER("PROGRAM");
//       int pos = current.getPosition();
//       match(lexer.Token.PROGRAM);
//       EXIT("PROGRAM");
//       closeTraceFile();
//    }

//    /********************************************************/
//    /*                      Statements                      */
//    /********************************************************/

//    /********************************************************/
//    /*                     Declarations                     */
//    /********************************************************/

//    /********************************************************/
//    /*                     Expressions                      */
//    /********************************************************/

//    /********************************************************/
//    /*                     Designator                       */
//    /********************************************************/

//    /********************************************************/
//    /*                     Main                             */
//    /********************************************************/

//    public static void main (String args[]) throws IOException{
//       if (args[0] == null) {
//           throw new IOException("Missing input file");
//       };
//       String traceFile = null;
//       if (args[1] != null) {
//           traceFile = args[1];
//       };
//       lexer.Lex scanner = new lexer.Lex(args[0]);
//       parser.Parse parser = new parser.Parse(scanner,traceFile);
//       parser.program();

//    }
// }
