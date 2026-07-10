package sym;

public class Standard {

   public static BasicType IntType;
   public static BasicType CharType;
   public static BasicType RealType;
   public static BasicType StringType;
   public static BasicType AddrType;
   public static EnumType BoolType;
   public static EnumSy TrueSy;
   public static EnumSy FalseSy;
   public static TypeSy NoType;
   public static Symbol NoSy;
   public static ClassType RootSy;
   public static ConstSy NilSy;
   public static ProcedureSy MainProc;

   public static SyTab TheStandardSyTab;

    public static boolean IsStandardSymbol(Symbol S) {
   return S == BoolType || S == BoolType   || S == TrueSy   ||
          S == FalseSy  || S == NoType     || 
          S == NoSy     || S == IntType    || S == CharType || 
          S == RealType || S == StringType || S == AddrType;
   }

   static {
      IntType    = new BasicType("INTEGER", 0, 0);
      RealType   = new BasicType("REAL", 0, 0);
      CharType   = new BasicType("CHAR", 0, 0);
      StringType = new BasicType("STRING", 0, 0);
    
      SyTab BoolSyTab = new SyTab();

      NoType     = new BasicType("$NOTYPE", 0, 0);
      NoSy       = new TempSy("$NOSYMBOL",NoType, 0, 0);
      AddrType   = new BasicType("$ADDRESS", 0, 0);

      IntType.SetSize(   Arch.SystemTypeSize(Arch.GetArch(), "int"));
      RealType.SetSize(  Arch.SystemTypeSize(Arch.GetArch(), "float"));
      CharType.SetSize(  Arch.SystemTypeSize(Arch.GetArch(), "char"));
      StringType.SetSize(0);
      NoType.SetSize(    0);
      AddrType.SetSize(  Arch.SystemTypeSize(Arch.GetArch(), "addr"));

      SyTab S = new SyTab();
      S = S.insert(IntType);
      S = S.insert(RealType);
      S = S.insert(CharType);

      BoolType = new EnumType("BOOLEAN", 0, 0);
      SyTab F = new SyTab();

      TrueSy = new EnumSy("TRUE", 0, 0);
      TrueSy.SetType(BoolType);
      TrueSy.SetEnumValue(1);
      F = F.insert(TrueSy);

      FalseSy = new EnumSy("FALSE", 0, 0);
      FalseSy.SetType(BoolType);
      FalseSy.SetEnumValue(0);
      F = F.insert(FalseSy);

      BoolType.SetEnumElements(F);
      BoolType.SetSize(Arch.SystemTypeSize(Arch.GetArch(), "bool"));

      S = S.insert(BoolType);
      S = S.insert(TrueSy);
      S = S.insert(FalseSy);

      RootSy = new ClassType("OBJECT", 0, 0);
      RootSy.SetSize(Arch.SystemTypeSize(Arch.GetArch(), "addr"));
      RootSy.SetFieldSize(Arch.SystemTypeSize(Arch.GetArch(), "addr"));
      RootSy.SetMethodSize(0);
      RootSy.SetFields(new SyTab());

      NilSy = new ConstSy("NIL", 0, 0);
      NilSy.SetSize(Arch.SystemTypeSize(Arch.GetArch(), "addr"));
      NilSy.SetConstantValue(new IntValue(0));
      NilSy.SetType(RootSy);

      S = S.insert(RootSy);
      S = S.insert(NilSy);

      TheStandardSyTab = S;
   }

   public static SyTab SyTab () {
      return TheStandardSyTab;
   }

 public static Env env () {
     Env e = new Env(TheStandardSyTab);
     return e;
   }

}
