package ast;
public class SPECIAL extends STATEMENT {
    public String value;

   public SPECIAL(String value, int position) {
       super(position);
       this.value = value;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""},
                {"value",value}
             },
             new AST[] {},
             Graphviz.stmtShape,
             Graphviz.stmtColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<SPECIAL " + 
          	   "value=\"" + value.toString() + "\"" +
                   " pos=\"" + position + "\"/>\n";
       return s;
   }
}


