package eu.accesa.gaobl.dto;

public class WalletRequest {

    private String nickname;
    private String publicKey;

    public WalletRequest() {
    }

    public WalletRequest(String nickname, String publicKey) {
        this.nickname = nickname;
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getNickname() {
        return nickname;
    }

}
