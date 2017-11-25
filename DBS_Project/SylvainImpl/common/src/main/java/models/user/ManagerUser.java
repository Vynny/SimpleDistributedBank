package models.user;

import models.branch.Branch;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class ManagerUser implements Serializable {

    private static final String MANAGER_PREFIX = "M";

    private static AtomicInteger lastId = new AtomicInteger(1000);

    private String managerId;

    public ManagerUser(Branch branch) {
        this.managerId = generateManagerId(branch);
    }

    private String generateManagerId(Branch branch) {
        return branch.toString() + MANAGER_PREFIX + lastId.getAndIncrement();
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }
}
