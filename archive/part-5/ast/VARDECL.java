package ast;
public class VARDECL extends DECLARATION {
   public String typeName;       // Input Attribute

   public VARDECL (String ident, String type, int position) {
       super(ident,position);
       this.typeName = type;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""},
                {"ident",ident},
                {"typeName",typeName}
             },
             new AST[] {},
             Graphviz.declShape,
             Graphviz.declColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<VARDECL " + 
                  "ident=\"" + ident + "\" " +
                  "type=\"" + typeName + "\" " +
                  "pos=\"" + position + "\"/>\n";
       return s;
   }
}

