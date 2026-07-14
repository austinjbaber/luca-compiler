package lexer;

class Char {
   public final static int WHITESPACE = 0;
   public final static int PLUS = 1;
   public final static int DASH = 2;
   public final static int STAR = 3;
   public final static int SLASH = 4;
   public final static int PERCENT = 5;
   public final static int COLON = 6;
   public final static int BANG = 7;
   public final static int COMMA = 8;
   public final static int LBRACK = 9;
   public final static int RBRACK = 10;
   public final static int LPAREN = 11;
   public final static int RPAREN = 12;
   public final static int PERIOD = 13;
   public final static int SEMICOLON = 14;
   public final static int CARET = 15;
   public final static int ATCHAR = 16;
   public final static int BACKQUOTE = 17;
   public final static int EQ = 18;
   public final static int GT = 19;
   public final static int LT = 20;
   public final static int HASH = 21;
   public static final int LETTER = 22;
   public static final int DIGIT = 23;
   public static final int NEWLINE = 24;
   public static final int DOUBLEQUOTE = 25;
   public static final int SINGLEQUOTE = 26;
   public static final int Ee = 27;
   public static final int EOF = 28;
   public static final int ILLEGAL = 29;

   public static final int FIRST = 0;
   public static final int LAST = ILLEGAL;

   public static int[] charClass = new int[256];

   static {
      for(int i=0; i<256; i++) charClass[i] = ILLEGAL;
      for(int i=(int)'a'; i<=(int)'z'; i++) charClass[i] = LETTER;
      for(int i=(int)'A'; i<=(int)'Z'; i++) charClass[i] = LETTER;
      for(int i=(int)'0'; i<=(int)'9'; i++) charClass[i] = DIGIT;
      charClass[(int)'e'] = Ee;
      charClass[(int)'E'] = Ee;
      charClass[(int)' '] = WHITESPACE;
      charClass[(int)'\t'] = WHITESPACE;
      charClass[(int)'\n'] = NEWLINE;
      charClass[(int)'+'] = PLUS;
      charClass[(int)'-'] = DASH;
      charClass[(int)'*'] = STAR;
      charClass[(int)'/'] = SLASH;
      charClass[(int)'%'] = PERCENT;
      charClass[(int)':'] = COLON;
      charClass[(int)'!'] = BANG;
      charClass[(int)','] = COMMA;
      charClass[(int)'['] = LBRACK;
      charClass[(int)']'] = RBRACK;
      charClass[(int)'('] = LPAREN;
      charClass[(int)')'] = RPAREN;
      charClass[(int)'.'] = PERIOD;
      charClass[(int)';'] = SEMICOLON;
      charClass[(int)'^'] = CARET;
      charClass[(int)'@'] = ATCHAR;
      charClass[(int)'`'] = BACKQUOTE;
      charClass[(int)'\''] = SINGLEQUOTE;
      charClass[(int)'#'] = HASH;
      charClass[(int)'='] = EQ;
      charClass[(int)'>'] = GT;
      charClass[(int)'<'] = LT;
      charClass[(int)'"'] = DOUBLEQUOTE;
   }

   public static int classify(int c) {
      if (c == -1) 
         return EOF;
      else
         return charClass[c];
   }
}