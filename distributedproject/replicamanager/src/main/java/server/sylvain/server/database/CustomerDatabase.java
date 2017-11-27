package server.sylvain.server.database;

import server.sylvain.common.models.user.CustomerUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerDatabase {
    private static CustomerDatabase ourInstance = new CustomerDatabase();
    public static CustomerDatabase getInstance() {
        return ourInstance;
    }

    private Integer customerCount;
    private Map<String, List<CustomerUser>> customerDatabase;

    private CustomerDatabase() {
        this.customerCount = 0;
        this.customerDatabase = new HashMap<>();
    }

    public synchronized CustomerUser getCustomer(String customerId) {
        for (Map.Entry<String, List<CustomerUser>> entrySet : customerDatabase.entrySet()) {
            for (CustomerUser user : entrySet.getValue())
                if (user.getCustomerId().equals(customerId))
                    return user;
        }
        return null;
    }

    public synchronized void addCustomer(CustomerUser client) {
        String lastNameLetter = client.getLastName().substring(0, 1).toUpperCase();

        if (!customerDatabase.containsKey(lastNameLetter)) {
            List<CustomerUser> clientList = new ArrayList<>();
            clientList.add(client);
            customerDatabase.put(lastNameLetter, clientList);
        } else {
            List<CustomerUser> clientList = customerDatabase.get(lastNameLetter);
            clientList.add(client);
        }

        customerCount++;
    }

    public synchronized void removeCustomer(CustomerUser client) {
        String lastNameLetter = client.getLastName().substring(0, 1).toUpperCase();

        if (customerDatabase.containsKey(lastNameLetter)) {
            customerDatabase.get(lastNameLetter).remove(client);
            customerCount--;
        }
    }

    public void printDB() {
        customerDatabase.forEach((letter, list) -> {
            System.out.println(letter);
            list.forEach(user -> {
                System.out.println("\tID: " + user.getCustomerId());
                System.out.println("\tFirst Name: " + user.getFirstName());
                System.out.println("\tLast Name: " + user.getLastName());
                System.out.println("\tAddress: " + user.getAddress());
                System.out.println("\tPhone: " + user.getPhoneNumber());
            });
        });
    }

    public synchronized int getCustomerCount() {
        return customerCount;
    }
}
