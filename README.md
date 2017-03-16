***********************************************************************
# ckoocnlp --ickooc自然语言处理


***********************************************************************
# 项目结构说明
本项目包括两个模块：`ckooc-spider`和`ckooc-ml`，ckooc-spider主要是爬虫代码实现，ckooc-ml主要是机器学习算法的实现代码

---
# ckooc-spider
鉴于机器学习和数据挖掘最核心的内容之一便是数据，因此特意添加了爬虫模块，本模块目前采用[WebMagic](https://github.com/code4craft/webmagic). 它是基于scrapy的java实现，使用非常灵活，并且有非常详细的[文档说明](http://webmagic.io/docs/zh/).

WebMagic由以下四个模块组成：<br>
>
* Downloader: Downloader负责从互联网上下载页面，以便后续处理。
* PageProcessor: PageProcessor负责解析页面，抽取有用信息，以及发现新的链接。
* Scheduler: Scheduler负责管理待抓取的URL，以及一些去重的工作。
* Pipeline: Pipeline负责抽取结果的处理，包括计算、持久化到文件、数据库等。

通常情况下，我们只需要重写其中的`PageProcessor`和`Pipeline`就可以了。<br>

目前项目已实现了针对以下网站的PageProcessor：
>
* [中国新闻网](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-spider/src/main/java/webmagic/processors/ChinaNewsProcessor.java)
* [豆瓣电影](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-spider/src/main/java/webmagic/processors/DoubanMovieProcessor.java)
* [凤凰科技](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-spider/src/main/java/webmagic/processors/FHInternetProcessor.java)
* [IT之家](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-spider/src/main/java/webmagic/processors/ITHomeProcessor.java)
* [2345电影](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-spider/src/main/java/webmagic/processors/Movie2345Processor.java)

同时实现了将结果写入本地文件的PipLine：
>
* [BasePipline](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-spider/src/main/java/webmagic/piplines/BasePipline.java)

如有需要的同学可以参考给出的代码自行实现特定网站的爬取和结果存储.

---
# ckooc-ml
本模块主要是一些算法实现以及基于spark的机器学习算法应用实践。<br>

目前实现的算法：
>
* 数据预处理与分词过滤

<br>

目前实现的案例：
>
* 基于LR的新闻分类
* 基于DT的新闻分类


## 数据说明
数据预处理的输入数据为[中国新闻网](http://www.chinanews.com/)上抓取的数据,分为6个类别`体育`,`军事`,`娱乐`,`文化`,`社会`和`经济`. 分为训练文本和测试文本.

输入文件位置：
* 训练文本: ckooc-ml/data/chinanews/train/
* 测试文本: ckooc-ml/data/chinanews/test/

数据格式：<br>
每行一条新闻数据，保留4个字段：`类别` `标题` `日期` `正文`. 不同字段之间使用分割符`ï`(unicode编码为`\u00EF`)

##算法

### 1. 数据预处理
数据预处理主要分成了两步：
>
* [数据清洗](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-ml/src/main/scala/algorithms/nlp/clean/Cleaner.scala)
* [分词过滤](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-ml/src/main/scala/algorithms/nlp/segment/Segmenter.scala)

*配置参数见[preprocess.properties](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-ml/src/main/resources/preprocess.properties)*

数据清洗目前实现的功能有`繁简转换` `全半角转换` `按长度过滤行`. 如果特殊处理，可以对该部分代码自行修改！<br>
分词过滤目前实现的功能有`分词` `词性标注` `过滤英文` `过滤数字` `按词长度过滤` `按词性过滤` `按词数量过滤行`. 使用到了[HanLP](https://github.com/hankcs/HanLP)相关的代码。
由于分词使用到的词典和模型较大，因此未上传到github上，请直接从[HanLP发布页](https://github.com/hankcs/HanLP/releases)下载对应版本的data包，
版本可在pom.xml文件查看.<br>
*下载完后解压到ckooc-ml/dictionaries目录下即可*


## 案例

### 1. 基于LR的新闻分类
基于LR的新闻分类采用了spark ML包提供的LogisticRegression方法，经过预处理步骤后进行6个类别的新闻分类.
>
* 训练代码: [TrainNewsClassWithLRDemo](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-ml/src/main/scala/applications/ml/TrainNewsClassWithLRDemo.scala)
* 预测代码: [PredictNewsClassDemo](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-ml/src/main/scala/applications/ml/PredictNewsClassDemo.scala)

### 2. 基于DT的新闻分类
基于LR的新闻分类采用了spark ML包提供的DecisionTreeClassifier方法，经过预处理步骤后进行6个类别的新闻分类.
>
* 训练代码: [TrainNewsClassWithDTDemo](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-ml/src/main/scala/applications/ml/TrainNewsClassWithDTDemo.scala)
* 预测代码: [PredictNewsClassDemo](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-ml/src/main/scala/applications/ml/PredictNewsClassDemo.scala)



---
# 各类测试记录
## 分类测试记录
       分类算法        数据类型         数据大小        分类数        训练文本数/per分类        测试文本数/per分类        特征维数        特征选择               准确率                   召回率                    F1值                  
          LR         新闻分类数据        34.6M            6                1300                       700                 10000           tf-idf         0.8935800599954036         0.8914027149321267         0.891430851616871 
          LR         新闻分类数据        34.6M            6                1300                       700                 15000           tf-idf         0.8947117208305829         0.8928316265777566         0.8928794316892751 
          LR         新闻分类数据        34.6M            6                1300                       700                 20000           tf-idf         0.8939860821464829         0.8921171707549418         0.8922029530256707 
          DT         新闻分类数据        34.6M            6                1300                       700                 10000           tf-idf         0.7541715743599178         0.7413669921409859         0.7445713157384694 
          DT         新闻分类数据        34.6M            6                1300                       700                 15000           tf-idf         0.7547732159937557         0.7418432960228626         0.7450737112870275 
          DT         新闻分类数据        34.6M            6                1300                       700                 20000           tf-idf         0.7552267854801558         0.7425577518456775         0.7456736966804649                


## LDA模型训练性能记录

        数据           CPU           内存           数据           语料大小           文本特点           文本个数           迭代次数          主题数          训练时长          avglogLikelihood          logPerplexity
    百度百科       5cores * 16     50g * 16         99M            4721710             长文本             10000                10               500            4.28min            -10374.58748             -20994.3680
    百度百科       5cores * 16     50g * 16         99M            4721814             长文本             10000                20               500            7.47min            -8786.095098             -24717.5388
    百度百科       5cores * 16     50g * 16         99M            4721490             长文本             10000                30               500              11min            -8517.495272             -24321.77698
    百度百科       5cores * 16     50g * 16         99M            4721450             长文本             10000                40               500           12.27min            -8451.208588             -25191.88867
    百度百科       5cores * 16     50g * 16         99M            4721573             长文本             10000                50               500           18.42min            -8424.327683             -24954.14616
    百度百科       5cores * 16     50g * 16         99M            4721609             长文本             10000                60               500           20.35min            -8410.599461             -24732.90191
    百度百科       5cores * 16     50g * 16         99M            4721452             长文本             10000                70               500            24.5min            -8404.112362             -25228.67469
    百度百科       5cores * 16     50g * 16         99M            4721814             长文本             10000                80               500           27.76min            -8401.891865             -24780.33578

    5cores * 12   30g * 12       百度百科          428M             长文本            100000              40              1000            2.82h



**说明**
>
* 6分类：经济、军事、社会、体育、文化、娱乐
* 13分类：公益、健康、交通、教育、经济、军事、历史、农业、时尚、数码、体育、通讯、娱乐