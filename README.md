# spring-boot-starter-elasticsearch说明文档
[![](https://jitpack.io/v/ooo176/spring-boot-starter-elasticsearch.svg)](https://jitpack.io/#ooo176/spring-boot-starter-elasticsearch)


## 1 客户端开发思路

开发spring-boot-starter-elasticsearch

使用starter的好处：

1.团队合作开发，Elasticsearch Java API Client中文文档很少，官方文档不也是很全，大部分时候只能阅读javadoc来寻找对应的方法，封装starter方便多人共同开发；

2.服务共用，多个服务共用一个starter，减少代码开发量，封装公用方法；

3.动态生效，开箱即用，通过是否存在elasticsearch.version参数动态生效该 starter；

4.统一日志管理，每次查询请求将入参输出，便于开发和测试人员调试输出结果；

## 2 UML图

1.封装ElasticSearchClient原生方法，入参和出参均为ElasticSearchClient原生包的类，提供对外服务方法ElasticsearchService，内部集成日志输入和输出；

2.考虑到项目开发并不需要很复杂的操作，对外也提供Elasticsearch简单操作服务ElasticsearchSimpleService，对ElasticsearchService方法进行封装；
![](https://reborn1.oss-cn-hangzhou.aliyuncs.com/md/202303252040569.png)

## 3  单元测试报告

ElasticsearchService方法都已通过单元测试，单元测试用例基于junit开发

## 4 使用说明

### 4.1 引入依赖

说明：目前service和simpleService部分方法未开发完全，有需要请提issue

#### 4.1.1 添加JitPack repository

```
<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

#### 4.1.2 添加依赖

```
<dependency>
	    <groupId>com.github.ooo176</groupId>
	    <artifactId>spring-boot-starter-elasticsearch</artifactId>
	    <version>1.0</version>
	</dependency>
```

### 4.2 新增配置

starter会根据是否存在elasticsearch.version是否等于7.17.7参数来决定会不会生效

```
elasticsearch:
  cluster_name: ESCluster1
  host: localhost
  port: 9200
  version: 7.17.7
  
management:
  health:
    elasticsearch:
      enabled: false
```

### 4.3 使用服务类

直接在需要的类里面，引入ElasticsearchSimpleService或者ElasticsearchService，调用对应的方法就行

```
@Autowired
private ElasticsearchSimpleService simpleService;
```

或者

```
@Autowired
private ElasticsearchService esService;
```



### 4.4 相关参数说明

| 参数                                | 参数默认值 | 说明                                            | 举例      |
| ----------------------------------- | ---------- | ----------------------------------------------- | --------- |
| elasticsearch.version                  |            | 根据参数决定使用elasticsearch的版本号，只支持7.17.7                       | 7.17.7 |
| elasticsearch.host                  |            | elasticsearch的master地址                       | 127.0.0.1 |
| elasticsearch.port                  | 9200       | elasticsearch的http端口                         | 9200      |
| elasticsearch.username              |            | elasticsearch需要认证的用户名，不配置默认不存在 | elastic   |
| elasticsearch.password              |            | elasticsearch需要认证的密码，不配置默认不存在   | 123456    |
| elasticsearch.connectTimeout        | 5000       | elasticsearch连接时设置的连接超时时间           | 5000      |
| elasticsearch.socketTimeout         | 60000      | elasticsearch连接时设置的socket超时时间         | 60000     |
| elasticsearch.index.numberOfShards  | 1          | elasticsearch创建索引时的分片                   | 3         |
| elasticsearch.index.maxResultWindow | 1000000    | elasticsearch创建索引时的深度分页查询参数       | 1000000   |









## 5.遇到的问题

### 5.1.JsonParser不存在

![](https://reborn1.oss-cn-hangzhou.aliyuncs.com/md/202211231542952.png)

解决办法：

pom新增

```
<dependency>
  <groupId>jakarta.json</groupId>
  <artifactId>jakarta.json-api</artifactId>
  <version>2.0.1</version>
</dependency>
```

参考： https://github.com/elastic/elasticsearch-java/issues/79

### 5.2.与springboot定义的es版本不一致

现象：

![](https://reborn1.oss-cn-hangzhou.aliyuncs.com/md/202211231620911.png)

解决办法：

在自身pom里，重写spring相关的elasticsearch版本

```
  <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-client</artifactId>
            <version>7.17.7</version>
            <exclusions>
                <exclusion>
                    <groupId>commons-logging</groupId>
                    <artifactId>commons-logging</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
```

### 5.3.Missing required property 

![](https://reborn1.oss-cn-hangzhou.aliyuncs.com/md/202211241720786.png)

找到对应的代码，设置需要的property就可以了

参考：https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/missing-required-property.html

### 5.4.创建索引后，eshead出现Unassigned节点

解决办法：创建索引时，调整副本数

通过PUT请求,修改指定索引的number_of_replicas参数，如果是单节点的elasticsearch，就将参数修改为0；

