package test;

import cli.ManagerInputHandler;

public class ManagerClientTest implements Runnable {
    private static String[] test0 = { "deposit QCM1001 QCC1000 50" };

    private static String[] test1 = { "deposit QCM1001 QCC1000 50", "deposit QCM1001 QCC1000 50",
            "withdraw QCM1001 QCC1000 100", "getBalance QCM1001 QCC1000" };

    private static String[] testCreate = { "createAccountRecord QCM1001 batman wayne 123-fake-street 555-555-5555 QC" };

    private static String[] testBalance = { "getBalance QCM1001 QCC1000" };

    private static String[] testAccountCount = { "getAccountCount QCM1001" };

    private static String[] testTransfer = { "deposit QCM1001 QCC1000 500", "transfer QCM1001 QCC1000 500 NBC1000" };

    private static String[] testBalanceAfterTransfer = { "getBalance QCM1001 QCC1000", "getBalance QCM1001 NBC1000" };

    private String[] inputs;
    private ManagerInputHandler client;

    public ManagerClientTest(String[] inputs) {
        this.inputs = inputs;
        this.client = new ManagerInputHandler("Customer");
    }

    public static void main(String[] args) {
        startTestThreads(test1, 10);

        System.out.println("Testing remaining balance, should be 0 on a fresh system");
        startTestThreads(testBalance, 1);

        System.out.println("Testing account count");
        startTestThreads(testAccountCount, 5);

        System.out.println("Testing create account");
        startTestThreads(testCreate, 5);

        System.out.println("Testing new account count");
        startTestThreads(testAccountCount, 1);

        System.out.println("Testing transfer");
        startTestThreads(testTransfer, 5);

        System.out.println("Balance after transfer");
        startTestThreads(testBalanceAfterTransfer, 1);
    }

    private static void startTestThreads(String[] inputs, int size) {
        Thread[] threads = new Thread[size];

        for (int i = 0; i < threads.length; i++) {
            ManagerClientTest client = new ManagerClientTest(inputs);
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
