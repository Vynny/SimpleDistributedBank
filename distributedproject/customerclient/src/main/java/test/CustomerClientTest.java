package test;

import cli.ClientInputHandler;

public class CustomerClientTest implements Runnable {
	private static String[] test1 = { "deposit QCC1000 50", "deposit QCC1000 50", "withdraw QCC1000 100",
			"getBalance QCC1000" };

	private String[] inputs;
	private ClientInputHandler client;

	public CustomerClientTest(String[] inputs) {
		this.inputs = inputs;
		this.client = new ClientInputHandler("Customer");
	}

	public static void main(String args[]) {
		startTestThreads(test1, 10);
	}

	private static void startTestThreads(String[] inputs, int size) {
		Thread[] threads = new Thread[size];

		for (int i = 0; i < threads.length; i++) {
			CustomerClientTest client = new CustomerClientTest(inputs);
			threads[i] = new Thread(client);
			threads[i].start();
		}

		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		for (String s : inputs) {
			client.parseInput(s);
		}
	}
}
