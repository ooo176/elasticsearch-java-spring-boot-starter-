package ooo.github.io.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Objects;


/**
 * Elasticsearch自动装配类
 *
 * @author qinkai
 */
@Configuration
@ConditionalOnExpression("#{'${elasticsearch.version}'.equals('7.17.7') }")
@ComponentScan("ooo.github.io.es")
public class ElasticsearchAutoConfiguration {

    private static final String DEFAULT_STR = "-1";

    @Value("${elasticsearch.host}")
    public String host;

    @Value("${elasticsearch.port:9200}")
    public Integer port;

    @Value("${elasticsearch.username:-1}")
    public String username;

    @Value("${elasticsearch.password:-1}")
    public String password;

    @Value("${elasticsearch.connectTimeout:5000}")
    public Integer connectTimeout;

    @Value("${elasticsearch.socketTimeout:60000}")
    public Integer socketTimeout;

    /**
     * create the API client
     *
     * @return ElasticsearchClient
     */
    @Bean("esClient")
    @Primary
    public ElasticsearchClient client() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port));
        if (!Objects.equals(DEFAULT_STR, username) && !Objects.equals(DEFAULT_STR, password)) {
            //参见elasticsearch的基本认证 https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/master/_basic_authentication.html
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.disableAuthCaching();
                return httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider);
            });
        }
        builder.setRequestConfigCallback(builder1 -> builder1.setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout));
        RestClient restClient = builder.build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        //elasticsearch 客户端
        return new ElasticsearchClient(transport);
    }


}

