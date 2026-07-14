package sym;

public class ArrayType   extends TypeSy {
   public int Count = 0;
   public TypeSy ElementType = null /*Standard.NoType*/;
   public ArrayType (String Name, int Pos, int Level) {
	super(Name, Pos, Level);
   }
   public int GetArrayCount() {return Count;}
   public void SetArrayCount(int Count) {this.Count = Count;}
   public TypeSy GetArrayElementType() {return ElementType;}
   public void SetArrayElementType(TypeSy ElementType) {this.ElementType = ElementType;}

   public String toString() {
       return super.toString() +
           "elementType=[" + ElementType.toStringShort() + "];" +
	   "count=" + Count + ";" ;
   }
}
