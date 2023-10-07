package dev.findfirst.imagesearch.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "dev.findfirst.imagesearch.repository")
public class ElasticClient
// extends ElasticsearchConfiguration
{

  @Value("${elastic.username}") String username;

  @Value("${elastic.password}") String password;

  @Bean
  public RestClient restClient() {
    try {

      // TODO: Should be on the dev profile to run this on self-signed TLS server.
      final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(
          AuthScope.ANY, new UsernamePasswordCredentials(username, password));

      SSLContextBuilder sslBuilder =
          SSLContexts.custom().loadTrustMaterial(null, (x509Certificates, s) -> true);
      final SSLContext sslContext = sslBuilder.build();

      RestClientBuilder builder =
          RestClient.builder(new HttpHost("localhost", 9200, "https"))
              .setRequestConfigCallback(
                  requestConfigBuilder ->
                      requestConfigBuilder.setConnectTimeout(5000).setSocketTimeout(60000))
              .setHttpClientConfigCallback(
                  httpClientBuilder ->
                      httpClientBuilder
                          .setSSLContext(sslContext)
                          .setDefaultCredentialsProvider(credentialsProvider));

      return builder.build();

    } catch (Exception e) {
      // throwing properly
      return null;
    }
  }

  @Bean
  public ElasticsearchTransport elasticsearchTransport() {
    return new RestClientTransport(restClient(), new JacksonJsonpMapper());
  }

  @Bean
  public ElasticsearchClient getElasticSearchClient() {
    try {
      // Create the transport with a Jackson mapper
      return new ElasticsearchClient(elasticsearchTransport());
    } catch (Exception e) {
      return null;
    }
  }

  @Bean
  JacksonJsonpMapper jacksonJsonpMapper(ObjectMapper objectMapper) {
    return new JacksonJsonpMapper(objectMapper);
  }
}
