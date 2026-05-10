import java.util.HashMap;
import java.util.Map;


public class JsonUtils {

    
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

    
    public static Map<String, String> jsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        
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
