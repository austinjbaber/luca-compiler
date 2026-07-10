/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

import java.util.*;

public class SyTab {
    Hashtable sytab = new Hashtable();
    int currentID = 0;

    // Insert ident into the symbol table, unless it's
    // already there. Assign a new number to the identifier.
    public void insert(String ident) {
       if (!sytab.containsKey(ident))
          sytab.put(ident, new java.lang.Integer(currentID++));
    }

    // Return the number of 'ident'. If 'ident' is not in the
    // symbol table, return -1.
    public int lookup(String ident) {
      if (sytab.containsKey(ident))
         return ((Integer)sytab.get(ident)).intValue();
      else
         return -1;
    }

    // Return the number of identifiers in the table.
    public int size() {
       return sytab.size(); 
    }
}
