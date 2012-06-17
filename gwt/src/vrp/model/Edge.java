package vrp.model;

public class Edge implements Comparable<Edge>{
	public Node n1;
	public Node n2;
	
	public int val;
	
	public Edge next;
	
	public Edge(Node ln1,Node ln2,int dist){
		this.n1 = ln1;
		this.n2 = ln2;
		this.val = dist;
	}
	
	public void connect(Edge e1){
		next = e1;
	}
	
	public void reverse(){
		Node swap = this.n2;
		this.n2 = n1;
		this.n1 = swap;
	}

	@Override
	public int compareTo(Edge o) {
		if(this.val<o.val)
			return -1;
		else if(o.val == this.val)
			return 0;
		else
			return 1;
	}
}
