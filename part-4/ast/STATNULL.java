package ast;
public class STATNULL extends STATS {

   public STATNULL (int position) {
       super(position);
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {},
             Graphviz.stmtShape,
             Graphviz.stmtColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<STATNULL pos=\"" + position + "\"/>\n";
       return s;
   }
}

