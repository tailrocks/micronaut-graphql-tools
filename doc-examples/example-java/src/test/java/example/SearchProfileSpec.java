package example;

import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.api.ApolloResponse;
import com.apollographql.apollo3.api.Optional;
import com.apollographql.apollo3.rx3.Rx3Apollo;
import example.client.SearchProfileQuery;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
class SearchProfileSpec {

    @Inject
    EmbeddedServer embeddedServer;

    @Test
    void test() {
        String url = "http://" + embeddedServer.getHost() + ":" + embeddedServer.getPort() + "/graphql";

        ApolloClient apolloClient = new ApolloClient.Builder()
                .serverUrl(url)
                .build();

        SearchProfileQuery searchProfileQuery =
                new SearchProfileQuery("a", Optional.Companion.presentIfNotNull(null));

        ApolloResponse<SearchProfileQuery.Data> data =
                Rx3Apollo.single(apolloClient.query(searchProfileQuery)).blockingGet();

        assertNotNull(data);
    }

}
