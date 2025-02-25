import com.google.gson.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class TransformRawData {

    public static void main(String[] args) {
        String rawDataUrl = "https://storage.googleapis.com/maoz-event/rawdata.txt";

        try {

            String jsonData = fetchJsonData(rawDataUrl);

            // แปลง Json string เป็น Gson object
            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
            JsonArray nodesArray = jsonObject.getAsJsonArray("nodes");
            JsonArray edgesArray = jsonObject.getAsJsonArray("edges");

            Map<String, String> nodeTypeMap = new LinkedHashMap<>();
            List<String> nodes = new ArrayList<>();
            List<String> addressIn = new ArrayList<>();
            List<String> addressOut = new ArrayList<>();

            for (JsonElement nodeElement : nodesArray) {
                JsonObject nodeObj = nodeElement.getAsJsonObject();
                String id = nodeObj.get("id").getAsString();
                String type = nodeObj.get("type").getAsString().trim();
                nodeTypeMap.put(id, type);
            }

            for (Map.Entry<String, String> entry : nodeTypeMap.entrySet()) {
                String id = entry.getKey();
                String type = entry.getValue();
                boolean hasAddressOut = false;

                for (JsonElement edgeElement : edgesArray) {
                    JsonObject edgeObj = edgeElement.getAsJsonObject();
                    String source = edgeObj.get("source").getAsString();
                    String target = edgeObj.get("target").getAsString();

                    if (source.equals(id)) {
                        nodes.add(type);

                        // input ให้ใส่ addressIn เป็น ""
                        if (type.equals("input")) {
                            addressIn.add("");
                        } else {
                            addressIn.add(id);
                        }
                        addressOut.add(target);
                        hasAddressOut = true;
                    }
                }

                if (!hasAddressOut) {
                    nodes.add(type);
                    addressIn.add(id);
                    addressOut.add("");
                }
            }

            // แสดงผล
            StringBuilder nodesStr = new StringBuilder();
            StringBuilder addressInStr = new StringBuilder();
            StringBuilder addressOutStr = new StringBuilder();
            for (int i = 0; i < nodes.size(); i++) {
                if (i > 0) {
                    nodesStr.append(", ");
                    addressInStr.append(", ");
                    addressOutStr.append(", ");
                }
                nodesStr.append("'").append(nodes.get(i)).append("'");
                addressInStr.append("'").append(addressIn.get(i)).append("'");
                addressOutStr.append("'").append(addressOut.get(i)).append("'");
            }

            System.out.println("Nodes = [" + nodesStr.toString() + "]");
            System.out.println("addressIn = [" + addressInStr.toString() + "]");
            System.out.println("addressOut = [" + addressOutStr.toString() + "]");

//            for (int i = 0; i < nodes.size(); i++) {
//                System.out.println("Nodes: [" + nodes.get(i) + "]");
//                System.out.println("addressIn: [" + (addressIn.get(i).isEmpty() ? "" : addressIn.get(i)) + "]");
//                System.out.println("addressOut: [" + (addressOut.get(i).isEmpty() ? "" : addressOut.get(i)) + "]\n");
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String fetchJsonData(String rawDataUrl) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(rawDataUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }
}
