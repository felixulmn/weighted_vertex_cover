import com.felixullmann.graphs.IntegerGraph;
import com.felixullmann.graphs.Set;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
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
        boolean cliquePruning = false;
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
                case "-c":
                case "--clique-pruning":
                    cliquePruning = true;
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
        Set<Integer> minimumVertexCover = new Set<>();

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


/*
        IntegerGraph.VertexCyclingIterator vertexCyclingIterator = new IntegerGraph.VertexCyclingIterator(myGraph.vertices);

        int i = 0;
        while(vertexCyclingIterator.hasNext()) {
            Integer next = vertexCyclingIterator.next();
            System.out.println(next);

           if(i == 4) {
                System.out.println("lastswap: " + next);
                vertexCyclingIterator.markSwap();
            }

            i++;
        }
*/



        long start = System.currentTimeMillis();

        // Optional vertex pruning
        if(vertexPruning) {
            minimumVertexCover.addAll(myGraph.preprocess());
        }

        // TODO edge (8,15) was added to toyproblem2 (for clique pruning), has been removed again
        // Optional clique pruning
        if(cliquePruning) {
            minimumVertexCover.addAll(myGraph.doCliquePruning());
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
            graphs.forEach(g -> g.initialSolution = g.getGreedyCover(g.vertices, g.neighborWeightDifferenceComparator));
        } else {
            graphs.forEach(g -> g.initialSolution = (Set<Integer>) g.vertices.clone());
        }

        // Calculate Vertex Cover
        long totalWeight = myGraph.getSetWeight(minimumVertexCover);
        for(IntegerGraph graph: graphs) {
            totalWeight += graph.getSetWeight(graph.initialSolution);
        }

        Set<Integer> currentSolution;
        // TODO there are mulitple ways of running this. See Besprechung 5 Notes for further information


        for(IntegerGraph graph : graphs) {
            totalWeight -= graph.getSetWeight(graph.initialSolution);
            currentSolution = graph.localSearchVertexCycling(graph.initialSolution, k_max, totalWeight);
            totalWeight += graph.getSetWeight(currentSolution);
            minimumVertexCover.addAll(currentSolution);
        }

        long time = (System.currentTimeMillis() - start);
        System.out.println("Finished Running in " + time + " milliseconds (" + time/1000 + " seconds.)");
        System.out.println("Solution weight: " + myGraph.getSetWeight(minimumVertexCover));
        System.out.println("Solution is cover: " + myGraph.isVertexCover(minimumVertexCover));


    }
}
