package webmagic.pipelines;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

    Long count = 0L;

    @Override
    public void process(ResultItems resultItems, Task task) {
        String reg = "\u00ef";

        if (count % 100000 == 0) {
            count += 1;
        }
        String path_new = path.substring(0, path.lastIndexOf(".")) + "_" + count + path.substring(path.lastIndexOf(".") + 1, path.length());

        try {
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path_new, true),"UTF-8"));
            for (Map.Entry<String, Object> entry : resultItems.getAll().entrySet()) {
                if (entry.getValue() instanceof Iterable) {
                    if (Objects.equals(entry.getKey(), "content")) {
                        Iterable value = (Iterable) entry.getValue();
                        for (Object o : value) {
                            printWriter.print(o);
                        }
                    }else {
                        Iterable value = (Iterable) entry.getValue();
                        String categories = "";
                        for (Object o : value) {
                            categories += o + "|";
                        }
                        printWriter.print(categories.substring(0, categories.length() - 1));
                    }
                } else {
                    printWriter.print(entry.getValue());
                }

                printWriter.print(reg);
            }
            printWriter.println();
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
