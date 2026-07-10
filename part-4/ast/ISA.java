package ast;
public class ISA extends EXPRESSION {
   public String typeName;       // Input Attribute
//    public sym.TypeSy symbol;
   public EXPRESSION left;

   public ISA (int op, EXPRESSION left, String typeName, int position) {
       super(position);
       this.left = left;
       this.typeName = typeName;
   }

   public String toString(int indent) {
       String s = blanks(indent) + 
                  "<ISA " + 
                  "typeName=\"" + typeName + "\" " +
                  "\" pos=\"" + position + "\"" +
                  ">\n";
       s += left.toString(indent+1);
       s += blanks(indent) + "</TYPEOP>\n";
       return s;
   }

}

