package sym;

public class StringValue extends Value{
    String v;
    public StringValue(String v) {this.v = v;}

    public String getValue() {return v;}

    public static Value binaryEval (int Op, StringValue Left, StringValue Right, int Pos) {
        if ((Left==null) ||  (Right==null)) return null;

	String L = Left.v;
        String R = Right.v;
        switch(Op) {
	case lexer.Token.PLUS: return new StringValue(L + R);
        case lexer.Token.LT: return new BoolValue(L.compareTo(R) < 0);
        case lexer.Token.GT: return new BoolValue(L.compareTo(R) > 0);
        case lexer.Token.EQ: return new BoolValue(L.compareTo(R) == 0);
        case lexer.Token.NE: return new BoolValue(L.compareTo(R) != 0);
        case lexer.Token.LE: return new BoolValue(L.compareTo(R) <= 0);
        case lexer.Token.GE: return new BoolValue(L.compareTo(R) >= 0);
        default:              return new ErrorValue(Pos, "Illegal operator" + lexer.Token.token2stringPrint(Op));
	}
     }

    public String toString() {
	return "StringValue[" + v + "]";
    }

}
