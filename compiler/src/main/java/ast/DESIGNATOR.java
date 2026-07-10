package ast;
public class DESIGNATOR extends EXPRESSION {
   public DESIGNATOR next;           //

    public DESIGNATOR(int position) {
	super(position);
    }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {next},
             Graphviz.exprShape,
             Graphviz.exprColor);
   }

}

