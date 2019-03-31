package com.ebolotina.moneytransfer.controller;

import com.ebolotina.moneytransfer.model.Transfer;
import com.ebolotina.moneytransfer.service.TransferService;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;

public class TransferController {

    private TransferService transferService;
    private Gson gson = new Gson();

    public void setTransferService(TransferService transferService) {
        this.transferService = transferService;
    }

    public String makeTransfer(Request request, Response response) {
        Transfer requestTransfer = gson.fromJson(request.body(), Transfer.class);
        Transfer responseTransfer = transferService.makeTransfer(requestTransfer);
        response.header("Content-Type", "application/json");
        return gson.toJson(responseTransfer);
    }

}

