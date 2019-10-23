package com.yferhaoui.walksafe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public final class OS {

	public final boolean isMac;
	public final boolean isLinux;
	public final boolean isPiUnix;
	public final boolean isWindows;

	public OS() {
		final String os = System.getProperty("os.name").toLowerCase();

		isWindows = os.indexOf("windows") >= 0;
		isMac = os.indexOf("mac") >= 0;
		isLinux = os.indexOf("linux") >= 0;

		this.isPiUnix = this.isLinux && new Object() {
			private final boolean isPiUnix() {
				final File file = new File("/etc", "os-release");
				try {
					if (file.exists()) {
						final FileInputStream fis = new FileInputStream(file);
						final BufferedReader br = new BufferedReader(new InputStreamReader(fis));
						try {
							String string;
							while ((string = br.readLine()) != null) {
								if (string.toLowerCase().contains("raspbian")) {
									if (string.toLowerCase().contains("name")) {
										return true;
									}
								}
							}
						} finally {
							br.close();
						}
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
				return false;
			}
		}.isPiUnix();
	}

	public final String[] getPort() {
		if (this.isPiUnix) {
			return new String[] { "/dev/ttyACM0", "/dev/ttyACM1", "/dev/ttyACM2", "/dev/ttyUSB0", "/dev/ttyUSB1",
					"/dev/ttyUSB2", "/dev/ttyS0", "/dev/ttyS1", "/dev/ttyS2" };

		} else if (this.isLinux) {
			return new String[] { "/dev/ttyACM0", "/dev/ttyACM1", "/dev/ttyACM2", "/dev/ttyUSB0", "/dev/ttyUSB1",
					"/dev/ttyUSB2", "/dev/ttyS0", "/dev/ttyS1", "/dev/ttyS2" };

		} else if (this.isWindows) {
			return new String[] { "COM0", "COM1", "COM2", "COM3" };

		} else if (this.isMac) {
			return new String[] { "/dev/tty.usbserial-A9007UX1" };
		}
		throw new RuntimeException("OS could not be detect !");
	}

}
