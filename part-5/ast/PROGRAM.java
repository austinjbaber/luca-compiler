package ast;
public class PROGRAM extends DECLARATION {
   public DECLS decls;          // Child
   public STATS stats;          // Child

   public PROGRAM (String ident, DECLS decls, STATS stats, int position) {
       super(ident,position);
       this.decls = decls;
       this.stats = stats;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""},
                {"ident",ident}
             },
             new AST[] {decls,stats},
             Graphviz.declShape,
             Graphviz.declColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<PROGRAM " + 
                 "name=\"" + ident + "\" " +
                  "pos=\"" + position + "\">\n";
       s += decls.toString(indent+1);
       s += stats.toString(indent+1);
       s += blanks(indent) + "</PROGRAM>\n";
       return s;
   }
}

