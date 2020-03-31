package com.yferhaoui.walksafe;

import com.yferhaoui.basic.helper.TimeHelper;
import com.yferhaoui.walksafe.data.Output;

import jssc.SerialPort;
import jssc.SerialPortException;

public final class Gateway extends Thread {

	private final Output output;
	private final OS os;

	private SerialPort serialPort = null;

	public Gateway() {
		this.output = new Output();
		this.os = new OS();
	}

	public static void main(String[] args) throws SerialPortException {
		new Gateway().start();
	}

	@Override
	public final void run() {

		// Initialization with the good port for communication
		while (true) {

			try {
				this.connect();
				TimeHelper.sleepUninterruptibly(2000);

				final String output = serialPort.readString();
				this.output.setOutput(output);
				System.out.println(this.output.getOutput()); // TO REMOVE

			} catch (final SerialPortException | InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private final synchronized void connect() throws SerialPortException {

		if (!this.deviceIsAvailable()) {
			do {

				System.out.println("Connect Device...");
				for (final String port : this.os.getPort()) {

					// Close port
					if (this.serialPort != null && this.serialPort.isOpened()) {
						this.serialPort.closePort();
					}

					try {

						this.serialPort = new SerialPort(port);

						// Open port for communication
						this.serialPort.openPort();

						this.serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8,
								SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, false, true);
						this.serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

						synchronized (this.serialPort) {
							this.serialPort.wait(200);
						}

						final String out = this.serialPort.readString();
						if (this.serialPort.isOpened() && out != null && out.contains("79616e69")) {

							System.out.println("Device connected !");
							return;
						}

					} catch (final SerialPortException | InterruptedException e) {
						e.printStackTrace();
					}
				}

				try {
					this.output.turnTrueFirstDone();
					System.out.println("Device not found, wainting...");
					this.wait();
				} catch (final InterruptedException e) {
					// Do nothing
				}
				System.out.println("Gateway notified !");

			} while (!this.deviceIsAvailable());
		}
	}

	// GETTERS
	public final Output getOutput() {
		return this.output;
	}

	public final boolean deviceIsAvailable() {
		return this.output.getOutput() != null;
	}
}
