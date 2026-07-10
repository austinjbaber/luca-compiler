package sym;

public class Label implements java.lang.Comparable {
    static int current = 0;
    int lab;
    public Label() {
	lab = current++;
    }

    public int getLabel() {
	return lab;
    }
 
    public int compareTo(Object o) {
	if (((Label)o).getLabel()==lab) return 0; else return -1;
    }

    public String toString() {
	return "L" + lab;
    }

}
