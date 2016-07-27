package webmagic.processors;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import webmagic.pipelines.BasePipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * 抓取IT之家的手机列表页以及从列表中的详情页抓取数据
 *
 * Created by Administrator on 2016/7/22.
 */
public class ITHomeProcessor implements PageProcessor {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.516.400 QQBrowser/9.4.8142.400";
    private Site site = Site.me()
            .setRetryTimes(5)
            .setSleepTime(300)
            .setCycleRetryTimes(6)
            .setUserAgent(UA);

    private static final String URL_LIST = "http://\\w+\\.ithome\\.com/category/\\d+_\\d+\\.html";
    private static final String URL_POST = "http://www\\.ithome\\.com/html/\\w+/\\d+\\.htm";


    private List<String> zipList(List<String> list1, List<String> list2) {
        if (list1.size() != list2.size()) {
            System.out.println("两个list长度不相等！");
            System.exit(1);
        }

        ArrayList<String> result = new ArrayList<>(list1.size());
        for (int i = 0; i < list1.size(); i++) {
            result.add(list1.get(i) + "(" + list2.get(i) + ")");
        }

        return result;
    }


    @Override
    public void process(Page page) {
        String list_xpath = "//div[@class='pagenew']/a";
        String post_xpath = "//div[@class='block']/h2/a";

        if(page.getUrl().regex(URL_LIST).match()) {
            String url = page.getUrl().get();
            String pageNum = url.substring(url.indexOf("_") + 1, url.lastIndexOf("."));
            System.out.println("正在抓取第 " + pageNum + " 页...");

            //获取详情页
            List<String> l_post = page.getHtml().xpath(post_xpath).links().regex(URL_POST).all();

            //获取列表页
            List<String> all_list = page.getHtml().xpath(list_xpath).links().all();
            String l_list = all_list.get(all_list.size() - 1);

            page.addTargetRequests(l_post);
            page.addTargetRequest(l_list);
        } else {
            page.putField("category", page.getHtml().xpath("//div[@class='current_nav']/a/text()").all());
            page.putField("title", page.getHtml().xpath("//div[@class='post_title']/h1/text()"));
            page.putField("time", page.getHtml().xpath("//span[@id='pubtime_baidu']/text()"));
            page.putField("source", page.getHtml().xpath("//span[@id='source_baidu']/a/text()"));
            page.putField("content", page.getHtml().xpath("//div[@class='post_content']/p/text()").all());
            page.putField("tags", page.getHtml().xpath("//div[@class='hot_tags']/span/a/text()").all());

            //相关文章
            List<String> relatedNames = page.getHtml().xpath("//div[@class='related_post']/ul/li/a/text()").all();
            List<String> relatedURLs = page.getHtml().xpath("//div[@class='related_post']/ul/li/a").links().all();
            List<String> related = zipList(relatedNames, relatedURLs);
            page.putField("related", related);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        String outPath = "E:/data/ithome/android.txt";
        BasePipeline pipeline = new BasePipeline();
        pipeline.setPath(outPath);

        Spider.create(new ITHomeProcessor())
                .addUrl("http://android.ithome.com/category/74_1.html")
                .addPipeline(pipeline)
                .thread(3)
                .run();
    }
}
