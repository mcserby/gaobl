package eu.accesa.gaobl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.accesa.gaobl.SecurityUtils;
import eu.accesa.gaobl.WalletRepository;
import eu.accesa.gaobl.dto.Wallet;
import eu.accesa.gaobl.dto.WalletRequest;
import eu.accesa.gaobl.dto.WalletTransaction;

import java.util.List;
import java.util.UUID;

import static eu.accesa.gaobl.SecurityUtils.OM;


public class WalletService {

    private WalletRepository walletRepository = new WalletRepository();

    public boolean isWalletDataValid(String value) throws JsonProcessingException {
        WalletTransaction walletTransaction = OM.readValue(value, WalletTransaction.class);
        boolean isValidSignature = SecurityUtils.passedSecurityValidation(walletTransaction, walletTransaction.getMessage().getPublicKey());
        if (!isValidSignature){
            return false;
        }
        return !walletRepository.walletExistsForPublicKey(walletTransaction.getMessage().getPublicKey());
    }

    public Wallet processWallet(String requestData) {
        WalletTransaction walletTransaction = null;
        try {
            walletTransaction = OM.readValue(requestData, WalletTransaction.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Wallet wallet = map(walletTransaction.getMessage());
        walletRepository.saveWallet(wallet);
        return wallet;
    }

    private Wallet map(WalletRequest message) {
        return new Wallet(UUID.randomUUID().toString(), 0, message.getPublicKey(), message.getNickname());
    }

    public List<Wallet> findWallets() {
        return walletRepository.findWallets();
    }

}
