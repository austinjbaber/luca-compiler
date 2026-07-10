package ast;
public class FIELDDECL extends DECLARATION {
   public String typeName;       // Input Attribute

   public FIELDDECL (String name, String type, int position) {
       super(name, position);
       this.typeName = type;
       this.position = position;
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
                  "<FIELDDECL " + 
                  "ident=\"" + ident + "\" " +
                  "type=\"" + typeName + "\" " +
                  "pos=\"" + position + "\"/>\n";
       return s;
   }
}

