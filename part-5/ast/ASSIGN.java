package ast;
public class ASSIGN extends STATEMENT {
   public DESIGNATOR left;           // Child
   public EXPRESSION right;          // Child

   public ASSIGN (DESIGNATOR left, EXPRESSION right, int position) {
       super(position);
       this.left = left;
       this.right = right;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {left,right},
             Graphviz.stmtShape,
             Graphviz.stmtColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<ASSIGN " + 
                  "pos=\"" + position + "\">\n";
       s += left.toString(indent+1);
       s += right.toString(indent+1);
       s += blanks(indent) + "</ASSIGN>\n";
       return s;
   }
}

