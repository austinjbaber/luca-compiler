package ast;
public class ARRAYDECL extends DECLARATION {
   public EXPRESSION count;          // Child, the constant expression
   public String elementTypeName;// Input Attribute

   public ARRAYDECL (String name, EXPRESSION expr, String type, int position) {
       super(name, position);
       this.count = expr;
       this.elementTypeName = type;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""},
                {"ident",ident},
                {"elementTypeName",elementTypeName}
             },
             new AST[] {count},
             Graphviz.declShape,
             Graphviz.declColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<ARRAYDECL " + 
                  "ident=\"" + ident + "\" " +
                  "elementTypeName=\"" + elementTypeName + "\" " +
                  "pos=\"" + position + "\">\n";
       s += count.toString(indent+1);
       s += blanks(indent) + "</ARRAYDECL>\n";
       return s;
   }
}

