import java.util.HashMap;
import java.util.Map;

/**
 * Utilitário minimalista para manipular strings JSON (chave=valor).
 * Criado manualmente para evitar o uso de bibliotecas externas (Jackson/Gson).
 * Suporta apenas objetos planos (sem aninhamento).
 */
public class JsonUtils {

    /**
     * Converte um Map em uma string JSON simples.
     */
    public static String mapToJson(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        int count = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append("  \"").append(entry.getKey()).append("\": \"")
              .append(entry.getValue()).append("\"");
            if (++count < map.size()) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Converte uma string JSON simples em um Map.
     * Nota: Este parser é básico e espera o formato exato gerado pelo mapToJson.
     */
    public static Map<String, String> jsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        // Remove chaves e quebras de linha
        String clean = json.replace("{", "").replace("}", "").trim();
        if (clean.isEmpty()) return map;

        String[] lines = clean.split(",\n|,\r\n");
        for (String line : lines) {
            String[] parts = line.split("\": \"");
            if (parts.length == 2) {
                String key = parts[0].trim().replace("\"", "");
                String value = parts[1].trim().replace("\"", "");
                map.put(key, value);
            }
        }
        return map;
    }
}
