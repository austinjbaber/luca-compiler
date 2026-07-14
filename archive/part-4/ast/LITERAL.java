package ast;
public class LITERAL extends EXPRESSION {
   public String literal;          // Input Attribute

    public LITERAL (String literal, int position) {
	super(position);
        this.literal = literal;
    }
}

