package ast;

public class GraphvizData {
   public String[][] attributes = {};
   public AST[] children = {};
   public int shape = 0;
   public int color = 0;

   public GraphvizData (
       String[][] attributes,
       AST[] children,
       int shape,
       int color) {
      this.attributes = attributes;
      this.children = children;
      this.shape = shape;
      this.color = color;
   }

   public GraphvizData (
       String[][] attributes,
       int shape,
       int color) {
      this.attributes = attributes;
      this.shape = shape;
      this.color = color;
   }

   public GraphvizData (
       AST[] children,
       int shape,
       int color) {
      this.children = children;
      this.shape = shape;
      this.color = color;
   }

   public GraphvizData () {
   }

}
