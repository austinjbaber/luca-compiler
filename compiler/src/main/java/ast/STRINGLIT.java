package ast;
public class STRINGLIT extends LITERAL {

   public STRINGLIT (String literal, int position) {
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
                  "<STRINGLIT " +
                  "literal=\"" + literal + "\" " +
                  "pos=\"" + position + "\"/>\n";
       return s;
   }
}

