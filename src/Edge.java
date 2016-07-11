
public class Edge implements Comparable<Object> {
		private final Integer from;
		private final Integer to;
		
		public Edge(Integer from, Integer to){
			this.from = from;
			this.to = to;
		}
		
		public Integer getFrom(){return from;}
		public Integer getTo(){return to;}
		
		@Override
		public String toString(){
			return "(" + ((from!=null) ? from.toString() : null) + "," + ((to!=null) ? to.toString() : null) + ")";
		}

		@Override
		public int compareTo(Object o) {
			
			    if(o instanceof Edge)
			    {
			    	Edge e = (Edge)o;
			    	if(this.from.intValue() == e.getFrom().intValue())
			    		return this.to.intValue() - e.getTo().intValue();
			        return (this.from.intValue() - e.getFrom().intValue());
			    }
			    else
			        return -1;
			
		}
	}