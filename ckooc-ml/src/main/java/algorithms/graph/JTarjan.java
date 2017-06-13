package algorithms.graph;

import algorithms.graph.common.JEdge;
import algorithms.graph.common.JNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2017/4/13.
 */
public class JTarjan implements Serializable {
    private ArrayList<JNode> nodeList;
    private ArrayList<JEdge> edgeList;
    private Boolean isDirected;

    public JTarjan(ArrayList<JNode> nodeList, ArrayList<JEdge> edgeList) {
        this(nodeList, edgeList, false);
    }

    public JTarjan(ArrayList<JNode> nodeList, ArrayList<JEdge> edgeList, Boolean isDirected) {
        this.nodeList = nodeList;
        this.edgeList = edgeList;
        this.isDirected = isDirected;
    }

    private HashMap<String, JNode> nodeMap = new HashMap<>();
    private static int count = 1;

    public HashMap<String, JNode> getNodeMap() {
        return nodeMap;
    }

    public ArrayList<String> run(String start) {
        ArrayList<String> result = new ArrayList<>();
        run(start, result);
        return result;
    }

    public ArrayList<String> run(String start, ArrayList<String> arcNodes) {
        JNode startNode = nodeMap.get(start);
        int subTreeNum = 0;

        startNode.setNum(count++);
        startNode.setLow(startNode.getNum());
        startNode.setFlag(false);

        for (String child : startNode.getChildren()) {
            subTreeNum++;
            JNode childNode = nodeMap.get(child);

            if (childNode.getFlag()) {
                childNode.setParent(start);
                run(child, arcNodes);
                if (startNode.getParent().isEmpty() && subTreeNum > 1 && !arcNodes.contains(start)) arcNodes.add(start);
                if (childNode.getLow() >= startNode.getNum() && startNode.getNum() != 1 && !arcNodes.contains(start)) arcNodes.add(start);

                startNode.setLow(Math.min(startNode.getLow(), childNode.getLow()));
            } else {
                if (!startNode.getParent().isEmpty() && !startNode.getParent().equals(child)) {
                    startNode.setLow(Math.min(startNode.getLow(), childNode.getNum()));
                }
            }
        }

        return arcNodes;
    }

    public void init() {
        //添加节点children，将节点flag初始化，并将节点添加到nodeMap
        for (JEdge edge : edgeList) {
            String src = edge.getSrc();
            String tar = edge.getTar();

            JNode srcNode = nodeMap.getOrDefault(src, new JNode(src));
            JNode tarNode = nodeMap.getOrDefault(tar, new JNode(tar));

            if (!isDirected) {
                ArrayList<String> tarChildren = tarNode.getChildren();
                tarChildren.add(src);
                tarNode.setFlag(true);
                nodeMap.put(tar, tarNode);
            }

            ArrayList<String> srcChildren = srcNode.getChildren();
            srcChildren.add(tar);
            srcNode.setFlag(true);
            nodeMap.put(src, srcNode);
        }

        //添加孤立节点到nodeMap
        for (JNode node : nodeList) {
            if (nodeMap.get(node.getId()) == null) {
                node.setFlag(true);
                nodeMap.put(node.getId(), node);
            }
        }

        count = 1;   //初始化计数器
    }
}
