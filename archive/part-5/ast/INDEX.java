package ast;
public class INDEX extends DESIGNATOR {
   public EXPRESSION index;          // Child

   public INDEX (EXPRESSION index, DESIGNATOR next, int position) {
       super(position);
       this.index = index;
       this.next = next;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {index},
             Graphviz.exprShape,
             Graphviz.exprColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<INDEX " +
                  "pos=\"" + 
                  position + "\">\n";
       s += index.toString(indent+1);
       s += next.toString(indent+1);
       s += blanks(indent) + "</INDEX>\n";
       return s;
   }
}

