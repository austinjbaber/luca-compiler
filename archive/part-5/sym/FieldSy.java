package sym;

public class FieldSy extends VariableSy {
   public Symbol Parent = null /*Standard.NoSy*/;
   public FieldSy (String Name, int Pos, int Level) {
	super(Name, Pos, Level);
   }
   public Symbol GetParent() {return Parent;}
   public void SetParent(Symbol Parent) {this.Parent = Parent;}

   public String toString() {
       return super.toString() +
	   "parent=[" + Parent.toStringShort() + "];" ;
   }
}
