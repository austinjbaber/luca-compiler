package ast;
public class READ extends STATEMENT {
   public DESIGNATOR des;            // Child

   public READ (DESIGNATOR des, int position) {
       super(position);
       this.des = des;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {des},
             Graphviz.stmtShape,
             Graphviz.stmtColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<READ " + 
                  "pos=\"" + position + "\">\n";
       s += des.toString(indent+1);
       s += blanks(indent) + "</READ>\n";
       return s;
   }

}

