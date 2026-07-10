package sym;

public class RecordType extends TypeSy {
   public SyTab Fields = new SyTab();

   public RecordType (String Name, int Pos, int Level) {
	super(Name, Pos, Level);
   }
   public SyTab GetFields() {return Fields;}
   public void SetFields(SyTab Fields) {this.Fields = Fields;}
   public void SetFieldSize(int Size) {this.Size = Size;}
   public int GetFieldSize() {return Size;}

   public String toString() {
       return super.toString() +
                 "fields=" + Fields.toString() + ";" +
                 "size=" + Size + ";";
   }
}
