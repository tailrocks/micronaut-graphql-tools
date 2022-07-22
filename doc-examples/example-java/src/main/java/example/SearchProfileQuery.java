package example;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.graphql.tools.annotation.GraphQLRootResolver;

import java.util.Collections;
import java.util.List;

@GraphQLRootResolver
public class SearchProfileQuery {

    public List<Profile> searchProfile(String contains, @Nullable Integer limit) {
        return Collections.emptyList();
    }

}
