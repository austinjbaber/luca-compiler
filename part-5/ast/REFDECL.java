package ast;
public class REFDECL extends DECLARATION {
   public String typeName;       // Input Attribute

   public REFDECL (String name, String type, int position) {
       super(name, position);
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
                  "<REFDECL " + 
                  "ident=\"" + ident + "\" " +
                  "type=\"" + typeName + "\" " +
                  "pos=\"" + position + "\">\n";
       s += blanks(indent) + "</REFDECL>\n";
       return s;
   }

}

