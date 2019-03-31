package com.ebolotina.moneytransfer.service;

import com.ebolotina.moneytransfer.model.Account;
import com.ebolotina.moneytransfer.model.Transfer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.persistence.LockModeType;
import javax.xml.transform.Source;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class TransferService {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Transfer makeTransfer(Transfer transfer) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        try{
            Transfer resultTransfer = makeTransfer(transfer, session);
            session.getTransaction().commit();
            return resultTransfer;
        }catch(Exception e){
            session.getTransaction().rollback();
            throw e;
        }finally {
            session.close();
        }
    }

    private Account getAccount(String number, Session session) {
        Query query = session.createQuery("from Account where number = :paramNumber");
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setParameter("paramNumber", number);
        List<Account> accounts = query.list();
        if(!accounts.isEmpty()) {
            return accounts.get(0);
        }else {
            return null;
        }
    }

    private String checkTransfer(Transfer transfer, Account sourceAccount, Account targetAccount) {
        String response;
        if (sourceAccount == null || targetAccount == null) {
            response = "Account not found";
        } else if (transfer.getAmount().compareTo(BigDecimal.ZERO)<0) {
            response = "Invalid amount";
        } else if (!sourceAccount.getCurrency().equals(targetAccount.getCurrency())
                || !sourceAccount.getCurrency().equals(transfer.getCurrency())) {
            response = "Currency of transfer is not equal to accounts' currency";
        } else if (sourceAccount.getBalance().compareTo(transfer.getAmount()) < 0) {
            response = "Non-sufficient funds";
        } else {
            response = "Successfully completed";
        }

        return response;
    }

    private Transfer makeTransfer(Transfer transfer, Session session) {

        Account sourceAccount;
        Account targetAccount;
        if (transfer.getSourceAccount().compareTo(transfer.getTargetAccount()) < 0 ) {
            sourceAccount = getAccount(transfer.getSourceAccount(), session);
            targetAccount = getAccount(transfer.getTargetAccount(), session);
        } else {
            targetAccount = getAccount(transfer.getTargetAccount(), session);
            sourceAccount = getAccount(transfer.getSourceAccount(), session);
        }

        transfer.setDate(new Date());

        transfer.setResponse(checkTransfer(transfer, sourceAccount, targetAccount));

        if (transfer.getResponse().equals("Successfully completed")) {
            BigDecimal newBalance = sourceAccount.getBalance().subtract(transfer.getAmount());
            sourceAccount.setBalance(newBalance);
            newBalance = targetAccount.getBalance().add(transfer.getAmount());
            targetAccount.setBalance(newBalance);
            session.save(sourceAccount);
            session.save(targetAccount);
        }
        session.save(transfer);
        return transfer;
    }

}
