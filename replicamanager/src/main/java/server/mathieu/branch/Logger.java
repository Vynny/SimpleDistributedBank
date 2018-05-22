package server.mathieu.branch;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper class to output to a text file.
 * 
 * @author Mathieu Breault 27093969
 */

public class Logger {
	private final static int attemptNumbers = 3;
	private final static int sleepAttempt = 1000;
	private final static String defaultFileName = "out.txt";

	private PrintWriter fileOutput;

	public Logger(String fileName, boolean append) {
		if (fileName == null) {
			fileName = defaultFileName;
		}
		int attemptLeft = attemptNumbers;
		while (attemptLeft > 0) {
			try {
				fileOutput = new PrintWriter(new FileOutputStream(fileName, append));
			} catch (FileNotFoundException notFound) {
				System.out.println("Can't open file, retrying...");
				try {
					Thread.sleep(sleepAttempt);
				} catch (InterruptedException interrupted) {
					interrupted.printStackTrace();
				}
			}
			attemptLeft--;
		}
	}

	// public static void setLogger(String fileName, boolean append) {
	// if (fileName == null) {
	// fileName = defaultFileName;
	// }
	// int attemptLeft = attemptNumbers;
	// while (attemptLeft > 0) {
	// try {
	// fileOutput = new PrintWriter(new FileOutputStream(fileName, append));
	// } catch (FileNotFoundException notFound) {
	// System.out.println("Can't open file, retrying...");
	// try {
	// Thread.sleep(sleepAttempt);
	// } catch (InterruptedException interrupted) {
	// interrupted.printStackTrace();
	// }
	// }
	// attemptLeft--;
	// }
	// }

	public void println(String clientId, String str) {
		println(clientId + ": " + str);
	}

	public void println(String str) {
		printMessage(str);
	}

	private void printMessage(String message) {
		if (fileOutput == null) {
			System.out.println("Inconsistent state, no output..");
			return;
		}
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		message = timeStamp + ":: " + message;
		fileOutput.println(message);
		fileOutput.flush();
		System.out.println(message);
	}

	public void println() {
		if (fileOutput == null) {
			System.out.println("Inconsistent state, no output..");
			return;
		}

		fileOutput.println();
		fileOutput.flush();
		System.out.println();
	}

	public void closeStream() {
		fileOutput.close();
	}
}
