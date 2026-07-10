package ast;

public class AST {
   public int position;            // Input Attribute

    public AST(int position) {
	this.position = position;
    }

   public String blanks(int indent) {
      String s = "";
      for(int i=0; i<indent; i++)
         s += "   ";
      return  s;
   }

   public String toString(int indent) {
      return "";
   }

   public GraphvizData graphvizData() {
      return new GraphvizData();
   }

    public int toGraphviz() {
       GraphvizData d = graphvizData();
       String name = this.getClass().getSimpleName();
       int n = Graphviz.addNode(name, d.shape, d.color, d.attributes);
       AST[] kids = d.children;
       for (int i=0; i<kids.length; i++) {
          int e = kids[i].toGraphviz();
          Graphviz.addEdge(n, e);
       };
       return n;
    }
}


