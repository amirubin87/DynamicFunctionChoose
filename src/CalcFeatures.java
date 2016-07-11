import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class CalcFeatures {
	private static int Diameter = 0;
	private static int NumOfNodes = 0;
	public PrintWriter runTimeLog;
	public long startTime;	
	public boolean printTimeLog;	
	
	public double[] processGraph(String pathToGraph, String networkFileName, boolean printTimeLog, String logName, boolean lightVersion) throws IOException {		
		this.printTimeLog = printTimeLog;
		
		if (printTimeLog){
			this.runTimeLog = new PrintWriter(new BufferedWriter(new FileWriter(logName, true)));
			runTimeLog.println(logName);
			this.startTime = System.currentTimeMillis();
		}
					
		Set<Edge> edges = GetEdgesList(pathToGraph + networkFileName);
		
		takeTime("Read-edges");
		
		Map<Integer,Set<Integer>> adjacencyMap = buildAdjacencyMap(edges);
		System.out.println("adjacencyMap");
		takeTime("adjacencyMap");
		
		Set<Integer> nodes = adjacencyMap.keySet();
		NumOfNodes = nodes.size();
		
			int[][] distances = null;
			
			long[] distanceToAllOthers =  null;
			
			long sumOfDistances = 0;
			
		
		if(!lightVersion){
			distances = buildDistances(adjacencyMap, nodes);
			takeTime("buildDistances");
			distanceToAllOthers =  buildDistancesToAllOthers(distances);
			takeTime("buildDistancesToAllOthers");
			sumOfDistances = sumOfDistances(distanceToAllOthers);
			takeTime("sumOfDistances");
		}
		
		Map<Integer,Integer> node2triangles = new HashMap<>();					
		double[] triCount = FindTrianglesAndTriplets(adjacencyMap, node2triangles);
		takeTime("FindTrianglesAndTriplets");
		double numOfConnectedTriplets = triCount[1];
		
		long numOfCouples = (long)NumOfNodes*(long)(NumOfNodes-1);
		
		double numOfNodes = NumOfNodes;		 
		double numOfEdges = edges.size();
		double averageDegree = (2*numOfEdges)/numOfNodes;
		double diameter = Diameter;
		double density =numOfEdges*2/numOfCouples;		
		double numOfNodesInTriangles = triCount[2];				
		double numOfTriangles = triCount[0];
		double avergaeTrianglesRate = numOfTriangles/numOfNodes;
		//double avergaeTrianglesParticipationRate = 3*numOfTriangles/numOfNodes;
		double ratioOfNodesInTriangles = numOfNodesInTriangles/numOfNodes;
		
			double averagePathLength = 0.0;
			
			double averageEfficiency = 0.0;
					
		if(!lightVersion){
			averagePathLength = (double)sumOfDistances/numOfCouples;
			startTime = System.currentTimeMillis();
			averageEfficiency = calcAverageEfficiency(distanceToAllOthers);
			takeTime("calcAverageEfficiency");
		}
		
		double GCC = 3*numOfTriangles/(double)numOfConnectedTriplets;
		System.currentTimeMillis();
		double ACC = calcACC(adjacencyMap, node2triangles);		
		takeTime("calcAverageEfficiency");
		if (printTimeLog){
			this.runTimeLog.close();
		}
		
		return new double[] {
				numOfNodes,numOfEdges,averageDegree,
				diameter,density,numOfNodesInTriangles,numOfTriangles,
				avergaeTrianglesRate,
				ratioOfNodesInTriangles,averagePathLength,averageEfficiency,
				GCC,ACC
				};
	}
	
	
	private static double calcACC(Map<Integer, Set<Integer>> adjacencyMap, 
			Map<Integer,Integer> node2triangles) {
		double sum = 0;
		
		for (Integer v : adjacencyMap.keySet()){
			Integer numOfTrianglesI = node2triangles.get(v);
			Double numOfTriangles = numOfTrianglesI == null ? 0.0 : 1.0*numOfTrianglesI;
			double deg = adjacencyMap.get(v).size();
			if (deg>1)
				sum = sum + 2*numOfTriangles / (deg *(deg-1));
		}
		
		return sum / NumOfNodes;
	}

	private static double calcAverageEfficiency(long[] distanceToAllOthers) {
		double sum = 0;
		for (long d : distanceToAllOthers)
			if(d>0)
				sum = sum + 1/(double)d;
		return sum/NumOfNodes;
	}

	private static long sumOfDistances(long[] distanceToAllOthers) {
		long sum = 0;
		for (long d : distanceToAllOthers)
			sum = sum +d;
		return sum;
	}

	private static long[] buildDistancesToAllOthers(int[][] distances) {
		long[] distanceToAll = new long[NumOfNodes+1];
		for (int i = 0 ; i <= NumOfNodes ; i++){
			long sum =0;
			for (int j = 0 ; j <= NumOfNodes ; j++){
				sum = sum +(long)distances[i][j];
			}
			distanceToAll[i] = sum;
		}
		return distanceToAll;
	}

	private static double[] FindTrianglesAndTriplets(Map<Integer, Set<Integer>> adjacencyMap, Map<Integer,Integer> node2triangles) {		
		int triangles = 0;
		int ONLYtriplets = 0;
		int numOfNodesInTriangles=0;
		
		for ( Integer v1 : adjacencyMap.keySet()){		
			Set<Integer> neighbors = adjacencyMap.get(v1);			
			for (Integer v2 : neighbors){
				int v2val = v2.intValue();
				for (Integer v3 : neighbors){
					int v3val = v3.intValue();
					if (v2val < v3val){							 
						 if (adjacencyMap.get(v2).contains(v3)){
							 //v1-v2-v3
							 triangles++;	
							 Integer val = node2triangles.get(v1);
							 if(val == null) {
								 numOfNodesInTriangles++;
								 val = 0;
							 }
							 node2triangles.put(v1, val+1);
						 }
						 else{
							// v1-v2-v3	
							ONLYtriplets++;
						 }
					}
				}
				
			}
		}	
		// any triangle has 3 connected triplets in it.
		return  new double[] {triangles/3, ONLYtriplets + triangles, numOfNodesInTriangles};
	}
		
	private static Set<Edge> GetEdgesList(String pathToGraph) throws IOException {
		List<String> lines= Files.readAllLines(Paths.get(pathToGraph));
		Set<Edge> edges= new HashSet<>();
		
		for (String line : lines){
			String[] parts = line.split(" |\t");
			Integer v = Integer.parseInt(parts[0].trim());
			Integer u = Integer.parseInt(parts[1].trim());
			if(v.intValue() == u.intValue()) continue;
			if(v.intValue() > u.intValue()){
				Integer t = u;
				u = v;
				v = t;				
			}				
			edges.add(new Edge(v, u));
		}
		return edges;
	}
	
	private void takeTime(String msg) throws IOException {
		if (this.printTimeLog){
			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			runTimeLog.println(msg + " " + totalTime/1000);
			startTime = endTime;
		}	
	}
	
	public static Map<Integer,Set<Integer>> buildAdjacencyMap(Set<Edge> edges){
		if ((edges==null) || (edges.isEmpty())){
			return Collections.<Integer,Set<Integer>>emptyMap();
		}
		
		Map<Integer,Set<Integer>> AdjacencyMap = new HashMap<>();
		for (Edge e : edges){
			if (!AdjacencyMap.containsKey(e.getFrom())){
				AdjacencyMap.put((Integer)e.getFrom(), new HashSet<Integer>());
			}
			if (!AdjacencyMap.containsKey(e.getTo())){
				AdjacencyMap.put((Integer)e.getTo(), new HashSet<Integer>());
			}
			AdjacencyMap.get(e.getFrom()).add((Integer)e.getTo());
			AdjacencyMap.get(e.getTo()).add((Integer)e.getFrom());
		}
		
		return AdjacencyMap;
	}
	
	
	// http://web.archive.org/web/20080330235019/http://www.cs.auckland.ac.nz/~ute/220ft/graphalg/node19.html
	//The following method maxDistance returns the maximum distance of any of the vertices or  tex2html_wrap2754 if not all vertices are reachable, along with setting an integer array dist with the corresponding shortest distances. 
	static public int maxDistance(Map<Integer,Set<Integer>> adjacencyMap, Integer v, int dist[])
	{
	   int cnt = 0;
	   for (int i : adjacencyMap.keySet()) dist[i]=NumOfNodes;   // set to maximum distance
	   
	   dist[v.intValue()]=0;
	   
	   int depth = 1;                      // next distance in BFS	   
	   Vector<Integer> distList = new Vector<Integer>();	 

	   distList.addElement( v );	 

	   while ( distList.size() > 0 )
	   {
	     Vector<Integer> nextList = new Vector<Integer>();	 
	     for (Enumeration<Integer> e = distList.elements(); e.hasMoreElements(); )
	     {
	        Set<Integer> nbrs = adjacencyMap.get(( e.nextElement()));	 

	        for (Integer u : nbrs)
	        {	          
	          if (dist[u.intValue()] == NumOfNodes) // first time reachable?
	          {
	             cnt++;
	             dist[u] = depth;
	             nextList.addElement(u);
	           }

	        }

	     }
	     distList = nextList; // next try set of vertices further away
	     depth++;
	   }
	   
	   return cnt == NumOfNodes ? depth-1 : NumOfNodes;
	}
	
	public static int[][] buildDistances(Map<Integer,Set<Integer>> adjacencyMap, Set<Integer> nodes)
	{
	   int D[][] = new int[NumOfNodes+1][NumOfNodes+1];	    

	   for (Integer v : nodes)
	   {
		   int d = maxDistance(adjacencyMap, v, D[v]);
		   Diameter = d > Diameter ? d : Diameter;
	   }
	   return D;
	}
	

}
