package hu.laba;

public class TimeKeeper {

	private long zeroTime = System.currentTimeMillis();
	private float lastTick = -1f;
	private float tickTime = 0.1f;
	private Runnable tickFunction = null;

	public void setTickFunction(Runnable runnable) {
		this.tickFunction = runnable;
	}

	public void resetTime() {
		zeroTime = System.currentTimeMillis();
	}

	public float now() {
		long nowMillis = System.currentTimeMillis() - zeroTime;
		float now = nowMillis / 1000f;
		if (tickFunction != null && now > lastTick + tickTime) {
			synchronized (this) {
				if (now > lastTick + tickTime) {
					lastTick = now;
					tickFunction.run();
				}
			}
		}
		return now;
	}

}
