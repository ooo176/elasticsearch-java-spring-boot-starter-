package ooo.github.io.es.util;

import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fengwang26
 * @date 2022/12/6 15:14
 * @describe
 */
@Data
public class SearchResponseUtil {

    /**
     * 读取聚合结果集
     *
     * @param searchResponse  查询结果
     * @param aggregetionName 聚合分组名
     * @param <T>
     * @return Map<分组Key, 数量>
     */
    public static <T> Map<Object, Long> readStreamTypeAggregation(SearchResponse<T> searchResponse, String aggregetionName) {
        Map<Object, Long> result = new HashMap<>();
        if (searchResponse != null) {
            List<StringTermsBucket> qymcGroup = null;
            try {
                qymcGroup = searchResponse.aggregations().get(aggregetionName).sterms().buckets().array();
            } catch (Exception e) {
                throw new RuntimeException("请确保存在聚合分组名：" + aggregetionName);
            }
            if (!CollectionUtils.isEmpty(qymcGroup)) {
                for (StringTermsBucket bucket : qymcGroup) {
                    result.put(bucket.key()._get(), bucket.docCount());
                }
            }

        }
        return result;

    }

}
