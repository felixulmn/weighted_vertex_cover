package com.felixullmann.graphs;

import robustTwoClub.graph.RtcGraph;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

public class IntegerGraph {

    public Set<Integer> vertices;
    public HashMap<Integer, Set<Integer>> adjacency;
    public IntegerGraph(String fileName) {
        try {
            File file = new File(fileName);
            final LineNumberReader reader = new LineNumberReader(new FileReader(file));

            // prepare vertex set and adjacency map
            int vertexCount = Integer.parseInt(reader.readLine());
            vertices = new Set<>(vertexCount*2);
            adjacency = new HashMap<>(vertexCount*2);

            for (int v = 0; v < vertexCount; v++) {
                vertices.add(v);
                adjacency.put(v,new Set<>());
            }

            // populate graph with edges
            String str;
            while ((str = reader.readLine()) != null) {
                StringTokenizer tokens = new StringTokenizer(str);
                Integer a = Integer.parseInt(tokens.nextToken());
                Integer b = Integer.parseInt(tokens.nextToken());

                adjacency.get(a).add(b);
                adjacency.get(b).add(a);
            }

        } catch (IOException e) {
            System.out.println("Could not locate input file '"+fileName+"'.");
            System.exit(0);
        }
    }
    public static Set<Integer> getNeighbors(Integer vertex, HashMap<Integer,Set<Integer>> adjacency) {
        return (Set<Integer>) adjacency.get(vertex).clone();
    }

    public static boolean isIndependent(Set<Integer> vertexSet, HashMap<Integer,Set<Integer>> adjacency) {
        //System.out.println(String.format("Vertexset: %s", vertexSet));
        for(Integer v : vertexSet) {
            //System.out.println(String.format("Vertex: %s - Neighbours: %s", v, this.adjacency.get(v)));
            for(Integer n_v : adjacency.get(v)) {
                if(vertexSet.contains(n_v))
                    return false;
            }
        }
        return true;
    }
}
