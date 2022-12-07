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

    public Set<T> union(Set<T> partner) {
        Set<T> union = (Set<T>) this.clone();
        union.addAll(partner);
        return union;
    }

    public Set<T> intersect(Set<T> partner) {
        Set<T> intersection = (Set<T>) this.clone();
        intersection.retainAll(partner);
        return intersection;
    }

    public Set<T> minus(Set<T> partner) {
        Set<T> setDifference = (Set<T>) this.clone();
        setDifference.removeAll(partner);
        return setDifference;
    }

}
