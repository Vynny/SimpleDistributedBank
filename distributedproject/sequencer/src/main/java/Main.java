import java.io.IOException;

import sequencer.Sequencer;

public class Main {
	public static void main(String args[]) {
		// TODO validate args
		
		Sequencer seq = null;
		try {
			seq = new Sequencer(args[0]);
		} catch (IOException e) {
			System.err.println("Unable to start the sequencer");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Sequencer Instantiated successfully");
		seq.start();
	}
}
