package visualize.util;

public class Timer 
{
    private long lastNanos, currentNanos, start;
    private float fps = 0;
    private long ticks = 0;
    private long framesTime = 0;
    
    public void reset() 
    {
        this.lastNanos = System.nanoTime();
        this.currentNanos = System.nanoTime();
        this.start = System.nanoTime();
    }
    
    public long getLastNanos() 
    {
        return this.currentNanos - this.lastNanos;
    }
    
    public long getLastMillis() 
    {
        return getLastNanos() / (long)1e6;
    }
    
    public float getFps() 
    {
        return this.fps;
    }
    
    public long getTime() 
    {
        return System.nanoTime() - this.start;
    }
    
    public void tick() 
    {
        this.lastNanos = this.currentNanos;
        this.currentNanos = System.nanoTime();
        this.framesTime += this.getLastNanos();
        if((++ticks) % 100 == 0) 
        {
            this.fps = 100.f/this.framesTime * 1e9f;
            this.framesTime = 0;
        }
    }
}
