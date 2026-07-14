package ast;
public class WRITELN extends STATEMENT {

   public WRITELN(int position) {
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
                  "<WRITELN " + 
                  "pos=\"" + position + "\"/>\n";
       return s;
   }
}

