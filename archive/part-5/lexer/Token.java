/* Copyright 2009, Christian Collberg, collberg@cs.arizona.edu. */
package lexer;

public class Token {
   public final static int PLUS = 1;
   public final static int MINUS = 2;
   public final static int STAR = 3;
   public final static int SLASH = 4;
   public final static int PERCENT = 5;
   public final static int COLONEQ = 6;
   public final static int BANG = 7;
   public final static int COLON = 8;
   public final static int COMMA = 9;
   public final static int LBRACK = 10;
   public final static int RBRACK = 11;
   public final static int LPAREN = 12;
   public final static int RPAREN = 13;
   public final static int PERIOD = 14;
   public final static int SEMICOLON = 15;
   public final static int CARET = 16;
   public final static int ATCHAR = 17;
   public final static int BACKQUOTE = 18;
   public final static int EQ = 19;
   public final static int GE = 20;
   public final static int GT = 21;
   public final static int LT = 22;
   public final static int LE = 23;
   public final static int NE = 24;
   public final static int INTLIT = 25;
   public final static int REALLIT = 26;
   public final static int STRINGLIT = 27;
   public final static int IDENT = 28;
   public final static int AND = 29;
   public final static int OR = 30;
   public final static int ISA = 31;
   public final static int NARROW = 32;
   public final static int TRUNC = 33;
   public final static int FLOAT = 34;
   public final static int NOT = 35;
   public final static int PROGRAM = 36;
   public final static int PROCEDURE = 37;
   public final static int VAR = 38;
   public final static int BEGIN = 39;
   public final static int END = 40;
   public final static int FOR = 41;
   public final static int NEW = 42;
   public final static int TYPE = 43;
   public final static int WRITE = 44;
   public final static int READ = 45;
   public final static int WRITELN = 46;
   public final static int ENDFOR = 47;
   public final static int EXTENDS = 48;
   public final static int REF = 49;
   public final static int ENUM = 50;
   public final static int CONST = 51;
   public final static int ARRAY = 52;
   public final static int RECORD = 53;
   public final static int METHOD = 54;
   public final static int CLASS = 55;
   public final static int OF = 56;
   public final static int IN = 57;
   public final static int TO = 58;
   public final static int DO = 59;
   public final static int BY = 60;
   public final static int IF = 61;
   public final static int THEN = 62;
   public final static int ELSE = 63;
   public final static int ENDIF = 64;
   public final static int LOOP = 65;
   public final static int ENDLOOP = 66;
   public final static int EXIT = 67;
   public final static int WHILE = 68;
   public final static int REPEAT = 69;
   public final static int UNTIL = 70;
   public final static int ENDDO = 71;
   public final static int EOF = 72;
   public final static int CHARLIT = 73;
   public final static int ILLEGAL = 74;
   public final static int ERROR_UNTERMINATED_STRING = 75;
   public final static int ERROR_REALLIT = 76;
   public final static int ERROR_ILLEGAL_CHARACTER = 77;
   public final static int ERROR_UNTERMINATED_COMMENT = 78;
   public final static int ERROR_UNTERMINATED_CHAR = 79;
   public final static int ERROR_EMPTY_CHAR = 80;

   static String[] token2string = new String[100];
   static String[] token2stringPrint = new String[100];
   static java.util.Hashtable keywords = new java.util.Hashtable();

