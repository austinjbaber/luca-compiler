package ast;
public class WHILE extends STATEMENT {
   public EXPRESSION expr;      // Child
   public STATS stats;          // Child

   public WHILE (EXPRESSION expr, STATS stats, int position) {
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
                  "<WHILE " + 
                  "pos=\"" + position + "\">\n";
       s += expr.toString(indent+1);
       s += stats.toString(indent+1);
       s += blanks(indent) + "</WHILE>\n";
       return s;
   }
}

