public class GOTO extends STAT {
    public int label;
    public GOTO(int label) {this.label = label;}
    public String toString() {return "(GOTO " + label + ")"; }
}
