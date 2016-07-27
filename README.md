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

如有需要的同学可以参考给出的代码自行实现特定网站的爬取和结果存储

---
#ckooc-ml
本模块主要是基于spark等的机器学习算法实现。<br>

目前以实现的功能有：
>
* 数据预处理
* 数据向量化
* 基于spark的LDA
* 基于spark的LR分类


## 数据预处理
[数据预处理](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-ml/src/main/scala/nlp/preprocess/PreProcessUtils.scala)主要对算法需要用到的数据进行前期的清洗等操作，其中分词等使用到了[Ansj](https://github.com/ansjsun/ansj_seg)和[HanLP](https://github.com/hankcs/HanLP)相关的代码。

**由于分词使用到的词典和模型较大，因此未上传到github上，请从网盘下载：**<br>
链接：[http://pan.baidu.com/s/1qYpeaza](http://pan.baidu.com/s/1qYpeaza) 密码：kox1 大小：638MB<br>
*下载完后解压到ckooc-ml目录下即可*

主要有以下功能：
>
* 繁简转换
* 全半角转换
* 去除无意义词
* 分词
* 去除停用词
* 去除低频词

### 输入数据格式
数据预处理的输入数据为[中国新闻网](http://www.chinanews.com/)上抓取的数据,分为6个类别`体育`,`军事`,`娱乐`,`文化`,`社会`和`经济`. 分为训练文本和测试文本.

输入文件位置：
* 训练文本: ckooc-ml/data/news/train/
* 测试文本: ckooc-ml/data/news/test/

### 输出数据格式
输出经过分词等预处理之后的数据<br>

输出文件位置：
* 训练文本: ckooc-ml/data/preprocess/train
* 测试文本: ckooc-ml/data/preprocess/test


## spark-LDA
这是一个基于[spark](http://spark.apache.org/)的常规定义的
[LDA](https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation)的Scala代码实现.
本代码根据spark官网提供的
[LDAExample.scala](https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/mllib/LDAExample.scala)
文件进行代码改进，实现了LDA的模型训练和保存、加载模型并进行预测功能。
下面是一些具体的说明。

[LDA实现代码](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-ml/src/main/scala/ml/clustering/lda/LDAUtils.scala)


## 分类
目前主要使用分类算法进行新闻分类，已实现的算法有：
>
* [LR逻辑回归](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-ml/src/main/scala/ml/classification/LRCliassifyUtils.scala)



**上述所有功能的Demo均在[ckooc-ml/src/test/scala](https://github.com/yhao2014/CkoocNLP/blob/master/ckooc-ml/src/test/scala)目录下**

---
# 各种测试记录
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


## 分类记录
### LR分类测试记录
       数据类型        数据大小        分类数        训练文本数/per分类        测试文本数/per分类        分类算法        特征维数         准确度
    新闻分类数据         457M            13               10000                     5000                LR             7000     0.817751203603044
    新闻分类数据         457M            13               10000                     5000                LR             8000     0.8633949371020345
    新闻分类数据         457M            13               10000                     5000                LR            10000     0.8678832116788321
    新闻分类数据         516M            15               10000                     5000                LR             8000     0.7378484249241117
    新闻分类数据         571M            16               10000                     5000                LR             8000     0.8016991074309066
    新闻分类(chinaNews)  683M            2              140000+                   60000+                LR            15000     0.9565125193703613
    新闻分类(chinaNews) 1.98G            6              160000+                   70000+                LR            50000     0.8599223781293982