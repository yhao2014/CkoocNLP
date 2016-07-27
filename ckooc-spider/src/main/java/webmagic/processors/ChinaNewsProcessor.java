package webmagic.processors;

import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import webmagic.pipelines.BasePipeline;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * "中国新闻网"按类别抓取数据。需要从指定的列表页开始抓取：http://www.chinanews.com/scroll-news/[category]/[year]/[day]/news.shtml
 * 中括号部分由自己指定。
 * <p/>Created by Administrator on 2016/7/4.
 */
public class ChinaNewsProcessor implements PageProcessor {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.516.400 QQBrowser/9.4.8142.400";
    private Site site = Site.me().setRetryTimes(5).setSleepTime(300).setCycleRetryTimes(6).setUserAgent(UA);

    private static final String URL_LIST = "http://\\w+\\.chinanews\\.com/scroll-news/\\w+/\\d+/\\d+/news.shtml";
    private static final String URL_POST = "http://\\w+\\.chinanews\\.com/\\w+/\\d+/\\d+\\-\\d+/\\d+\\.shtml";

    /**
     * 将URL中包含的日期增加指定的天数
     *
     * @param url  输入的URL
     * @param days 增加的天数
     * @return 返回新URL
     * @throws Exception
     */
    private static String addDay(String url, int days) throws Exception {
        String[] fields = url.split("/");
        String year = fields[5];
        String day = fields[6];

        String startDay = year + day;

        Date d1 = new SimpleDateFormat("yyyyMMdd").parse(startDay);

        Calendar cal = Calendar.getInstance();
        cal.setTime(d1);

        cal.add(Calendar.DAY_OF_MONTH, days);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(cal.getTime());

        System.out.println(date);

        String newURL = "";
        for (int i = 0; i < fields.length - 3; i++) {
            newURL += fields[i] + "/";
        }
        newURL += date.substring(0, 4) + "/" + date.substring(4, date.length()) + "/" + fields[fields.length - 1];
        return newURL;
    }


    @Override
    public void process(us.codecraft.webmagic.Page page) {
        String post_xpath = "//div[@class='content_list']/ul/li";

        //处理列表页，将其加入待抓取队列
        if (page.getUrl().regex(URL_LIST).match()) {
            List<String> l_post = page.getHtml().xpath(post_xpath).links().regex(URL_POST).all();

            String newURL = "";
            try {
                newURL = addDay(page.getUrl().get(), -1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            page.addTargetRequests(l_post);
            page.addTargetRequest(newURL);
        } else {
            //处理详情页,添加需要爬取的字段
            page.putField("category", page.getHtml().xpath("//div[@id='nav']/a/text()").all());
            page.putField("title", page.getHtml().xpath("//div[@id='cont_1_1_2']/h1/text()"));
            page.putField("time", page.getHtml().xpath("//div[@class='left-t']/text()"));
            page.putField("content", page.getHtml().xpath("//div[@class='left_zw']/p/text()").all());
        }
    }

    @Override
    public Site getSite() {
        return site;
    }


    public static void main(String[] args) throws Exception {
//        String year = args[0];
//        String day = args[1];
//        String category = args[2];
//        String outPath = args[3];

        String year = "2016";
        String day = "0707";
        String category = "ty";     //待抓取的类别（可通过url查看）
        String outPath = "E:/spider/chinaNews/ty.txt";      //保存路径

        BasePipeline pipeline = new BasePipeline();
        pipeline.setPath(outPath);

        Spider.create(new ChinaNewsProcessor())
                .addUrl("http://www.chinanews.com/scroll-news/" + category + "/" + year + "/" + day + "/news.shtml")
                .addPipeline(pipeline)
                .thread(3)
                .run();
    }
}
