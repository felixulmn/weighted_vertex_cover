import com.felixullmann.graphs.IntegerGraph;
import com.felixullmann.graphs.Set;

import java.util.Arrays;


/*
    USED DATASETS
    https://networkrepository.com/dimacs.php
 */
public class Main {
    public static void main(String[] args) {

        IntegerGraph myGraph = new IntegerGraph("datasets/C125-9.mtx");
        
        Set<Integer> initialCover = (Set<Integer>) myGraph.vertices.clone();
        Set<Integer> minCover = IntegerGraph.localSearch(myGraph.adjacency, initialCover, 30);

        System.out.println(initialCover);
        System.out.println(String.format("Is cover: %s",IntegerGraph.isVertexCover(initialCover, myGraph.adjacency)));
        System.out.println();
        System.out.println(minCover);
        System.out.println(String.format("Size of cover: %d", minCover.size()));
        System.out.println(String.format("Is cover: %s",IntegerGraph.isVertexCover(initialCover, myGraph.adjacency)));


/*        Set<Integer> independent = new Set<>(Arrays.asList(0,1,3));
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
        System.out.println(notIndependent);*/

    }
}
