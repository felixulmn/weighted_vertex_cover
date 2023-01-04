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
            myGraph = new IntegerGraph(args[0]);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Please provide a path to the problem file.");
            System.exit(1);
        }

        Set<Integer> initialCover = (Set<Integer>) myGraph.vertices.clone();
        System.out.println("Initial solution");
        System.out.println(String.format("Is cover: %s",IntegerGraph.isVertexCover(initialCover, myGraph.adjacency)));
        System.out.println(String.format("Size: %d", initialCover.size()));
        System.out.println();


        // Calculate minimum vertex cover and measure time
        long start = System.currentTimeMillis();
        Set<Integer> minCover = IntegerGraph.localSearch(myGraph.adjacency, initialCover, 30);

        System.out.println("Took " + ((System.currentTimeMillis()-start)/1000.0) +  " seconds to calculate minimum vertex cover.");
        System.out.println(String.format("Is cover: %s",IntegerGraph.isVertexCover(initialCover, myGraph.adjacency)));
        System.out.println(String.format("Size: %d", minCover.size()));

    }
}
