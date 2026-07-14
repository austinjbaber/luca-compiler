package sym;

public class RefType extends TypeSy {
   public TypeSy Type = null /*Standard.NoType*/;
   public RefType (String Name, int Pos, int Level) {
	super(Name, Pos, Level);
   }
   public void SetRefType(TypeSy Type) {this.Type = Type;}
   public TypeSy GetRefType() {return Type;}

   public String toString() {
       return super.toString() +
	   "type=[" + Type.toStringShort() + "];" ;
   }
}
