package sym;

public class EnumType extends TypeSy {
   public SyTab elements = new SyTab();
   public EnumType (String Name, int Pos, int Level) {
	super(Name, Pos, Level);
   }
   public SyTab GetEnumElements() {return elements;}
   public void SetEnumElements(SyTab elements) {this.elements = elements;}

   public String toString() {
       return super.toString() +
                 "Elements=" + elements.toString() + ";";
   }
}
