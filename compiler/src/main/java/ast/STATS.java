package ast;
public class STATS extends AST {
   public STATEMENT left;           // Child
   public STATS right;          // Child


   public STATS (int position) {
       super(position);
    //    this.label = new sym.Label();
   }

   public STATS (STATEMENT left, STATS right, int position) {
       super(position);
       this.left = left;
       this.right = right;
    //    this.label = new sym.Label();
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
                  "<STATS " + 
                  "pos=\"" + position + "\">\n";
       s += left.toString(indent+1);
       s += right.toString(indent+1);
       s += blanks(indent) + "</STATS>\n";
       return s;
   }
}

