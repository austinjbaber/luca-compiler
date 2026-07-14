package ast;
public class CHARLIT extends LITERAL {

   public CHARLIT (String literal, int position) {
       super(literal,position);
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""},
                {"literal",literal}
             },
             new AST[] {},
             Graphviz.exprShape,
             Graphviz.exprColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<CHARLIT " +
                  "literal=\"" + literal + "\" " +
                  "pos=\"" + position + "\"/>\n";
       return s;
   }

}

