package sym;

public class Env {

// This module contains public TYPEs that manipulate
// environments. An environment is a list of symbol
// tables, as defined in sytab.icn. Since symbol
// tables are sets of integers, an environment is
// essentially a list of sets of numbers. For
// example, the standard environment looks like this:
//     [{1,2,3,4,5,6,7}].

   public java.util.LinkedList List;

    public Env() {
	List = new java.util.LinkedList();
    }

    public Env(java.util.LinkedList list) {
	this.List = list;
    }

    public Env(SyTab s) {
	List = new java.util.LinkedList();
        List.add(s);
    }

// Create a new environment by adding SyTab
// to the beginning of Env.
// PRE:   Env          = [SyTab1, SyTab2, ..., SyTabN]
// POST:  Env          = [SyTab1, SyTab2, ..., SyTabN]
//        ReturnValue  = [SyTab, SyTab1, SyTab2, ..., SyTabN]
public Env cons (SyTab s) {
   java.util.LinkedList L = (java.util.LinkedList)List.clone();
   L.addFirst(s);
   Env c = new Env(L);
   return c;
}

// Create a new environment by adding SyTab
// to the end of Env.
// PRE:   Env          = [SyTab1, SyTab2, ..., SyTabN]
// POST:  Env          = [SyTab1, SyTab2, ..., SyTabN]
//        ReturnValue  = [SyTab1, SyTab2, ..., SyTabN, SyTab]
public Env snoc (SyTab s) {
   Env c = new Env();
   c.List = (java.util.LinkedList)this.List.clone();
   c.List.addLast(s);
   return c;
}

public Env append (Env E2) {
   Env c = new Env();
   c.List = (java.util.LinkedList)this.List.clone();
   c.List.addAll(E2.List);
   return c;
}

// Go through the symbol tables in Env, from the
// first one to the last one. In each symbol table
// look for the identifier Name. If it is found,
// return the corresponding symbol. Otherwise,
// continue with the next symbol table. If Name is
// not in any of the symbol tables, return standard_NoSy.
public Symbol locateByName (String Name) {
   java.util.ListIterator i = List.listIterator();
   while (i.hasNext()) {
       SyTab s = (SyTab) i.next();
       Symbol sy = s.locateByName(Name);
       if (sy != null /*Standard.NoSy*/) 
	   return sy;
   }
   return null /*Standard.NoSy*/;
}

// ---------------------------------------------------------
// Return a string representation of the environment E,
// in a compact format.
public String toStringOfNames () {
   String L = "[";
   java.util.ListIterator i = List.listIterator();
   while (i.hasNext()) {
       SyTab s = (SyTab) i.next();
       L = L + ((L.equals("["))?"":",") + s.toStringOfNames();
   }
   L = L + "]";
   return L;
}

public String toStringOfSymbols () {
   String L = "[";
   java.util.ListIterator i = List.listIterator();
   while (i.hasNext()) {
       SyTab s = (SyTab) i.next();
       L = L + ((L.equals("["))?"":",") + s.toStringOfSymbols();
   }
   L = L + "]";
   return L;
}

public String toString () {
    return toStringOfNames();
}

// Print the environment.
public void show () {
    System.out.println(toStringOfNames());
}

    public static void main (String[] args) {
	Env e1 = new Env();
        System.out.println("1:" + e1.toString());

        SyTab s1 = new SyTab();
        s1 = s1.insert(Standard.IntType);
        s1 = s1.insert(Standard.BoolType);
        System.out.println("2:" + s1.toString());
        Env e2 = e1.cons(s1);
        System.out.println("3:" + e1.toString());
        System.out.println("4:" + e2.toString());

        SyTab s2 = new SyTab();
        s2 = s2.insert(Standard.RealType);
        s2 = s2.insert(Standard.CharType);
        System.out.println("5:" + s2.toString());
        Env e3 = e2.cons(s2);
        System.out.println("6:" + e1.toString());
        System.out.println("7:" + e2.toString());
        System.out.println("8:" + e3.toString());
    }

}
