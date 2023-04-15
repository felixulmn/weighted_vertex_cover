package com.felixullmann.graphs;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;

public class IntegerGraph {

    public Set<Integer> vertices;
    public HashMap<Integer, Integer> weights;
    public HashMap<Integer, Set<Integer>> adjacency;
    public Set<Integer> initialSolution = null;         // may be used to save greedy solutions

    // comparators for finding greedy solution
    public Comparator<Integer> maxDegreeComparator = (Integer v1, Integer v2) -> Integer.compare(adjacency.get(v2).size(), adjacency.get(v1).size());
    public Comparator<Integer> neighborWeightRatioComparator = (Integer v1, Integer v2) -> Float.compare((float) getSetWeight(getNeighbors(v2))/weights.get(v2), (float) getSetWeight(getNeighbors(v1))/weights.get(v1));
    public Comparator<Integer> neighborWeightDifferenceComparator = (Integer v1, Integer v2) -> Long.compare(getSetWeight(getNeighbors(v2)) - weights.get(v2), getSetWeight(getNeighbors(v1)) - weights.get(v1));

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
     * Calculates the set of vertices adjacent to a given vertex set
     * @param vertices the set of vertices to find the neighbors of
     * @return set of integers representing the neighbors
     */
    public Set<Integer> getNeighbors(Set<Integer> vertices) {
        Set<Integer> neighbors = new Set<>();

        for(Integer vertex : vertices) {
            neighbors.addAll(this.adjacency.get(vertex));
        }

        return neighbors;
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
     * @return returns the sum of all the vertices' weights in the set.
     */
    public long getSetWeight(Set<Integer> vertexSet) {

        long totalWeight = 0;
        for(Integer vertex : vertexSet) {
            totalWeight += weights.get(vertex);
        }

        return totalWeight;
    }

    /**
     * Removes given vertices from graph
     * @param vertexSet vertices to be removed
     */
    public void removeVertices(Set<Integer> vertexSet) {
        this.vertices.removeAll(vertexSet);

        for(Integer vertex : vertexSet)
            this.adjacency.remove(vertex);

        this.adjacency.forEach((vertex, neighbors) -> {
            adjacency.put(vertex, neighbors.minus(vertexSet));
        });
    }

    /**
     * local search algorithm for finding kMax-optimal minimum weighted vertex cover
     * @param cover an inital vertex cover (may be all vertices of the graph)
     * @param kMax the maximum swap size
     * @param totalWeight parameter used to calculate current solution weight
     * @return returns k-max optimal mininum weighted vertex cover
     */
    public Set<Integer> localSearch(Set<Integer> cover, int kMax, long totalWeight) {
        Set<Integer> S;

        long start = System.currentTimeMillis();
        long current;

        for(int k = 1; k <= kMax; k++) {
/*            current = (System.currentTimeMillis() - start)/1000;
            System.out.println(String.format("%5s k = %s", current, k));*/

            for(Integer vertex : cover) {
                S = generateSwap(k, vertex, cover);
                if(S.size() != 0) {
                    cover = cover.minus(S).union(S.minus(cover));
                    current = (System.currentTimeMillis() - start);
                    // System.out.println(String.format("%5s    w = %s", current, (getSetWeight(cover) + totalWeight)));
                    printStats((getSetWeight(cover) + totalWeight), k, current);

                    // restart the k-loop at 1
                    k = 0;
                    break;
                }

            }

        }

        return cover;
    }

    public Set<Integer> generateSwap(int k, Integer vertex, Set<Integer> cover) {
        Set<Integer> S;
        Stack<Integer> P;
        Integer p = null;
        Set<Integer> F;

        S = getNeighbors(vertex).minus(cover);
        S.add(vertex);
        P = new Stack<>();
        P.addAll(getNeighbors(vertex).minus(cover));
        if(k != 1 && P.isEmpty())
            return new Set<>();
        if(k != 1)
            p = P.pop();
        F = getNeighbors(vertex).intersect(cover);
        return enumerate(k, cover, S, p, P, F);
    }

    // vertex cylcing variant using array and indices
    public Set<Integer> localSearch_cycling(Set<Integer> cover, int kMax, long totalWeight) {
        long start = System.currentTimeMillis();
        long current;

        int maxCycling = 3;
        boolean change = false;

        Set<Integer> S;

        Integer[] vertices = this.vertices.toArray(new Integer[this.vertices.size()]);
        Integer vertex;

        int swapcount = 0;

        for(int k = 1; k <= kMax; k++) {
/*            current = (System.currentTimeMillis() - start)/1000;
            System.out.println(String.format("%5s k = %s", current, k));*/

            int noSwap = 0;

            for(int i = 0; i < vertices.length; i++) {
                vertex = vertices[i];


                S = generateSwap(k, vertex, cover);
                if(S.size() != 0) {
                    noSwap = 0;

                    cover = cover.minus(S).union(S.minus(cover));
                    current = (System.currentTimeMillis() - start);
                    // System.out.println(String.format("%5s    w = %s", current, (getSetWeight(cover) + totalWeight)));
                    printStats((getSetWeight(cover) + totalWeight), k, current);

                    swapcount++;
                    change = true;

                    // restart the k-loop at 1
                    if(k > maxCycling) {
                        k = 0;
                        change = false;
                        break;
                    }

                }
                else {
                    noSwap++;
                }

                if(noSwap == vertices.length)
                    break;

/*                if(i == vertices.length-1 && k <= maxCycling)
                    i = 0;*/

                if(i == vertices.length-1 && k <= maxCycling && k > 1)
                    i = 0;


            }

            if(k == kMax && kMax == maxCycling && change == true) {
                change = false;
                k = 0;
            }

/*            if(change == true && k > 1) {
                change = false;
                k = 0;
            }*/



        }

        System.out.println("Swaps made: " + swapcount);
        return cover;
    }

    // pruning rule 2 variant
    public Set<Integer> localSearch_pruning(Set<Integer> cover, int kMax, long totalWeight) {

        Set<Integer> S;
        Stack<Integer> P;
        Integer p = null;
        Set<Integer> F;


        long start = System.currentTimeMillis();
        long current;


        Set<Integer>[] R = new Set[kMax];
        for(int i = 0; i < kMax; i++) {
            R[i] = (Set<Integer>) this.vertices.clone();
        }

        for(int k = 1; k <= kMax; k++) {
/*            current = (System.currentTimeMillis() - start)/1000;
            System.out.println(String.format("%5s k = %s", current, k));*/

            for(Integer vertex : cover.intersect(R[k-1])) {
                S = getNeighbors(vertex).minus(cover);
                S.add(vertex);
                P = new Stack<>();
                P.addAll(getNeighbors(vertex).minus(cover));
                if(k != 1 && P.isEmpty())
                    continue;
                if(k != 1)
                    p = P.pop();
                F = ((this.vertices.minus(R[k-1])).union(getNeighbors(vertex))).intersect(cover);
                S = enumerate(k, cover, S, p, P, F);
                if(S.size() != 0) {
                    cover = cover.minus(S).union(S.minus(cover));
                    current = (System.currentTimeMillis() - start);
/*                    System.out.println(String.format("%5s    w = %s", current, (getSetWeight(cover) + totalWeight)));*/
                    printStats((getSetWeight(cover) + totalWeight), k, current);
                    // update R
                    Set<Integer> frontier = getNeighbors(S);
                    Set<Integer> M = S.union(frontier);

                    for(int i = 0; i < kMax; i++) {
                        R[i].addAll(M);
                        frontier = getNeighbors(frontier);
                        M.addAll(frontier);
                    }
                    // end update R

                    // restart the k-loop at 1
                    k = 0;
                    break;
                } else {
                    R[k-1].remove(vertex);
                }

            }

        }

        return cover;
    }

    public Set<Integer> enumerate(int k, Set<Integer> cover, Set<Integer> S, Integer p, Stack<Integer> P, Set<Integer> F) {

        Set<Integer> s_intersect_c = S.intersect(cover);

        if(S.size() > k || !isIndependent(s_intersect_c))
            return new Set<>();

        if(S.size() == k) {
            if(getSetWeight(s_intersect_c) - getSetWeight(S.minus(cover)) > 0)
                return S;
            else
                return new Set<>();
        }

        Set<Integer> FF = (Set<Integer>) F.clone();
        for(Integer b : getNeighbors(p).minus(S.union(FF))) {
            Set<Integer> nb = getNeighbors(b);
            //if(nb.intersect(F.minus(cover)).size() == 0) {
            nb = nb.minus(S.union(cover));
            Stack<Integer> PP = (Stack<Integer>) P.clone();
            PP.add(p);
            PP.addAll(nb);
            Integer pp = PP.pop();

            Set<Integer> SS = S.union(nb);
            SS.add(b);

            Set<Integer> result = enumerate(k,cover,SS,pp, PP, FF);
            if(result.size() != 0)
                return result;
            //}
            FF.add(b);
        }


        if(P.isEmpty())
            return new Set<>();
        Integer pp = P.pop();
        return enumerate(k, cover, S, pp, P, FF);
    }

    private int[] independentSetNeighbors;
    private TreeSet<Integer> V;

    /**
     * optimized version of localSearch using "Improvement Upper Bound" rule
     * @param cover an inital vertex cover (may be all vertices of the graph)
     * @param kMax the maximum swap size
     * @param totalWeight
     * @return returns k-max optimal mininum weighted vertex cover
     */
    public Set<Integer> localSearch_imprUpperBound(Set<Integer> cover, int kMax, long totalWeight) {
        V = new TreeSet<>(Comparator.comparingInt((Integer i) -> weights.get(i)).thenComparing(Comparator.naturalOrder()));
        V.addAll(vertices);

        independentSetNeighbors = new int[vertices.size()];

        if(vertices.minus(cover).size() != 0) {
            for (Integer vertex : this.vertices) {
                independentSetNeighbors[vertex - 1] = adjacency.get(vertex).minus(cover).size();
            }
        }

        Set<Integer> S;
        Stack<Integer> P;
        Integer p = null;
        Set<Integer> F;

        long start = System.currentTimeMillis();
        long current;

        for(int k = 1; k <= kMax; k++) {
/*            current = (System.currentTimeMillis() - start)/1000;
            System.out.println(String.format("%5s k = %s", current, k));*/

            for(Integer vertex : cover) {
                S = getNeighbors(vertex).minus(cover);
                S.add(vertex);
                P = new Stack<>();
                P.addAll(getNeighbors(vertex).minus(cover));
                if(k != 1 && P.isEmpty())
                    continue;
                if(k != 1)
                    p = P.pop();
                F = getNeighbors(vertex).intersect(cover);
                S = enumerate_imprUpperBound(k, cover, S, p, P, F);

                if(S.size() != 0) {
                    // execute swap
                    cover = cover.minus(S).union(S.minus(cover));

                    current = (System.currentTimeMillis() - start);
/*                    System.out.println(String.format("%5s    w = %s", current, (getSetWeight(cover) + totalWeight)));*/
                    printStats((getSetWeight(cover) + totalWeight), k, current);

                    long starts = System.nanoTime();
                    // recalculate independent set neighbors
                    for(Integer v : S.union(getNeighbors(S))) {
                        independentSetNeighbors[v-1] = adjacency.get(v).minus(cover).size();
                    }
                    enumPruningTime += System.nanoTime()-starts;

                    //System.out.println(Arrays.toString(independentSetNeighbors));

                    // restart the k-loop at 1
                    k = 0;
                    break;
                }

            }

        }
        System.out.println("Enum applied " + enumPruningCount + " times.");
        System.out.println("Pruning time " + enumPruningTime + " ms.");
        return cover;
    }

    public Set<Integer> enumerate_imprUpperBound(int k, Set<Integer> cover, Set<Integer> S, Integer p, Stack<Integer> P, Set<Integer> F) {

        Set<Integer> s_intersect_c = S.intersect(cover);

        if(S.size() > k || !isIndependent(s_intersect_c))
            return new Set<>();

        long improvement  = getSetWeight(s_intersect_c) - getSetWeight(S.minus(cover));

        if(S.size() == k) {
            if(improvement > 0)
                return S;
            else
                return new Set<>();
        }

        // improvement upper bound check
        long start = System.nanoTime();

/*        Set<Integer> pP = new Set<>();
        pP.add(p);
        pP.addAll(P);
        if(improvement < 0 && improvement+getTopNCandidates2(F, cover, S,s_intersect_c.size(), k, getNeighbors(pP)) < 0) {
            enumPruningCount++;
            return new Set<>();
        }*/

        if(improvement < 0 && improvement+getTopNCandidates(F, cover, S,s_intersect_c.size(), k) < 0) {
            enumPruningCount++;
            return new Set<>();
        }

        enumPruningTime += System.nanoTime() - start;
        // end check

        Set<Integer> FF = (Set<Integer>) F.clone();
        for(Integer b : getNeighbors(p).minus(S.union(FF))) {
            Set<Integer> nb = getNeighbors(b);
            //if(nb.intersect(F.minus(cover)).size() == 0) {
            nb = nb.minus(S.union(cover));
            Stack<Integer> PP = (Stack<Integer>) P.clone();
            PP.add(p);
            PP.addAll(nb);
            Integer pp = PP.pop();

            Set<Integer> SS = S.union(nb);
            SS.add(b);

            Set<Integer> result = enumerate(k,cover,SS,pp, PP, FF);
            if(result.size() != 0)
                return result;
            //}
            FF.add(b);
        }


        if(P.isEmpty())
            return new Set<>();
        Integer pp = P.pop();
        return enumerate(k, cover, S, pp, P, FF);
    }

    private int enumPruningCount = 0;
    private long enumPruningTime = 0;

    private long getTopNCandidates(Set<Integer> F, Set<Integer> cover, Set<Integer> S, int c, int k) {
        int topN = k - S.size();
        int maxN = k - c;

        int i = 0;
        long weight = 0;
        for(Integer vertex : V.descendingSet()) {
            if(i == topN)
                break;

            if(cover.contains(vertex) && !F.contains(vertex) && !S.contains(vertex) && independentSetNeighbors[vertex-1] < maxN) { // ===  <= c+1
                weight += weights.get(vertex);
                i++;
            }

        }

        return weight;
    }

    private long getTopNCandidates2(Set<Integer> F, Set<Integer> cover, Set<Integer> S, int c, int k, Set<Integer> NP) {
        // we need at least one neighbor of P union p (NP)
        int topN = k - S.size();
        int maxN = k - c;

        int i = 0;
        long weight = 0;
        boolean NPfound = false;

        for(Integer vertex : V.descendingSet()) {
            if(i == topN)
                break;

            if(independentSetNeighbors[vertex-1] < maxN && cover.contains(vertex) && !F.contains(vertex) && !S.contains(vertex)) {
                if(i+1==topN && !NPfound && !NP.contains(vertex))
                    continue;

                if(!NPfound && NP.contains(vertex))
                    NPfound = true;

                weight += weights.get(vertex);
                i++;
            }

        }

        // no pivot neighbor can extend swap
        if(!NPfound)
            return 0;

        return weight;
    }

    /**
     * Removes vertices from graph that are certain to be in the solution.
     * The rule is to add all neighbors of a vertex to the cover if their total weight is less than the weight of the vertex
     */
    public Set<Integer> preprocess() {
        return preprocessRecursive(1,0, new Set<Integer>());
    }

    private void printStats(long weight, int k, long score) {
        System.out.println(weight + " " + k + " " + score);
    }

    private Set<Integer> preprocessRecursive(int removed, int totalRemoved, Set<Integer> inCover) {
        if(removed == 0) {
            System.out.println("Vertex Reduction: Removed " + totalRemoved + " vertices from graph and added " + inCover.size() + " vertices to cover.\n");
            return inCover;
        }



        Set<Integer> remove = new Set<>();
        // find vertices to be pruned

        for (Integer vertex : this.vertices) {
            Set<Integer> neighbors;
            neighbors = getNeighbors(vertex);
            if (weights.get(vertex) >= getSetWeight(neighbors)) {
                remove.add(vertex);
                inCover.addAll(neighbors);
            }
        }
        removed = remove.size();

        // update graph
        remove.addAll(inCover);
        this.removeVertices(remove);

        return preprocessRecursive(removed, totalRemoved+remove.size(), inCover);
    }

    /**
     * Removes vertices from graph that are certain to be in the solution.
     * The rule is to add all neighbors of a vertex to the cover if the vertex is clique-isolated and the heaviest among its neighbors
     */
    public Set<Integer> doCliquePruning() {
        Set<Integer> inCover = new Set<>();
        Set<Integer> remove = new Set<>();
        boolean improved = true;


        while(improved == true) {
            improved = false;

            outer:
            for (Integer v : this.vertices) {
                Set<Integer> nv = this.getNeighbors(v);
                nv.add(v);

                Integer max_w = -1;

                for (Integer n : adjacency.get(v)) {
                    Set<Integer> nn = this.getNeighbors(n);
                    nn.add(n);

                    if (!nn.intersect(nv).equals(nv))
                        continue outer;

                    if (this.weights.get(n) > max_w)
                        max_w = this.weights.get(n);
                }

                if (!(this.weights.get(v) > max_w)) {
                    continue;
                }

                System.out.println(nv.size() + "-clique found with root " + v + "\n");
                remove.addAll(nv);
                nv.remove(v);
                inCover.addAll(nv);
                improved = true;
            }

            this.removeVertices(remove);
        }

        System.out.println("Clique Reduction: Removed " + remove.size() + " vertices from graph and added " + inCover.size() + " vertices to cover.\n");
        return inCover;
    }

    /**
     * Calculates a greedy solution that can be used as the initial cover to the local search algorithm.
     * @param vertices The potential vertices to be in the cover.
     * @param comparator The comparator that defines the order for greedily adding vertices to the cover.
     * @return Returns a vertex cover.
     */
    public Set<Integer> getGreedyCover(Set<Integer> vertices, Comparator<Integer> comparator) {
        Set<Integer> cover = new Set<>();

        if(vertices.size() == 0)
                return cover;

        PriorityQueue<Integer> vertexQueue = new PriorityQueue<>(vertices.size(), comparator);
        vertexQueue.addAll(vertices);
        HashMap<Integer, Set<Integer>> adjacencyCopy = this.getAdjacencyCopy();

        System.out.println("Queue built.");

        while(adjacencyCopy.size() != 0) {
            Integer v = vertexQueue.poll();

            if(adjacencyCopy.containsKey(v))
                cover.add(v);
            else
                continue;

            Set<Integer> neighbors = adjacencyCopy.get(v);

            for(Integer neighbor : neighbors) {
                adjacencyCopy.get(neighbor).remove(v);
                if(adjacencyCopy.get(neighbor).size() == 0)
                    adjacencyCopy.remove(neighbor);
            }

            adjacencyCopy.remove(v);
        }

        return cover;
    }

    /**
     * Get all subgraphs induced by vertices
     * @param vertices
     * @return returns a set of vertex sets that represent disconnected subgraphs induced by vertices
     */
    public Set<IntegerGraph> getDisconnectedSubgraphs(Set<Integer> vertices) {
        Stack<Integer> frontier = new Stack<>();
        Stack<Integer> remaining = new Stack<>();
        remaining.addAll((Set<Integer>) this.vertices.clone());

        Set<IntegerGraph> subgraphs = new Set<>();

        Set<Integer> currentVertices = new Set<>();
        HashMap<Integer, Set<Integer>> currentAdjacency = new HashMap<>();
        HashMap<Integer, Integer> currentWeights = new HashMap<>();

        while(!remaining.isEmpty()) {
            frontier.add(remaining.pop());

            while(!frontier.isEmpty()) {
                Integer v = frontier.pop();
                currentVertices.add(v);

                currentAdjacency.put(v, this.adjacency.get(v));
                currentWeights.put(v, this.weights.get(v));

                frontier.addAll(this.adjacency.get(v).minus(currentVertices));
                currentVertices.addAll(this.adjacency.get(v));
            }

            subgraphs.add(new IntegerGraph(currentVertices, currentWeights, currentAdjacency));
            remaining.removeAll(currentVertices);
            //System.out.println("Subset of size " + currentVertices.size() +  " found. " + remaining.size() + " vertices remaining.");

            currentVertices = new Set<>();
            currentAdjacency = new HashMap<>();
            currentWeights = new HashMap<>();

        }

        return subgraphs;
    }
}
