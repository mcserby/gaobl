package eu.accesa.gaobl.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Wallet implements Serializable {
    //generated

    private String id;
    //number of coins; default value = 0
    private int amount = 0;
    //public key
    private String publicKey;

    private String nickname;

    public Wallet() {
    }

    public Wallet(String id, int amount, String publicKey, String nickname) {
        this.id = id;
        this.amount = amount;
        this.publicKey = publicKey;
        this.nickname = nickname;
    }

    public Wallet(String id, int amount, String publicKey) {
        this.id = id;
        this.amount = amount;
        this.publicKey = publicKey;
    }

    public Wallet(int amount, String publicKey) {
        this.amount = amount;
        this.publicKey = publicKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


    public String getNickname() {
        return nickname;
    }
}
