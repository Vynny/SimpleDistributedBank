package server.sylvain.common.models.account;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

public class BankAccount implements Serializable {

    private BigDecimal balance;

    public BankAccount() {
        this.balance = new BigDecimal(BigInteger.ZERO);
    }

    public BankAccount(String balance) {
        this.balance = formatCurrency(new BigDecimal(balance));
    }

    public synchronized boolean validateAmount(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) == 1;
    }

    public synchronized boolean canWithdraw(BigDecimal amount) {
        return this.balance.compareTo(amount) != -1;
    }

    public synchronized void withdraw(BigDecimal amount) {
        this.balance = formatCurrency(this.balance.subtract(amount));
    }

    public synchronized void deposit(BigDecimal amount) {
        this.balance = formatCurrency(balance.add(amount));
    }

    public BigDecimal getBalance() {
        return formatCurrency(this.balance);
    }

    private BigDecimal formatCurrency(BigDecimal number) {
        return number.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "balance=" + balance +
                '}';
    }
}
