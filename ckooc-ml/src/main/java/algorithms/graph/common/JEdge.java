package algorithms.graph.common;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/4/13.
 */
public class JEdge implements Serializable {
    private String id;
    private String src;
    private String tar;
    private Double weight;

    public JEdge(String id, String src, String tar) {
        new JEdge(id, src, tar, 1.0);
    }

    public JEdge(String id, String src, String tar, Double weight) {
        this.id = id;
        this.src = src;
        this.tar = tar;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getTar() {
        return tar;
    }

    public void setTar(String tar) {
        this.tar = tar;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
