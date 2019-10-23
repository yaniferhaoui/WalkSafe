package com.yferhaoui.walksafe.data;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.yferhaoui.walksafe.WalkSafe;

public final class Output {

	public enum DATA {
		ALTITUDE, PRESSION, OXYGEN, TEMPERATURE, HUMIDITE;
	}

	private final Map<Output.DATA, Float> datas = new TreeMap<Output.DATA, Float>();
	private final Lock lock = new ReentrantLock(true);
	private String lastOutput;

	private int nbBadOutput = 0;
	private boolean firstDone = false;

	public Output() {
		this.datas.put(Output.DATA.ALTITUDE, null);
		this.datas.put(Output.DATA.PRESSION, null);
		this.datas.put(Output.DATA.OXYGEN, null);
		this.datas.put(Output.DATA.TEMPERATURE, null);
		this.datas.put(Output.DATA.HUMIDITE, null);
	}

	// Setters
	public final void setOutput(String output) throws InterruptedException {

		try {
			this.lock.lock();

			if (output != null) {
				output = output.replace("\n", "").replace(" ", "");

				if (output.charAt(0) == '{' && output.charAt(output.length() - 2) == '}') {
					this.lastOutput = output.replace("\n", "").replace(" ", "");

					try {
						final String[] datas = this.lastOutput.replace("{", "")//
								.replace("}", "")//
								.replace(";Sign:79616e69", "")//
								.split(";");

						for (final String data : datas) {
							final String[] keyValue = data.split(":");

							final Output.DATA key = Output.DATA.valueOf(keyValue[0].toUpperCase());
							final Float value = Float.valueOf(keyValue[1]);
							this.datas.put(key, value);
						}
						this.nbBadOutput = 0;

						if (this.datas.get(Output.DATA.ALTITUDE) != null) {
							this.datas.put(Output.DATA.OXYGEN,
									20.95f * (8848.01f / (8848f - this.datas.get(Output.DATA.ALTITUDE))));
						}

					} catch (final IllegalArgumentException | ArrayIndexOutOfBoundsException | NullPointerException e) {
						this.nbBadOutput++;
						if (this.nbBadOutput > 5) {
							WalkSafe.manageExceptionOutThread(e);
						}
						System.out.println("Bad input : " + this.lastOutput);
					}
				}
			} else {
				this.lastOutput = null;
			}
		} finally {
			this.firstDone = true;
			this.lock.unlock();
		}
	}

	public final void turnTrueFirstDone() {
		this.firstDone = true;
	}

	// Getters
	public final String getOutput() {
		try {
			this.lock.lock();
			return this.lastOutput;
		} finally {
			this.lock.unlock();
		}
	}

	public final Map<Output.DATA, Float> getDatas() {
		try {
			this.lock.lock();
			return new HashMap<Output.DATA, Float>(this.datas);
		} finally {
			this.lock.unlock();
		}
	}

	public final boolean firstDone() {
		return this.firstDone;
	}
}
