package vrp.model;

public class Stopwatch {
	private long start;
    private long stop;
    
    public void start() {
        start = System.currentTimeMillis(); // start timing
    }
    
    public void stop() {
        stop = System.currentTimeMillis(); // stop timing
    }
    
    public long elapsedTimeMillis() {
        return stop - start;
    }
    
    public String toString() {
        return "elapsedTimeMillis: " + Long.toString(elapsedTimeMillis()); // print execution time
    }
}
