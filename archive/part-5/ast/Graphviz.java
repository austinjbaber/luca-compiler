package ast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Graphviz {
 
    private static final String[] colors = {   
      "\"#7293CB\"",
      "\"#E1974C\"",
      "\"#84BA51\"",
      "\"#D35E60\"",
      "\"#808585\"",
      "\"#9067A7\"",
      "\"#AB6857\"",
      "\"#CCC210\""
    };

    private static final String[] shapes = {   
      "\"box\"",
      "\"ellipse\"",
      "\"diamond\""
    };

    public Graphviz() {}

    static int declColor = 0;
    static int declShape = 0;

    static int stmtColor = 1;
    static int stmtShape = 0;

    static int exprColor = 2;
    static int exprShape = 0;

    static int nodeNumber = 0;
    static int edgeNumber = 0;
    static String nodes = "";
    static String edges = "";

    static int addNode (
        String kind,
        int shape,
        int color,
        String [][] attributes) {
      int n = nodeNumber;
      String label = "";
      if (attributes.equals("")) {
         label = "label=\"" + kind + "\";";
      } else {
         String attrs = "";
         for (String[] pair : attributes) {
            if (attrs.equals("")) {
               attrs = pair[0] + "=" + pair[1];
            } else {
               attrs = attrs + "\n" + pair[0] + "=" + pair[1];
            }
         };
         label = "label=\"" + kind + "\n" + attrs + "\"; ";
      };

      String node = 
           "   node" + n + "[" + 
          label +
          "style=\"filled\";" +
          "shape=" + shapes[shape] + ";" +
          "color=\"black\";" +
          "fillcolor=" + colors[color] + ";" +
       "   ];";
       nodes = nodes + "\n" + node;
       nodeNumber++;
       return n;
    }

    static void addEdge (
        int from,
        int to) {
      String edge = 
           "   node" + from  + 
           " -> " + 
           "node" + to + " []";
       edges = edges + "\n" + edge;
    }

    static String mkGraph (
        String name) {
      String graph = 
           "digraph " + name  + " {" +
              nodes + 
              edges +
           "}";
      return graph;
    }

    public static void clear () {
       nodeNumber = 0;
       edgeNumber = 0;
       nodes = "";
       edges = "";
    }

    public static void toFile (String name, String path) throws IOException {
        String content = mkGraph(name);
        Path file = Paths.get(path);

        try {
            Files.writeString(file, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

