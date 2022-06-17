package eu.accesa.gaobl.model;

public class GaoBlTransaction {

    private GaoBlMessage message;
    private String signature;

    public GaoBlTransaction() {
    }

    public GaoBlTransaction(GaoBlMessage message, String signature) {
        this.message = message;
        this.signature = signature;
    }

    public GaoBlMessage getMessage() {
        return message;
    }

    public String getSignature() {
        return signature;
    }
}
