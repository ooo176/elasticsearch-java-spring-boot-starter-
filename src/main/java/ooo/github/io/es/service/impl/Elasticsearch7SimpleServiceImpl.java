package ooo.github.io.es.service.impl;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.search.TrackHits;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import ooo.github.io.es.anno.Id;
import ooo.github.io.es.dto.SearchInput;
import ooo.github.io.es.service.ElasticsearchService;
import ooo.github.io.es.service.ElasticsearchSimpleService;
import ooo.github.io.es.util.TypeMappingBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kaiqin
 */
@Component
public class Elasticsearch7SimpleServiceImpl implements ElasticsearchSimpleService {

    @Value("${elasticsearch.index.maxResultWindow:1000000}")
    public Integer maxResultWindow;
    @Value("${elasticsearch.index.numberOfShards:1}")
    public String numberOfShards;
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Override
    public boolean createIndex(String indexName) {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                .index(indexName)
                .settings(new IndexSettings.Builder()
                        .maxResultWindow(maxResultWindow)
                        .numberOfShards(numberOfShards)
                        .build())
                .build();
        CreateIndexResponse index = elasticsearchService.createIndex(createIndexRequest);
        return index != null;
    }

    @Override
    public boolean createIndex(String indexName, TypeMapping typeMapping) {
        CreateIndexRequest.Builder createRequest = new CreateIndexRequest.Builder()
                .index(indexName)
                .settings(new IndexSettings.Builder()
                        .maxResultWindow(maxResultWindow)
                        .numberOfShards(numberOfShards)
                        .build());
        if (typeMapping != null) {
            createRequest.mappings(typeMapping);
        }
        CreateIndexResponse index = elasticsearchService.createIndex(createRequest.build());
        return index != null;
    }

    @Override
    public <T> boolean createIndex(String indexName, Class<T> clazz) {
        TypeMapping typeMapping = TypeMappingBuilder.mapBuilder(clazz);
        return createIndex(indexName, typeMapping);
    }

    @Override
    public boolean deleteIndex(String indexName) {
        DeleteIndexRequest deleteRequest = new DeleteIndexRequest.Builder().index(indexName).build();
        DeleteIndexResponse response = elasticsearchService.deleteIndex(deleteRequest);
        return response.acknowledged();
    }

    @Override
    public boolean delete(String indexName, Query query) {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest.Builder().query(query).index(indexName).build();
        DeleteByQueryResponse delete = elasticsearchService.delete(deleteByQueryRequest);
        return CollectionUtils.isEmpty(delete.failures());
    }

    @Override
    public boolean existIndex(String indexName) {
        ExistsRequest existsRequest = new ExistsRequest.Builder().index(indexName).build();
        BooleanResponse response = elasticsearchService.existIndex(existsRequest);
        if (response == null) {
            throw new RuntimeException("查询elasticsearch是否存在索引失败！");
        }
        return response.value();
    }

    @Override
    public <T> boolean bulk(String indexName, List<T> ts) {
        if (CollectionUtils.isEmpty(ts)) {
            return false;
        }
        Field idField = getId(ts.get(0).getClass());
        List<BulkOperation> operationList = new ArrayList<>(ts.size());
        for (T t : ts) {
            try {
                String id = idField.get(t).toString();
                BulkOperation bulkOperation = new BulkOperation.Builder()
                        .index(new IndexOperation.Builder<>().document(t).id(id).build()).build();
                operationList.add(bulkOperation);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
        }
        BulkRequest bulkRequest = new BulkRequest.Builder().index(indexName).operations(operationList).build();
        BulkResponse bulkResponse = elasticsearchService.bulk(bulkRequest);
        return !bulkResponse.errors();
    }

    @Override
    public <T> boolean bulk(String indexName, List<T> ts, boolean ignoreEsId) {
        if (!ignoreEsId) {
            //如果强制要求document对应的id存在，则走另一个bulk方法
            return bulk(indexName, ts);
        }
        if (CollectionUtils.isEmpty(ts)) {
            return false;
        }
        List<BulkOperation> operationList = new ArrayList<>(ts.size());
        for (T t : ts) {
            BulkOperation bulkOperation = new BulkOperation.Builder()
                    .index(new IndexOperation.Builder<>().document(t).build()).build();
            operationList.add(bulkOperation);
        }
        BulkRequest bulkRequest = new BulkRequest.Builder().index(indexName).operations(operationList).build();
        BulkResponse bulkResponse = elasticsearchService.bulk(bulkRequest);
        return !bulkResponse.errors();
    }

    @Override
    public <T> SearchResponse<T> search(String indexName, Query query, Class<T> tClass) {
        SearchRequest searchRequest = new SearchRequest.Builder().index(indexName).query(query).build();
        return elasticsearchService.search(searchRequest, tClass);
    }

    @Override
    public <T> SearchResponse<T> search(String indexName, Query query, Integer from, Integer size, Class<T> tClass) {
        SearchRequest searchRequest = new SearchRequest.Builder().index(indexName).query(query).from(from).size(size)
                .trackTotalHits(new TrackHits.Builder().enabled(true).build())
                .build();
        return elasticsearchService.search(searchRequest, tClass);
    }

    @Override
    public <T> SearchResponse<T> search(SearchInput<T> input) {
        if (input.getTClass() == null) {
            throw new RuntimeException("搜索的泛型不能为空");
        }
        List<String> indexNameList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(input.getIndexNames())) {
            indexNameList.addAll(input.getIndexNames());
        }
        if (!StringUtils.isEmpty(input.getIndexName())) {
            indexNameList.add(input.getIndexName());
        }
        SearchRequest.Builder searchBuilder = new SearchRequest.Builder().index(indexNameList);
        if (input.getQuery() != null) {
            searchBuilder.query(input.getQuery());
        }
        if (input.getAggregations() != null) {
            searchBuilder.aggregations(input.getAggregations());
        }
        if (input.getFrom() != null) {
            searchBuilder.from(input.getFrom());
        }
        if (input.getSize() != null) {
            searchBuilder.size(input.getSize());
        }
        if (input.getHighlight() != null) {
            searchBuilder.highlight(input.getHighlight());
        }
        if (input.getSortOptions() != null) {
            searchBuilder.sort(input.getSortOptions());
        }
        if (input.getCollapse() != null) {
            searchBuilder.collapse(input.getCollapse());
        }
        if (input.getTrackHits() != null) {
            searchBuilder.trackTotalHits(input.getTrackHits());
        }
        return elasticsearchService.search(searchBuilder.build(), input.getTClass());
    }

    private <T> Field getId(Class<T> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            Id esId = field.getAnnotation(Id.class);
            if (esId != null) {
                return field;
            }
        }
        throw new RuntimeException("未发现对应的@Id!");
    }

}
