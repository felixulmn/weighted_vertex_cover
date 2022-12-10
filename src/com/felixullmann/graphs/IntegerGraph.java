package com.felixullmann.graphs;

import robustTwoClub.graph.RtcGraph;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.StringTokenizer;

public class IntegerGraph {

    public Set<Integer> vertices;
    public HashMap<Integer, Set<Integer>> adjacency;

    /**
     * loads graph from file containing graph information in the format from https://csacademy.com/app/graph_editor/
     * @param fileName the file(path) containing the graph
     */
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

    /**
     * Calculates the set of vertices adjacent to a given vertex
     * @param vertex the vertex to find the neighbors of
     * @param adjacency the matrix containing all adjacencies of the underlying graph
     * @return set of integers representing the neighbors
     */
    public static Set<Integer> getNeighbors(Integer vertex, HashMap<Integer,Set<Integer>> adjacency) {
        return (Set<Integer>) adjacency.get(vertex).clone();
    }

    /**
     * Determines if a given vertex set from a graph is independent
     * @param vertexSet the set of vertices to be tested for independence
     * @param adjacency the matrix containing all adjacencies of the underlying graph
     * @return true when vertexSet is independent, false otherwise
     */
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
