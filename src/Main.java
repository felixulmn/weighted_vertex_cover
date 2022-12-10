import com.felixullmann.graphs.IntegerGraph;
import com.felixullmann.graphs.Set;

import java.util.Arrays;


/*
    USED DATASETS
    https://networkrepository.com/dimacs.php
 */
public class Main {
    public static void main(String[] args) {

        IntegerGraph myGraph = new IntegerGraph("datasets/mydataset.txt");

        Set<Integer> independent = new Set<>(Arrays.asList(0,1,3));
        Set<Integer> notIndependent = new Set<>(Arrays.asList(0,1,2));

        System.out.println(myGraph.vertices);

        for(Integer v : myGraph.vertices) {
            System.out.println(String.format("Vertex: %s - Neighbours: %s", v, myGraph.adjacency.get(v)));
        }


        System.out.println(IntegerGraph.isIndependent(independent,myGraph.adjacency));
        System.out.println(IntegerGraph.isIndependent(notIndependent,myGraph.adjacency));

        Set<Integer> difference = independent.minus(notIndependent);
        System.out.println(difference);
        System.out.println(independent);
        System.out.println(notIndependent);




    }
}
