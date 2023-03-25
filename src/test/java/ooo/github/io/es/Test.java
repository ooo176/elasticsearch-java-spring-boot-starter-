package ooo.github.io.es;

import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.LatLonGeoLocation;
import co.elastic.clients.elasticsearch._types.mapping.GeoPointProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import ooo.github.io.es.dto.QyDocument;
import ooo.github.io.es.service.ElasticsearchService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ElasticsearchAutoConfiguration.class})
public class Test {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Before
    public void createIndexIfNotExist() {
        //exist index
        ExistsRequest existsRequest = new ExistsRequest.Builder().index("aaa").build();
        BooleanResponse response = elasticsearchService.existIndex(existsRequest);
        if (Objects.equals(response.value(), Boolean.TRUE)) {
            return;
        }
        //create index
        TypeMapping typeMapping = new TypeMapping.Builder()
                .properties("location", new Property.Builder().geoPoint(new GeoPointProperty.Builder().build()).build())
                .properties("qyMc", new Property.Builder().text(new TextProperty.Builder().analyzer("ik_smart").build()).build())
                .build();
        IndexSettings indexSettings = new IndexSettings.Builder()
                .maxResultWindow(1000000).numberOfReplicas("0").build();
        CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                .index("aaa").settings(indexSettings).mappings(typeMapping).build();
        CreateIndexResponse createIndexResponse = elasticsearchService.createIndex(createIndexRequest);
        Assert.assertEquals(createIndexResponse.acknowledged(), Boolean.TRUE);


    }

    @org.junit.Test
    public void existIndex() {
        ExistsRequest existsRequest = new ExistsRequest.Builder().index("aaa").build();
        BooleanResponse response = elasticsearchService.existIndex(existsRequest);
        Assert.assertNotNull(response);
    }

    @org.junit.Test
    public void bulk() {
        List<QyDocument> tList = QyDocument.mock();
        List<BulkOperation> operationList = new ArrayList<>(tList.size());
        for (QyDocument t : tList) {
            operationList.add(BulkOperation.of(o -> o.index(i -> i.document(t).id(t.getQyId()))));
        }
        BulkRequest bulkRequest = new BulkRequest.Builder().index("bbb").operations(operationList).build();
        BulkResponse response = elasticsearchService.bulk(bulkRequest);
        Assert.assertEquals(response.errors(), Boolean.FALSE);
    }

    @org.junit.Test
    public void boolSearchTest() {
        Query query = new Query.Builder().bool(new BoolQuery.Builder().build()).build();
        SearchRequest searchRequest = new SearchRequest.Builder().index("aaa").query(query).build();
        SearchResponse response = elasticsearchService.search(searchRequest, QyDocument.class);
        Assert.assertNotEquals(response.took(), 0L);
    }

    @org.junit.Test
    public void geoSearch() {
        Query query = new Query.Builder()
                .geoDistance(new GeoDistanceQuery.Builder()
                        .distance("100km")
                        .location(new GeoLocation.Builder()
                                .latlon(new LatLonGeoLocation.Builder()
                                        .lat(29.992445d)
                                        .lon(108.121307d)
                                        .build())
                                .build())
                        .field("location")
                        .build())
                .build();
        SearchRequest searchRequest = new SearchRequest.Builder().index("aaa").query(query).build();
        SearchResponse response = elasticsearchService.search(searchRequest, QyDocument.class);
        Assert.assertNotEquals(response.took(), 0L);
    }


    @org.junit.Test
    public void geoPolySearch() {
        List<GeoLocation> list = new ArrayList<>();
        GeoLocation geoLocation1 = new GeoLocation.Builder().latlon(new LatLonGeoLocation.Builder().lat(1d).lon(1d).build()).build();
        GeoLocation geoLocation2 = new GeoLocation.Builder().latlon(new LatLonGeoLocation.Builder().lat(2d).lon(2d).build()).build();
        GeoLocation geoLocation3 = new GeoLocation.Builder().latlon(new LatLonGeoLocation.Builder().lat(3d).lon(3d).build()).build();
        GeoLocation geoLocation4 = new GeoLocation.Builder().latlon(new LatLonGeoLocation.Builder().lat(4d).lon(4d).build()).build();
        list.add(geoLocation1);
        list.add(geoLocation2);
        list.add(geoLocation3);
        list.add(geoLocation4);

        GeoPolygonPoints geoPolygonPoints = new GeoPolygonPoints.Builder().points(list).build();
        GeoPolygonQuery geoPolygonQuery = new GeoPolygonQuery.Builder().field("location").polygon(geoPolygonPoints).build();
        SearchRequest searchRequest = new SearchRequest.Builder().index("aaa").query(geoPolygonQuery._toQuery()).build();
        SearchResponse<QyDocument> response = elasticsearchService.search(searchRequest, QyDocument.class);
        System.out.println(response);
        Assert.assertNotEquals(response.took(), 0L);
    }

    @org.junit.Test
    public void ikTest() {
        Query query = new Query.Builder()
                .term(new TermQuery.Builder().field("qyMc").value("武汉").build())
                .build();
        SearchRequest searchRequest = new SearchRequest.Builder().index("aaa").query(query).build();
        SearchResponse response = elasticsearchService.search(searchRequest, QyDocument.class);
        Assert.assertNotEquals(response.took(), 0L);
    }

    @org.junit.Test
    public void deleteIndex() {
        DeleteIndexRequest.Builder builder = new DeleteIndexRequest.Builder().index("aaa");
        DeleteIndexResponse response = elasticsearchService.deleteIndex(builder.build());
        Assert.assertEquals(response.acknowledged(), Boolean.TRUE);
    }

}
