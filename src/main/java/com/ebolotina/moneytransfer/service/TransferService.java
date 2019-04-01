package com.ebolotina.moneytransfer.service;

import com.ebolotina.moneytransfer.model.Account;
import com.ebolotina.moneytransfer.model.Transfer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.persistence.LockModeType;
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
	
	public Account getAccount(String number) {
        try (Session session = sessionFactory.openSession()) {
            return getAccount(number, session, false);
        }
    }
	
    private Account getAccount(String number, Session session, boolean lock) {
        Query query = session.createQuery("from Account where number = :paramNumber");
        query.setParameter("paramNumber", number);

        if (lock) {
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        }

        List<Account> accounts = query.list();
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    private String checkTransfer(Transfer transfer, Account sourceAccount, Account targetAccount) {
        if (sourceAccount == null || targetAccount == null) {
            return "Account not found";
			
        } else if (transfer.getAmount().compareTo(BigDecimal.ZERO)<0) {
            return"Invalid amount";
			
        } else if (!sourceAccount.getCurrency().equals(targetAccount.getCurrency())
                || !sourceAccount.getCurrency().equals(transfer.getCurrency())) {
            return "Currency of transfer is not equal to accounts' currency";
        
		} else if (sourceAccount.getBalance().compareTo(transfer.getAmount()) < 0) {
            return "Non-sufficient funds";
        }
			
        return"Successfully completed";
    }

    private Transfer makeTransfer(Transfer transfer, Session session) {

        Account sourceAccount;
        Account targetAccount;
        if (transfer.getSourceAccount().compareTo(transfer.getTargetAccount()) < 0 ) {
            sourceAccount = getAccount(transfer.getSourceAccount(), session, true);
            targetAccount = getAccount(transfer.getTargetAccount(), session, true);
        } else {
            targetAccount = getAccount(transfer.getTargetAccount(), session, true);
            sourceAccount = getAccount(transfer.getSourceAccount(), session, true);
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
