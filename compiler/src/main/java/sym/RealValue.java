package sym;

public class RealValue extends Value{
    float v;
    public RealValue(float v) {this.v = v;}

    public float getValue() {return v;}

    public static Value binaryEval (int Op, RealValue Left, RealValue Right, int Pos) {
        if ((Left==null) ||  (Right==null)) return null;

	float L = Left.v;
        float R = Right.v;
        switch (Op) {
	case lexer.Token.PLUS:  return new RealValue(L + R);
        case lexer.Token.MINUS:  return new RealValue(L - R);
        case lexer.Token.STAR:  return new RealValue(L * R);
        case lexer.Token.SLASH:  {
	    if ((R < 0.00001) && (R > -0.00001))
               return new ErrorValue(Pos, "Division by zero");
           else
               return new RealValue(L / R);
        } 
        case lexer.Token.PERCENT:  return new RealValue(L % R);
        case lexer.Token.LT: return new BoolValue(L < R);
        case lexer.Token.GT: return new BoolValue(L > R);
        case lexer.Token.EQ: return new BoolValue(java.lang.Math.abs(L-R) < 0.00001);
        case lexer.Token.NE: return new BoolValue(java.lang.Math.abs(L-R) > 0.00001);
        case lexer.Token.LE: return new BoolValue(L <= R);
        case lexer.Token.GE: return new BoolValue(L >= R);
        default:
               return new ErrorValue(Pos, "Illegal operator" + lexer.Token.token2stringPrint(Op));
        }
    }
    public static Value unaryEval (int Op, RealValue Left, int Pos) {
        if (Left==null) return null;

	float L = Left.v;
        switch (Op) {
	case lexer.Token.TRUNC:  return new IntValue((int)L);
        case lexer.Token.MINUS:  return new RealValue(-L);
        default:   return new ErrorValue(Pos, "Illegal operator:" + lexer.Token.token2stringPrint(Op));
        }
    }

    public String toString() {
	return "RealValue[" + v + "]";
    }

}
