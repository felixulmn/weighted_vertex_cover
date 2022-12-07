package com.felixullmann.graphs;

import java.util.HashMap;
import java.util.HashSet;

public class Graph<E> {

    public Set<E> vertices;
    public HashMap<E, Set<E>> adjacency;


    public static <T> Set<T> getNeighbors(T vertex, HashMap<T,Set<T>> adjacency) {
        return (Set<T>) adjacency.get(vertex).clone();
    }

    public static <T> boolean isIndependent(Set<T> vertexSet, HashMap<T,Set<T>> adjacency) {
        //System.out.println(String.format("Vertexset: %s", vertexSet));
        for(T v : vertexSet) {
            //System.out.println(String.format("Vertex: %s - Neighbours: %s", v, this.adjacency.get(v)));
            for(T n_v : adjacency.get(v)) {
                if(vertexSet.contains(n_v))
                    return false;
            }
        }
        return true;
    }
}
