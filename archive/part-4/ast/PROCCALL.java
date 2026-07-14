package ast;
public class PROCCALL extends STATEMENT {
   public DESIGNATOR des;        // Input Attribute
   public ACTUAL actuals;        // Input Attribute

   public PROCCALL (DESIGNATOR des, ACTUAL actuals, int position) {
       super(position);
       this.des = des;
       this.actuals = actuals;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""}
             },
             new AST[] {des,actuals},
             Graphviz.stmtShape,
             Graphviz.stmtColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<PROCCALL " + 
                  "pos=\"" + position + "\">\n";
       s += des.toString(indent+1);
       s += actuals.toString(indent+1);
       s += blanks(indent) + "</PROCCALL>\n";
       return s;
   }
}

