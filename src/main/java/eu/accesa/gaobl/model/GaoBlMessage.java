package eu.accesa.gaobl.model;

public class GaoBlMessage {

        private String senderWalletId;
        private String receiverWalletId;
        private int amount;

    public GaoBlMessage() {
    }

    public GaoBlMessage(String senderWalletId, String receiverWalletId, int amount) {
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
