package sym;

public class IntValue extends Value {
    int v;
    public IntValue(int v) {this.v = v;}

    public int getValue() {return v;}

    public static Value binaryEval (int Op, IntValue Left, IntValue Right, int Pos) {
        if ((Left==null) ||  (Right==null)) return null;

	int L = Left.v;
        int R = Right.v;
        switch (Op) {
	case lexer.Token.PLUS:  return new IntValue(L + R);
        case lexer.Token.MINUS:  return new IntValue(L - R);
        case lexer.Token.STAR:  return new IntValue(L * R);
        case lexer.Token.SLASH: { 
           if (R == 0) 
               return new ErrorValue(Pos, "Division by zero");
           else
               return new IntValue(L / R);
        } 
        case lexer.Token.PERCENT:  return new IntValue(L % R);
        case lexer.Token.LT:  return new BoolValue(L < R);
        case lexer.Token.GT:  return new BoolValue(L > R);
        case lexer.Token.EQ:  return new BoolValue(L == R);
        case lexer.Token.NE:  return new BoolValue(L != R);
        case lexer.Token.LE: return new BoolValue(L <= R);
        case lexer.Token.GE: return new BoolValue(L >= R);
        default:
               return new ErrorValue(Pos, "Illegal operator" + lexer.Token.token2stringPrint(Op));
        }
    }
    public static Value unaryEval (int Op, IntValue Left, int Pos) {
        if (Left==null) return null;

	int L = Left.v;
        switch (Op) {
	case lexer.Token.FLOAT:  return new RealValue((float)L);
        case lexer.Token.MINUS:  return new IntValue(-L);
        default:   return new ErrorValue(Pos, "Illegal operator" + lexer.Token.token2stringPrint(Op));
        }
    }

    public String toString() {
	return "IntValue[" + v + "]";
    }
}
