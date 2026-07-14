package ast;
public class FORMALDECL extends DECLARATION {
   public String typeName;       // Input Attribute
   public String mode;           // Input Attribute, "VAL" or "VAR"

   public FORMALDECL (String ident, String type, String mode, int position) {
       super(ident,position);
       this.typeName = type;
       this.mode = mode;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""},
                {"ident",ident},
                {"typeName",typeName},
                {"mode",mode}
             },
             new AST[] {},
             Graphviz.declShape,
             Graphviz.declColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<FORMALDECL " + 
                  "ident=\"" + ident + "\" " +
                  "type=\"" + typeName + "\" " +
                  "mode=\"" + mode + "\" " +
                  "pos=\"" + position + "\"/>\n";
       return s;
   }
}


