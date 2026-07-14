package sym;

public class ErrorValue extends Value{
    public String msg;
    public int Pos;
    public ErrorValue(int Pos, String msg) {this.Pos = Pos; this.msg = msg;}
}
