package vrp.model;

import java.util.ArrayList;
import java.util.Collections;


public class VRPProgram {

	public static int CAR_LIMIT = 40;
	private static int[][] savings;
	public static int[][] distances;
	private static Node[] nodes;
	private static String[] adds;
	private static ArrayList<Route> routes;
	private static int nCount;
	private static int[] amounts;
	
	
	public static ArrayList<Node> cluster(){
		Node depo = nodes[0];
		ArrayList<Node> nodesList = new ArrayList<Node>();
		
		for(int i=1;i<nodes.length;i++){
		   Node n = nodes[i];
		   if(n.x >= depo.x){
			   if(n.y>= depo.y){
				   n.cluster = 1;
			   }else{
				   n.cluster = 4;
			   }
		   }else{
			   if(n.y>= depo.y){
				   n.cluster = 2;
			   }else{
				   n.cluster = 3;
			   }
		   }
		   
		   
		   for(int j=1;j<5;j++){
			   if(n.cluster == j){
				   double difx = Math.abs(n.x - depo.x);
				   double dify = Math.abs(n.y - depo.y);
				   
				   if(dify!=0){
					   double tangA = (double)dify/difx;
					   
					   if(n.cluster == 2 || n.cluster == 4){
						   tangA= 1/tangA;
					   }
					   n.angle+= Math.atan(tangA);
				   }
				   
				   break;
			   }
			   else{
				   n.angle+= Math.PI/2;
			   }
		   }
		   nodesList.add(n);
	   }
	   return nodesList;
	}
	
	/**
	 * Load the data from external variables
	 * @param lNodes
	 * @param lCount
	 * @param lDistances
	 * @param lAmounts
	 * @param lAdds
	 * @param carLim
	 * @return
	 */
	public static boolean loadData(Node[] lNodes,int lCount, int[][] lDistances, int[] lAmounts,String[] lAdds,int carLim){
		boolean returnVal = true;
		
		try{
			CAR_LIMIT = carLim;
			nCount = lCount;
			nodes = lNodes;
			distances = lDistances;
			amounts = lAmounts;
			adds = lAdds;
		}catch(Exception ex){
			returnVal = false;
		}
		
		return returnVal;
	}
	
	
	/**
	 * Implementation of the Sweep algorithm
	 * @return
	 */
	public static String sweep(){
		ArrayList<Node> nodesList = cluster();
		Collections.sort(nodesList);
		
		//Cluster
		Cluster actualCluster = new Cluster();
		
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		
		//pridam 0 do clusteru
		actualCluster.add(nodes[0]);
		for(int i=0;i<nodesList.size();i++){
			Node n = nodesList.get(i);
			
			//pokud by byla prekrocena kapacita vytvorim novy cluster
			if(actualCluster.amount + n.amount> CAR_LIMIT){
				clusters.add(actualCluster);
				actualCluster = new Cluster();
				//pridam depot uzel do kazdeho clusteru
				actualCluster.add(nodes[0]);
			}
			
			//pridam uzel do clusteru
			//pridam vsechny hrany ktere inciduji s uzly ktere jiz jsou v clusteru
			actualCluster.add(n);
			for(int j=0;j<actualCluster.nodes.size();j++){
				Node nIn = actualCluster.nodes.get(j);
				Edge e = new Edge(nIn,n,distances[nIn.index][n.index]);
				
				Edge eReverse = new Edge(n,nIn,distances[n.index][nIn.index]);
				
				actualCluster.edges.add(e);
				actualCluster.edges.add(eReverse);
			}
			
			//v pripade posledni polozky musim pridat i cluster.
			if(i==nodesList.size()-1){
				clusters.add(actualCluster);
			}
		}
		
		int totalCost = 0;
		int clusterCount = clusters.size();
		
		StringBuilder sb = new StringBuilder();
		sb.append(clusterCount +"\r\n");
		
		for(int i=0;i<clusterCount;i++){
			//System.out.println("Cluster: " + clusters.get(i).amount);
			clusters.get(i).mst();
			//clusters.get(i).printMST();
			clusters.get(i).dfsONMST();
			clusters.get(i).printTSP(sb);
			sb.append("");
			sb.append("\r\n");
			totalCost += MyUtils.compClusterCost(clusters.get(i), distances);
		}
		
		for(int i=0;i<clusterCount;i++){
			clusters.get(i).printTSPAdds(sb);
			sb.append("\r\n");
		}
		sb.append("TOTAL COST OF THE ROUTES:" + totalCost);
		return sb.toString();
	}
	
	/**
	 * Implementation of the Clarks' & Wright's algorithm
	 * @return
	 */
	public static String clarkWright(){
		routes = new ArrayList<Route>();
		
		//I create N nodes. Each node will be inserted into a route.
		//each route will contain 2 edges - from the depo to the edge and back
		for(int i=0;i<nCount;i++){
			
			Node n = nodes[i];
			
			if(i!=0){
				//creating the two edges
				Edge e  = new Edge(nodes[0],n,distances[0][n.index]);
				Edge e2 = new Edge(n,nodes[0],distances[0][n.index]);
			
				Route r = new Route(nCount);
				//40 omezeni kamionu
				r.allowed = CAR_LIMIT;
				r.add(e);
				r.add(e2);
				r.actual += n.amount;
				
				routes.add(r);
			}	
		}
		
		
		MyUtils.printRoutes(routes);
		//Computing the savings - the values which made be saved by optimization
		ArrayList<Saving> sList = computeSaving(distances, nCount, savings,nodes);
		//sorting the savings
		Collections.sort(sList);
		
		//and use the savings until the list is not empty
		while(!sList.isEmpty()){
			Saving actualS = sList.get(0);
			
			Node n1 = actualS.from;
			Node n2 = actualS.to;
			
			Route r1 = n1.route;
			Route r2 = n2.route;
			
			int from = n1.index;
			int to = n2.index;
			
			//MyUtils.printSaving(actualS);
			
			if(actualS.val>0 && r1.actual+r2.actual<r1.allowed && !r1.equals(r2)){
				
				//moznozt jedna z uzlu do kteryho se de se de do cile
				
				Edge outgoingR2 = r2.outEdges[to];
				Edge incommingR1 = r1.inEdges[from];
				
				
				if(outgoingR2!=null && incommingR1 != null){
					boolean succ = r1.merge(r2, new Edge(n1,n2,distances[n1.index][n2.index]));
					if(succ){
						routes.remove(r2);
					}
				}else{
					System.out.println("Problem");
				}
				
			}
			
			sList.remove(0);
			//MyUtils.printRoutes(routes);
			
		}
		StringBuilder sb = new StringBuilder();
		sb.append(routes.size() + "\r\n");
		
		MyUtils.printRoutesCities(routes,sb);
		MyUtils.printAdds(routes,adds,sb);
		return sb.toString();
	}
	
	
	/**
	 * Computation of savings. The value which could be saved if we would not return to the depo, but instead pass directly from one node to other.
	 * @param dist
	 * @param n
	 * @param sav
	 * @param nodesField
	 * @return
	 */
	public static ArrayList<Saving> computeSaving(int[][] dist,int n,int[][] sav,Node[] nodesField){
		sav = new int[n][n];
		ArrayList<Saving> sList = new ArrayList<Saving>();
		for(int i=1;i<n;i++){
			for(int j=i+1;j<n;j++){
				sav[i][j] = dist[0][i] + dist[j][0] - dist[i][j];
				Node n1 = nodesField[i];
				Node n2 = nodesField[j];
				Saving s = new Saving(sav[i][j],n1, n2);
				sList.add(s);
			}
		}
		return sList;		
	}
}
