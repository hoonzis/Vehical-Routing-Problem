package vrp.model;

import java.util.ArrayList;

public class Route {
	
	int allowed;
	int actual;
	int totalCost;
	
	
	public int[] nodes;
	public Edge[] inEdges;
	public Edge[] outEdges;
	
	
	ArrayList<Edge> edges;
	
	public Route(int nodesNumber){
		edges = new ArrayList<Edge>();
		
		nodes = new int[nodesNumber];
		inEdges = new Edge[nodesNumber];
		outEdges = new Edge[nodesNumber];
	}
	
	public void add(Edge e){
		edges.add(e);
		
		outEdges[e.n1.index] = e;
		inEdges[e.n2.index] = e;
		
		e.n1.route = this;
		e.n2.route = this;
		
		totalCost+= e.val;
	}
	
	public void removeEdgeToNode(int index){
		Edge e = inEdges[index];
		outEdges[e.n1.index] = null;
		
		totalCost-= e.val;
		
		edges.remove(e);
		inEdges[index] = null;
	}
	
	public void removeEdgeFromNode(int index){
		Edge e = outEdges[index];
		inEdges[e.n2.index] = null;
		
		totalCost-=e.val;
		edges.remove(e);
		outEdges[index] = null;
	}
	
	public int predecesor(int nodeIndex){
		return inEdges[nodeIndex].n1.index;
	}
	
	
	public int sucessor(int nodeIndex){
		return outEdges[nodeIndex].n2.index;
	}
	
	public boolean merge(Route r2,Edge mergingEdge){

		int from = mergingEdge.n1.index;
		int to = mergingEdge.n2.index;
		
		int predecesorI = this.predecesor(from);
		int predecesorJ = r2.predecesor(to);
		
		int sucessorI = this.sucessor(from);
		int sucessorJ = r2.sucessor(to);
		
		//moznost jedna
		//hrana smeruje z uzlu ze, ktereho v prvni Route se vracime zpatky do uzlu 0
		//v druhe route je naopak predecesor uzlu J sklad = 0
		if(sucessorI == 0 && predecesorJ == 0){
			this.removeEdgeToNode(0);
			r2.removeEdgeFromNode(0);
			for(Edge e:r2.edges){
				this.add(e);
			}
			this.actual+= r2.actual;
			this.add(mergingEdge);
			return true;
		// moznost dva
		//hrana jde jakoby v protismeru
		//uzel i je v prvni route druhy hned za skladem 
	    //musime otocit hranu
		}else if(sucessorJ == 0 && predecesorI == 0){
			mergingEdge.reverse();
			this.removeEdgeFromNode(0);
			r2.removeEdgeToNode(0);
			for(Edge e:r2.edges){
				this.add(e);
			}
			this.actual+= r2.actual;
			this.add(mergingEdge);
			return true;
		}
		
		return false;
	}
}
