package webmagic.processors;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import webmagic.pipelines.BasePipeline;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 豆瓣电影爬虫程序(按年份)
 * URL: https://movie.douban.com/tag/[year]?start=0&type=T
 * 中括号部分由自己指定。
 *
 * 爬取字段：标题、发行年份、导演、编剧、主演、类型、上映日期、时长、发行国家或地区、语言、别名、得分、得分细节、简介、相关推荐、
 * 短评、问题、影评、标签、推荐豆列、想看/看过
 *
 * Created by Administrator on 2016/7/20.
 */
public class DoubanMovieProcessor implements PageProcessor {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.516.400 QQBrowser/9.4.8142.400";
    private Site site = Site.me()
            .setRetryTimes(5)
            .setSleepTime(1000)
            .setCycleRetryTimes(6)
            .setUserAgent(UA);
//            .setHttpProxyPool(getProxyIP("ckooc-spider/lib/dic/proxyIP"))
//            .setProxyReuseInterval(3000);


    private static final String URL_LIST = "https://movie\\.douban\\.com/tag/\\d+\\?start=\\d+&type=T";
    private static final String URL_POST = "https://movie\\.douban\\.com/subject/\\d+/";

    public DoubanMovieProcessor() throws IOException {
    }


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


    public List<String[]> getProxyIP(String path) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
        String line = br.readLine();

        ArrayList<String[]> ipPool = new ArrayList<>();
        while (line != null) {
            String[] tokens = line.split(":");
            ipPool.add(tokens);
            line = br.readLine();
        }

