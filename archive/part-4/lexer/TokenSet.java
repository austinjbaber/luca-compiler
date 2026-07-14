package lexer;
public class TokenSet {

   java.util.HashSet set = new java.util.HashSet();

   public TokenSet (int[] b) {
      for(int i=0; i<b.length; i++) 
         set.add(java.lang.Integer.valueOf(b[i]));
   }

   public TokenSet (TokenSet b) {
      set.addAll(b.set);
   }

   public TokenSet (java.util.HashSet b) {
      set.addAll(b);
   }

   public TokenSet (int tok) {
      set.add(java.lang.Integer.valueOf(tok));
   }

   public TokenSet (int tok1, int tok2) {
      set.add(java.lang.Integer.valueOf(tok1));
      set.add(java.lang.Integer.valueOf(tok2));
   }

   public TokenSet (int tok1, int tok2, int tok3) {
      set.add(java.lang.Integer.valueOf(tok1));
      set.add(java.lang.Integer.valueOf(tok2));
      set.add(java.lang.Integer.valueOf(tok3));
   }

   public TokenSet union(TokenSet b) {
      java.util.HashSet s = new java.util.HashSet();
      s.addAll(set);
      s.addAll(b.set);
      return new TokenSet(s);
   }

   public boolean member(int kind) {
      return set.contains(java.lang.Integer.valueOf(kind));
   }

   public boolean member(Token tok) {
      return member(tok.getKind());
   }

   public String toString(boolean brackets) {
      String[] r = new String[set.size()];
      java.util.Iterator iter = set.iterator();
      int i=0;
      while (iter.hasNext()) {
         r[i] = Token.token2stringPrint(((java.lang.Integer)iter.next()).intValue());
         i++;
      }

      java.util.Arrays.sort(r);

      String s = brackets?"{":"";
      for(int j=0; j<r.length; j++) 
         s += r[j] + ((j<r.length-1)?",":"");
      s += brackets?"}":"";
      return s;
   }
}
