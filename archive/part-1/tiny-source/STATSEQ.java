/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

public class STATSEQ extends AST {
    public STAT stat;
    public STATSEQ next;

    public STATSEQ() {}

    public STATSEQ(STAT stat, STATSEQ next) {
      this.stat = stat;
      this.next = next;
    }

    public String toString(String indent) {
       return indent + 
               "(STATSEQ\n" + 
                indent + "   " + stat.toString() + "\n" +
                next.toString(indent + "   ") + 
                "\n" + indent + ")";
    }
}
