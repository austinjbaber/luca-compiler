package sym;

public class Arch {

   static String TheArch = "gvm";

   static class TYPES {
      public int int_, long_, float_, double_, char_, bool_, addr_, align_;
      public TYPES (int int_, int long_, int float_, int double_, int char_, int bool_, int addr_, int align_) {
	   this.int_ = int_;
	   this.long_ = long_;
	   this.float_ = float_;
	   this.double_ = double_;
	   this.char_ = char_;
	   this.bool_ = bool_;
	   this.addr_ = addr_;
	   this.align_ = align_;
       }
   }

    static java.util.Hashtable TypeSize = new java.util.Hashtable();
    static {
       TypeSize.put("gvm",      new TYPES(1,1,1,1,1,1,1,1));
       TypeSize.put("luca-vm",  new TYPES(8,8,8,8,8,8,8,8));
       TypeSize.put("alpha",    new TYPES(4,8,4,8,1,1,8,8));
       TypeSize.put("simple-sparc-v9", new TYPES(8,8,8,8,8,8,8,8));
       TypeSize.put("sparc-v9", new TYPES(8,8,8,8,1,8,8,8));
       TypeSize.put("sparc",    new TYPES(4,4,4,8,1,1,4,4));
       TypeSize.put("mips",     new TYPES(4,4,4,8,1,4,4,4));
       TypeSize.put("32bit",    new TYPES(4,4,4,4,1,1,4,4));
       TypeSize.put("64bit",    new TYPES(8,8,8,8,1,1,8,8));
       TypeSize.put("null",     new TYPES(0,0,0,0,0,0,0,1));
   }

   public static int SystemTypeSize (String arch, String type) {
       TYPES t = (TYPES) TypeSize.get(arch);
            if (type.equals("int"    )) return t.int_;
       else if (type.equals("long"   )) return t.long_;
       else if (type.equals("float"  )) return t.float_;
       else if (type.equals("double" )) return t.double_;
       else if (type.equals("char"   )) return t.char_;
       else if (type.equals("bool"   )) return t.bool_;
       else if (type.equals("addr"   )) return t.addr_;
       else return -1;
   }

   public static void SetArch(String arch) {
      TheArch = arch;
   }

   public static String GetArch() {
      return TheArch;
   }

   public static int GetBound (int size) {
   switch (size) {
      case 1 : return 1;
      case 2 : return 2;
      case 3 : return 4;
      case 4 : return 4;
      default : 
         TYPES t = (TYPES) TypeSize.get(TheArch);
         return t.align_;
    }
}

public static int AddAlign (int offset, TypeSy type) {
   int size = type.GetSize();
   return size + CalcOffset(offset, type);
}

 public static int CalcOffset (int offset, TypeSy type) {
   int size = type.GetSize();
   int bound = GetBound(size);
   int extra = offset % bound;
   if (extra == 0)
      return offset;
   else
      return offset + (bound - extra);
}

}
