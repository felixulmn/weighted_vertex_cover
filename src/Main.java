import robustTwoClub.graph.RtcGraph;
import com.felixullmann.graphs.Set;

import java.util.Arrays;
import java.util.HashSet;


/*
    USED DATASETS
    https://networkrepository.com/dimacs.php
 */
public class Main {
    public static void main(String[] args) {

/*
        RtcGraph myGraph = new RtcGraph("datasets/C1000-9.mtx", 0);
        System.out.println(String.format("Vertex count: %d", myGraph.size()));
        System.out.println(String.format("Edge count: %d", myGraph.getEdgeCount()));
*/
/*
        RtcGraph myGraph = new RtcGraph("datasets/mydataset.txt", 0);
        System.out.println(String.format("Vertex count: %d", myGraph.size()));
        System.out.println(String.format("Edge count: %d", myGraph.getEdgeCount()));
*/

/*
        HashSet<Integer> set1 = new HashSet<>(Arrays.asList(1, 2, 3));
        HashSet<Integer> set2 = new HashSet<>(Arrays.asList(3,4,5));

        // union operation
        HashSet<Integer> union = (HashSet<Integer>) set1.clone();
        union.addAll(set2);

        // intersection operation
        HashSet<Integer> intersect = (HashSet<Integer>) set1.clone();
        intersect.retainAll(set2);

        // set difference operation
        HashSet<Integer> set1minusSet2 = (HashSet<Integer>) set1.clone();
        set1minusSet2.removeAll(set2);


        System.out.println(union); // should be 1,2,3,4,5
        System.out.println(intersect); // should be 3
        System.out.println(set1minusSet2); // should be 1,2
*/

        // isIndependent

/*        RtcGraph myGraph = RtcGraph.createGraphFromOnlineFile("datasets/mydataset.txt");

        HashSet<Integer> independent = new HashSet<>(Arrays.asList(0,1,3));
        HashSet<Integer> notIndependent = new HashSet<>(Arrays.asList(0,1,2));

        System.out.println(myGraph.nodes);

        for(Integer v : myGraph.nodes) {
            System.out.println(String.format("Vertex: %s - Neighbours: %s", v, myGraph.adjacency.get(v)));
        }


        System.out.println(myGraph.isIndependent(independent));
        System.out.println(myGraph.isIndependent(notIndependent));*/


        Set<Integer> independent = new Set<>();
        Set<Integer> notIndependent = new Set<>();

        independent.add(0);
        independent.add(1);
        independent.add(3);

        notIndependent.add(0);
        notIndependent.add(1);
        notIndependent.add(2);

        Set<Integer> difference = independent.minus(notIndependent);
        System.out.println(difference);
        System.out.println(independent);
        System.out.println(notIndependent);




    }
}
