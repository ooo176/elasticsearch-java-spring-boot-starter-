package ooo.github.io.es.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.extern.slf4j.Slf4j;
import ooo.github.io.es.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kaiqin
 */
@Slf4j
@Component
public class Elasticsearch7ServiceImpl implements ElasticsearchService {

    @Autowired
    @Qualifier("esClient")
    private ElasticsearchClient client;

    @Override
    public CreateIndexResponse createIndex(CreateIndexRequest createIndexRequest) {
        try {
            log.info("创建索引开始,入参：" + createIndexRequest.toString());
            CreateIndexResponse indexResponse = client.indices().create(createIndexRequest);
            log.info("创建索引成功,出参：" + indexResponse.toString());
            return indexResponse;
        } catch (IOException e) {
            log.info("创建索引失败");
            e.printStackTrace();
        }
        return new CreateIndexResponse.Builder().acknowledged(Boolean.FALSE).build();
    }

    @Override
    public DeleteIndexResponse deleteIndex(DeleteIndexRequest deleteIndexRequest) {
        try {
            log.info("删除索引开始,入参：" + deleteIndexRequest.toString());
            DeleteIndexResponse indexResponse = client.indices().delete(deleteIndexRequest);
            log.info("删除索引成功,入参：" + indexResponse.toString());
            return indexResponse;
        } catch (IOException | ElasticsearchException e) {
            log.info("删除索引失败");
            e.printStackTrace();
        }
        return new DeleteIndexResponse.Builder().acknowledged(Boolean.FALSE).build();
    }

    @Override
    public BooleanResponse existIndex(ExistsRequest existsRequest) {
        try {
            log.info("查询索引是否存在,入参：" + existsRequest.toString());
            BooleanResponse response = client.indices().exists(existsRequest);
            log.info("查询索引是否存在,出参：" + response.value());
            return response;
        } catch (IOException e) {
            log.info("查询索引请求失败");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> BulkResponse bulk(BulkRequest bulkRequest) {
        try {
            BulkResponse bulkResponse = client.bulk(bulkRequest);
            if (bulkResponse.errors()) {
                List<BulkResponseItem> errorItems = bulkResponse.items().stream()
                        .filter(k -> k.status() != 201).collect(Collectors.toList());
                log.info(errorItems.toString());
            }
            return bulkResponse;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <TDocument> SearchResponse<TDocument> search(SearchRequest request, Class<TDocument> tDocumentClass) {
        try {
            log.info("查询es数据,入参：" + request.toString());
            return client.search(request, tDocumentClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public DeleteByQueryResponse delete(DeleteByQueryRequest request) {
        try {
            log.info("删除es数据,入参：" + request.toString());
            DeleteByQueryResponse deleteByQuery = client.deleteByQuery(request);
            log.info(deleteByQuery.toString());
            return deleteByQuery;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
