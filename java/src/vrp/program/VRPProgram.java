package vrp.program;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import vrp.model.Cluster;
import vrp.model.Edge;
import vrp.model.Node;
import vrp.model.Route;
import vrp.model.Saving;



public class VRPProgram {

	public static int CAR_LIMIT = 40;
	
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
			   if(n.index == 11){
				   System.out.println("OKOK!");
			   }
			   if(n.cluster == j){
				   int difx = Math.abs(n.x - depo.x);
				   int dify = Math.abs(n.y - depo.y);
				   
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
	 * Load the data from file specified in the parameter
	 * @param file
	 * @throws IOException
	 */
	public static void loadData(String file) throws IOException{
		//BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-16"));
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		nCount = Integer.parseInt(in.readLine());
		distances = new int[nCount][nCount];
		
		//nactu tabulku vzdalenosti
		for(int i=0;i<nCount;i++){
			String line = in.readLine();
			String[] inDist = line.split(" ");
			for(int k=0;k<inDist.length;k++){
				int dis = Integer.parseInt(inDist[k]);
				distances[i][k] = dis;
			}
		}
		
		in.readLine();
		
		//nactu mnozstvi objednaneho zbozi
		//adresy a souradnice
		amounts = new int[nCount];
		nodes = new Node[nCount];
		adds = new String[nCount];
		
		for(int i=0;i<nCount;i++){
			String nodeInfo = in.readLine();
			String[] info = nodeInfo.split("\t");
			amounts[i] = Integer.parseInt(info[0]);
			
			Node n = new Node(i);
			n.amount = amounts[i];
			adds[i] = info[1];
			n.add = adds[i];
			
			if(info.length==4){
				n.x = Integer.parseInt(info[2]);
				n.y = Integer.parseInt(info[3]);
			}
			nodes[i] = n;
		}
	}
	
	public static void sweep(){
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
		for(int i=0;i<clusters.size();i++){
			//System.out.println("Cluster: " + clusters.get(i).amount);
			clusters.get(i).mst();
			//clusters.get(i).printMST();
			clusters.get(i).dfsONMST();
			clusters.get(i).printTSP();
			System.out.println("");
			totalCost+=MyUtils.compClusterCost(clusters.get(i), distances);
		}
		System.out.println("TOTAL COST: " + totalCost);
	}
	
	public static void main(String[] args) throws IOException{
		//nactu data
		loadData("D:\\scool\\8semestr\\sync\\ko\\data100.in");
		Stopwatch s = new Stopwatch();
		s.start();
		clarkWright();
		//sweep();
		s.stop();
		System.out.println(s.toString());
		//MyUtils.generateRandomNodes(100,300,15);
		
		
	}
	
	
	
	public static void clarkWright(){
		routes = new ArrayList<Route>();
		
		//vytvorim N uzlu a kazdy vlozim do cesty(Route)
		//kazda cesta bude obsahovat dve hrany: ze skladu do uzlu a zpet
		for(int i=0;i<nCount;i++){
			
			Node n = nodes[i];
			
			if(i!=0){
				
				//vytvorim hranu
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
		//spocitam hodnoty, ktere muzu usetrit
		ArrayList<Saving> sList = computeSaving(distances, nCount, savings,nodes);
		//seradim Savings objekty od nejvetsiho (nejvyssi hodnoty, kterou muzu usetrit)
		Collections.sort(sList);
		
		//dokud nevyprazdnim Savings List
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
				
				Edge vystupniR2 = r2.outEdges[to];
				Edge vstupniR1 = r1.inEdges[from];
				
				
				if(vystupniR2!=null && vstupniR1 != null){
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
		MyUtils.printRoutesCities(routes);
		MyUtils.printAdds(routes,adds);
	}
	
	
	/**
	 * 
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
	
	private static int[][] savings;
	public static int[][] distances;
	private static Node[] nodes;
	private static String[] adds;
	private static ArrayList<Route> routes;
	private static int nCount;
	private static int[] amounts;
	
}
