package ast;
public class DECLNULL extends DECLS {

   public DECLNULL (int position) {
       super(position);
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {},
             Graphviz.declShape,
             Graphviz.declColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<DECLNULL pos=\"" + position + "\"/>\n";
       return s;
   }
}

