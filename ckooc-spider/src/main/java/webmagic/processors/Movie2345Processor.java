package webmagic.processors;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import webmagic.pipelines.BasePipeline;

import java.util.List;

/**
 * Created by Administrator on 2016/7/25.
 */
public class Movie2345Processor implements PageProcessor {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.516.400 QQBrowser/9.4.8142.400";
    private Site site = Site.me()
            .setRetryTimes(5)
            .setSleepTime(500)
            .setCycleRetryTimes(6)
            .setUserAgent(UA);

    private static final String URL_LIST = "http://dianying\\.2345\\.com/list/----\\d+---\\d+\\.html";
    private static final String URL_POST = "http://dianying\\.2345\\.com/detail/\\d+\\.html";

    @Override
    public void process(Page page) {
        String post_xpath = "//div[@id='contentList']/ul/li/span[1]/a";
        String list_xpath = "//div[@id='contentList']/div/a";

        //处理列表页，将其加入待抓取队列
        if (page.getUrl().regex(URL_LIST).match()) {
            String url = page.getUrl().get();
            int pageNum = Integer.parseInt(url.substring(url.lastIndexOf("-") + 1, url.lastIndexOf(".")));
            System.out.println("正在抓取第 " + pageNum + " 页...");

            List<String> l_post = page.getHtml().xpath(post_xpath).links().regex(URL_POST).all();

            List<String> all_list = page.getHtml().xpath(list_xpath).links().regex(URL_LIST).all();
            String l_list = all_list.get(all_list.size() - 1);

            page.addTargetRequests(l_post);
            page.addTargetRequest(l_list);
        } else {
            //处理详情页,添加需要爬取的字段
            page.putField("title", page.getHtml().xpath("//div[@class='tit']/h1/a/text()"));
            page.putField("url", page.getUrl().get());
            page.putField("director", page.getHtml().xpath("//div[@class='wholeTxt']/ul/li[2]/a/text()").all());
            page.putField("actor", page.getHtml().xpath("//div[@class='wholeTxt']/ul/li[1]/a/text()").all());
            page.putField("score", page.getHtml().xpath("//div[@class='tit']/p/em/text()"));
            page.putField("type", page.getHtml().xpath("//div[@class='wholeTxt']/ul/li[3]/a/text()"));
            page.putField("district", page.getHtml().xpath("//div[@class='wholeTxt']/ul/li[6]/a/text()"));
            page.putField("runTime", page.getHtml().xpath("//div[@class='wholeTxt']/ul/li[4]/em/text()").all());
            page.putField("year", page.getHtml().xpath("//div[@class='wholeTxt']/ul/li[5]/em/text()").all());
            page.putField("summary", page.getHtml().xpath("//div[@class='wholeTxt']/ul/li[7]/p/span[2]/text()"));
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        String year = "2015";
        String outPath = "E:/spider/movie2345/" + year + ".txt";

        BasePipeline pipeline = new BasePipeline();
        pipeline.setPath(outPath);

        Spider.create(new Movie2345Processor())
                .addUrl("http://dianying.2345.com/list/----" + year + "---1.html")
                .addPipeline(pipeline)
                .thread(3)
                .run();
    }
}
