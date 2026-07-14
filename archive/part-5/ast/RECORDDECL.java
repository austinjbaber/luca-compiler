package ast;
public class RECORDDECL extends DECLARATION {
   public DECLS fields;         // Child, the list of fields

   public RECORDDECL (String name, DECLS fields, int position) {    
       super(name, position);
       this.fields = fields;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""},
                {"ident",ident}
             },
             new AST[] {fields},
             Graphviz.declShape,
             Graphviz.declColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<RECORDDECL " + 
                  "ident=\"" + ident + "\" " +
                  "pos=\"" + position + "\">\n";
       s += fields.toString(indent+1);
       s += blanks(indent) + "</RECORDDECL>\n";
       return s;
   }}

