package server.radu;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServerDatabase {
    private HashMap<String, List<String>> localDatabase; // a copy for each server to work on separately
    public String branch;
    // the list has the following contents:
    // id, balance, branch, first name, last name, address, phone and then repeats for the next person
    public final int userInfoSize = 7;

    public ServerDatabase(String branch) {
        super();
        System.err.println("creating database");
        this.branch = branch;

        try {
            //create the user file
            File f = new File(branch + "log.txt");
            if (!f.exists() && !f.isDirectory()) {
                PrintWriter file = new PrintWriter(branch + "log.txt", "UTF-8");
                file.close();
            }
        } catch (IOException e) {
            System.err.println("Could not create file");

        }
        localDatabase = new HashMap<String, List<String>>();
        populateWithInitialData();
    }

    public int getDBSize() {

        return localDatabase.size();
    }

    public void trackOperation(String message) {
        try {
            message += System.lineSeparator();
            Files.write(Paths.get(branch + "log.txt"), message.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            System.out.println("Could not write to file " + branch + "log.txt");
        }
    }

    // checks to see if a user exists inside the database
    public boolean userIDExists(String ID) {
        for (String key : localDatabase.keySet()) {
            List<String> listAtKey = localDatabase.get(key);
            for (int j = 0; j < listAtKey.size(); ++j) {
                if (listAtKey.get(j).equals(ID)) {
                    return true;
                }
            }
        }
        return false;
    }

    // adds a new user to the local server
    public boolean addUserToDatabase(String key, List<String> userInfo) {
        boolean message = false;
        List<String> newList;
//		if (localServer)
        newList = localDatabase.get(key);
//		else
//			newList = database.get(key);
        if (newList == null)
            newList = new ArrayList<String>();
        for (int i = 0; i < userInfo.size(); ++i) {
            newList.add(userInfo.get(i));

        }
        message = true;
//		if (localServer) {
        localDatabase.put(key, newList);
//		}
//		else
//			database.put(key, newList);
        return message;
    }

    // this method returns a copy list of all the info of a client. Only his info
    public List<String> getClientList(String ID) {
        System.out.println("Getting list in server " + branch + ", " + localDatabase.size());
        List<String> s = new ArrayList<String>();
        for (String key : localDatabase.keySet()) {
            List<String> listAtKey = localDatabase.get(key);
            for (int j = 0; j < listAtKey.size(); ++j) {
                if (listAtKey.get(j).equals(ID)) {
                    for (int k = j; k < j + userInfoSize; ++k) {
                        s.add(listAtKey.get(k));
                    }
                }
            }
        }

        return s;
    }

    // this method updates a clients info. It updates all his entries, even if they were not modified
    public boolean updateClientList(List<String> newList) {
        String ID = newList.get(0);
        boolean message = false;
        for (String key : localDatabase.keySet()) {
            List<String> listAtKey = localDatabase.get(key); // all clients with the name start with char key
            for (int j = 0; j < listAtKey.size(); ++j) {
                if (listAtKey.get(j).equals(ID)) {
                    for (int k = j; k < j + userInfoSize; ++k) {
                        listAtKey.set(k, newList.get(k - j));
                    }
                    localDatabase.put(key, listAtKey);
                    message = true;
                }
            }
        }
        System.err.println("Operation in server: " + branch);
        System.err.println("For testing purposes, local database in this server has the following stuff: ");
        System.err.println(localDatabase);
        return message;
    }

    // just a method to put some users inside the database when the server first starts. It puts 2 users, one client and one manager
    // per server. The only different thing between them is their ID and their branch
    public void populateWithInitialData() {
        if (localDatabase == null)
            localDatabase = new HashMap<String, List<String>>();

        List<String> s = new ArrayList<String>();
        List<String> s2 = new ArrayList<String>();

        s.add(branch + "C1000");
        s.add("500");
        s.add(branch);
        s.add("Radu");
        s.add("Saghin");
        s.add("Terrebonne");
        s.add("514");
        s2.add(branch + "M1111");
        s2.add("500");
        s2.add(branch);
        s2.add("Loco");
        s2.add("Toto");
        s2.add("Montreal");
        s2.add("514");

        addUserToDatabase("S", s);
        addUserToDatabase("T", s2);
    }
}
