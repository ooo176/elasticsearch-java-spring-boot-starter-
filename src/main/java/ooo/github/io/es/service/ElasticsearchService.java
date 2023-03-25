package ooo.github.io.es.service;

import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.transport.endpoints.BooleanResponse;

/**
 * @author kaiqin
 */
public interface ElasticsearchService {

    /**
     * 创建索引
     *
     * @param createIndexRequest 创建索引请求
     * @return 结果
     */
    CreateIndexResponse createIndex(CreateIndexRequest createIndexRequest);


    /**
     * 删除索引
     *
     * @param deleteIndexRequest 删除索引请求
     * @return 结果
     */
    DeleteIndexResponse deleteIndex(DeleteIndexRequest deleteIndexRequest);

    /**
     * 索引是否存在
     *
     * @param existsRequest 存在索引请求
     * @return 结果
     */
    BooleanResponse existIndex(ExistsRequest existsRequest);


    /**
     * 批量新增
     *
     * @param bulkRequest 批量请求
     * @return 结果
     */
    <T> BulkResponse bulk(BulkRequest bulkRequest);


    /**
     * 批量新增
     *
     * @param request        查询请求
     * @param tDocumentClass 文档类
     * @return 结果
     */
    <TDocument> SearchResponse<TDocument> search(SearchRequest request, Class<TDocument> tDocumentClass);

    /**
     * 批量删除
     *
     * @param request 删除请求
     * @return 结果
     */
    DeleteByQueryResponse delete(DeleteByQueryRequest request);

}
