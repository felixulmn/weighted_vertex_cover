package com.felixullmann.graphs;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;

public class SetTest {

    private Set<Integer> emptySet;
    private Set<Integer> set1;
    private Set<Integer> set2;
    @Before
    public void setUp() {
        emptySet = new Set<>();
        set1 = new Set<>(Arrays.asList(1,2,3,4));
        set2 = new Set<>(Arrays.asList(3,4,5,6));
    }

    @Test
    public void union() {
        assertEquals(new Set<>(Arrays.asList(1,2,3,4)),set1.union(emptySet));
        assertEquals(new Set<>(Arrays.asList(1,2,3,4,5,6)), set1.union(set2));

        // check that set1 and set2 remain unchanged
        assertEquals(4, set1.size());
        assertEquals(4, set2.size());
    }

    @Test
    public void intersect() {
        assertEquals(new Set<Integer>(),set1.intersect(emptySet));
        assertEquals(new Set<>(Arrays.asList(3,4)), set1.intersect(set2));

        // check that set1 and set2 remain unchanged
        assertEquals(4, set1.size());
        assertEquals(4, set2.size());
    }

    @Test
    public void minus() {
        assertEquals(new Set<>(),emptySet.minus(set2));
        assertEquals(new Set<>(Arrays.asList(1,2,3,4)),set1.minus(emptySet));
        assertEquals(new Set<>(Arrays.asList(1,2)), set1.minus(set2));

        // check that set1 and set2 remain unchanged
        assertEquals(0, emptySet.size());
        assertEquals(4, set1.size());
        assertEquals(4, set2.size());
    }
}