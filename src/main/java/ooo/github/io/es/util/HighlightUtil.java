package ooo.github.io.es.util;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 高亮替换工具类
 * <p>
 * 例如：
 * 原始出参：{
 * "_index": "enterprise_archive_dev",
 * "_type": "_doc",
 * "_id": "51f9a00fca839f42a67f9bbfd4c93371",
 * "_score": 1,
 * "_source": {
 * "qyid": "51f9a00fca839f42a67f9bbfd4c93371",
 * "dwxxmc": "重庆市开州区渝巫加油站",
 * },
 * "highlight": {
 * "dwxxmc": [
 * "<em>重庆市开州区渝巫加油站</em>"
 * ]
 * }}
 * <p>
 * 替换之后的出参：（会使用preTags和postTags替换），方便前端直接渲染页面
 * {
 * "_index": "enterprise_archive_dev",
 * "_type": "_doc",
 * "_id": "51f9a00fca839f42a67f9bbfd4c93371",
 * "_score": 1,
 * "_source": {
 * "qyid": "51f9a00fca839f42a67f9bbfd4c93371",
 * "dwxxmc": "<span style=\"color:red;font-weight:bold;\">巫山县林家沟加油站</span>",
 * },
 * "highlight": {
 * "dwxxmc": [
 * "<em>重庆市开州区渝巫加油站</em>"
 * ]
 * }}
 *
 * @author kaiqin
 */
@Slf4j
public class HighlightUtil {

    /**
     * 高亮替换
     * todo 目前只支持获取当前类以及当前类的父类属性替换，之后会优化成递归获取所有父类的属性
     *
     * @param searchResponse 入参
     * @param <T>            泛型
     */
    public static <T> void convert(SearchResponse<T> searchResponse) {
        List<Hit<T>> hits = searchResponse.hits().hits();
        for (Hit<T> hit : hits) {
            T source = hit.source();
            Map<String, List<String>> highlight = hit.highlight();
            for (String fieldName : highlight.keySet()) {
                Field declaredField = null;
                try {
                    assert source != null;
                    declaredField = source.getClass().getDeclaredField(fieldName);
                    declaredField.setAccessible(true);
                    declaredField.set(source, highlight.get(fieldName).get(0));
                } catch (NoSuchFieldException | IllegalAccessException ignored) {
                }
                //暂时只支持获取父类属性，以后会改造用递归获取
                if (declaredField == null) {
                    try {
                        Field parentDeclaredField = source.getClass().getSuperclass().getDeclaredField(fieldName);
                        parentDeclaredField.setAccessible(true);
                        parentDeclaredField.set(source, highlight.get(fieldName).get(0));
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
