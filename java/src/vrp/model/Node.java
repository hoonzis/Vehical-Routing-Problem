package vrp.model;

import java.util.ArrayList;

public class Node implements Comparable<Node>{
	public int index;
	public Route route;
	
	
	public int cluster;
	public double x;
	public double y;
	public double angle;

	public int state;
	public boolean visited;
	
	public ArrayList<Edge> mstEdges;
	
	public String add;
	
	public int amount;
	
	
	public Node(int i){
		index = i;
	}


	@Override
	public int compareTo(Node o) {
		if(this.angle<o.angle){
			return -1;
		}else if(o.angle == this.angle){
			return 0;
		}else{
			return 1;
		}
	}
}
