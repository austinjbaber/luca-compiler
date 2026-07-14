package sym;

public class ProcedureSy extends Symbol {
   public SyTab Formals = new SyTab();
   public SyTab Locals = new SyTab();
   public int LocalSize = 0;
   public int FormalSize = 0;
   public ProcedureSy (String Name, int Pos, int Level) {
	super(Name, Pos, Level);
   }
   public SyTab GetProcLocals() {return Locals;}
   public void SetProcLocals(SyTab Locals) {this.Locals = Locals;}
   public SyTab GetProcFormals() {return Formals;}
   public void SetProcFormals(SyTab Formals) {this.Formals = Formals;}
   public static FormalSy GetFormalParam(SyTab Formals, int FormalNumber)  {
      java.util.Iterator i = Formals.iterator();
      while (i.hasNext()) {
          FormalSy f = (FormalSy) i.next();
          if (f.GetFormalNumber() == FormalNumber) return f;
      }
      return null /*Standard.NoSy*/;
   }
   public int GetLocalSize () {return LocalSize;}
   public void SetLocalSize (int LocalSize) {this.LocalSize = LocalSize;}
   public int GetFormalSize () {return FormalSize;}
   public void SetFormalSize (int FormalSize) {this.FormalSize = FormalSize;}

   public String toString() {
       return super.toString() +
                 "formals=" + Formals.toString() + ";" +
                 "locals=" + Locals.toString() + ";" +
                 "formalSize=" + FormalSize + ";" +
                 "localSize=" + LocalSize + ";";
   }
}
