package auxx;

public class Error {
    public static void Internal(String Proc, String Msg) {
    System.err.println("*** Internal error in " + Proc + ". " + Msg);
}

static int Sems = 0;

public static int SemanticCount() {
    return Sems;
}

public static void Sem (int Pos, String Msg) {
    //   System.err.println("ERROR (Line " + Pos + "), " + Msg);
    System.err.println("<SEMANTIC_ERROR pos=\"" + Pos + "\" message=\"" + Msg + "\"" + "/>");
}

public static void SemId (int Pos, String Msg, String Id) {
    System.err.println("<SEMANTIC_ERROR pos=\"" + Pos + "\" message=\"" + Msg + "\" argument=\"" + Id + "\"" + "/>");
		      //    Sem(Pos, Msg + ": '" + Id + "'");
    Sems++;
}

}


