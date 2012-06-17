package vrp.model;

import java.util.ArrayList;

public class Route {
	
	public int allowed;
	public int actual;
	public int totalCost;
	
	
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
	
	public int predecessor(int nodeIndex){
		return inEdges[nodeIndex].n1.index;
	}
	
	
	public int successor(int nodeIndex){
		return outEdges[nodeIndex].n2.index;
	}
	
	public boolean merge(Route r2,Edge mergingEdge){

		int from = mergingEdge.n1.index;
		int to = mergingEdge.n2.index;
		
		int predecessorI = this.predecessor(from);
		int predecessorJ = r2.predecessor(to);
		
		int successorI = this.successor(from);
		int successorJ = r2.successor(to);
		
		//moznost jedna
		//hrana smeruje z uzlu ze, ktereho v prvni Route se vracime zpatky do uzlu 0
		//v druhe route je naopak predecessor uzlu J sklad = 0
		if(successorI == 0 && predecessorJ == 0){
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
		}else if(successorJ == 0 && predecessorI == 0){
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
