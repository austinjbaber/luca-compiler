/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

public class PRINT extends STAT {
    public EXPR expr;
    public PRINT(EXPR expr) {this.expr = expr;}
    public String toString() {return "(PRINT " + expr.toString() + ")";}
}
