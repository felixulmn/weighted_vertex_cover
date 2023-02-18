import com.felixullmann.graphs.IntegerGraph;
import com.felixullmann.graphs.Set;

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;


/*
    USED DATASETS
    https://networkrepository.com/dimacs.php
 */
public class Main {
    public static void main(String[] args) {


        // Parse arguments and flags
        String inputFileName = null;
        Integer k_max = null;
        boolean greedySolution = false;
        boolean vertexPruning = false;
        boolean splitSubgraphs = false;

        // TODO might add argument for --help flag that outputs verbose description for flags and positional arguments

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {
                case "-g":
                case "--greedy":
                    greedySolution = true;
                    break;
                case "-v":
                case "--vertex-pruning":
                    vertexPruning = true;
                    break;
                case "-s":
                case "--split-subgraphs":
                    splitSubgraphs = true;
                    break;
                default:
                    if (inputFileName == null) {
                        inputFileName = arg;
                    } else if (k_max == null) {
                        k_max = Integer.parseInt(arg);
                    } else {
                        // Handle unexpected argument
                        System.err.println("Unexpected argument: " + arg);
                        System.exit(1);
                    }
                    break;
            }
        }

        if (inputFileName == null || k_max == null) {
            // Handle missing arguments
            System.err.println("Usage: inputfile k_max [-g][-v][-s] ");
            System.exit(1);
        }

        // Initialize graph
        IntegerGraph myGraph = null;

        try {
            myGraph = IntegerGraph.fromVehicleRoutingApplication(inputFileName);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Error initializing graph.");
            System.exit(1);
        }

        int edgecount = 0;
        for(Set<Integer> neighbors : myGraph.adjacency.values()) {
            edgecount += neighbors.size();
        }
        edgecount /= 2;


        System.out.println("Initialized Graph.");
        System.out.println(String.format("Added %s vertices and %s edges", myGraph.vertices.size(), edgecount));

        // TODO add benchmarking timer

        // Optional vertex pruning
        if(vertexPruning) {
            myGraph.preprocess();
            myGraph.initialCover = myGraph.vertices; // update initialCover to account for removed vertices
        }

        // Optional disconnected subgraph splitting (subgraphs are in ascending order by vertexcount)
        TreeSet<IntegerGraph> graphs = new TreeSet<>(Comparator.comparingInt((IntegerGraph g) -> g.vertices.size()).thenComparing(g -> g.vertices.toString()));

        if(splitSubgraphs) {
            graphs.addAll(myGraph.getDisconnectedSubgraphs(myGraph.vertices));
        } else {
            graphs.add(myGraph);
        }

        // Optional calculation of greedy solutions
        if(greedySolution) {
            graphs.forEach(g -> g.initialCover = g.getGreedyCover(g.vertices, g.neighborWeightDifferenceComparator));
        }

        // Calculate Vertex Cover
        Set<Integer> cover = myGraph.inCover;   // add vertices that might have been pruned earlier

        long totalWeight = myGraph.getSetWeight(cover);
        for(IntegerGraph graph: graphs) {
            totalWeight += graph.getSetWeight(graph.initialCover);
        }

        Set<Integer> currentSolution;
        // TODO there are mulitple ways of running this. See Besprechung 5 Notes for further information
        for(IntegerGraph graph : graphs) {
            totalWeight -= graph.getSetWeight(graph.initialCover);
            currentSolution = graph.mvc_localsearch(graph.initialCover, k_max, totalWeight);
            totalWeight += graph.getSetWeight(currentSolution);
            cover.addAll(currentSolution);
        }


        System.out.println("Finished Running");
        System.out.println("Solution weight: " + myGraph.getSetWeight(cover));
        System.out.println("Solution is cover: " + myGraph.isVertexCover(cover));
        System.out.println(totalWeight);


/*        myGraph.preprocess();
        Set<Integer> initialCover = (Set<Integer>) myGraph.vertices.clone();

        System.out.println("Initial solution");
        System.out.println(String.format("Is cover: %s",myGraph.isVertexCover(initialCover)));
        System.out.println(String.format("Weight: %d", myGraph.getSetWeight(initialCover)+myGraph.getSetWeight(myGraph.inCover)));
        System.out.println();*/

/*
        Set<Integer> greedyCover = myGraph.getGreedyCover(initialCover, myGraph.neighborWeightDifferenceComparator);
        System.out.println("Greedy solution");
        System.out.println(String.format("Is cover: %s",myGraph.isVertexCover(greedyCover)));
        System.out.println(String.format("Weight: %d", myGraph.getSetWeight(greedyCover)+myGraph.getSetWeight(myGraph.inCover)));
        System.out.println();
*/


/*
        long start = System.currentTimeMillis();
        TreeSet<IntegerGraph> subgraphs = myGraph.getDisconnectedSubgraphs(myGraph.vertices);
        System.out.println("Took " + ((System.currentTimeMillis()-start)/1000) + " seconds to compute subsets");

        System.out.println("Number of disconnected subgraphs: " + subgraphs.size());

        for(IntegerGraph subgraph : subgraphs) {
            System.out.println(subgraph.vertices.size());
        }
*/



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
