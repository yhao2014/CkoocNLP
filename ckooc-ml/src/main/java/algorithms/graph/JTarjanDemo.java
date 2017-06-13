package algorithms.graph;

import algorithms.graph.common.JEdge;
import algorithms.graph.common.JNode;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Administrator on 2017/4/13.
 */
public class JTarjanDemo {

    public static void main(String[] args)throws IOException {
        String startId = "v0001";
        String filePath = "ckooc-ml/data/json/graph1.json";

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "utf-8"));

        String line = br.readLine();
        String json = "";
        while (line != null && !line.isEmpty()) {
            json += line;
            line = br.readLine();
        }

        JTarjanDemo JTarjanDemo = new JTarjanDemo();
        HashMap<String, ArrayList> graphMap = JTarjanDemo.parseJson(json);
        ArrayList<JNode> nodeList = graphMap.get("nodeList");
        ArrayList<JEdge> edgeList = graphMap.get("edgeList");

        JTarjan jTarjan = new JTarjan(nodeList, edgeList);
        jTarjan.init();
        ArrayList<String> arcNodes = jTarjan.run(startId);
        System.out.println("割点: " + arcNodes);

        Iterator<JNode> iterator = jTarjan.getNodeMap().values().iterator();
        while (iterator.hasNext()) {
            JNode node = iterator.next();
            System.out.println(node.getId() + "\t" + node.getNum() + "\t" + node.getLow());
        }
    }

    /** 解析JSON数据 */
    public HashMap<String, ArrayList> parseJson(String json) {
        JSONArray edges = JSON.parseObject(json).getJSONArray("edges");
        JSONArray nodes = JSON.parseObject(json).getJSONArray("nodes");

        ArrayList<JNode> nodeList = new ArrayList<>();
        ArrayList<JEdge> edgeList = new ArrayList<>();

        for (int i = 0; i < edges.size(); i++) {
            JSONObject edge = edges.getJSONObject(i);
            String edgeId = edge.getString("id");
            String srcId = edge.getString("src");
            String tarId = edge.getString("tar");
            Double edgeWeight = edge.getDouble("weight");
            edgeList.add(new JEdge(edgeId, srcId, tarId, edgeWeight));
        }

        for (int i = 0; i < nodes.size(); i++) {
            JSONObject node = nodes.getJSONObject(i);
            String nodeId = node.getString("id");
            Double nodeWeight = node.getDouble("weight");
            nodeList.add(new JNode(nodeId, nodeWeight));
        }

        HashMap<String, ArrayList> resultMap = new HashMap<>();
        resultMap.put("nodeList", nodeList);
        resultMap.put("edgeList", edgeList);

        return resultMap;
    }
}
