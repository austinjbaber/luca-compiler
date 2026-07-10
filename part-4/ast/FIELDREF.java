package ast;
public class FIELDREF extends DESIGNATOR {
   public String ident;          // Input Attribute

    public FIELDREF (String ident, DESIGNATOR next, int position) {
       super(position);
       this.ident = ident;
       this.next = next;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""},
                {"ident",ident}
             },
             new AST[] {},
             Graphviz.exprShape,
             Graphviz.exprColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<FIELDREF " +
                  "ident=\"" + ident + "\" " +
                  "pos=\"" + 
                  position + "\">\n";
       s += next.toString(indent+1);
       s += blanks(indent) + "</FIELDREF>\n";
       return s;
   }
}

