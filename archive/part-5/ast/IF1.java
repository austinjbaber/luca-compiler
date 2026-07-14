package ast;
public class IF1 extends STATEMENT {
   public EXPRESSION expr;           // Child
   public STATS then_;            // Child

   public IF1 (EXPRESSION expr, STATS then_, int position) {
       super(position);
       this.expr = expr;
       this.then_ = then_;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {expr,then_},
             Graphviz.stmtShape,
             Graphviz.stmtColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<IF1 " + 
                  "pos=\"" + position + "\">\n";
       s += expr.toString(indent+1);
       s += then_.toString(indent+1);
       s += blanks(indent) + "</IF1>\n";
       return s;
   }
}

