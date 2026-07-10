package sym;

public class MethodSy extends Symbol {
   public int Offset = 0;
   public ProcedureSy MethodProc = null /*Standard.NoSy*/;
   public Symbol Parent = null /*Standard.NoSy*/;

   public MethodSy (String Name, int Pos, int Level) {
	super(Name, Pos, Level);
   }

   public Symbol GetParent() {return Parent;}
   public void SetParent(Symbol Parent) {this.Parent = Parent;}
   public ProcedureSy GetMethodProc() {return MethodProc;}
   public void SetMethodProc(ProcedureSy MethodProc) {this.MethodProc = MethodProc;}
   public int GetOffset() {return Offset;}
   public void SetOffset (int Offset) {this.Offset = Offset;}

   public String toString() {
       return super.toString() +
                 "methodProc=[" + MethodProc.toStringShort() + "];" +
                 "parent=[" + Parent.toStringShort() + "];" +
	   "offset=" + Offset + ";";
   }
}
