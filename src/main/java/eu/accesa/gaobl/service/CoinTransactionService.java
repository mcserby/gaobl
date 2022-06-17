package eu.accesa.gaobl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import eu.accesa.gaobl.WalletRepository;
import eu.accesa.gaobl.dto.CoinTransaction;
import eu.accesa.gaobl.dto.CoinTransactionRequest;
import eu.accesa.gaobl.dto.Wallet;
import io.grpc.netty.shaded.io.netty.util.internal.StringUtil;

import static eu.accesa.gaobl.SecurityUtils.OM;
import static eu.accesa.gaobl.SecurityUtils.passedSecurityValidation;

public class CoinTransactionService {

    private WalletRepository walletRepository = new WalletRepository();


    public boolean isTransactionValid(String value) {
        CoinTransactionRequest coinTransactionRequest = null;
        try {
            coinTransactionRequest = OM.readValue(value, CoinTransactionRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Wallet senderWallet = walletRepository.findWalletById(coinTransactionRequest.getCoinTransaction().getSenderWalletId());

        if (Strings.isNullOrEmpty(senderWallet.getId())) {
            return false;
        }

        Wallet receiverWallet = walletRepository.findWalletById(coinTransactionRequest.getCoinTransaction().getReceiverWalletId());

        if (Strings.isNullOrEmpty(receiverWallet.getId())) {
            return false;
        }

        boolean isValidSignature = passedSecurityValidation(coinTransactionRequest, senderWallet.getPublicKey());

        if (!isValidSignature){
            return false;
        }

        return senderWallet.getAmount() >= coinTransactionRequest.getCoinTransaction().getAmount();
    }

    public void processCoinTransaction(String value) {
        CoinTransaction coinTransaction = null;
        try {
            coinTransaction = OM.readValue(value, CoinTransactionRequest.class).getCoinTransaction();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //retrieve from wallet entity by id
        Wallet senderWallet = walletRepository.findWalletById(coinTransaction.getSenderWalletId());

        //retrieve to wallet entity by id
        Wallet receiverWallet = walletRepository.findWalletById(coinTransaction.getReceiverWalletId());

        int amountToSend = coinTransaction.getAmount();

        int newAmountInSenderWallet = senderWallet.getAmount() - amountToSend;
        int newAmountInReceiverWallet = receiverWallet.getAmount() + amountToSend;

        senderWallet.setAmount(newAmountInSenderWallet);
        receiverWallet.setAmount(newAmountInReceiverWallet);

        walletRepository.updateWallet(senderWallet);
        walletRepository.updateWallet(receiverWallet);
    }

}
