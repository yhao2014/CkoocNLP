package webmagic.processors;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import webmagic.pipelines.BasePipeline;

import java.util.List;

/**
 * Created by Administrator on 2016/7/28.
 */
public class FHAllProcessor implements PageProcessor {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.516.400 QQBrowser/9.4.8142.400";
    private Site site = Site.me().setRetryTimes(5).setSleepTime(300).setCycleRetryTimes(6).setUserAgent(UA);

    private static final String URL_LIST = "http://\\w+\\.ifeng\\.com/listpage/\\d+/\\d+/list.shtml(\\?\\w+)?";
    private static final String URL_POST = "http://\\w+\\.ifeng\\.com/a/\\d+/\\d+\\_0.shtml";

    @Override
    public void process(Page page) {
        String post_xpath1 = "//div[@class='box_list clearfix']/h2/a";
        String post_xpath2 = "//div[@class='newsList']/ul/li/a";

        String list_xpath1 = "//a[@class='btnNext']";
        String list_xpath2 = "//div[@class='m_page']/span[2]/a";

        //处理列表页
        if (page.getUrl().regex(URL_LIST).match()) {

            //获取详情页
            List<String> l_post1 = page.getHtml().xpath(post_xpath1).links().regex(URL_POST).all();
            List<String> l_post2 = page.getHtml().xpath(post_xpath2).links().regex(URL_POST).all();

            //获取所有列表链接
            String l_list1 = page.getHtml().xpath(list_xpath1).links().get();
            String l_list2 = page.getHtml().xpath(list_xpath2).links().get();

            page.addTargetRequests(l_post1);
            page.addTargetRequests(l_post2);
            page.addTargetRequest(l_list1);
            page.addTargetRequest(l_list2);

        } else if (page.getHtml().xpath("//h1[@id='artical_topic']/text()").get() != null){
            //处理详情页
            page.putField("category", page.getHtml().xpath("//div[@class='theLogo']/div/a/text()").all());
            page.putField("title", page.getHtml().xpath("//h1[@id='artical_topic']/text()"));
            page.putField("time", page.getHtml().xpath("//span[@class='ss01']/text()"));
            page.putField("content", page.getHtml().xpath("//div[@id='main_content']/p/text()").all());
        }
    }

    @Override
    public Site getSite() {
        return site;
    }


    public static void main(String[] args) {
        BasePipeline pipeline = new BasePipeline();
        pipeline.setPath("E:/spider/fenghuang/mingxing.txt");

        String[] mx = {
                "http://ent.ifeng.com/listpage/227/1536/list.shtml",
                "http://ent.ifeng.com/listpage/226/4215/list.shtml"
        };

        Spider.create(new FHAllProcessor())
                .addUrl(mx)
                .addPipeline(pipeline)
                .thread(10)
                .run();
    }
}
