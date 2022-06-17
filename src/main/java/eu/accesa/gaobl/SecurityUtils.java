package eu.accesa.gaobl;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.accesa.gaobl.model.GaoBlTransaction;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

public class SecurityUtils {

    private final static ObjectMapper OM = new ObjectMapper();

    public static boolean passedSecurityValidation(GaoBlTransaction transaction, String publicKey) {
        try {
            // TODO: use sender wallet id to get public ket from storage transaction.getMessage().getSenderWalletId();
            PublicKey pk = getKey(publicKey);
            String signature = transaction.getSignature();
            byte[] message = OM.writeValueAsString(transaction.getMessage()).getBytes(StandardCharsets.UTF_8);
            Signature signature1 = Signature.getInstance("SHA1withRSA", "BC");
            signature1.initVerify(pk);
            signature1.update(message);
            return signature1.verify(signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey getKey(String key) {
        try {
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(key.getBytes(StandardCharsets.UTF_8));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(X509publicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
