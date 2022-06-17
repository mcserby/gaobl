package eu.accesa.gaobl.dto;

public class CoinTransactionRequest {

    private CoinTransaction coinTransaction;
    private String signature;

    public CoinTransactionRequest() {
    }

    public CoinTransactionRequest(CoinTransaction message, String signature) {
        this.coinTransaction = message;
        this.signature = signature;
    }

    public CoinTransaction getCoinTransaction() {
        return coinTransaction;
    }

    public String getSignature() {
        return signature;
    }
}
