package remote.database;

import models.user.ManagerUser;

import java.util.HashMap;
import java.util.Map;

public class ManagerDatabase {
    private static ManagerDatabase ourInstance = new ManagerDatabase();
    public static ManagerDatabase getInstance() {
        return ourInstance;
    }

    private Map<String, ManagerUser> managerDatabase;

    private ManagerDatabase() {
        this.managerDatabase = new HashMap<>();
    }

    public synchronized ManagerUser getManager(String managerId) {
        return this.managerDatabase.get(managerId);
    }

    public synchronized void addManager(ManagerUser manager) {
        this.managerDatabase.put(manager.getManagerId(), manager);
    }

    public void printDB() {
        this.managerDatabase.forEach((id, managerUser) -> {
            System.out.println(id);
        });
    }
}
