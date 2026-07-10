package ast;
public class DECLARATION extends AST {
   public String ident;          // Input Attribute



   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""},
                {"ident",ident}
             },
             new AST[] {},
             Graphviz.declShape,
             Graphviz.declColor);
   }

    public DECLARATION(String ident, int position) {
	super(position);
        this.ident = ident;
    }
}

