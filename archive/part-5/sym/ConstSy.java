package sym;

public class ConstSy extends VariableSy {
   public Value value = null;
   public ConstSy (String Name, int Pos, int Level) {
	super(Name, Pos, Level);
   }
   public Value GetConstantValue () {return value;}
   public void SetConstantValue (Value value) {this.value =value;}
   public String toString() {
       return super.toString() +
	   "value=" + ((value==null)?"null":value.toString()) + ";";
   }
}
