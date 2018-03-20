package fly.speedmeter.grub;

/**
 * Created by fly on 17/04/15.
 */
public class Data {
    private boolean isRunning;
    private long time;
    private long timeStopped;
    private boolean isFirstTime;

    private double distanceM;
    private double curSpeed;
    private double maxSpeed;

    private onGpsServiceUpdate onGpsServiceUpdate;

    public interface onGpsServiceUpdate{
        public void update();
    }

    public void setOnGpsServiceUpdate(onGpsServiceUpdate onGpsServiceUpdate){
        this.onGpsServiceUpdate = onGpsServiceUpdate;
    }

    public void update(){
        onGpsServiceUpdate.update();
    }

    public Data() {
        isRunning = false;
        distanceM = 0;
        curSpeed = 0;
        maxSpeed = 0;
        timeStopped = 0;
    }

    public Data(onGpsServiceUpdate onGpsServiceUpdate){
        this();
        setOnGpsServiceUpdate(onGpsServiceUpdate);
    }

    public void addDistance(double distance){
        distanceM = distanceM + distance;
    }

    public double getDistance(){
        return distanceM;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getAverageSpeed(){
        double average;
        String units;
        if (time <= 0) {
            average = 0.0;
        } else {
            average = (distanceM / (time / 1000.0)) * 3.6;
        }
        return average;
    }

    public double getAverageSpeedMotion(){
        long motionTime = time - timeStopped;
        double average;
        String units;
        if (motionTime <= 0){
            average = 0.0;
        } else {
            average = (distanceM / (motionTime / 1000.0)) * 3.6;
        }
        return average;
    }

    public void setCurSpeed(double curSpeed) {
        this.curSpeed = curSpeed;
        if (curSpeed > maxSpeed){
            maxSpeed = curSpeed;
        }
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }

    public void setFirstTime(boolean isFirstTime) {
        this.isFirstTime = isFirstTime;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public void setTimeStopped(long timeStopped) {
        this.timeStopped += timeStopped;
    }

    public double getCurSpeed() {
        return curSpeed;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

