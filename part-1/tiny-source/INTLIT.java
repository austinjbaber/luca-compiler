/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

public class INTLIT extends EXPR {
    public int val;
    public INTLIT(int val) {this.val = val;}
    public String toString() {return "(INTLIT " + val + ")";}
}
