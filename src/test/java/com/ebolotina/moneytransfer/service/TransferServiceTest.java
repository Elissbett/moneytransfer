package com.ebolotina.moneytransfer.service;

import com.ebolotina.moneytransfer.Main;
import com.ebolotina.moneytransfer.model.Transfer;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class TransferServiceTest {
    private SessionFactory sessionFactory;
    private TransferService transferService;

    @Before
    public void setup() {
        sessionFactory = new Configuration().configure().buildSessionFactory();
        transferService = new TransferService();
        transferService.setSessionFactory(sessionFactory);
        Main.loadData(sessionFactory);
    }

    @After
    public void close() {
        sessionFactory.close();
    }

    private Transfer createTransfer(
            String sourceAccount, String targetAccount, BigDecimal amount, String currency) {
        Transfer transfer = new Transfer();
        transfer.setSourceAccount(sourceAccount);
        transfer.setTargetAccount(targetAccount);
        transfer.setAmount(amount);
        transfer.setCurrency(currency);
        return transfer;
    }

    @Test
    public void transfer100RURSuccessful() {

        Transfer transfer = createTransfer("11111111", "22222222",
                new BigDecimal(100),"RUR");

        BigDecimal sourceBalance = transferService.getAccount(transfer.getSourceAccount()).getBalance();
        BigDecimal targetBalance = transferService.getAccount(transfer.getTargetAccount()).getBalance();

        Transfer result = transferService.makeTransfer(transfer);

        assertEquals(transfer.getSourceAccount(), result.getSourceAccount());
        assertEquals(transfer.getTargetAccount(), result.getTargetAccount());
        assertEquals(transfer.getCurrency(), result.getCurrency());
        assertEquals(transfer.getAmount(), result.getAmount());
        assertEquals("Successfully completed", result.getResponse());

        assertEquals(sourceBalance.subtract(transfer.getAmount()),
                transferService.getAccount(transfer.getSourceAccount()).getBalance());
        assertEquals(targetBalance.add(transfer.getAmount()),
                transferService.getAccount(transfer.getTargetAccount()).getBalance());
    }

    @Test
    public void transfer4000RURNonSufficientFunds() {
        Transfer transfer = createTransfer("11111111", "22222222",
                new BigDecimal(4000),"RUR");

        BigDecimal sourceBalance = transferService.getAccount(transfer.getSourceAccount()).getBalance();
        BigDecimal targetBalance = transferService.getAccount(transfer.getTargetAccount()).getBalance();

        Transfer result = transferService.makeTransfer(transfer);

        assertEquals(transfer.getSourceAccount(), result.getSourceAccount());
        assertEquals(transfer.getTargetAccount(), result.getTargetAccount());
        assertEquals(transfer.getCurrency(), result.getCurrency());
        assertEquals(transfer.getAmount(), result.getAmount());
        assertEquals("Non-sufficient funds", result.getResponse());

        assertEquals(sourceBalance, transferService.getAccount(transfer.getSourceAccount()).getBalance());
        assertEquals(targetBalance, transferService.getAccount(transfer.getTargetAccount()).getBalance());
    }

    @Test
    public void transfer100USDDifferentCurrency() {
        Transfer transfer = createTransfer("11111111", "22222222",
                new BigDecimal(100),"USD");

        BigDecimal sourceBalance = transferService.getAccount(transfer.getSourceAccount()).getBalance();
        BigDecimal targetBalance = transferService.getAccount(transfer.getTargetAccount()).getBalance();

        Transfer result = transferService.makeTransfer(transfer);

        assertEquals(transfer.getSourceAccount(), result.getSourceAccount());
        assertEquals(transfer.getTargetAccount(), result.getTargetAccount());
        assertEquals(transfer.getCurrency(), result.getCurrency());
        assertEquals(transfer.getAmount(), result.getAmount());
        assertEquals("Currency of transfer is not equal to accounts' currency", result.getResponse());

        assertEquals(sourceBalance, transferService.getAccount(transfer.getSourceAccount()).getBalance());
        assertEquals(targetBalance, transferService.getAccount(transfer.getTargetAccount()).getBalance());
    }

    @Test
    public void invalidAmount() {
        Transfer transfer = createTransfer("11111111", "22222222",
                new BigDecimal(-100),"RUR");

        BigDecimal sourceBalance = transferService.getAccount(transfer.getSourceAccount()).getBalance();
        BigDecimal targetBalance = transferService.getAccount(transfer.getTargetAccount()).getBalance();

        Transfer result = transferService.makeTransfer(transfer);

        assertEquals(transfer.getSourceAccount(), result.getSourceAccount());
        assertEquals(transfer.getTargetAccount(), result.getTargetAccount());
        assertEquals(transfer.getCurrency(), result.getCurrency());
        assertEquals(transfer.getAmount(), result.getAmount());
        assertEquals("Invalid amount", result.getResponse());

        assertEquals(sourceBalance, transferService.getAccount(transfer.getSourceAccount()).getBalance());
        assertEquals(targetBalance, transferService.getAccount(transfer.getTargetAccount()).getBalance());
    }

}
