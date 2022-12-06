package robustTwoClub.graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import java.util.StringTokenizer;


public class RtcGraph {

    public static long	flowConstructGraph, flowComputePath;

    // TODO only added class to avoid IDE complaints
    public static class GraphException extends Exception {

    }

    public HashMap<Integer, String> idMap;					// remembers vertex names for outputting solution graphs
    public HashSet<Integer> nodes;							// set of vertices of the graph
    public HashMap<Integer, HashSet<Integer>> adjacency;	// sparse adjacency matrix of the graph

    /** Loads a graph from a DIMACS or METIS file.
     *
     * @param filename Name of the file to be read.
     * @param type 0 = DIMACS, 1 = METIS, 2 = DIMACS clq
     * @throws GraphException
     */
    public RtcGraph (String filename, int type) {

        idMap = new HashMap<Integer, String>();
        nodes = new HashSet<Integer>();
        adjacency = new HashMap<Integer, HashSet<Integer>>();
        HashMap<String, Integer> seenIds = new HashMap<String, Integer>();	// only required during construction
        File file = new File(filename);

        try {
            final LineNumberReader reader = new LineNumberReader(new FileReader(file));
            String str;
            int id = 0;
            if (type == 0) {		// Parse DIMACS file
                reader.readLine();  //read first line containing the number of vertices and edges
                while ((str = reader.readLine()) != null)
                {
                    str = str.trim();										// trim away whitespace at either end of line
                    if (!str.startsWith("#")) {								// skip comment lines
                        StringTokenizer tokens = new StringTokenizer(str);
                        if (tokens != null && tokens.countTokens() > 1) {	// only consider well-formed lines
                            String vertexA = tokens.nextToken();
                            String vertexB = tokens.nextToken();
                            if (!seenIds.containsKey(vertexA)) {		// add vertex 0 if never seen before
                                seenIds.put(vertexA, id);
                                idMap.put(id, vertexA);
                                addVertex(id);
                                id++;
                            }
                            if (!seenIds.containsKey(vertexB)) {		// add vertex 1 if never seen before
                                seenIds.put(vertexB, id);
                                idMap.put(id, vertexB);
                                addVertex(id);
                                id++;
                            }
                            // add edge to both adjacency lists
                            addEdge(seenIds.get(vertexA), seenIds.get(vertexB));
                        }
                    }
                }
            } else if (type == 1) {		// parse METIS file
                StringTokenizer header = new StringTokenizer(reader.readLine());
                int vertices = Integer.parseInt(header.nextToken());
                int edges = Integer.parseInt(header.nextToken());
                boolean weights = header.hasMoreTokens() && !header.nextToken().equals("0");
                for (int v = 0; v < vertices; v++) {
                    addVertex(v);
                    idMap.put(v, Integer.toString(v+1));
                }
                int edgesCounted = 0;
                int currentVertex = 0;
                while ((str = reader.readLine()) != null)
                {
                    if (!str.startsWith("#") && !str.startsWith("%")) {				// skip comment lines
                        StringTokenizer tokens = new StringTokenizer(str);
                        while (tokens.hasMoreTokens()) {
                            int v = Integer.parseInt(tokens.nextToken()) - 1;
                            if (!adjacent(currentVertex, v)) {
                                addEdge(currentVertex, v);
                                edgesCounted++;
                            }
                            if (weights) tokens.nextToken(); // Throw away weight token
                        }
                        currentVertex++;
                    }
                }
                if (edges != edgesCounted) {
                    System.out.println("WARNING: Wrong edge count while loading METIS file "+filename+".");
                    System.out.println("Edges stated: "+edges+"   Edges counted: "+edgesCounted+"\n");
                }
            } else if (type == 2) {
                while ((str = reader.readLine()) != null)
                {
                    StringTokenizer tokens = new StringTokenizer(str);
                    String lineType = tokens.nextToken();
                    if (lineType.equals("c")) continue;
                    if (lineType.equals("p")) {
                        tokens.nextToken();
                        int vertices = Integer.parseInt(tokens.nextToken());
                        for (int v = 0; v < vertices; v++) {
                            addVertex(v);
                            idMap.put(v, Integer.toString(v+1));
                        }
                    }
                    if (lineType.equals("e")) {
                        addEdge(Integer.parseInt(tokens.nextToken()) - 1, Integer.parseInt(tokens.nextToken()) - 1);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Could not locate input file '"+filename+"'.");
            System.exit(0);
        }
    }

    /**	Tries to guess whether input file has DIMACS or METIS format.
     *  Guess will be nonsense, if file is neither.
     *
     * @param filename	Name of the input file for which the type shall be guessed.
     * @return			Whether the input seems to be DIMACS. (METIS otherwise)
     */
    public static int guessInputType (String filename) {
        if (filename.endsWith(".dimacs")) return 0;	// I guess it is a DIMACS graph
        if (filename.endsWith(".edges")) return 0;	// I guess it is a DIMACS graph
        if (filename.endsWith(".graph")) return 1;	// I guess it is a METIS graph
        if (filename.endsWith(".clq")) return 2;	// I guess it is a DIMACS clq graph
        System.out.println("Cannot determine type of input file.");
        System.out.println("If file is either a valid DIMACS or Metis file, please specify file type");
        System.out.println("by appending options '-dimacs' or '-metis' to call of program.");
        System.exit(0); // Abort program if input type can't be guessed from extension
        return 0;	// Dummy return
    }

    public int size () {
        return nodes.size();
    }

    /** Returns whether the given vertex ID belongs to the graph. */
    public boolean contains (int v) {
        return nodes.contains(v);
    }

    public int degree (int v) {
        return adjacency.get(v).size();
    }

    /** Returns whether vertices v and w are adjacent. */
    public boolean adjacent (int v, int w) {
        return adjacency.get(v).contains(w);
    }

    public HashSet<Integer> getVertices () {
        return nodes;
    }

    public int getEdgeCount () {
        int edges = 0;
        for (int v : nodes)
            edges += adjacency.get(v).size();
        edges /= 2;
        return edges;
    }

    public String edgesToString ()
    {
        return adjacency.toString();
    }

    public HashSet<Integer> getNeighbors (int v) {
        return adjacency.get(v);
    }

    public HashSet<Integer> getTwoNeighbors (int v) {
        HashSet<Integer> nb = new HashSet<Integer>();
        for (int w : adjacency.get(v))
            nb.addAll(adjacency.get(w));
        nb.addAll(adjacency.get(v));
        nb.remove(v);
        return nb;
    }

    public int countCommonNeighbors (int v, int w) {
        int neighbors = 0;
        for (int x : adjacency.get(v))
            if (adjacency.get(w).contains(x))
                neighbors++;
        return neighbors;
    }

    public int countCommonNeighbors (Integer[] vertices) {
        int neighbors = 0;
        for (int x : adjacency.get(vertices[0])) {
            boolean compatible = true;
            for (int i = 1; i < vertices.length; i++)
                if (!adjacency.get(vertices[i]).contains(x))
                {compatible = false; break;}
            if (compatible) neighbors++;
        }
        return neighbors;
    }

    public HashSet<Integer> getCommonNeighbors (int v, int w) {
        HashSet<Integer> neighbors = new HashSet<Integer>();
        for (int x : adjacency.get(v))
            if (adjacency.get(w).contains(x))
                neighbors.add(x);
        return neighbors;
    }

    public int sizeOfTwoNeighborhood (int x, boolean countX) {
        HashSet<Integer> vertices = new HashSet<Integer>();
        for (int v : adjacency.get(x)) {
            vertices.add(v);
            for (int w : adjacency.get(v))
                vertices.add(w);
        }
        if (!countX) vertices.remove(x);
        return vertices.size();
    }

    /** Creates a subgraph of given vertices and remaps vertex IDs to range 0 .. (subgraph size - 1) */
    public RtcGraph getSubgraph (HashSet<Integer> vertices, int center) {
        RtcGraph subgraph = new RtcGraph();
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> mapBack = new HashMap<Integer, Integer>();
        // Add center
        map.put(center, 0);
        map.put(0, center);
        subgraph.addVertex(0);
        int id = 1;
        // Add vertices
        for (int v : vertices) if (v != center) {
            map.put(v, id);
            mapBack.put(id, v);
            subgraph.addVertex(id++);
        }
        // Add edges
        for (int v : vertices) for (int w : adjacency.get(v))
            if (w > v && vertices.contains(w))
                subgraph.addEdge(map.get(v), map.get(w));
        // Construct new idMap
        HashMap<Integer, String> newIdMap = new HashMap<Integer, String>();
        for (int i = 0; i < subgraph.size(); i++)
            newIdMap.put(i, idMap.get(mapBack.get(i)));
        subgraph.idMap = newIdMap;
        return subgraph;
    }

    /** Returns the 2-neighborhood of some vertex as a new graph.
     *
     * @param c Vertex for which to obtain the 2-neighborhood.
     * @return The new graph.
     */
    public RtcGraph getTwoNeighborhood (Integer c) {
        RtcGraph g = new RtcGraph();
        // We map the original integer IDs to new ones in order to minimize the range of vertex IDs
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> mapBack = new HashMap<Integer, Integer>();
        g.addVertex(0);										// add center vertex
        map.put(c, 0);
        mapBack.put(0, c);
        int index = 1;
        for (int v : adjacency.get(c)) {
            if (!map.containsKey(v)) {
                g.addVertex(index);
                map.put(v, index);
                mapBack.put(index, v);
                index++;
            }
            for (int w : adjacency.get(v)) {
                if (!map.containsKey(w)) {
                    g.addVertex(index);
                    map.put(w, index);
                    mapBack.put(index, w);
                    index++;
                }
            }
        }
        for (int v : g.nodes)
            for (int w : adjacency.get(mapBack.get(v)))
                if (map.containsKey(w) && map.get(w) > v)
                    g.addEdge(v, map.get(w));

        // Construct idMap for new graph, mapping new integer IDs to original string identifiers
        HashMap<Integer, String> newIdMap = new HashMap<Integer, String>();
        for (int i = 0; i < g.size(); i++)
            newIdMap.put(i, idMap.get(mapBack.get(i)));
        g.idMap = newIdMap;
        return g;
    }

    public RtcGraph getOneNeighborhood (Integer c) {
        RtcGraph g = new RtcGraph();
        // We map the original integer IDs to new ones in order to minimize the range of vertex IDs
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> mapBack = new HashMap<Integer, Integer>();
        g.addVertex(0);										// add center vertex
        map.put(c, 0);
        mapBack.put(0, c);
        int index = 1;
        for (int v : adjacency.get(c)) {
            g.addVertex(index);
            map.put(v, index);
            mapBack.put(index, v);
            index++;
        }

        for (int v : g.nodes)
            for (int w : adjacency.get(mapBack.get(v)))
                if (map.containsKey(w) && map.get(w) > v)
                    g.addEdge(v, map.get(w));

        // Construct idMap for new graph, mapping new integer IDs to original string identifiers
        HashMap<Integer, String> newIdMap = new HashMap<Integer, String>();
        for (int i = 0; i < g.size(); i++)
            newIdMap.put(i, idMap.get(mapBack.get(i)));
        g.idMap = newIdMap;
        return g;
    }

    public void deleteVertex (int vertex) {
        for (int neighbor : adjacency.get(vertex))
            adjacency.get(neighbor).remove(vertex);
        nodes.remove(vertex);
    }

    public void retainVertices (HashSet<String> vertices) {
        HashSet<Integer> deleteSet = new HashSet<Integer>();
        for (int v : nodes)
            if (!vertices.contains(getVertexName(v)))
                deleteSet.add(v);
        for (int v : deleteSet)
            deleteVertex(v);
    }

    public void retainVerticesByID (HashSet<Integer> vertices) {
        HashSet<Integer> deleteSet = new HashSet<Integer>();
        for (int v : nodes)
            if (!vertices.contains(v))
                deleteSet.add(v);
        for (int v : deleteSet)
            deleteVertex(v);
    }

    /**
     * IMPORTANT:	UNDELETES ARE ONLY CORRECT when performed IN REVERSE ORDER of deletion!
     *
     * @param vertex
     * @throws GraphException
     */
    public void undeleteVertex (int vertex) {
        nodes.add(vertex);
        for (int neighbor : adjacency.get(vertex))
            adjacency.get(neighbor).add(vertex);
    }

    public RtcGraph getClone() {
        RtcGraph clone = new RtcGraph();
        for (int v : nodes)
            clone.addVertex(v);
        for (int v : nodes)
            for (int w : adjacency.get(v))
                if (w > v)
                    clone.addEdge(v, w);
        clone.idMap = idMap;
        return clone;
    }

    public String getVertexName (Integer vertex) {
        return idMap.get(vertex);
    }

    public HashSet<String> getVertexNames (HashSet<Integer> ids) {
        HashSet<String> vertexNames = new HashSet<String>();
        for (int vertex : ids)
            vertexNames.add(idMap.get(vertex));
        return vertexNames;
    }

    public HashSet<String> getVertexNames () {
        HashSet<String> vertexNames = new HashSet<String>();
        for (int vertex : nodes)
            vertexNames.add(idMap.get(vertex));
        return vertexNames;
    }

    /** Returns whether the whole graph is connected. */
    public boolean isConnected ()
    {
        if (nodes.size() == 0)
            return true;
        int v = nodes.iterator().next();
        HashSet<Integer> visitedNodes = new HashSet<Integer>();
        Stack<Integer> activeNodes = new Stack<Integer>();
        activeNodes.add(v);
        visitedNodes.add(v);
        while (!activeNodes.isEmpty())
        {
            int currentNode = activeNodes.pop();
            for (int w : adjacency.get(currentNode))
            {
                if (!visitedNodes.contains(w))
                {
                    activeNodes.add(w);
                    visitedNodes.add(w);
                }
            }
        }
        return (visitedNodes.size() == nodes.size());
    }


    /**
     * returns the edges of a path from v to w
     * @param v
     * @param w
     * @return returns null if v=w or if there is no path. Otherwise the vertices of a path are returned.
     */
    public LinkedList<Integer> getPath(int v, int w)
    {
        if (v == w)
        {
            return null;
        }
        long time = System.nanoTime();

        LinkedList<Integer> result = new LinkedList<Integer>();
        if (adjacent(v, w))
        {
            result.add(v);
            result.add(w);
            flowComputePath += System.nanoTime() - time;
            return result;
        }

        LinkedList<Integer> toVisit = new LinkedList<Integer>();
        HashMap<Integer,Integer> predecessor = new HashMap<Integer,Integer>();
        HashSet<Integer> seen = new HashSet<Integer>();

        toVisit.add(v);
        seen.add(v);

        boolean reached = false;

        while (!reached && !toVisit.isEmpty())
        {
            int activeVertex = toVisit.removeFirst();
            for (int neighbor : getNeighbors(activeVertex))
            {
                if (neighbor == w)
                {
                    reached = true;
                }
                if (!seen.contains(neighbor))
                {
                    predecessor.put(neighbor, activeVertex);
                    seen.add(neighbor);
                    toVisit.add(neighbor);
                }
            }
        }

        if (reached)
        {
            int activeVertex = w;
            while (activeVertex != v)
            {
                result.addFirst(activeVertex);
                activeVertex = predecessor.get(activeVertex);
            }
            result.addFirst(v);

            flowComputePath += System.nanoTime() - time;
            return result;
        }
        else
        {
            flowComputePath += System.nanoTime() - time;
            return null;
        }
    }

    public void writeToFile(String directory, String filename, String[] header) {
        writeToFile(directory, filename, header, 0);
    }

    /** Writes this graph to disk in edge list format.
     *
     * @param directory String representation of the directory to which the graph shall be saved.
     * @param filename	Name of the file to which the graph shall be saved.
     * @param header	String array of header information for file. Pass null, if not required.
     */
    public void writeToFile(String directory, String filename, String[] header, int indexDecrement) {
        File file; File dir;
        if (directory != null) {
            file = new File(directory+"/"+filename+".dimacs");
            dir = new File(directory+"/");
            if (!dir.exists()) dir.mkdir();
        } else {
            file = new File(filename+".dimacs");
        }
        try {
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            // Write header information to file
            if (header != null)
                for (int line = 0; line < header.length; line++)
                    bw.write(header[line]+"\n");
            // Write adjacency information to file as edge list
            HashSet<Integer> seenNodes = new HashSet<Integer>();
            for (Integer v : nodes) {
                seenNodes.add(v);
                for (Integer w : adjacency.get(v))
                    if (!seenNodes.contains(w))
                        if (indexDecrement == 0)
                            bw.write(idMap.get(v) + "\t" + idMap.get(w) + "\n");
                        else {
                            bw.write((Integer.parseInt(idMap.get(v)) - indexDecrement) + "\t" +
                                    (Integer.parseInt(idMap.get(w)) - indexDecrement) + "\n");
                        }
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public RtcGraph () {
        idMap = new HashMap<Integer, String>();
        nodes = new HashSet<Integer>();
        adjacency = new HashMap<Integer, HashSet<Integer>>();
    }

    private void addVertex (Integer v) {
        nodes.add(v);
        adjacency.put(v, new HashSet<Integer>());
    }

    public void addVertex (Integer v, String name) {
        nodes.add(v);
        adjacency.put(v, new HashSet<Integer>());
        idMap.put(v, name);
    }

    public void addEdge (Integer v, Integer w) {
        if (v == w) return;		// No loops!
        adjacency.get(v).add(w);
        adjacency.get(w).add(v);
    }

    /**
     * check if given vertex set is independent using the graphs edges
     * @param vertexSet the vertex set to be tested
     * @return returns true if vertex set is independent, false if not.
     */
    public boolean isIndependent(HashSet<Integer> vertexSet) {
        //System.out.println(String.format("Vertexset: %s", vertexSet));
        for(Integer v : vertexSet) {
            //System.out.println(String.format("Vertex: %s - Neighbours: %s", v, this.adjacency.get(v)));
            for(Integer n_v : this.adjacency.get(v)) {
                if(vertexSet.contains(n_v))
                    return false;
            }
        }
        return true;
    }

    /**
     * creates graph from online graph tool format
     * @param fileName
     */
    public static RtcGraph createGraphFromOnlineFile(String fileName) {

        HashSet<Integer> vertices;
        HashMap<Integer, HashSet<Integer>> edges = new HashMap<>();

        try {
            File file = new File(fileName);
            final LineNumberReader reader = new LineNumberReader(new FileReader(file));

            // prepare vertex set and adjacency map
            int vertexCount = Integer.parseInt(reader.readLine());
            vertices = new HashSet<>(vertexCount*2);
            edges = new HashMap<>(vertexCount*2);

            for (int v = 0; v < vertexCount; v++) {
                vertices.add(v);
                edges.put(v,new HashSet<>());
            }

            // populate graph with edges
            String str;
            while ((str = reader.readLine()) != null) {
                StringTokenizer tokens = new StringTokenizer(str);
                Integer a = Integer.parseInt(tokens.nextToken());
                Integer b = Integer.parseInt(tokens.nextToken());

                edges.get(a).add(b);
                edges.get(b).add(a);
            }

            return new RtcGraph(vertices, edges);

        } catch (IOException e) {
            System.out.println("Could not locate input file '"+fileName+"'.");
            System.exit(0);
        }

        return null;
    }

    /**
     * creates graph from set of nodes and adjacency matrix
     * @param vertices a hashset containing all vertices
     * @param edges a hashmap that maps vertices to a hashset of neighbouring vertices
     */
    public RtcGraph(HashSet<Integer> vertices, HashMap<Integer, HashSet<Integer>> edges) {
        this.idMap = null;
        this.nodes = vertices;
        this.adjacency = edges;
    }

}

