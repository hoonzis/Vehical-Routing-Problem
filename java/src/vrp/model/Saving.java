package vrp.model;

public class Saving implements Comparable<Saving>{
	public int val;
	public Node from;
	public Node to;
	
	public Saving(int v,Node f,Node t){
		val = v;
		from = f;
		to = t;
	}

	@Override
	public int compareTo(Saving o) {
		if(o.val<this.val){
			return -1;
		}else if(o.val == this.val){
			return 0;
		}else{
			return 1;
		}
	}
}
