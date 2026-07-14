package sym;

public class CharValue extends Value{
    char v;
    public CharValue(char v) {this.v = v;}

    public char getValue() {return v;}

    public static Value binaryEval (int Op, CharValue Left, CharValue Right, int Pos) {
        if ((Left==null) ||  (Right==null)) return null;

	char L = Left.v;
        char R = Right.v;
        switch (Op) {
        case lexer.Token.EQ: return new BoolValue(L == R);
        case lexer.Token.NE: return new BoolValue(L != R);
        default: return new ErrorValue(Pos, "Illegal operator:" + lexer.Token.token2stringPrint(Op));
	}
     }

    public String toString() {
	return "CharValue[" + v + "]";
    }

}
