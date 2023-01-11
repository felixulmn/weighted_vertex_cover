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



        Set<Integer> initialCover = (Set<Integer>) myGraph.vertices.clone();
        //initialCover = new Set<>(Arrays.asList(0, 1, 3, 4, 6, 7, 8));

        System.out.println("Initial solution");
        System.out.println(String.format("Is cover: %s",myGraph.isVertexCover(initialCover)));
        System.out.println(String.format("Weight: %d", myGraph.getSetWeight(initialCover)));
        System.out.println();


        // Calculate minimum vertex cover and measure time
        long start = System.currentTimeMillis();
        Set<Integer> minCover = myGraph.mvc_localsearch(initialCover, myGraph.vertices.size());

        System.out.println("Took " + ((System.currentTimeMillis()-start)/1000.0) +  " seconds to calculate minimum vertex cover.");
        System.out.println(String.format("Is cover: %s",myGraph.isVertexCover(minCover)));
        System.out.println(String.format("Weight: %d", myGraph.getSetWeight(minCover)));
        System.out.println("Cover nodes:");
        System.out.println(minCover);


        /*
        System.out.println("Removing 8 manually");
        minCover.remove(8);

        System.out.println(String.format("Is cover: %s",myGraph.isVertexCover(minCover)));
        System.out.println(String.format("Weight: %d", myGraph.getSetWeight(minCover)));
        System.out.println("Cover nodes:");
        System.out.println(minCover);
        */

    }
}