        return ipPool;
    }


    @Override
    public void process(Page page) {
        String post_xpath = "//tr[@class='item']/td/div/a";
        String list_xpath = "//span[@class='next']/a";

        //处理列表页
        if (page.getUrl().regex(URL_LIST).match()) {
            String url = page.getUrl().get();
            int pageNum = Integer.parseInt(url.substring(url.indexOf("=") + 1, url.indexOf("&"))) / 20 + 1;
            System.out.println("正在抓取第 " + pageNum + " 页...");

            //获取详情页
            List<String> l_post = page.getHtml().xpath(post_xpath).links().regex(URL_POST).all();

            //获取所有列表链接
            String l_list = page.getHtml().xpath(list_xpath).links().regex(URL_LIST).get();

            page.addTargetRequests(l_post);
            page.addTargetRequest(l_list);
        } else {
            //处理详情页

            //标题
            String title = page.getHtml().xpath("//div[@id='content']/h1/span[1]/text()") + "(" + page.getUrl().get() + ")";
            page.putField("title", title);

            //发行年份
            page.putField("year", page.getHtml().xpath("//span[@class='year']/text()").regex("\\d+"));

            //导演
            List<String> directorNames = page.getHtml().xpath("//div[@id='info']/span[1]/span[2]/a/text()").all();
            List<String> directorURLs = page.getHtml().xpath("//div[@id='info']/span[1]/span[2]/a").links().all();
            List<String> directors = zipList(directorNames, directorURLs);
            page.putField("director", directors);

            //编剧
            List<String> writerNames = page.getHtml().xpath("//div[@id='info']/span[2]/span[2]/a/text()").all();
            List<String> writerURLs = page.getHtml().xpath("//div[@id='info']/span[2]/span[2]/a").links().all();
            List<String> writers = zipList(writerNames, writerURLs);
            page.putField("writer", writers);

            //主演
            List<String> actorNames = page.getHtml().xpath("//span[@class='actor']/span[2]/a/text()").all();
            List<String> actorURLs = page.getHtml().xpath("//span[@class='actor']/span[2]/a").links().all();
            List<String> actors = zipList(actorNames, actorURLs);
            page.putField("actor", actors);

            //类型
            page.putField("type", page.getHtml().xpath("//span[@property='v:genre']/text()").all());

            //上映日期
            page.putField("exhibitionDate", page.getHtml().xpath("//span[@property='v:initialReleaseDate']/text()").all());

            //时长
            page.putField("runTime", page.getHtml().xpath("//span[@property='v:runtime']/text()"));

            List<String> info = page.getHtml().xpath("//div[@id='info']/text()").regex("[\u4e00-\u9fa5]+").all();
            String country = info.get(0);
            String language = info.get(1);
            //发行国家或地区
            page.putField("country", country);
            //语言
            page.putField("language", language);

            //别名
            String[] infoArray = page.getHtml().xpath("//div[@id='info']/text()").get().split("/");
            String alias = "";
            for (int i = infoArray.length - 1; i >= 0; i--) {
                if (!infoArray[i].trim().equals("") && !infoArray[i].trim().matches("\\d+")) {
                    alias += infoArray[i].trim() + "/";
                } else {
                    break;
                }
            }
            page.putField("alias", alias.substring(0, alias.length() - 1));

            //得分
            page.putField("score", page.getHtml().xpath("//strong[@property='v:average']/text()"));

            //得分细节
            page.putField("scoreDetail", page.getHtml().xpath("//span[@class='rating_per']/text()").all());

            //简介
            page.putField("summary", page.getHtml().xpath("//span[@property='v:summary']/text()"));

            //相关推荐
            List<String> recommendationNames = page.getHtml().xpath("//div[@class='recommendations-bd']/dl/dd/a/text()").all();
            List<String> recommendationURLs = page.getHtml().xpath("//div[@class='recommendations-bd']/dl/dd/a").links().all();
            List<String> recommendations = zipList(recommendationNames, recommendationURLs);
            page.putField("recommendation", recommendations);

            //短评
            String shortCommentNums = page.getHtml().xpath("//div[@id='comments-section']/div[1]/h2/span/a/text()").regex("\\d+").get();
            String shortCommentURL = page.getHtml().xpath("//div[@id='comments-section']/div[1]/h2/span/a").links().get();
            page.putField("shortComment", shortCommentNums + "(" + shortCommentURL + ")");

            //问题
            String questionNums = page.getHtml().xpath("//div[@id='askmatrix']/div[1]/h2/span/a/text()").regex("\\d+").get();
            String questionURL = page.getHtml().xpath("//div[@id='askmatrix']/div[1]/h2/span/a").links().get();
            page.putField("question", questionNums + "(" + questionURL + ")");

            //影评
            String reviewNums = page.getHtml().xpath("//div[@id='review_section']/div[1]/h2/span/a/text()").regex("\\d+").get();
            String reviewURL = page.getHtml().xpath("//div[@id='review_section']/div[1]/h2/span/a").links().get();
            page.putField("review", reviewNums + "(" + reviewURL + ")");

            //标签
            List<String> tagNames = page.getHtml().xpath("//div[@class='tags']/div/a/text()").all();
            List<String> tagURLs = page.getHtml().xpath("//div[@class='tags']/div/a").links().all();
            List<String> tags = zipList(tagNames, tagURLs);
            page.putField("tags", tags);

            //推荐豆列
            List<String> douListNames = page.getHtml().xpath("//div[@id='subject-doulist']/ul/li/a/text()").all();
            List<String> douListURLs = page.getHtml().xpath("//div[@id='subject-doulist']/ul/li/a").links().all();
            List<String> douLists = zipList(douListNames, douListURLs);
            page.putField("douList", douLists);

            //看过/想看
            List<String> interestNums = page.getHtml().xpath("//div[@class='subject-others-interests-ft']/a/text()").regex("\\d+").all();
            List<String> interestURLs = page.getHtml().xpath("//div[@class='subject-others-interests-ft']/a").links().all();
            List<String> interests = zipList(interestNums, interestURLs);
            page.putField("interests", interests);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) throws Exception {
        String year = "2016";
        int page = 1;
        String url = "https://movie.douban.com/tag/" + year + "?start=" + (page - 1) * 20 + "&type=T";

        String outPath = "G:/douban/" + year + ".txt";      //保存路径

        BasePipeline pipeline = new BasePipeline();
        pipeline.setPath(outPath);

        Spider.create(new DoubanMovieProcessor())
                .addUrl(url)
                .addPipeline(pipeline)
                .thread(1)
                .run();
    }
}
