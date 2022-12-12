package com.felixullmann.graphs;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

public class IntegerGraphTest {

    private Set<Integer> vertices;
    private HashMap<Integer, Set<Integer>> adjacency;
    private IntegerGraph myGraph;

    @Before
    public void setUp() {
        vertices = new Set<>(Arrays.asList(0,1,2,3,4,5));
        adjacency = new HashMap<>();

        adjacency.put(0, new Set<>(Arrays.asList(2, 4, 5)));
        adjacency.put(1, new Set<>(Arrays.asList(4, 5)));
        adjacency.put(2, new Set<>(Arrays.asList(0, 3, 4)));
        adjacency.put(3, new Set<>(Arrays.asList(2)));
        adjacency.put(4, new Set<>(Arrays.asList(0, 1, 2, 5)));
        adjacency.put(5, new Set<>(Arrays.asList(1, 4)));

        myGraph = new IntegerGraph(vertices, adjacency);
    }

    @Test
    public void isIndependent() {
        Set<Integer> independent = new Set<>(Arrays.asList(0,1,3));
        Set<Integer> notIndependent = new Set<>(Arrays.asList(0,1,2));

        assertFalse(IntegerGraph.isIndependent(vertices, adjacency));
        assertFalse(IntegerGraph.isIndependent(notIndependent, adjacency));
        assertTrue(IntegerGraph.isIndependent(independent, adjacency));
    }

    @Test
    public void getNeighbors() {
        assertEquals(new Set<>(Arrays.asList(2)), IntegerGraph.getNeighbors(3, adjacency));
        assertEquals(new Set<>(Arrays.asList(2,0,5,1)), IntegerGraph.getNeighbors(4, adjacency));
        assertEquals(new Set<>(Arrays.asList(5,4)), IntegerGraph.getNeighbors(1, adjacency));
    }

    @Test
    public void isVertexCover() {
        assertTrue(IntegerGraph.isVertexCover(vertices, adjacency));
        assertTrue(IntegerGraph.isVertexCover(new Set<>(Arrays.asList(2,4,5)), adjacency));
        assertFalse(IntegerGraph.isVertexCover(new Set<>(Arrays.asList(2,1,0)), adjacency));
        assertFalse(IntegerGraph.isVertexCover(new Set<>(Arrays.asList(4,5,0,1)), adjacency));
    }

    @Test
    public void getAdjacencyCopy() {
        HashMap<Integer, Set<Integer>> copy = myGraph.getAdjacencyCopy(adjacency);
        assertEquals(adjacency, copy);
        adjacency.get(2).add(1);
        assertNotEquals(adjacency, copy);
    }
}