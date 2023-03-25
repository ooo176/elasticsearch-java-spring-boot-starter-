package ooo.github.io.es.util;

import co.elastic.clients.elasticsearch._types.mapping.*;
import ooo.github.io.es.anno.Type;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author kaiqin
 */
public class TypeMappingBuilder {

    public static <T> TypeMapping mapBuilder(Class<T> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        TypeMapping.Builder builder = new TypeMapping.Builder();
        for (Field field : declaredFields) {
            Type esType = field.getAnnotation(Type.class);
            if (esType != null) {
                String clazzFieldName = field.getName();
                typeMapping(builder, clazzFieldName, esType);
            }
        }
        return builder.build();
    }

    /**
     * 持续维护中
     *
     * @param builder   构造器
     * @param fieldName 字段名
     * @param esType    类型
     */
    private static void typeMapping(TypeMapping.Builder builder, String fieldName, Type esType) {
        String[] types = esType.type();
        for (String type : types) {
            if (Objects.equals(type, "geo_point")) {
                builder.properties(fieldName, new Property.Builder().geoPoint(new GeoPointProperty.Builder().build()).build());
            }
            if (Objects.equals(type, "text")) {
                builder.properties(fieldName, new Property.Builder().text(new TextProperty.Builder().build()).build());
            }
            if (Objects.equals(type, "keyword")) {
                builder.properties(fieldName, new Property.Builder().keyword(new KeywordProperty.Builder().build()).build());
            }
        }
    }


}
