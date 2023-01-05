package com.felixullmann.graphs;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

public class IntegerGraph {

    public Set<Integer> vertices;
    public HashMap<Integer, Integer> weights;
    public HashMap<Integer, Set<Integer>> adjacency;

    public IntegerGraph(Set<Integer> vertices, HashMap<Integer, Integer> weights, HashMap<Integer, Set<Integer>> adjacency) {
        this.vertices = vertices;
        this.weights = weights;
        this.adjacency = adjacency;
    }


    /**
     * loads problem for minimum weighted vertex cover from problem sets presented in https://doi.org/10.1007/s43069-021-00084-x
     * @return returns new IntegerGraph instance based on the given problem
     */
    public static IntegerGraph fromVehicleRoutingApplication(String path) {
        try {
            Set<Integer> vertices;
            HashMap<Integer, Set<Integer>> adjacency;
            HashMap<Integer, Integer> weights;

            File edgeFile = new File(path+"/conflict_graph.txt");
            final LineNumberReader reader = new LineNumberReader(new FileReader(edgeFile));

            // prepare vertex set and adjacency map
            StringTokenizer tokens = new StringTokenizer(reader.readLine());
            Integer vertexCount = Integer.parseInt(tokens.nextToken());
            Integer edgeCount = Integer.parseInt(tokens.nextToken());
            vertices = new Set<>(vertexCount*2);
            weights = new HashMap<>(vertexCount*2);
            adjacency = new HashMap<>(edgeCount*2);


            // populate graph with edges
            String str;
            Integer a,b;
            while ((str = reader.readLine()) != null) {
                tokens = new StringTokenizer(str);
                a = Integer.parseInt(tokens.nextToken());
                b = Integer.parseInt(tokens.nextToken());

                adjacency.get(a).add(b);
                adjacency.get(b).add(a);
            }

            File vertexFile = new File(path+"/conflict_graph.txt");
            final LineNumberReader vertexReader = new LineNumberReader(new FileReader(vertexFile));
            // add weights and vertices
            while ((str = reader.readLine()) != null) {
                tokens = new StringTokenizer(str);
                a = Integer.parseInt(tokens.nextToken());
                b = Integer.parseInt(tokens.nextToken());

                vertices.add(a);
                weights.put(a,b);
            }


            return new IntegerGraph(vertices, weights, adjacency);

        } catch (IOException e) {
            System.out.println("Required files could not be read.");
            System.exit(0);
        }

        return null;
    }

    /**
     * Calculates the set of vertices adjacent to a given vertex
     * @param vertex the vertex to find the neighbors of
     * @param adjacency the matrix containing all adjacencies of the underlying graph
     * @return set of integers representing the neighbors
     */
    public Set<Integer> getNeighbors(Integer vertex, HashMap<Integer,Set<Integer>> adjacency) {
        return (Set<Integer>) adjacency.get(vertex).clone();
    }

    /**
     * Determines if a given vertex set from a graph is independent
     * @param vertexSet the set of vertices to be tested for independence
     * @param adjacency the matrix containing all adjacencies of the underlying graph
     * @return true when vertexSet is independent, false otherwise
     */
    public boolean isIndependent(Set<Integer> vertexSet, HashMap<Integer,Set<Integer>> adjacency) {
        for(Integer v : vertexSet) {
            for(Integer n_v : adjacency.get(v)) {
                if(vertexSet.contains(n_v))
                    return false;
            }
        }
        return true;
    }

    /**
     * Determines if a given vertex set from a graph is a vertex cover
     * @param vertexSet the set of vertices to be tested
     * @return true when vertexSet is a vertex cover, false otherwise
     */
    public boolean isVertexCover(Set<Integer> vertexSet) {
        HashMap<Integer, Set<Integer>> edges = getAdjacencyCopy();

        for(Map.Entry<Integer, Set<Integer>> entry : edges.entrySet()) {
            if(vertexSet.contains(entry.getKey()))
                continue;

            for(Integer neighbor : entry.getValue()) {
                if(!vertexSet.contains(neighbor))
                    return false;
            }
        }

        return true;
    }

    /**
     * Creates and returns a deep copy of adjacency
     * @return the copy of adjacency
     */
    public HashMap<Integer, Set<Integer>> getAdjacencyCopy() {
        HashMap<Integer, Set<Integer>> copy = new HashMap<>();

        adjacency.forEach((vertex, neighbors) -> {
            copy.put(vertex, (Set<Integer>) neighbors.clone());
        });

        return copy;
    }

    public Set<Integer> mvc_localsearch(Set<Integer> cover, int kMax) {
        Set<Integer> S;
        Stack<Integer> P = new Stack();
        Integer p = null;
        Set<Integer> F;

        for(int k = 1; k <= kMax; k+=2) {
            for(Integer vertex : cover) {
                S = getNeighbors(vertex, adjacency).minus(cover);
                S.add(vertex);
                P.addAll(getNeighbors(vertex, adjacency).minus(cover));
                if(k != 1 && P.isEmpty()) // TODO this is not validated but used to avoid EmptyStackException
                    continue;
                if(k != 1)
                    p = P.pop();
                F = getNeighbors(vertex,adjacency).intersect(cover);
                S = enumerate(k, cover, adjacency, S, p, P, F);
                if(S.size() != 0) {
                    cover = cover.minus(S).union(S.minus(cover));
                    break;
                }

            }
        }

        return cover;
    }

    public Set<Integer> enumerate(int k, Set<Integer> cover, HashMap<Integer, Set<Integer>> adjacency, Set<Integer> S, Integer p, Stack<Integer> P, Set<Integer> F) {
        Set<Integer> s_intersect_c = S.intersect(cover);
        if(s_intersect_c.size() > Math.ceil((k+1)/2) || S.minus(cover).size() > k/2 || !isIndependent(s_intersect_c, adjacency))
            return new Set<>();

        if(S.size() == k)
            return S;

        for(Integer b : getNeighbors(p, adjacency).minus(S.union(F))) {
            Set<Integer> nb = getNeighbors(b, adjacency);
            if(nb.intersect(F.minus(cover)).size() == 0) {
                nb = nb.minus(S.union(cover));
                Stack<Integer> PP = (Stack<Integer>) P.clone();
                PP.add(p);
                PP.addAll(nb);
                Integer pp = PP.pop();

                Set<Integer> SS = S.union(nb);
                SS.add(b);

                Set<Integer> result = enumerate(k,cover,adjacency,SS,pp, PP, F);
                if(result.size() != 0)
                    return result;
            }
            F.add(b);
        }


        if(P.isEmpty()) // TODO this is not validated but used to avoid EmptyStackException
            return new Set<>();
        Integer pp = P.pop();
        return enumerate(k, cover, adjacency, S, pp, P, F);
    }
}
