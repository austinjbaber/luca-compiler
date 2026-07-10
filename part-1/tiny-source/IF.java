public class IF extends STAT {
    public EXPR condition;
    public int label;
    IF(EXPR condition, int label) {this.condition = condition; this.label = label;}
    public String toString() {return "(IF " + condition.toString() + " GOTO " + label + ")";}
}
