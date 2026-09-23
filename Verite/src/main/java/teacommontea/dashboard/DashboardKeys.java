package teacommontea.dashboard;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class DashboardKeys {

    private static final String ALGORITHM = "EC";
    private static final String CURVE = "secp256r1";
    private static final String SIGNATURE = "SHA256withECDSAinP1363Format";

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final String encodedPublicKey;

    private DashboardKeys(KeyPair pair) {
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
        this.encodedPublicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
    }

    public String publicKey() {
        return encodedPublicKey;
    }

    public static DashboardKeys load(File dataFolder) throws Exception {
        File dir = new File(dataFolder, "dashboard");
        File pub = new File(dir, "public.key");
        File priv = new File(dir, "private.key");

        if (pub.isFile() && priv.isFile()) {
            try {
                KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
                byte[] pubBytes = Base64.getDecoder().decode(Files.readString(pub.toPath()).trim());
                byte[] privBytes = Base64.getDecoder().decode(Files.readString(priv.toPath()).trim());
                PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(pubBytes));
                PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(privBytes));
                return new DashboardKeys(new KeyPair(publicKey, privateKey));
            } catch (Exception ignored) {
                pub.delete();
                priv.delete();
            }
        }

        dir.mkdirs();
        KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM);
        gen.initialize(new ECGenParameterSpec(CURVE));
        KeyPair pair = gen.generateKeyPair();

        Files.writeString(pub.toPath(),
                Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
        Files.writeString(priv.toPath(),
                Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
        try {
            priv.setReadable(false, false);
            priv.setReadable(true, true);
        } catch (Exception ignored) {
        }

        return new DashboardKeys(pair);
    }

    public String sign(String message) throws Exception {
        Signature signer = Signature.getInstance(SIGNATURE);
        signer.initSign(privateKey);
        signer.update(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signer.sign());
    }

    public static boolean verify(String encodedKey, String message, String encodedSignature) {
        try {
            KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
            PublicKey key = factory.generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(encodedKey)));
            Signature verifier = Signature.getInstance(SIGNATURE);
            verifier.initVerify(key);
            verifier.update(message.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(encodedSignature));
        } catch (Exception e) {
            return false;
        }
    }
}
