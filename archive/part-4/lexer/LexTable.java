package lexer;

class LexTable {
   public static int[][] NEXTSTATE = new int[100][];
   public static boolean[] ACCEPT = new boolean[100];
   public static int[] TOKEN = new int[100];
   public static boolean[][] ADVANCE = new boolean[100][];

   static int position;
   static StringBuilder value;

   static final int SKIP = 0; // consume but don't return a token

   // state numbers
   static final int S_START = 0;
   static final int S_WS = 1;
   static final int S_PLUS = 2;
   static final int S_MINUS = 3;
   static final int S_STAR = 4;
   static final int S_SLASH = 5;
   static final int S_PERCENT = 6;
   static final int S_BANG = 7;
   static final int S_COMMA = 8;
   static final int S_LBRACK = 9;
   static final int S_RBRACK = 10;
   static final int S_LPAREN = 11;
   static final int S_RPAREN = 12;
   static final int S_SEMI = 13;
   static final int S_CARET = 14;
   static final int S_AT = 15;
   static final int S_BACKQUOTE = 16;
   static final int S_EQ = 17;
   static final int S_HASH = 18;
   static final int S_GT = 19;
   static final int S_GE = 20;
   static final int S_LT = 21;
   static final int S_LE = 22;
   static final int S_COLON = 23;
   static final int S_COLONEQ = 24;
   static final int S_PERIOD = 25;
   static final int S_IDENT = 26;
   static final int S_INT = 27;
   static final int S_REAL_AFTER_INT = 28; // digit+.digit*
   static final int S_REAL_FRAC = 29; // .digit+
   static final int S_EXP_START = 30; // after E
   static final int S_EXP_SIGN = 31; // after E and + or -
   static final int S_EXP_DIGITS = 32; // exponent digits
   static final int S_STR_BODY = 33;
   static final int S_STR_END = 34;
   static final int S_CHAR_AFTER_QUOTE = 35;
   static final int S_CHAR_GOT_CHAR = 36;
   static final int S_CHAR_END = 37;
   static final int S_LINE_COMMENT = 38;
   static final int S_BLOCK_COMMENT = 39;
   static final int S_BLOCK_COMMENT_STAR = 40;
   static final int S_BLOCK_COMMENT_END = 41;
   static final int S_ERR_UNTERM_STRING = 42;
   static final int S_ERR_REALLIT = 43;
   static final int S_ERR_ILLEGAL_CHAR = 44;
   static final int S_ERR_UNTERM_COMMENT = 45;
   static final int S_ERR_UNTERM_CHAR = 46;
   static final int S_ERR_EMPTY_CHAR = 47;
   static final int S_EOF = 48;

   static int tokenStart () {
      return position;
   }

   static String tokenValue() {
      return value.toString();
   }

   private static void accept(int state, int tokenKind) {
      ACCEPT[state] = true;
      TOKEN[state] = tokenKind;
   }

   private static void transition(int from, int charClass, int to) {
      NEXTSTATE[from][charClass] = to;
      ADVANCE[from][charClass] = true;
   }

   private static void transition(int from, int charClass, int to, boolean advance) {
      NEXTSTATE[from][charClass] = to;
      ADVANCE[from][charClass] = advance;
   }

