package com.ebolotina.moneytransfer;

import com.ebolotina.moneytransfer.controller.TransferController;
import com.ebolotina.moneytransfer.model.Account;
import com.ebolotina.moneytransfer.service.TransferService;
import org.hibernate.Session;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


import java.math.BigDecimal;
import static spark.Spark.post;

public class Main {
    public static void main(String[] args) {

        SessionFactory sessionFactory = new Configuration().configure()
                .buildSessionFactory();
        loadData(sessionFactory);
        TransferService transferService = new TransferService();
        transferService.setSessionFactory(sessionFactory);
        TransferController transferController = new TransferController();
        transferController.setTransferService(transferService);
        post("/moneytransfer","application/json", transferController::makeTransfer);
    }

    public static void loadData(SessionFactory sessionFactory) {
        Account acc1 = new Account();
        Account acc2 = new Account();
        acc1.setNumber("11111111");
        acc1.setBalance(new BigDecimal(3000));
        acc1.setCurrency("RUR");

        acc2.setNumber("22222222");
        acc2.setBalance(new BigDecimal(200));
        acc2.setCurrency("RUR");

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(acc1);
            session.save(acc2);
            session.getTransaction().commit();
        }
    }
}
