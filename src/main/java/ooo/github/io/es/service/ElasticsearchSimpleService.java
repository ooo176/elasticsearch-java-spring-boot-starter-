package ooo.github.io.es.service;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import ooo.github.io.es.dto.SearchInput;

import java.util.List;

/**
 * @author kaiqin
 */
public interface ElasticsearchSimpleService {

    /**
     * 创建索引
     *
     * @param indexName 索引名称
     * @return 结果
     */
    boolean createIndex(String indexName);

    /**
     * 创建索引
     *
     * @param indexName   索引名称
     * @param typeMapping 字段类型
     * @return 结果
     */
    boolean createIndex(String indexName, TypeMapping typeMapping);


    /**
     * 根据class创建索引
     *
     * @param indexName 索引名称
     * @param clazz     类型
     * @param <T>       泛型
     * @return 成功与否
     */
    public <T> boolean createIndex(String indexName, Class<T> clazz);


    /**
     * 删除索引
     *
     * @param indexName 索引名称
     * @return 结果
     */
    boolean deleteIndex(String indexName);

    /**
     * 删除索引
     *
     * @param indexName 索引名称
     * @param query     匹配条件
     * @return 结果
     */
    boolean delete(String indexName, Query query);

    /**
     * 索引是否存在
     *
     * @param indexName 索引名称
     * @return 结果
     */
    boolean existIndex(String indexName);


    /**
     * 批量新增
     *
     * @param indexName 索引名称
     * @param tList     数据数组
     * @param <T>       数据对象
     * @return 结果
     */
    <T> boolean bulk(String indexName, List<T> tList);


    /**
     * 批量新增
     *
     * @param indexName  索引名称
     * @param tList      数据数组
     * @param <T>        数据对象
     * @param ignoreEsId 忽略elasticsearch对应document的id
     * @return 结果
     */
    <T> boolean bulk(String indexName, List<T> tList, boolean ignoreEsId);


    /**
     * 查询
     *
     * @param indexName 索引名称
     * @param query     查询条件
     * @param tClass    类
     * @param <T>       数据对象
     * @return 结果
     */
    <T> SearchResponse<T> search(String indexName, Query query, Class<T> tClass);


    /**
     * 查询
     *
     * @param indexName 索引名称
     * @param query     查询条件
     * @param from      from
     * @param size      size
     * @param tClass    类
     * @param <T>       数据对象
     * @return 结果
     */
    <T> SearchResponse<T> search(String indexName, Query query, Integer from, Integer size, Class<T> tClass);


    /**
     * 查询
     *
     * @param searchInput 搜索入参
     * @param <T>         数据对象
     * @return 结果
     */
    <T> SearchResponse<T> search(SearchInput<T> searchInput);


}
