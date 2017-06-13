package algorithms.graph.common;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/4/13.
 */
public class JNode implements Serializable {
    private String id;
    private Double weight;
    private Boolean flag;
    private int num;
    private int low;
    private String parent;
    private ArrayList<String> children;

    public JNode(String id) {
        this(id, 1.0);
    }

    public JNode(String id, Double weight) {
        this(id, weight, true, 1, 1, "", new ArrayList<>());
    }

    public JNode(String id, Double weight, Boolean flag, int num, int low, String parent, ArrayList<String> children) {
        this.id = id;
        this.weight = weight;
        this.flag = flag;
        this.num = num;
        this.low = low;
        this.parent = parent;
        this.children = children;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public ArrayList<String> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<String> children) {
        this.children = children;
    }
}
