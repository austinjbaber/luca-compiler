/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

public class ASSIGN extends STAT {
    public String ident;
    public EXPR expr;
    public ASSIGN(String ident, EXPR expr) {this.ident = ident;this.expr = expr;}
    public String toString() {return "(ASSIGN " + ident + ", " + expr.toString() + ")"; }
}
