/* Copyright 2001, Christian Collberg, collberg@cs.arizona.edu. */

public class LABEL extends STAT {
    public int label;
    public LABEL(int label) {this.label = label;}
    public String toString() {return "(LABEL " + label + ")"; }
}
