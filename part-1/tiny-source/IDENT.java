/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

public class IDENT extends EXPR {
    public String ident;
    public IDENT(String ident) {this.ident = ident;}
    public String toString() {return "(IDENT " + ident + ")";}
}
