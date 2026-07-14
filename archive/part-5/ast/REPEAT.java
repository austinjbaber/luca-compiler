package ast;
public class REPEAT extends STATEMENT {
   public EXPRESSION expr;           // Child
   public STATS stats;          // Child

   public REPEAT (EXPRESSION expr, STATS stats, int position) {
       super(position);
       this.expr = expr;
       this.stats = stats;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {expr,stats},
             Graphviz.stmtShape,
             Graphviz.stmtColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<REPEAT " + 
                  "pos=\"" + position + "\">\n";
       s += expr.toString(indent+1);
       s += stats.toString(indent+1);
       s += blanks(indent) + "</REPEAT>\n";
       return s;
   }
}

