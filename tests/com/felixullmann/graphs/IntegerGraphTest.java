package com.felixullmann.graphs;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

public class IntegerGraphTest {

    private Set<Integer> vertices;
    private HashMap<Integer, Set<Integer>> adjacency;
    private HashMap<Integer, Integer> weights;
    private IntegerGraph myGraph;

    @Before
    public void setUp() {
        vertices = new Set<>(Arrays.asList(0,1,2,3,4,5));
        adjacency = new HashMap<>();
        weights = new HashMap<>();

        adjacency.put(0, new Set<>(Arrays.asList(2, 4, 5)));
        adjacency.put(1, new Set<>(Arrays.asList(4, 5)));
        adjacency.put(2, new Set<>(Arrays.asList(0, 3, 4)));
        adjacency.put(3, new Set<>(Arrays.asList(2)));
        adjacency.put(4, new Set<>(Arrays.asList(0, 1, 2, 5)));
        adjacency.put(5, new Set<>(Arrays.asList(1, 4)));

        weights.put(0, 3);
        weights.put(1, 2);
        weights.put(2, 3);
        weights.put(3,1);
        weights.put(4,4);
        weights.put(5,6);


        myGraph = new IntegerGraph(vertices, weights, adjacency);
    }

    @Test
    public void isIndependent() {
        Set<Integer> independent = new Set<>(Arrays.asList(0,1,3));
        Set<Integer> notIndependent = new Set<>(Arrays.asList(0,1,2));

        assertFalse(myGraph.isIndependent(vertices, adjacency));
        assertFalse(myGraph.isIndependent(notIndependent, adjacency));
        assertTrue(myGraph.isIndependent(independent, adjacency));
    }

    @Test
    public void getNeighbors() {
        assertEquals(new Set<>(Arrays.asList(2)), myGraph.getNeighbors(3, adjacency));
        assertEquals(new Set<>(Arrays.asList(2,0,5,1)), myGraph.getNeighbors(4, adjacency));
        assertEquals(new Set<>(Arrays.asList(5,4)), myGraph.getNeighbors(1, adjacency));
    }

    @Test
    public void isVertexCover() {
        assertTrue(myGraph.isVertexCover(vertices));
        assertTrue(myGraph.isVertexCover(new Set<>(Arrays.asList(2,4,5))));
        assertFalse(myGraph.isVertexCover(new Set<>(Arrays.asList(2,1,0))));
        assertFalse(myGraph.isVertexCover(new Set<>(Arrays.asList(4,5,0,1))));
    }

    @Test
    public void getAdjacencyCopy() {
        HashMap<Integer, Set<Integer>> copy = myGraph.getAdjacencyCopy();
        assertEquals(adjacency, copy);
        adjacency.get(2).add(1);
        assertNotEquals(adjacency, copy);
    }
}