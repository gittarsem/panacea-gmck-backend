package com.panacea.backend.dto.brcard;

public class BrCardVerifyResponse {

    private String result;
    private String message;
    private String cardId;
    private String passType;
    private String status;

    public BrCardVerifyResponse() {
    }

    public BrCardVerifyResponse(
            String result,
            String message,
            String cardId,
            String passType,
            String status
    ) {
        this.result = result;
        this.message = message;
        this.cardId = cardId;
        this.passType = passType;
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public String getCardId() {
        return cardId;
    }

    public String getPassType() {
        return passType;
    }

    public String getStatus() {
        return status;
    }
}