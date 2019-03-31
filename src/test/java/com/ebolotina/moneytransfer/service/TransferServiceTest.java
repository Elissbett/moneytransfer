package com.ebolotina.moneytransfer.service;

import com.ebolotina.moneytransfer.model.Account;
import com.ebolotina.moneytransfer.model.Transfer;
import com.ebolotina.moneytransfer.service.TransferService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

public class TransferServiceTest {
    SessionFactory sessionFactory;
    TransferService transferService;

    @Before
    public void setup() {
        sessionFactory = new Configuration().configure().buildSessionFactory();
        transferService = new TransferService();
        transferService.setSessionFactory(sessionFactory);

        Account acc1 = new Account();
        Account acc2 = new Account();
        acc1.setNumber("11111111");
        acc1.setBalance(new BigDecimal(3000));
        acc1.setCurrency("RUR");

        acc2.setNumber("22222222");
        acc2.setBalance(new BigDecimal(200));
        acc2.setCurrency("RUR");

        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(acc1);
        session.save(acc2);
        session.getTransaction().commit();
    }

    @After
    public void close(){
        sessionFactory.close();
    }

    private Account getAccount(String number) {
        try (Session session = sessionFactory.openSession()) {
            Query query = session.createQuery("from Account where number = :paramNumber");
            query.setParameter("paramNumber", number);
            List<Account> accounts = query.list();
            if (!accounts.isEmpty()) {
                return accounts.get(0);
            } else {
                return null;
            }
        }
    }

    private Transfer createTransfer(String sourceAccount, String targetAccount,
                                    BigDecimal amount, String currency) {
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

        BigDecimal sourceBalance = getAccount(transfer.getSourceAccount()).getBalance();
        BigDecimal targetBalance = getAccount(transfer.getTargetAccount()).getBalance();

        Transfer result = transferService.makeTransfer(transfer);

        assertEquals(transfer.getSourceAccount(), result.getSourceAccount());
        assertEquals(transfer.getTargetAccount(), result.getTargetAccount());
        assertEquals(transfer.getCurrency(), result.getCurrency());
        assertEquals(transfer.getAmount(), result.getAmount());
        assertEquals("Successfully completed", result.getResponse());

        assertEquals(sourceBalance.subtract(transfer.getAmount()),
                getAccount(transfer.getSourceAccount()).getBalance());
        assertEquals(targetBalance.add(transfer.getAmount()),
                getAccount(transfer.getTargetAccount()).getBalance());
    }

    @Test
    public void transfer4000RURNonSufficientFunds() {
        Transfer transfer = createTransfer("11111111", "22222222",
                new BigDecimal(4000),"RUR");

        BigDecimal sourceBalance = getAccount(transfer.getSourceAccount()).getBalance();
        BigDecimal targetBalance = getAccount(transfer.getTargetAccount()).getBalance();

        Transfer result = transferService.makeTransfer(transfer);

        assertEquals(transfer.getSourceAccount(), result.getSourceAccount());
        assertEquals(transfer.getTargetAccount(), result.getTargetAccount());
        assertEquals(transfer.getCurrency(), result.getCurrency());
        assertEquals(transfer.getAmount(), result.getAmount());
        assertEquals("Non-sufficient funds", result.getResponse());

        assertEquals(sourceBalance, getAccount(transfer.getSourceAccount()).getBalance());
        assertEquals(targetBalance, getAccount(transfer.getTargetAccount()).getBalance());
    }

    @Test
    public void transfer100USDDifferentCurrency() {
        Transfer transfer = createTransfer("11111111", "22222222",
                new BigDecimal(100),"USD");

        BigDecimal sourceBalance = getAccount(transfer.getSourceAccount()).getBalance();
        BigDecimal targetBalance = getAccount(transfer.getTargetAccount()).getBalance();

        Transfer result = transferService.makeTransfer(transfer);

        assertEquals(transfer.getSourceAccount(), result.getSourceAccount());
        assertEquals(transfer.getTargetAccount(), result.getTargetAccount());
        assertEquals(transfer.getCurrency(), result.getCurrency());
        assertEquals(transfer.getAmount(), result.getAmount());
        assertEquals("Currency of transfer is not equal to accounts' currency", result.getResponse());

        assertEquals(sourceBalance, getAccount(transfer.getSourceAccount()).getBalance());
        assertEquals(targetBalance, getAccount(transfer.getTargetAccount()).getBalance());
    }

    @Test
    public void invalidAmount() {
        Transfer transfer = createTransfer("11111111", "22222222",
                new BigDecimal(-100),"RUR");

        BigDecimal sourceBalance = getAccount(transfer.getSourceAccount()).getBalance();
        BigDecimal targetBalance = getAccount(transfer.getTargetAccount()).getBalance();

        Transfer result = transferService.makeTransfer(transfer);

        assertEquals(transfer.getSourceAccount(), result.getSourceAccount());
        assertEquals(transfer.getTargetAccount(), result.getTargetAccount());
        assertEquals(transfer.getCurrency(), result.getCurrency());
        assertEquals(transfer.getAmount(), result.getAmount());
        assertEquals("Invalid amount", result.getResponse());

        assertEquals(sourceBalance, getAccount(transfer.getSourceAccount()).getBalance());
        assertEquals(targetBalance, getAccount(transfer.getTargetAccount()).getBalance());
    }

}