   static {
      token2string[PLUS] ="PLUS";
      token2string[MINUS] ="MINUS";
      token2string[STAR] ="STAR";
      token2string[SLASH] ="SLASH";
      token2string[PERCENT] ="PERCENT";
      token2string[COLONEQ] ="COLONEQ";
      token2string[BANG] ="BANG";
      token2string[COLON] ="COLON";
      token2string[COMMA] ="COMMA";
      token2string[LBRACK] ="LBRACK";
      token2string[RBRACK] ="RBRACK";
      token2string[LPAREN] ="LPAREN";
      token2string[RPAREN] ="RPAREN";
      token2string[PERIOD] ="PERIOD";
      token2string[SEMICOLON] ="SEMICOLON";
      token2string[CARET] ="CARET";
      token2string[ATCHAR] ="ATCHAR";
      token2string[BACKQUOTE] ="BACKQUOTE";
      token2string[EQ] ="EQ";
      token2string[GE] ="GE";
      token2string[GT] ="GT";
      token2string[LT] ="LT";
      token2string[LE] ="LE";
      token2string[NE] ="NE";
      token2string[INTLIT] ="INTLIT";
      token2string[REALLIT] ="REALLIT";
      token2string[STRINGLIT] ="STRINGLIT";
      token2string[CHARLIT] ="CHARLIT";
      token2string[IDENT] ="IDENT";
      token2string[AND] ="AND";
      token2string[OR] ="OR";
      token2string[ISA] ="ISA";
      token2string[NARROW] ="NARROW";
      token2string[TRUNC] ="TRUNC";
      token2string[FLOAT] ="FLOAT";
      token2string[NOT] ="NOT";
      token2string[PROGRAM] ="PROGRAM";
      token2string[PROCEDURE] ="PROCEDURE";
      token2string[VAR] ="VAR";
      token2string[BEGIN] ="BEGIN";
      token2string[END] ="END";
      token2string[FOR] ="FOR";
      token2string[NEW] ="NEW";
      token2string[TYPE] ="TYPE";
      token2string[WRITE] ="WRITE";
      token2string[READ] ="READ";
      token2string[WRITELN] ="WRITELN";
      token2string[ENDFOR] ="ENDFOR";
      token2string[EXTENDS] ="EXTENDS";
      token2string[REF] ="REF";
      token2string[ENUM] ="ENUM";
      token2string[CONST] ="CONST";
      token2string[ARRAY] ="ARRAY";
      token2string[RECORD] ="RECORD";
      token2string[METHOD] ="METHOD";
      token2string[CLASS] ="CLASS";
      token2string[OF] ="OF";
      token2string[IN] ="IN";
      token2string[TO] ="TO";
      token2string[DO] ="DO";
      token2string[BY] ="BY";
      token2string[IF] ="IF";
      token2string[THEN] ="THEN";
      token2string[ELSE] ="ELSE";
      token2string[ENDIF] ="ENDIF";
      token2string[LOOP] ="LOOP";
      token2string[ENDLOOP] ="ENDLOOP";
      token2string[EXIT] ="EXIT";
      token2string[WHILE] ="WHILE";
      token2string[REPEAT] ="REPEAT";
      token2string[UNTIL] ="UNTIL";
      token2string[ENDDO] ="ENDDO";
      token2string[EOF] ="EOF";
      token2string[ERROR_UNTERMINATED_STRING] ="ERROR_UNTERMINATED_STRING";
      token2string[ERROR_UNTERMINATED_CHAR] ="ERROR_UNTERMINATED_CHAR";
      token2string[ERROR_EMPTY_CHAR] ="ERROR_EMPTY_CHAR";
      token2string[ERROR_REALLIT] ="ERROR_REALLIT";
      token2string[ERROR_ILLEGAL_CHARACTER] ="ERROR_ILLEGAL_CHARACTER";
      token2string[ERROR_UNTERMINATED_COMMENT] ="ERROR_UNTERMINATED_COMMENT";

      token2stringPrint[PLUS] ="+";
      token2stringPrint[MINUS] ="-";
      token2stringPrint[STAR] ="*";
      token2stringPrint[SLASH] ="/";
      token2stringPrint[PERCENT] ="%";
      token2stringPrint[COLONEQ] =":=";
      token2stringPrint[BANG] ="!";
      token2stringPrint[COLON] =":";
      token2stringPrint[COMMA] =",";
      token2stringPrint[LBRACK] ="[";
      token2stringPrint[RBRACK] ="]";
      token2stringPrint[LPAREN] ="(";
      token2stringPrint[RPAREN] =")";
      token2stringPrint[PERIOD] =".";
      token2stringPrint[SEMICOLON] =";";
      token2stringPrint[CARET] ="^";
      token2stringPrint[ATCHAR] ="@";
      token2stringPrint[BACKQUOTE] ="`";
      token2stringPrint[EQ] ="=";
      token2stringPrint[GE] =">=";
      token2stringPrint[GT] =">";
      token2stringPrint[LT] ="<";
      token2stringPrint[LE] ="<=";
      token2stringPrint[NE] ="#";
      token2stringPrint[INTLIT] ="integer";
      token2stringPrint[REALLIT] ="real";
      token2stringPrint[STRINGLIT] ="string";
      token2stringPrint[CHARLIT] ="char";
      token2stringPrint[IDENT] ="identifier";

      keywords.put("AND",java.lang.Integer.valueOf(AND));
      keywords.put("OR",java.lang.Integer.valueOf(OR));
      keywords.put("ISA",java.lang.Integer.valueOf(ISA));
      keywords.put("NARROW",java.lang.Integer.valueOf(NARROW));
      keywords.put("TRUNC",java.lang.Integer.valueOf(TRUNC));
      keywords.put("FLOAT",java.lang.Integer.valueOf(FLOAT));
      keywords.put("NOT",java.lang.Integer.valueOf(NOT));
      keywords.put("PROGRAM",java.lang.Integer.valueOf(PROGRAM));
      keywords.put("PROCEDURE",java.lang.Integer.valueOf(PROCEDURE));
      keywords.put("VAR",java.lang.Integer.valueOf(VAR));
      keywords.put("BEGIN",java.lang.Integer.valueOf(BEGIN));
      keywords.put("END",java.lang.Integer.valueOf(END));
      keywords.put("FOR",java.lang.Integer.valueOf(FOR));
      keywords.put("NEW",java.lang.Integer.valueOf(NEW));
      keywords.put("TYPE",java.lang.Integer.valueOf(TYPE));
      keywords.put("WRITE",java.lang.Integer.valueOf(WRITE));
      keywords.put("READ",java.lang.Integer.valueOf(READ));
      keywords.put("WRITELN",java.lang.Integer.valueOf(WRITELN));
      keywords.put("ENDFOR",java.lang.Integer.valueOf(ENDFOR));
      keywords.put("EXTENDS",java.lang.Integer.valueOf(EXTENDS));
      keywords.put("REF",java.lang.Integer.valueOf(REF));
      keywords.put("ENUM",java.lang.Integer.valueOf(ENUM));
      keywords.put("CONST",java.lang.Integer.valueOf(CONST));
      keywords.put("ARRAY",java.lang.Integer.valueOf(ARRAY));
      keywords.put("RECORD",java.lang.Integer.valueOf(RECORD));
      keywords.put("METHOD",java.lang.Integer.valueOf(METHOD));
      keywords.put("CLASS",java.lang.Integer.valueOf(CLASS));
      keywords.put("OF",java.lang.Integer.valueOf(OF));
      keywords.put("IN",java.lang.Integer.valueOf(IN));
      keywords.put("TO",java.lang.Integer.valueOf(TO));
      keywords.put("DO",java.lang.Integer.valueOf(DO));
      keywords.put("BY",java.lang.Integer.valueOf(BY));
      keywords.put("IF",java.lang.Integer.valueOf(IF));
      keywords.put("THEN",java.lang.Integer.valueOf(THEN));
      keywords.put("ELSE",java.lang.Integer.valueOf(ELSE));
      keywords.put("ENDIF",java.lang.Integer.valueOf(ENDIF));
      keywords.put("LOOP",java.lang.Integer.valueOf(LOOP));
      keywords.put("ENDLOOP",java.lang.Integer.valueOf(ENDLOOP));
      keywords.put("EXIT",java.lang.Integer.valueOf(EXIT));
      keywords.put("WHILE",java.lang.Integer.valueOf(WHILE));
      keywords.put("REPEAT",java.lang.Integer.valueOf(REPEAT));
      keywords.put("UNTIL",java.lang.Integer.valueOf(UNTIL));
      keywords.put("ENDDO",java.lang.Integer.valueOf(ENDDO));
   }

   public static int keyword(String k) {
      java.lang.Integer i = (java.lang.Integer) keywords.get(k);
      if (i == null)
         return IDENT;
      else 
         return i.intValue();
   }

   public int getKind() {
      return kind;
   }

   public String getValue() {
      return value;
   }

   public int getPosition() {
      return position;
   }

   public static String token2string(int tok) {
      return token2string[tok];
   }

   public static String token2stringPrint(int tok) {
      if (token2stringPrint[tok] == null)
         return token2string[tok];
      else
         return token2stringPrint[tok];
   }

   int kind;
   String value;
   int position;

   public Token(int kind, int position, String value) {
      this.kind = kind; 
      this.position = position; 
      this.value = value; 
   }

    public String toString() {
       String val = "";
       if (kind==IDENT || kind==INTLIT || 
           kind == REALLIT || kind == STRINGLIT ||
           kind == CHARLIT)
          val = " value=\"" + value + "\"";
       return "<TOKEN" +
                 " kind=\"" + token2string[kind] + "\"" +
                 " line=\"" + position + "\"" +
                 val +
              "/>";
    }
}



