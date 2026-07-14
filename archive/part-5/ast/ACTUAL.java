package ast;
public class ACTUAL extends STATEMENT {
   public EXPRESSION expr;       // Child
   public ACTUAL nextActual;     // Child



   public ACTUAL (int position) {
       super(position);
   }

   public ACTUAL (EXPRESSION expr, ACTUAL nextActual, int position) {
       super(position);
       this.expr = expr;
       this.nextActual = nextActual;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {expr,nextActual},
             Graphviz.stmtShape,
             Graphviz.stmtColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<ACTUAL " + 
                  "pos=\"" + position + "\">\n";
       s += expr.toString(indent+1);
       s += nextActual.toString(indent+1);
       s += blanks(indent) + "</ACTUAL>\n";
       return s;
   }
}
