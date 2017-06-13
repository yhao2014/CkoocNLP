package utils;

import java.io.*;
import java.util.HashMap;

/**
 * 读取行政区映射文件
 *
 * Created by yhao on 2017/4/6.
 */
public class ReadDistrict {
    public HashMap<String, String> read(String file) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
        HashMap<String, String> districtMap = new HashMap<>();

        String line = br.readLine();
        while (line != null && !line.isEmpty()) {
            String[] tokens = line.split("\\s+");
            if (tokens.length > 1) {
                String id = tokens[0];
                String name = tokens[1];
                districtMap.put(id, name);
            }

            line = br.readLine();
        }

        return districtMap;
    }
}
