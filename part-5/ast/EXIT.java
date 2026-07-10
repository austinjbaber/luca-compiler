package ast;
public class EXIT  extends STATEMENT {
   public EXIT (int position) {
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
                  "<EXIT " + 
                  "pos=\"" + position + "\"/>\n";
       return s;
   }
}

