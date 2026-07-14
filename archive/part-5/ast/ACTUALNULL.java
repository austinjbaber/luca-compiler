package ast;
public class ACTUALNULL extends ACTUAL {
   public ACTUALNULL (int position) {
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
                  "<ACTUALNULL pos=\"" + position + "\"/>\n";
       return s;
   }
}

