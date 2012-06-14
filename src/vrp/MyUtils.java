package vrp;

import java.util.ArrayList;

public class MyUtils {

	public static int[][] randomMatrix(int n){
		int[][] random = new int[n][n];
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if(i!=j){
					random[i][j] = (int) (100 * Math.random());
			    }
				else{
					random[i][i] = 0;
				}
			}
		}
		return random;
	}
	
	public static int[] randomAmounts(int n){
		int[] amounts = new int[n];
		for(int i=1;i<n;i++){
			amounts[i] = (int) (100*Math.random());
		}
		return amounts;
	}
	
	public static void printSaving(Saving s){
		int from = s.from.index;
		int to = s.to.index;
		System.out.println("Saving - From: " + from + " To: " + to + " Val: " + s.val);
	}
	
	public static void printRoutesCities(ArrayList<Route> routes){
		for(Route r:routes){
			printCities(r);
			System.out.print("\n");
		}
	}
	
	public static void printAdds(ArrayList<Route> routes,String[] adds){
		for(Route r:routes){
			printAdds(r,adds);
			System.out.print("\n");
		}
	}
	
	public static void printRoute(Route r){
		System.out.print("Route: ");
		Edge edge = r.outEdges[0];
		
		System.out.print("(" + edge.n1.index + "->" + edge.n2.index + ")");
		
		do{
			edge = r.outEdges[edge.n2.index];
			System.out.print("(" + edge.n1.index + "->" + edge.n2.index + ")");
		}while(edge.n2.index!=0);
		
		
		System.out.print(" Amount: " + r.actual + " Cost: " + r.totalCost);
		
		System.out.println("");
	}
	
	/**
	 * Vytiskne mesta z jedne cesty
	 * @param r
	 */
	public static void printCities(Route r){
		System.out.print(0 + " ");
		Edge edge = r.outEdges[0];
		System.out.print(edge.n2.index + " ");
		do{
			edge = r.outEdges[edge.n2.index];
			System.out.print(edge.n2.index + " ");
		}while(edge.n2.index!=0);
	}
	
	/**
	 * Vytiskne adresy mest z jedne cesty
	 * @param r
	 * @param adds
	 */
	public static void printAdds(Route r,String[] adds){
		System.out.print(adds[0]);
		Edge edge = r.outEdges[0];
		System.out.print(" -> " + adds[edge.n2.index]);
		do{
			edge = r.outEdges[edge.n2.index];
			System.out.print(" -> " + adds[edge.n2.index]);
		}while(edge.n2.index!=0);
	}
	
	/**
	 * Vytiskne hrany ze vsech cest
	 * @param routes
	 */
	public static void printRoutes(ArrayList<Route> routes){
		int totalCost = 0;
		for(Route r:routes){
			printRoute(r);
			totalCost+= r.totalCost;
		}
		
		System.out.println("Total cost of the routes: " + totalCost);
	}
	
	public static int compClusterCost(Cluster cl,int distances[][]){
		int cost = 0;
		for(int i=0;i<cl.tsp.size()-1;i++){
			Node n=cl.tsp.get(i);
			Node n1 = cl.tsp.get(i+1);
			
			cost+=distances[n.index][n1.index];
		}
		return cost;
	}
	
	public static void generateRandomNodes(int count,int distanceMultConst,int amountConst){
		Node[] nodes = new Node[count];
		int[][] distances = new int[count][count];
		
		for(int i=0;i<count;i++){
			int x = (int)(Math.random() * distanceMultConst);
			int y = (int)(Math.random() * distanceMultConst);
			for(int j=0;j<i;j++){
				Node n = nodes[j];
				int dist = (int)Math.sqrt((x-n.x)*(x-n.x) + (y-n.y)*(y-n.y));
				distances[i][n.index] = dist;
				distances[n.index][i] = dist;			
			}
			Node newNode = new Node(i);
	        newNode.x = x;
	        newNode.y = y;
	        newNode.add = "N" + i;
	        newNode.amount = (int) (Math.random() * amountConst);
			nodes[i] = newNode;
		}
		//vypisu
		StringBuilder sb = new StringBuilder();
		
		sb.append(count);
		for(int i=0;i<count;i++){
			sb.append("\n");
			for(int j=0;j<count;j++){
				if(i==j){
					sb.append(0);
				}else{
					if(distances[i][j] != 0){
						sb.append(distances[i][j]);
					}else{
						sb.append(distances[j][i]);
					}
				}
				sb.append(" ");
			}
		}
		
		sb.append("\n");
		sb.append("\n");
		
		for(int i=0;i<nodes.length;i++){
			sb.append(nodes[i].amount + "\t" + nodes[i].add + "\t" + nodes[i].x + "\t" + nodes[i].y);
			sb.append("\n");
		}
		
		System.out.print(sb.toString());
	}
}
