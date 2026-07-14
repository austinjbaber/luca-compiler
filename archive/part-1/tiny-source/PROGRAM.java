/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

public class PROGRAM extends AST {
    public STATSEQ stats;
    public PROGRAM (STATSEQ stats) {this.stats = stats;}
    public String toString() {
       return "(PROGRAM\n" + 
                   stats.toString("   ") + 
              "\n)";
    }
}
