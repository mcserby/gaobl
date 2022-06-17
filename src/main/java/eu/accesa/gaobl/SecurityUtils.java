package eu.accesa.gaobl;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.accesa.gaobl.dto.WalletTransaction;
import eu.accesa.gaobl.dto.CoinTransactionRequest;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SecurityUtils {

    public final static ObjectMapper OM = new ObjectMapper();

    public static boolean passedSecurityValidation(CoinTransactionRequest transaction, String publicKey) {
        try {
            PublicKey pk = getKey(publicKey);
            byte[] signature = Base64.getDecoder().decode(transaction.getSignature());
            byte[] message = OM.writeValueAsString(transaction.getCoinTransaction()).getBytes(StandardCharsets.UTF_8);
            Signature signature1 = Signature.getInstance("DSA", "SUN");
            signature1.initVerify(pk);
            signature1.update(message);
            return signature1.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean passedSecurityValidation(WalletTransaction transaction, String publicKey) {
        try {
            PublicKey pk = getKey(publicKey);
            byte[] signature = Base64.getDecoder().decode(transaction.getSignature());
            byte[] message = OM.writeValueAsString(transaction.getMessage()).getBytes(StandardCharsets.UTF_8);
            Signature signature1 = Signature.getInstance("DSA", "SUN");
            signature1.initVerify(pk);
            signature1.update(message);
            return signature1.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static PublicKey getKey(String key) {
        try {
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(Base64.getDecoder().decode(key.getBytes(StandardCharsets.UTF_8)));
            KeyFactory kf = KeyFactory.getInstance("DSA");
            return kf.generatePublic(X509publicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
