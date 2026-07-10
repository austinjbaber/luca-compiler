package sym;

public class VariableSy extends Symbol {   
   public TypeSy Type = null /*Standard.NoType*/;
   public int Size = 0;
   public int Offset = 0;
   public VariableSy (String Name, int Pos, int Level) {
	super(Name, Pos, Level);
   }
   public VariableSy (String Name, TypeSy tp, int Pos, int Level) {
	super(Name, Pos, Level);
        this.Type = tp;
   }
   public void SetSize(int Size) {this.Size = Size;}
   public int GetSize() {return Size;}
   public TypeSy GetType() {return Type;}
   public void SetType(TypeSy Type) {this.Type = Type;}
   public int GetOffset() {return Offset;}
   public void SetOffset (int Offset) {this.Offset = Offset;}

   public String toString() {
       return super.toString() +
                 "type=[" + Type.toStringShort() + "];" +
                 "size=" + Size + ";" +
                 "offset=" + Offset + ";";
   }
}
