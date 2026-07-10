package ast;
public class UNARY extends EXPRESSION {
   public int op;             // Input Attribute
   public EXPRESSION left;    // Child

   public UNARY (int op, EXPRESSION left, int position) {
       super(position);
       this.op = op;
       this.left = left;
   }

   public UNARY (int position) {
       super(position);
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""},
                {"op",lexer.Token.token2string(op)}
             },
             new AST[] {left},
             Graphviz.exprShape,
             Graphviz.exprColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<UNARY op=\"" + 
                  lexer.Token.token2string(op) + 
                  "\" pos=\"" + 
                  position + "\">\n";
       s += left.toString(indent+1);
       s += blanks(indent) + "</UNARY>\n";
       return s;
   }
}

