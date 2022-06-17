package eu.accesa.gaobl.dto;

public class WalletTransaction {

    private WalletRequest message;
    private String signature;

    public WalletTransaction() {
    }

    public WalletTransaction(WalletRequest message, String signature) {
        this.message = message;
        this.signature = signature;
    }

    public WalletRequest getMessage() {
        return message;
    }

    public String getSignature() {
        return signature;
    }
}
