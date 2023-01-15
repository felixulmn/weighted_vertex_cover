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

            String str;

            File edgeFile = new File(path+"/conflict_graph.txt");
            final LineNumberReader reader = new LineNumberReader(new FileReader(edgeFile));

            // prepare vertex set and adjacency map
            StringTokenizer tokens = new StringTokenizer(reader.readLine());
            Integer vertexCount = Integer.parseInt(tokens.nextToken());
            Integer edgeCount = Integer.parseInt(tokens.nextToken());
            vertices = new Set<>(vertexCount*2);
            weights = new HashMap<>(vertexCount*2);
            adjacency = new HashMap<>(edgeCount*2);

            // add weights and vertices
            File vertexFile = new File(path+"/node_weights.txt");
            final LineNumberReader vertexReader = new LineNumberReader(new FileReader(vertexFile));
            Integer vertex, weight;
            StringTokenizer vertexTokens;
            while ((str = vertexReader.readLine()) != null) {
                vertexTokens = new StringTokenizer(str);
                vertex = Integer.parseInt(vertexTokens.nextToken());
                weight = Integer.parseInt(vertexTokens.nextToken());

                vertices.add(vertex);
                adjacency.put(vertex, new Set<>());
                weights.put(vertex,weight);
            }

            // populate graph with edges
            Integer a,b;
            while ((str = reader.readLine()) != null) {
                tokens = new StringTokenizer(str);
                a = Integer.parseInt(tokens.nextToken());
                b = Integer.parseInt(tokens.nextToken());

                adjacency.get(a).add(b);
                adjacency.get(b).add(a);
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
     * @return set of integers representing the neighbors
     */
    public Set<Integer> getNeighbors(Integer vertex) {
        return (Set<Integer>) adjacency.get(vertex).clone();
    }

    /**
     * Determines if a given vertex set from a graph is independent
     * @param vertexSet the set of vertices to be tested for independence
     * @return true when vertexSet is independent, false otherwise
     */
    public boolean isIndependent(Set<Integer> vertexSet) {
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

    /**
     * Calculates the total weight of all vertices in the set.
     * @param vertexSet the set of vertices to calculate the weight of
     * @return returns the sum of all of the vertices weights in the set.
     */
    public long getSetWeight(Set<Integer> vertexSet) {
        long totalWeight = 0;
        for(Integer vertex : vertexSet) {
            totalWeight += weights.get(vertex);
        }

        return totalWeight;
    }

    public Set<Integer> mvc_localsearch(Set<Integer> cover, int kMax) {
        Set<Integer> S;
        Stack<Integer> P = new Stack();
        Integer p = null;
        Set<Integer> F;


        long start = System.currentTimeMillis();
        long current;
        int ks = 0;

        for(int k = 1; k <= kMax; k++) {


    /*
            if(getSetWeight(cover) ==  472537418)
                break;


     */






            /*
            if(ks != 0)
                k = ks;
            ks = 0;

             */

            System.out.println("k = " + k);

            for(Integer vertex : cover) {
                S = getNeighbors(vertex).minus(cover);
                S.add(vertex);
                P.addAll(getNeighbors(vertex).minus(cover));
                if(k != 1 && P.isEmpty())
                    continue;
                if(k != 1)
                    p = P.pop();
                F = getNeighbors(vertex).intersect(cover);
                S = enumerate(k, cover, S, p, P, F);
                if(S.size() != 0) {
                    cover = cover.minus(S).union(S.minus(cover));
                    current = (System.currentTimeMillis() - start)/1000;
                    System.out.println("   " + current + " " + getSetWeight(cover));
                    //ks = S.size();
                    //ks = k;
                    //break;
                }

            }

        }

        return cover;
    }

    public Set<Integer> enumerate(int k, Set<Integer> cover, Set<Integer> S, Integer p, Stack<Integer> P, Set<Integer> F) {
        /*
        Set<Integer> s_intersect_c = S.intersect(cover);

        if(getSetWeight(S) > k || !isIndependent(s_intersect_c))
            return new Set<>();

        if(getSetWeight(s_intersect_c) - getSetWeight(S.minus(cover)) > 0)
            return S;
        */

        Set<Integer> s_intersect_c = S.intersect(cover);

        if(S.size() > k || !isIndependent(s_intersect_c))
            return new Set<>();

        if(S.size() == k) {
            if(getSetWeight(s_intersect_c) - getSetWeight(S.minus(cover)) > 0)
                return S;
            else
                return new Set<>();
        }

        for(Integer b : getNeighbors(p).minus(S.union(F))) {
            Set<Integer> nb = getNeighbors(b);
            if(nb.intersect(F.minus(cover)).size() == 0) {
                nb = nb.minus(S.union(cover));
                Stack<Integer> PP = (Stack<Integer>) P.clone();
                PP.add(p);
                PP.addAll(nb);
                Integer pp = PP.pop();

                Set<Integer> SS = S.union(nb);
                SS.add(b);

                Set<Integer> result = enumerate(k,cover,SS,pp, PP, F);
                if(result.size() != 0)
                    return result;
            }
            F.add(b);
        }


        if(P.isEmpty()) // TODO this is not validated but used to avoid EmptyStackException
            return new Set<>();
        Integer pp = P.pop();
        return enumerate(k, cover, S, pp, P, F);
    }
}
