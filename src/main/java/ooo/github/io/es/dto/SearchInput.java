package ooo.github.io.es.dto;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.search.FieldCollapse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.TrackHits;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * elasticsearch搜索入参(超过三个参数就要封装一个对象)
 *
 * @author kaiqin
 */
@Data
public class SearchInput<T> {


    /**
     * 索引名称
     */
    private String indexName;

    /**
     * 索引名称复数集合
     */
    private List<String> indexNames;

    /**
     * 分组
     */
    private Map<String, Aggregation> aggregations;

    /**
     * 查询的条件
     */
    private Query query;

    /**
     * 分页参数
     */
    private Integer from;

    /**
     * 分页参数
     */
    private Integer size;

    /**
     * 泛型
     */
    private Class<T> tClass;

    /**
     * 高亮
     */
    private Highlight highlight;

    /**
     * 排序
     */
    private List<SortOptions> sortOptions;

    /**
     * 去重(折叠)
     * refer: https://www.elastic.co/guide/en/elasticsearch/reference/current/collapse-search-results.html
     */
    private FieldCollapse collapse;

    /**
     * 分页符
     * refer: https://www.elastic.co/guide/en/elasticsearch/reference/7.5/search-request-body.html#request-body-search-track-total-hits
     */
    private TrackHits trackHits;

    /**
     * 添加聚合
     *
     * @param aggregetionName 聚合分组名称
     * @param field           聚合字段（keyword）
     * @param size            结果集分组个数
     */
    public void addStringTermsTypeAggregation(String aggregetionName, String field, Integer size) {
        if (StringUtils.isEmpty(field)) {
            throw new RuntimeException("聚合字段不为空");
        }
        if (CollectionUtils.isEmpty(this.aggregations)) {
            this.aggregations = new HashMap<>();
        }
        Aggregation build = new Aggregation.Builder().terms(t -> t.field(field).size(size)).build();
        aggregations.put(aggregetionName, build);
    }


}
