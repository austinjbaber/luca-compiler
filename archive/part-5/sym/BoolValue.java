package sym;

public class BoolValue extends Value{
    boolean v;
    public BoolValue(boolean v) {this.v = v;}

    public boolean getValue() {return v;}

    public static Value binaryEval (int Op, BoolValue Left, BoolValue Right, int Pos) {
        if ((Left==null) ||  (Right==null)) return null;

	boolean L = Left.v;
        boolean R = Right.v;
        switch(Op) {
	case lexer.Token.AND: return new BoolValue(L && R);
	case lexer.Token.OR:  return new BoolValue(L || R);
        case lexer.Token.EQ:  return new BoolValue(L == R);
        case lexer.Token.NE:  return new BoolValue(L != R);
        default:  return new ErrorValue(Pos, "Illegal operator:" + lexer.Token.token2stringPrint(Op));
	}
     }

    public static Value unaryEval (int Op, BoolValue Left, int Pos) {
        if (Left==null) return null;

	boolean L = Left.v;
        switch(Op) {
	case lexer.Token.NOT: return new BoolValue(!L);
        default: return new ErrorValue(Pos, "Illegal operator:" + lexer.Token.token2stringPrint(Op));
	}
     }

    public String toString() {
	return "BoolValue[" + v + "]";
    }

}