   // fill table at runtime
   static {
      // initialize all to no transition and advance = true
      for (int state = 0; state < NEXTSTATE.length; state++) {
         NEXTSTATE[state] = new int[Char.LAST + 1];
         ADVANCE[state] = new boolean[Char.LAST + 1];
         for (int curr = 0; curr <= Char.LAST; curr++) {
            NEXTSTATE[state][curr] = -1;
            ADVANCE[state][curr] = true;
         }
         ACCEPT[state] = false;
         TOKEN[state] = Token.ILLEGAL;
      }

      // accepting states
      accept(S_WS, SKIP);
      accept(S_PLUS, Token.PLUS);
      accept(S_MINUS, Token.MINUS);
      accept(S_STAR, Token.STAR);
      accept(S_SLASH, Token.SLASH);
      accept(S_PERCENT, Token.PERCENT);
      accept(S_BANG, Token.BANG);
      accept(S_COMMA, Token.COMMA);
      accept(S_LBRACK, Token.LBRACK);
      accept(S_RBRACK, Token.RBRACK);
      accept(S_LPAREN, Token.LPAREN);
      accept(S_RPAREN, Token.RPAREN);
      accept(S_SEMI, Token.SEMICOLON);
      accept(S_CARET, Token.CARET);
      accept(S_AT, Token.ATCHAR);
      accept(S_BACKQUOTE, Token.BACKQUOTE);
      accept(S_EQ, Token.EQ);
      accept(S_HASH, Token.NE);
      accept(S_GT, Token.GT);
      accept(S_GE, Token.GE);
      accept(S_LT, Token.LT);
      accept(S_LE, Token.LE);
      accept(S_COLON, Token.COLON);
      accept(S_COLONEQ, Token.COLONEQ);
      accept(S_PERIOD, Token.PERIOD);
      accept(S_IDENT, Token.IDENT);
      accept(S_INT, Token.INTLIT);
      accept(S_REAL_AFTER_INT, Token.REALLIT);
      accept(S_REAL_FRAC, Token.REALLIT);
      accept(S_EXP_DIGITS, Token.REALLIT);
      accept(S_STR_END, Token.STRINGLIT);
      accept(S_CHAR_END, Token.CHARLIT);
      accept(S_LINE_COMMENT, SKIP);
      accept(S_BLOCK_COMMENT_END, SKIP);
      accept(S_ERR_UNTERM_STRING, Token.ERROR_UNTERMINATED_STRING);
      accept(S_ERR_REALLIT, Token.ERROR_REALLIT);
      accept(S_ERR_ILLEGAL_CHAR, Token.ERROR_ILLEGAL_CHARACTER);
      accept(S_ERR_UNTERM_COMMENT, Token.ERROR_UNTERMINATED_COMMENT);
      accept(S_ERR_UNTERM_CHAR, Token.ERROR_UNTERMINATED_CHAR);
      accept(S_ERR_EMPTY_CHAR, Token.ERROR_EMPTY_CHAR);
      accept(S_EOF, Token.EOF);

      // transitions
      transition(S_START, Char.WHITESPACE, S_WS);
      transition(S_START, Char.NEWLINE, S_WS);
      transition(S_START, Char.PLUS, S_PLUS);
      transition(S_START, Char.DASH, S_MINUS);
      transition(S_START, Char.STAR, S_STAR);
      transition(S_START, Char.SLASH, S_SLASH);
      transition(S_START, Char.PERCENT, S_PERCENT);
      transition(S_START, Char.BANG, S_BANG);
      transition(S_START, Char.COMMA, S_COMMA);
      transition(S_START, Char.LBRACK, S_LBRACK);
      transition(S_START, Char.RBRACK, S_RBRACK);
      transition(S_START, Char.LPAREN, S_LPAREN);
      transition(S_START, Char.RPAREN, S_RPAREN);
      transition(S_START, Char.SEMICOLON, S_SEMI);
      transition(S_START, Char.CARET, S_CARET);
      transition(S_START, Char.ATCHAR, S_AT);
      transition(S_START, Char.BACKQUOTE, S_BACKQUOTE);
      transition(S_START, Char.EQ, S_EQ);
      transition(S_START, Char.HASH, S_HASH);
      transition(S_START, Char.PERIOD, S_PERIOD);
      transition(S_START, Char.GT, S_GT);
      transition(S_START, Char.LT, S_LT);
      transition(S_START, Char.COLON, S_COLON);
      transition(S_START, Char.DOUBLEQUOTE, S_STR_BODY);
      transition(S_START, Char.SINGLEQUOTE, S_CHAR_AFTER_QUOTE);
      transition(S_START, Char.LETTER, S_IDENT);
      transition(S_START, Char.Ee, S_IDENT);
      transition(S_START, Char.DIGIT, S_INT);
      transition(S_START, Char.EOF, S_EOF, false); // method overload
      transition(S_START, Char.ILLEGAL, S_ERR_ILLEGAL_CHAR);
      transition(S_WS, Char.WHITESPACE, S_WS);
      transition(S_WS, Char.NEWLINE, S_WS);
      transition(S_COLON, Char.EQ, S_COLONEQ);
      transition(S_GT, Char.EQ, S_GE);
      transition(S_LT, Char.EQ, S_LE);

      transition(S_MINUS, Char.DASH, S_LINE_COMMENT);
      for (int curr = Char.FIRST; curr <= Char.LAST; curr++) {
         if (curr == Char.NEWLINE || curr == Char.EOF) continue; // end comment
         transition(S_LINE_COMMENT, curr, S_LINE_COMMENT);
      }

      transition(S_LPAREN, Char.STAR, S_BLOCK_COMMENT);
      for (int curr = Char.FIRST; curr <= Char.LAST; curr++) {
         if (curr == Char.STAR || curr == Char.EOF) continue; // end comment
         transition(S_BLOCK_COMMENT, curr, S_BLOCK_COMMENT);
      }
      transition(S_BLOCK_COMMENT, Char.STAR, S_BLOCK_COMMENT_STAR);
      transition(S_BLOCK_COMMENT, Char.EOF, S_ERR_UNTERM_COMMENT, false); // method overload

      transition(S_BLOCK_COMMENT_STAR, Char.RPAREN, S_BLOCK_COMMENT_END);
      transition(S_BLOCK_COMMENT_STAR, Char.STAR, S_BLOCK_COMMENT_STAR);
      transition(S_BLOCK_COMMENT_STAR, Char.EOF, S_ERR_UNTERM_COMMENT, false); // method overload
      for (int curr = Char.FIRST; curr <= Char.LAST; curr++) {
         if (curr == Char.RPAREN || curr == Char.STAR || curr == Char.EOF) continue; // end comment
         transition(S_BLOCK_COMMENT_STAR, curr, S_BLOCK_COMMENT);
      }

      transition(S_IDENT, Char.LETTER, S_IDENT);
      transition(S_IDENT, Char.Ee, S_IDENT);
      transition(S_IDENT, Char.DIGIT, S_IDENT);
      transition(S_INT, Char.DIGIT, S_INT);
      transition(S_INT, Char.PERIOD, S_REAL_AFTER_INT);
      transition(S_PERIOD, Char.DIGIT, S_REAL_FRAC);
      transition(S_REAL_AFTER_INT, Char.DIGIT, S_REAL_AFTER_INT);
      transition(S_REAL_AFTER_INT, Char.Ee, S_EXP_START);
      transition(S_REAL_FRAC, Char.DIGIT, S_REAL_FRAC);
      transition(S_REAL_FRAC, Char.Ee, S_EXP_START);
      transition(S_EXP_START, Char.PLUS, S_EXP_SIGN);
      transition(S_EXP_START, Char.DASH, S_EXP_SIGN);
      transition(S_EXP_START, Char.DIGIT, S_EXP_DIGITS);
      for (int curr = Char.FIRST; curr <= Char.LAST; curr++) {
         if (curr == Char.PLUS || curr == Char.DASH || curr == Char.DIGIT) continue; // followed only by +, -, or digits
         transition(S_EXP_START, curr, S_ERR_REALLIT);
      }

      transition(S_EXP_SIGN, Char.DIGIT, S_EXP_DIGITS);
      for (int curr = Char.FIRST; curr <= Char.LAST; curr++) {
         if (curr == Char.DIGIT) continue; // followed only by digits
         transition(S_EXP_SIGN, curr, S_ERR_REALLIT);
      }

      transition(S_EXP_DIGITS, Char.DIGIT, S_EXP_DIGITS);
      transition(S_STR_BODY, Char.DOUBLEQUOTE, S_STR_END);
      transition(S_STR_BODY, Char.NEWLINE, S_ERR_UNTERM_STRING);
      transition(S_STR_BODY, Char.EOF, S_ERR_UNTERM_STRING, false);
      for (int curr = Char.FIRST; curr <= Char.LAST; curr++) {
         if (curr == Char.DOUBLEQUOTE || curr == Char.NEWLINE || curr == Char.EOF) continue; // end string
         transition(S_STR_BODY, curr, S_STR_BODY);
      }

      transition(S_CHAR_AFTER_QUOTE, Char.SINGLEQUOTE, S_ERR_EMPTY_CHAR);
      transition(S_CHAR_AFTER_QUOTE, Char.NEWLINE, S_ERR_UNTERM_CHAR);
      transition(S_CHAR_AFTER_QUOTE, Char.EOF, S_ERR_UNTERM_CHAR, false);
      for (int curr = Char.FIRST; curr <= Char.LAST; curr++) {
         if (curr == Char.SINGLEQUOTE || curr == Char.NEWLINE || curr == Char.EOF) continue; // end char
         transition(S_CHAR_AFTER_QUOTE, curr, S_CHAR_GOT_CHAR);
      }

      transition(S_CHAR_GOT_CHAR, Char.SINGLEQUOTE, S_CHAR_END);
      transition(S_CHAR_GOT_CHAR, Char.NEWLINE, S_ERR_UNTERM_CHAR);
      transition(S_CHAR_GOT_CHAR, Char.EOF, S_ERR_UNTERM_CHAR, false);
      for (int curr = Char.FIRST; curr <= Char.LAST; curr++) {
         if (curr == Char.SINGLEQUOTE || curr == Char.NEWLINE || curr == Char.EOF) continue; // end char
         transition(S_CHAR_GOT_CHAR, curr, S_ERR_UNTERM_CHAR);
      }
   }
}
