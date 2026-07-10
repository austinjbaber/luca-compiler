package sym;

public class Symbol {
   public String Name = "";
   public int Id = 0;
   public int Level = 0;
   public int Pos = 0;

   public static int CurrentId = 0;
   public static java.util.Vector Symbols =  new java.util.Vector(100,100);

   public Symbol (String Name, int Pos, int Level) {
       this.Name = Name; this.Pos = Pos; this.Level = Level;
       Id = CurrentId++;
       Symbols.add(Id, this);
   }

   public int SymbolCount() {return CurrentId;}
   public String GetName() {return Name;}
   public int GetNumber() {return Id;}
   public int GetLevel() {return Level;}
   public void SetLevel(int Level) {this.Level = Level;}
   public int GetPos() {return Pos;}
   public int GetId() {return Id;}

   public String toStringShort() {
       return "id=\"" + Id + "\" name=\"" + Name + "\"";
   }

   public String toString() {
       String C = this.getClass().getSimpleName();
       return C + ":id=" + Id + ";name=" + Name + ";level=" + Level + ";Pos=" + Pos + ";";
   }

   public void ShowAll () {
      for (java.util.Enumeration e = Symbols.elements() ; e.hasMoreElements() ;) {
          System.out.println(e.nextElement());
       }
   }
}














