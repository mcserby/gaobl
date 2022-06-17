package eu.accesa.gaobl.dto;

public class CoinTransaction {

    private String senderWalletId;
    private String receiverWalletId;
    private int amount;

    public CoinTransaction() {
    }

    public CoinTransaction(String senderWalletId, String receiverWalletId, int amount) {
        this.senderWalletId = senderWalletId;
        this.receiverWalletId = receiverWalletId;
        this.amount = amount;
    }

    public String getSenderWalletId() {
        return senderWalletId;
    }

    public String getReceiverWalletId() {
        return receiverWalletId;
    }

    public int getAmount() {
        return amount;
    }
}
