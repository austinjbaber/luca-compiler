package ast;
public class PROCDECL extends DECLARATION {
   public DECLS formals;       // Child
   public DECLS decls;         // Child
   public STATS stats;         // Child

   public PROCDECL (String ident, DECLS formals, DECLS decls, STATS stats, int position) {
       super(ident,position);
       this.formals = formals;
       this.stats = stats;
       this.decls = decls;
   }

   public GraphvizData graphvizData() {
      return new GraphvizData(
             new String[][] {
                {"position",position+""},
                {"ident",ident}
             },
             new AST[] {formals,decls,stats},
             Graphviz.declShape,
             Graphviz.declColor);
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<PROCDECL " + 
                  "ident=\"" + ident + "\" " +
                  "pos=\"" + position + "\">\n";
       s += formals.toString(indent+1);
       s += decls.toString(indent+1);
       s += stats.toString(indent+1);
       s += blanks(indent) + "</PROCDECL>\n";
       return s;
   }
}

