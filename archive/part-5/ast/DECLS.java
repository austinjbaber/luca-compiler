package ast;
public class DECLS extends AST {
   public DECLARATION left;           // Child
   public DECLS right;          // Child

   public String ident;          // Input Attribute



   public DECLS (DECLARATION left, DECLS right, int position) {
       super(position);
       this.left = left;
       this.right = right;
   }

   public DECLS (int position) {
       super(position);
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""},
                {"ident",ident}
             },
             new AST[] {left,right},
             Graphviz.declShape,
             Graphviz.declColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<DECLS " + 
                  "pos=\"" + position + "\">\n";
       s += left.toString(indent+1);
       s += right.toString(indent+1);
       s += blanks(indent) + "</DECLS>\n";
       return s;
   }
}

