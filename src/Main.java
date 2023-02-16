import com.felixullmann.graphs.IntegerGraph;
import com.felixullmann.graphs.Set;

import java.util.Arrays;


/*
    USED DATASETS
    https://networkrepository.com/dimacs.php
 */
public class Main {
    public static void main(String[] args) {

        IntegerGraph myGraph = null;

        try {
            myGraph = IntegerGraph.fromVehicleRoutingApplication(args[0]);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Please provide a path to the problem file.");
            System.exit(1);
        }

        int edgecount = 0;
        for(Set<Integer> neighbors : myGraph.adjacency.values()) {
            edgecount += neighbors.size();
        }
        edgecount /= 2;


        System.out.println("Initialized Graph.");
        System.out.println(String.format("Added %s vertices and %s edges", myGraph.vertices.size(), edgecount));


        myGraph.preprocess();
        Set<Integer> initialCover = (Set<Integer>) myGraph.vertices.clone();

        System.out.println("Initial solution");
        System.out.println(String.format("Is cover: %s",myGraph.isVertexCover(initialCover)));
        System.out.println(String.format("Weight: %d", myGraph.getSetWeight(initialCover)+myGraph.getSetWeight(myGraph.inCover)));
        System.out.println();

/*
        Set<Integer> greedyCover = myGraph.getGreedyCover(initialCover, myGraph.neighborWeightDifferenceComparator);
        System.out.println("Greedy solution");
        System.out.println(String.format("Is cover: %s",myGraph.isVertexCover(greedyCover)));
        System.out.println(String.format("Weight: %d", myGraph.getSetWeight(greedyCover)+myGraph.getSetWeight(myGraph.inCover)));
        System.out.println();
*/


        long start = System.currentTimeMillis();
        Set<Set<Integer>> subgraphs = myGraph.getDisconnectedSubgraphs(myGraph.vertices);
        System.out.println("Took " + ((System.currentTimeMillis()-start)/1000) + " seconds to compute subsets");

        System.out.println("Number of disconnected subgraphs: " + subgraphs.size());

        //subgraphs.forEach(subraph -> System.out.println(subraph));


        Set<Integer> union = new Set<>();

        for(Set<Integer> subgraph : subgraphs) {
            union = union.union(subgraph);
        }

        boolean noIntersection = true;
        outer: for(Set<Integer> subgraph : subgraphs) {
            for(Set<Integer> sg : subgraphs) {

                if(subgraph.equals(sg))
                    continue;

                if(subgraph.intersect(sg).size() > 0) {
                    noIntersection = false;
                    break outer;
                }
            }
        }

        System.out.println("Subgraphs form complete vertex set: " + union.equals(myGraph.vertices));
        System.out.println("Subgraphs have no intersections:    " + noIntersection);


        // Calculate minimum vertex cover and measure time
/*
        long start = System.currentTimeMillis();
        Set<Integer> minCover = myGraph.mvc_localsearch(greedyCover, myGraph.vertices.size());

        System.out.println("\nTook " + ((System.currentTimeMillis()-start)/1000.0) +  " seconds to calculate minimum vertex cover.");
        System.out.println(String.format("Is cover: %s",myGraph.isVertexCover(minCover)));
        System.out.println(String.format("Weight: %d", myGraph.getSetWeight(minCover)));
        System.out.println("Cover nodes:");
        System.out.println(minCover);
*/



        //System.out.println(minCover.union(myGraph.inCover));


    }
}
