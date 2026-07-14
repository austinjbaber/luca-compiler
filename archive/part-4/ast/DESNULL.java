package ast;
public class DESNULL extends DESIGNATOR {

   public DESNULL (int position) {
       super(position);
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {},
             Graphviz.exprShape,
             Graphviz.exprColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<DESNULL pos=\"" + position + "\"/>\n";
       return s;
   }
}

