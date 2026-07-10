package sym;

public class EnumSy extends ConstSy {
   public EnumSy (String Name, int Pos, int Level) {
	super(Name, Pos, Level);
   }
    public int GetEnumValue () {return ((IntValue)this.value).getValue();}
    public void SetEnumValue (int value) {this.value = new IntValue(value);}

   public String toString() {
       return super.toString();
   }
}
