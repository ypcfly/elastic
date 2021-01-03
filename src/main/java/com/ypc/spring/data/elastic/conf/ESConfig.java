package com.ypc.spring.data.elastic.conf;


import cn.hutool.core.util.StrUtil;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ESConfig {

    @Value("${spring.elasticsearch.rest.uris}")
    private List<String> uris;

    @Value("${spring.elasticsearch.rest.username}")
    private String userName;

    @Value("${spring.elasticsearch.rest.password}")
    private String password;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        HttpHost[] httpHosts = createHosts();
        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts)
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                        credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(userName,password));
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClientBuilder);
        return restHighLevelClient;
    }

    private HttpHost[] createHosts() {
        HttpHost[] httpHosts = new HttpHost[uris.size()];
        for (int i = 0; i < uris.size(); i++) {
            String hostStr = uris.get(i);
            String[] host = hostStr.split(":");
            httpHosts[i] = new HttpHost(host[0].trim(),Integer.valueOf(host[1].trim()));
        }
        return httpHosts;
    }
}
