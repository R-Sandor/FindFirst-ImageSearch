package dev.findfirst.bookmarkit.configuration;

import javax.net.ssl.SSLContext;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "dev.findfirst.bookmarkit.repository.elastic")
public class ElasticClient extends ElasticsearchConfiguration {

    @Value("${elastic.username}") String username;
    @Value("${elastic.password}") String password;

    @Override
    public ClientConfiguration clientConfiguration() {
        try {

            // TODO: Should be on the dev profile to run this on self-signed TLS server.
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

            SSLContextBuilder sslBuilder = SSLContexts.custom()
                    .loadTrustMaterial(null,
                            (x509Certificates, s) -> true
                    );
            final SSLContext sslContext = sslBuilder.build();

            return ClientConfiguration.builder()
                    .connectedTo("localhost:9200")
                    .usingSsl(sslContext, NoopHostnameVerifier.INSTANCE)
                    .withConnectTimeout(10000)
                    .withBasicAuth(username, password)
                    .build();
        } catch (Exception e) {
            // throwing properly
            return null;
        }

    }
}