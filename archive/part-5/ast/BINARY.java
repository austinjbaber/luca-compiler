package ast;
public class BINARY extends EXPRESSION {
   public int op;             // Input Attribute
   public EXPRESSION left;    // Child
   public EXPRESSION right;   // Child

   public BINARY (int op, EXPRESSION left, EXPRESSION right, int position) {
       super(position);
       this.op = op;
       this.left = left;
       this.right = right;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""},
                {"op",lexer.Token.token2string(op)}
             },
             new AST[] {left, right},
             Graphviz.exprShape,
             Graphviz.exprColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<BINARY op=\"" + 
                  lexer.Token.token2string(op) + 
                  "\" pos=\"" + 
                  position + "\">\n";
       s += left.toString(indent+1);
       s += right.toString(indent+1);
       s += blanks(indent) + "</BINARY>\n";
       return s;
   }
}

