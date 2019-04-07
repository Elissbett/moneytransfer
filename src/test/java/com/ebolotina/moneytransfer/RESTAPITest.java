package com.ebolotina.moneytransfer;

import com.ebolotina.moneytransfer.model.Transfer;
import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class RESTAPITest {

    private static Gson gson = new Gson();

    @BeforeClass
    public static void setup() {
        String[] args = {};
        Main.main(args);
    }

    @AfterClass
    public static void shutdown() {
//        Main.shutdown();
    }

    @Test
    public void transfer100RURSuccessful() {
        Transfer requestTransfer = new Transfer();
        requestTransfer.setSourceAccount("11111111");
        requestTransfer.setTargetAccount("22222222");
        requestTransfer.setAmount(new BigDecimal(100));
        requestTransfer.setCurrency("RUR");

        Response response = RestAssured.given()
                .port(4567)
                .header("Content-Type", "application/json")
                .body(gson.toJson(requestTransfer))
                .post("/moneytransfer");

        Transfer resultTransfer = gson.fromJson(response.asString(), Transfer.class);

        assertEquals(requestTransfer.getAmount(), resultTransfer.getAmount());
        assertEquals(requestTransfer.getCurrency(), resultTransfer.getCurrency());
        assertEquals(requestTransfer.getSourceAccount(), resultTransfer.getSourceAccount());
        assertEquals(requestTransfer.getTargetAccount(), requestTransfer.getTargetAccount());
        assertEquals("Successfully completed", resultTransfer.getResponse());
    }

    @Test
    public void accountNotFound() {

        Transfer requestTransfer = new Transfer();
        requestTransfer.setSourceAccount("11111113");
        requestTransfer.setTargetAccount("22222222");
        requestTransfer.setAmount(new BigDecimal(40));
        requestTransfer.setCurrency("RUR");

        Response response = RestAssured.given()
                .port(4567)
                .header("Content-Type", "application/json")
                .body(gson.toJson(requestTransfer))
                .post("/moneytransfer");

        Transfer resultTransfer = gson.fromJson(response.asString(), Transfer.class);

        assertEquals(requestTransfer.getAmount(), resultTransfer.getAmount());
        assertEquals(requestTransfer.getCurrency(), resultTransfer.getCurrency());
        assertEquals(requestTransfer.getSourceAccount(), resultTransfer.getSourceAccount());
        assertEquals(requestTransfer.getTargetAccount(), requestTransfer.getTargetAccount());
        assertEquals("Account not found", resultTransfer.getResponse());
    }

}