package ast;

public class BLOCK extends STATEMENT {
   public DECLARATION decls;          // Child
   public STATEMENT stats;          // Child

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {decls,decls},
             Graphviz.stmtShape,
             Graphviz.stmtColor);
   }
}
// -----------------------------------------------------
//             Expression Type Nodes
// -----------------------------------------------------
