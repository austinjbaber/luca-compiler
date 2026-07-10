package sym;

public class SyTab implements Cloneable {

    public java.util.HashSet Ids;

    public SyTab() {
	Ids = new java.util.HashSet();
    }

    public SyTab(sym.Symbol sy) {
	Ids = new java.util.HashSet();
        Ids.add(sy);
    }

    public SyTab clone () {
        SyTab s = new SyTab();
        s.Ids = (java.util.HashSet)Ids.clone();
        return s;
    }


    // Return a new symbol table consisting of the
    // elements from S1 and S2.
public SyTab merge (SyTab S2) {
    SyTab s = this.clone();
    s.Ids.addAll(S2.Ids);
    return s;
}

// Return a fresh copy of SyTab to which Sy
// has been added.
public SyTab insert (Symbol sy) {
    SyTab c = this.clone();
    c.Ids.add(sy);
    return c;
}

// Return the symbol whose name is Name, if
// it exists in SyTab. If not, return
// standard_NoSy
public Symbol locateByName (String Name) {
    java.util.Iterator i = Ids.iterator();
    while (i.hasNext()) {
	Symbol sy = (Symbol) i.next();
        if (sy.GetName().equals(Name))
	    return sy;
    }
    return null /*Standard.NoSy*/;
}

public boolean memberById (Symbol s) {
    java.util.Iterator i = Ids.iterator();
    while (i.hasNext()) {
	Symbol sy = (Symbol) i.next();
        if (sy.GetId() == s.GetId())
	    return true;
    }
    return false;
}

// Return a new symbol table consisting of
// the symbols from SyTab, except the symbols
// whose names are Name, if any.
public SyTab deleteByName (String Name) {
    SyTab c = new SyTab();
    java.util.Iterator i = Ids.iterator();
    while (i.hasNext()) {
	Symbol sy = (Symbol) i.next();
        if (!sy.GetName().equals(Name))
	    c = c.insert(sy);
    }
    return c;
}

// Generate all the symbols in SyTab. Typically
// used like this:
//   every sy := Ids(S) do
//      ....
public java.util.Iterator iterator () {
    return Ids.iterator();
}

// Return the number of symbols in SyTab.
public int count () {
    return Ids.size();
}

// ---------------------------------------------------------
// Return a string representing this symbol table
// in a compact format.
public String toString() {
    return toStringOfNames();
}

// Return a string representing this symbol table
// in a readable format.
public String toStringOfNames() {
    String L = "{";
    java.util.Iterator i = Ids.iterator();
    while (i.hasNext()) {
	Symbol sy = (Symbol) i.next();
        L = L + ((L.equals("{"))?"":",")  + sy.GetName();
    }
    L = L + "}";
    return L;
}

// Return a string representing this symbol table
// in a complete format.
public String toStringOfSymbols() {
    String L = "{";
    java.util.Iterator i = Ids.iterator();
    while (i.hasNext()) {
	Symbol sy = (Symbol) i.next();
        L = L + ((L.equals("{"))?"":",")  + sy.toString();
    }
    L = L + "}";
    return L;
}


// Print this symbol table.
public void show() {
    System.out.println(toStringOfSymbols());
}


    public static void main (String[] args) {
        SyTab s1 = new SyTab();
        s1 = s1.insert(Standard.IntType);
        s1 = s1.insert(Standard.BoolType);
        System.out.println("1:" + s1.toString());

        SyTab s2 = new SyTab();
        s2 = s2.insert(Standard.RealType);
        s2 = s2.insert(Standard.CharType);
        System.out.println("2:" + s2.toString());

        SyTab s3 = s2.deleteByName("REAL");
        System.out.println("3:" + s2.toString());
        System.out.println("4:" + s3.toString());
        
    }

}
