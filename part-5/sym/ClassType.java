package sym;

public class ClassType extends TypeSy {
   public SyTab Fields = new SyTab();
   public int MethodSize = 0;
   public int FieldSize = 0;
   public TypeSy SuperType = null /*Standard.NoType*/;

   public ClassType (String Name, int Pos, int Level) {
	super(Name, Pos, Level);
   }
   public SyTab GetInstanceVariables() {
      SyTab s = new SyTab();
      java.util.Iterator i = Fields.iterator();
      while (i.hasNext()) {
          Symbol f = (Symbol) i.next();
          if (f instanceof FieldSy) s = s.insert(f);
      }
      return s;
   }

   public SyTab GetClassMethods() {
      SyTab s = new SyTab();
      java.util.Iterator i = Fields.iterator();
      while (i.hasNext()) {
          Symbol f = (Symbol) i.next();
          if (f instanceof MethodSy) s = s.insert(f);
      }
      return s;
   }

   public SyTab GetFields() {return Fields;}
   public void SetFields(SyTab Fields) {this.Fields = Fields;}

   public void SetFieldSize(int FieldSize) {this.FieldSize = FieldSize;}
   public int GetFieldSize() {return FieldSize;}

   public void SetMethodSize(int MethodSize) {this.MethodSize = MethodSize;}
   public int GetMethodSize() {return MethodSize;}

   public TypeSy GetSuperType() {return SuperType;}
   public void SetSuperType(TypeSy SuperType) {this.SuperType = SuperType;}

   public String toString() {
       return super.toString() +
           "fields=" + GetInstanceVariables().toString() + ";" +
           "methods=" + GetClassMethods().toString() + ";" +
	   "fieldSize=" + FieldSize + ";"  +
	   "methodSize=" + MethodSize + ";"  +
           "superType=[" + SuperType.toStringShort() + "];";
   }
}
