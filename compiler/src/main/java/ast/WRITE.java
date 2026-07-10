package ast;
public class WRITE extends STATEMENT {
   public EXPRESSION expr;           // Child

   public WRITE(EXPRESSION expr, int position) {
       super(position);
       this.expr = expr;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {expr},
             Graphviz.stmtShape,
             Graphviz.stmtColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<WRITE " + 
                  "pos=\"" + position + "\">\n";
       s += expr.toString(indent+1);
       s += blanks(indent) + "</WRITE>\n";
       return s;
   }
}


