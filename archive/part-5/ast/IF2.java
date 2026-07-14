package ast;
public class IF2 extends STATEMENT {
   public EXPRESSION expr;           // Child
   public STATS then_;           // Child
   public STATS else_;           // Child

   public IF2 (EXPRESSION expr, STATS then_, STATS else_, int position) {
       super(position);
       this.expr = expr;
       this.then_ = then_;
       this.else_ = else_;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {then_,else_},
             Graphviz.stmtShape,
             Graphviz.stmtColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<IF2 " + 
                  "pos=\"" + position + "\">\n";
       s += expr.toString(indent+1);
       s += then_.toString(indent+1);
       s += else_.toString(indent+1);
       s += blanks(indent) + "</IF2>\n";
       return s;
   }
}

