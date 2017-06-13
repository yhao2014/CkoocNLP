package webmagic.pipelines;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.*;
import java.util.Map;
import java.util.Objects;

/**
 * 自定义的通用输出管道
 * Created by Administrator on 2016/7/4.
 */
public class BasePipeline implements Pipeline {
    private String path = "";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        String reg = "\u00ef";

        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true),"UTF-8"));
            for (Map.Entry<String, Object> entry : resultItems.getAll().entrySet()) {
                if (entry.getValue() instanceof Iterable) {
                    if (Objects.equals(entry.getKey(), "content")) {
                        Iterable value = (Iterable) entry.getValue();
                        for (Object o : value) {
                            bw.write(o.toString());
                        }
                    }else {
                        Iterable value = (Iterable) entry.getValue();
                        String categories = "";
                        for (Object o : value) {
                            categories += o + "|";
                        }
                        bw.write(categories.substring(0, categories.length() - 1));
                    }
                } else {
                    if (entry.getValue() != null) {
                        bw.write(entry.getValue().toString());
                    } else {
                        bw.write("");
                    }
                }

                bw.write(reg);
            }
            bw.write("\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
