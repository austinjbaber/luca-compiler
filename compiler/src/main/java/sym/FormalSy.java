package sym;

public class FormalSy extends VariableSy {
   public int Number = 0;
   public String Mode = "VAL";
   public VariableSy Copy  = null /*Standard.NoSy*/;
   public FormalSy (String Name, int Pos, int Level) {
	super(Name, Pos, Level);
   }

   public int GetFormalNumber () {return Number;}
   public void SetFormalNumber (int Number) {this.Number = Number;}

   public VariableSy GetFormalCopy () {return Copy;}
   public void SetFormalCopy (VariableSy Copy) {this.Copy = Copy;}
   public String GetFormalMode () {return Mode;}
   public void SetFormalMode (String Mode) {this.Mode = Mode;}

   public String toString() {
       return super.toString() +
                 "number=" + Number + ";" +
                 "mode=" + Mode +  ";" +
	   "copy=[" + ((Copy!=null)?Copy.toStringShort():"") + "];";
   }
}
