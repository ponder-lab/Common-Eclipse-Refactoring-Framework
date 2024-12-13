/**
 *
 */
package edu.cuny.citytech.refactoring.common.core;

/**
 * @author <a href="mailto:khatchad@hunter.cuny.edu>Raffi Khatchadourian</a>
 */
public class TimeCollector {

	private long collectedTime;
	private long start;
	private boolean started;

	public void clear() {
		assert !this.started : "Shouldn't clear a running time collector.";

		this.collectedTime = 0;
	}

	public long getCollectedTime() {
		return this.collectedTime;
	}

	public void start() {
		assert !this.started : "Time colletor is already started.";
		this.started = true;

		this.start = System.currentTimeMillis();
	}

	public void stop() {
		assert this.started : "Trying to stop a time collector that isn't started.";
		this.started = false;

		final long elapsed = System.currentTimeMillis() - this.start;
		this.collectedTime += elapsed;
	}
}
