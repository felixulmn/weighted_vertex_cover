package com.felixullmann.graphs;

import java.util.Collection;
import java.util.HashSet;

public class Set<T> extends HashSet<T> {

    public Set() {
        super();
    }
    public Set(Collection<? extends T> c) {
        super(c);
    }

    public Set(int initialCapacity) {
        super(initialCapacity);
    }

    public Set(int initialCapacity,float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Calculates the union of this set with another.
     * @param partner the set to unite with
     * @return a new set containing all elements from this set and partner
     */
    public Set<T> union(Set<T> partner) {
        Set<T> union = (Set<T>) this.clone();
        union.addAll(partner);
        return union;
    }

    /**
     * Calculates the intersection of this set with another.
     * @param partner the set to intersect with
     * @return a new set containing all elements that are both in this set and the partner set
     */
    public Set<T> intersect(Set<T> partner) {
        Set<T> intersection = (Set<T>) this.clone();
        intersection.retainAll(partner);
        return intersection;
    }

    /**
     * Calculates the set difference of this set and a partner set
     * @param partner the set that is subtracted from this set
     * @return returns all values that are in this set but not in the partner set
     */
    public Set<T> minus(Set<T> partner) {
        Set<T> setDifference = (Set<T>) this.clone();
        setDifference.removeAll(partner);
        return setDifference;
    }

}
