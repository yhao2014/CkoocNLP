package webmagic.processors;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import webmagic.pipelines.BasePipeline;

import java.util.List;

/**
 * "凤凰网"科技频道数据抓取
 * Created by Administrator on 2016/7/1.
 */
public class FHInternetProcessor implements PageProcessor {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.516.400 QQBrowser/9.4.8142.400";
    private Site site = Site.me().setRetryTimes(5).setSleepTime(300).setCycleRetryTimes(6).setUserAgent(UA);


    private static final String URL_LIST = "http://tech\\.ifeng\\.com/listpage/\\d+/\\d+/list.shtml\\?\\w+";
    private static final String URL_POST = "http://\\w+\\.ifeng\\.com/a/\\d+/\\d+\\_0.shtml";

    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public void process(Page page) {
        String post_xpath = "//div[@class='box_list clearfix']/h2/a";
        String list_xpath = "//a[@class='btnNext']";


        //处理列表页
        if (page.getUrl().regex(URL_LIST).match()) {
            //获取详情页
            List<String> l_post = page.getHtml().xpath(post_xpath).links().regex(URL_POST).all();

            //获取所有列表链接
            List<String> l_list = page.getHtml().xpath(list_xpath).links().regex(URL_LIST).all();

            page.addTargetRequests(l_post);
            page.addTargetRequests(l_list);
        } else {
            //处理详情页
            page.putField("category", page.getHtml().xpath("//div[@class='theLogo']/div/a/text()").all());
            page.putField("title", page.getHtml().xpath("//h1[@id='artical_topic']/text()"));
            page.putField("time", page.getHtml().xpath("//span[@class='ss01']/text()"));
            page.putField("content", page.getHtml().xpath("//div[@id='main_content']/p/text()").all());
        }
    }

    public static void main(String[] args) {
        BasePipeline pipeline = new BasePipeline();
        pipeline.setPath("G:/fenghuang/smartWear.txt");

        Spider.create(new FHInternetProcessor())
                .addUrl("http://tech.ifeng.com/listpage/26335/1/list.shtml?cflag")
                .addPipeline(pipeline)
                .thread(3)
                .run();
    }
}
