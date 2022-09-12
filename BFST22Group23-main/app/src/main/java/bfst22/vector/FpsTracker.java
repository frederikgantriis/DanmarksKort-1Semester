package bfst22.vector;
import javafx.animation.AnimationTimer;
import javafx.scene.control.Label;

public class FpsTracker extends AnimationTimer{
    private final long[] frameTimes = new long[10];
    private int frameTimeIndex = 0;
    private boolean arrayFilled = false;
    private int avgFrIndex = 0;
    private Label fpsLabel;

    public FpsTracker(Label fpsLabel){
        this.fpsLabel = fpsLabel;
    }

    @Override
    public void handle(long now) {
        long oldFrameTime = frameTimes[frameTimeIndex];
        frameTimes[frameTimeIndex] = now;
        frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length;
        if (frameTimeIndex == 0) {
            arrayFilled = true;
        }
        if (arrayFilled) {
            long elapsedNanos = now - oldFrameTime;
            long elapsedNanosPerFrame = elapsedNanos / frameTimes.length;
            double frameRate = 1000000000.0 / elapsedNanosPerFrame;
            avgFrIndex = (avgFrIndex + 1) % 10;
            if (avgFrIndex == 0) {
                fpsLabel.setText(String.format("fps: %.0f", frameRate));
            }
        }
    }

    @Override
    public void stop(){
        super.stop();
        arrayFilled = false;
        frameTimeIndex = 0;
    }
}
