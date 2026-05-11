package engine;

/**
 * Keeps track of call frequency of this class's update() method.
 * Used to calculate frame rate -> update() method is called once
 * every renderer frame (image).
 */
public class FrameRate {
    private long lastTime;
    private int frameCount;
    private int fps;
    private long deltaTime;     // time accumulator

    public FrameRate() { }

    public void init() {
        lastTime = System.currentTimeMillis();
    }

    // call this once every time the method whose frequency you want
    // to keep track of is called
    public void update() {
        ++frameCount;
        long currentTime = System.currentTimeMillis();
        deltaTime += (currentTime - lastTime);
        lastTime = currentTime;
        if ( deltaTime > 1000 ) {
            fps = frameCount;
            frameCount = 0;
            deltaTime -= 1000;
        }
    }

    public int getFPS() {
        return fps;
    }
}
