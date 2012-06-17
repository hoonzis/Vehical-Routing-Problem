package vrp.model;

import java.util.ArrayList;
import java.util.Collections;

public class Cluster {
	
	public int amount;
	public ArrayList<Node> nodes;
	public ArrayList<Edge> edges;
    public ArrayList<Node> tsp;
	public ArrayList<Edge> mstE;
	
    public int cost;
	
	public Cluster(){
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
	}
	
	public void add(Node n){
		nodes.add(n);
		amount+= n.amount;
	}
	
	public Cluster copy(){
		Cluster n = new Cluster();
		n.amount = this.amount;
		n.nodes = this.nodes;
		return n;
	}
	
	public void printMST(StringBuilder sb){
		System.out.println("CLUSTER:");
		for(Edge e:mstE){
			sb.append("From: " + e.n1.index + " To: " + e.n2.index + " Val: " + e.val);
		}
	}
	
	public void mst(){
		mstE = new ArrayList<Edge>();
		
		nodes.get(0).visited = true;
		nodes.get(0).mstEdges = new ArrayList<Edge>();
		int visitedNodes = 1;
		Collections.sort(edges);

		while(visitedNodes!= nodes.size()){
			boolean added = false;
			int counter = 0;
			while(!added){
				
				Edge e = edges.get(counter);
				if((e.n1.visited == true && e.n2.visited == false)){
					//||(e.n2.visited == true && e.n1.visited == false)){
					mstE.add(e);
					//e.n1.visited = true;
					e.n2.visited = true;
					//pridam do MST hran, ktere tvori kostru
					if(e.n1.mstEdges == null){
						e.n1.mstEdges = new ArrayList<Edge>();
					}
					e.n1.mstEdges.add(e);
					added = true;
					visitedNodes++;
				}
				counter++;
			}
		}	
	}
	
	public void dfsONMST(){
		tsp = new ArrayList<Node>();
		Node start = nodes.get(0);
		dfsProjdi(start);
		tsp.add(start);
	}
	
	public void dfsProjdi(Node n){
		tsp.add(n);
		if(n.mstEdges!=null){
			for(Edge e:n.mstEdges){
				Node n2 = e.n2;
				dfsProjdi(n2);
			}
		}
	}
	
	public void printTSP(StringBuilder sb){
		//System.out.println("CLUSTER TSP:");
		for(Node n:tsp){
			sb.append(n.index + " ");
		}		
	}

	public void printTSPAdds(StringBuilder sb) {
		int nodeCount  = tsp.size();
		for(int i=0;i<nodeCount;i++){
			Node n = tsp.get(i);
			sb.append(n.add);
			if(i<nodeCount-1){
				sb.append("->");
			}
		}		
	}
}
