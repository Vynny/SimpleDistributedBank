package messages.branch;

import com.message.MessageBody;

import java.util.HashMap;
import java.util.Map;

public class BranchRequestBody implements MessageBody {

    public enum OperationType {
        DEPOSIT,
        WITHDRAW,
        GET_BALANCE,
        CREATE_ACCOUNT_RECORD,
        EDIT_RECORD,
        ERROR
    }

    private OperationType operationType;
    private Map<String, String> requestMap;

    
    /*
     * 
     *-----------------
     *Error operation
     *----------------- 
     * 
     */
    public BranchRequestBody notifyCrashError(String originID1,String originID2) {
        requestMap = new HashMap<>();
        operationType = OperationType.ERROR;

        requestMap.put("originID1", originID1);
        requestMap.put("originID2", originID2);
        return this;
    }
    
    public BranchRequestBody notifyByzantineError(String originID) {
        requestMap = new HashMap<>();
        operationType = OperationType.ERROR;

        requestMap.put("originID", originID);

        return this;
    }
    
    /*
     * -----------------
     * Client Operations
     * -----------------
     */

    public BranchRequestBody deposit(String customerId, String amount) {
        requestMap = new HashMap<>();
        operationType = OperationType.DEPOSIT;

        requestMap.put("customerId", customerId);
        requestMap.put("amount", amount);

        return this;
    }

    public BranchRequestBody withdraw(String customerId, String amount) {
        requestMap = new HashMap<>();
        operationType = OperationType.WITHDRAW;

        requestMap.put("customerId", customerId);
        requestMap.put("amount", amount);

        return this;
    }

    public BranchRequestBody getBalance(String customerId) {
        requestMap = new HashMap<>();
        operationType = OperationType.GET_BALANCE;

        requestMap.put("customerId", customerId);

        return this;
    }

    /*
     * --------------------
     * Manager Operations
     * --------------------
     */

    public BranchRequestBody createAccountRecord(String managerId, String firstName, String lastName, String address, String phone, String branch) {
        requestMap = new HashMap<>();
        operationType = OperationType.CREATE_ACCOUNT_RECORD;

        requestMap.put("managerId", managerId);
        requestMap.put("firstName", firstName);
        requestMap.put("lastName", lastName);
        requestMap.put("address", address);
        requestMap.put("phone", phone);
        requestMap.put("branch", branch);

        return this;
    }

    public BranchRequestBody editRecord(String managerId, String customerId, String fieldName, String newValue) {
        requestMap = new HashMap<>();
        operationType = OperationType.EDIT_RECORD;

        requestMap.put("managerId", managerId);
        requestMap.put("customerId", customerId);
        requestMap.put("fieldName", fieldName);
        requestMap.put("newValue", newValue);

        return this;
    }


    public OperationType getOperationType() {
        return operationType;
    }

    public Map<String, String> getRequestMap() {
        return requestMap;
    }
}
