package ast;
public class LOOP extends STATEMENT {
   public STATS stats;          // Child

   public LOOP (STATS stats, int position) {
       super(position);
       this.stats = stats;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {stats},
             Graphviz.stmtShape,
             Graphviz.stmtColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<LOOP " + 
                  "pos=\"" + position + "\">\n";
       s += stats.toString(indent+1);
       s += blanks(indent) + "</LOOP>\n";
       return s;
   }
}

