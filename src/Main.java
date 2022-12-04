import robustTwoClub.graph.RtcGraph;


/*
    USED DATASETS
    https://networkrepository.com/dimacs.php
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");

        RtcGraph myGraph = new RtcGraph("datasets/C1000-9.mtx", 0);
        System.out.println(String.format("Vertex count: %d", myGraph.size()));
        System.out.println(String.format("Edge count: %d", myGraph.getEdgeCount()));


    }
}
