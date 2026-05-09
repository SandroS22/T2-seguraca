import org.bouncycastle.util.encoders.Hex;
import java.nio.charset.StandardCharsets;

/**
 * Utilitários de conversão e manipulação de dados para o MiniBlockchain.
 * Focado em transformações Hex, String e Byte Array.
 */
public class BlockchainUtils {

    /**
     * Converte um array de bytes para sua representação Hexadecimal.
     */
    public static String toHex(byte[] data) {
        return Hex.toHexString(data);
    }

    /**
     * Converte uma string Hexadecimal de volta para array de bytes.
     */
    public static byte[] fromHex(String hex) {
        return Hex.decode(hex);
    }

    /**
     * Converte uma String UTF-8 para array de bytes.
     */
    public static byte[] strToBytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Converte um array de bytes UTF-8 de volta para String.
     */
    public static String bytesToStr(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Concatena múltiplos arrays de bytes (útil para hashing de blocos).
     */
    public static byte[] concatenate(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }
        byte[] result = new byte[totalLength];
        int currentPos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentPos, array.length);
            currentPos += array.length;
        }
        return result;
    }

    /**
     * Converte um array de bytes para Base32 (Padrão TOTP).
     */
    public static String toBase32(byte[] data) {
        org.apache.commons.codec.binary.Base32 base32 = new org.apache.commons.codec.binary.Base32();
        return base32.encodeToString(data).replace("=", ""); // Remove padding para compatibilidade
    }

    /**
     * Formata um timestamp em milissegundos para uma String legível.
     */
    public static String formatTimestamp(String timestampMillis) {
        try {
            long millis = Long.parseLong(timestampMillis);
            java.time.LocalDateTime dt = java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(millis), 
                java.time.ZoneId.systemDefault()
            );
            return dt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return "N/A";
        }
    }
}
