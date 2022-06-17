package eu.accesa.gaobl;
import com.fasterxml.jackson.core.JsonProcessingException;
import eu.accesa.gaobl.dto.WalletRequest;
import eu.accesa.gaobl.dto.WalletTransaction;
import eu.accesa.gaobl.dto.CoinTransaction;
import eu.accesa.gaobl.dto.CoinTransactionRequest;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

import static eu.accesa.gaobl.SecurityUtils.OM;
import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random); //generate key pair
        KeyPair pair = keyGen.generateKeyPair();
        return pair;
    }

    @org.junit.jupiter.api.Test
    void passedSecurityValidation()
            throws NoSuchAlgorithmException, JsonProcessingException, InvalidKeyException, SignatureException,
            NoSuchProviderException {
        KeyPair pair = generateRSAKeyPair();
        PrivateKey priv = pair.getPrivate();
        PublicKey pub = pair.getPublic();
        Signature sig = Signature.getInstance("DSA");
        sig.initSign(priv);
        CoinTransaction message = new CoinTransaction("9e6291e3-d3c2-4e3c-b024-d0ca73677d69", "61f41376-c542-4bb5-be76-245cb942f721", 23);
        byte[] bytes = OM.writeValueAsBytes(message);
        sig.update(bytes);
        byte[] signatureBytes = sig.sign();
        String sign = new String(Base64.getEncoder().encode(signatureBytes), StandardCharsets.UTF_8);
        CoinTransactionRequest t = new CoinTransactionRequest(message, sign);
        String key = new String(Base64.getEncoder().encode(pub.getEncoded()), StandardCharsets.UTF_8);
        String privatekey = new String(Base64.getEncoder().encode(priv.getEncoded()), StandardCharsets.UTF_8);
        System.out.println(key);
        System.out.println(privatekey);
        boolean b = SecurityUtils.passedSecurityValidation(t, key);
        assertTrue(b);

        WalletRequest walletRequest = new WalletRequest("serby",key);
        Signature walsig = Signature.getInstance("DSA");
        walsig.initSign(priv);
        byte[] walbytes = OM.writeValueAsBytes(walletRequest);
        walsig.update(walbytes);
        byte[] walsignatureBytes = walsig.sign();
        String walsign = new String(Base64.getEncoder().encode(walsignatureBytes), StandardCharsets.UTF_8);
        WalletTransaction walletTransaction = new WalletTransaction(walletRequest, walsign);
        System.out.println(walsign);
    }


}