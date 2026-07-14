package ast;
public class EXPRESSION extends AST{
    public sym.TypeSy type;        // Synthesized Attribute
    public sym.Value value;        // Synthesized Attribute
    public boolean isConstant;     // Synthesized Attribute
    public boolean isLValue;       // Synthesized Attribute

    public EXPRESSION(int position) {
	super(position);
    }
}

