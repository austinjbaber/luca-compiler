package sym;

public class TypeSy extends Symbol {
   public int Size;
   public TypeSy (String Name, int Pos, int Level) {
	super(Name, Pos, Level);
   }
   public void SetSize(int Size) {this.Size = Size;}
   public int GetSize() {return Size;}

   public String toString() {
       return super.toString() +
	   "size=" + Size + ";";
   }
}       
