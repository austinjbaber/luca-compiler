package ast;
public class CONSTDECL extends DECLARATION {
   public String typeName;       // Input Attribute
   public EXPRESSION expr;           // Child, the value of the constant

   public CONSTDECL (String ident, String type, EXPRESSION expr, int position) {
       super(ident,position);
       this.typeName = type;
       this.expr = expr;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""},
                {"ident",ident},
                {"typeName",typeName}
             },
             new AST[] {expr},
             Graphviz.declShape,
             Graphviz.declColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<CONSTDECL " + 
                  "ident=\"" + ident + "\" " +
                  "type=\"" + typeName + "\" " +
                  "pos=\"" + position + "\">\n";
       s += expr.toString(indent+1);
       s += blanks(indent) + "</CONSTDECL>\n";
       return s;
   }
}

