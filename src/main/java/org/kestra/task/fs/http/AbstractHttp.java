package org.kestra.task.fs.http;

import io.micronaut.http.*;
import io.micronaut.http.client.DefaultHttpClient;
import io.micronaut.http.client.DefaultHttpClientConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.kestra.core.exceptions.IllegalVariableEvaluationException;
import org.kestra.core.models.annotations.PluginProperty;
import org.kestra.core.models.tasks.Task;
import org.kestra.core.runners.RunContext;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
abstract public class AbstractHttp extends Task {
    @Schema(
        title = "The fully-qualified URIs that point to destination http server"
    )
    @PluginProperty(dynamic = true)
    protected String uri;

    @Schema(
        title = "The http method to use"
    )
    @Builder.Default
    protected HttpMethod method = HttpMethod.GET;

    @Schema(
        title = "The full body as string"
    )
    @PluginProperty(dynamic = true)
    protected String body;

    @Schema(
        title = "The form data to be send"
    )
    protected Map<String, Object> formData;


    @Schema(
        title = "The request content type"
    )
    @PluginProperty(dynamic = true)
    protected String contentType;

    @Schema(
        title = "The header to pass to current request"
    )
    protected Map<CharSequence, CharSequence> headers;

    protected DefaultHttpClientConfiguration configuration() {
        // @TODO
        DefaultHttpClientConfiguration configuration = new DefaultHttpClientConfiguration();

        // configuration.setSslConfiguration(new SslConfiguration());

        return configuration;
    }

    protected DefaultHttpClient client(RunContext runContext) throws IllegalVariableEvaluationException, MalformedURLException, URISyntaxException {
        URI from = new URI(runContext.render(this.uri));

        return new DefaultHttpClient(from.toURL(), this.configuration());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected HttpRequest request(RunContext runContext) throws IllegalVariableEvaluationException, URISyntaxException {
        URI from = new URI(runContext.render(this.uri));

        MutableHttpRequest request = HttpRequest
            .create(method, from.toString());

        if (this.formData != null) {
            request.contentType(MediaType.APPLICATION_FORM_URLENCODED);
            request.body(runContext.render(this.formData));
        } else if (this.body != null) {
            request.body(runContext.render(body));
        }

        if (this.contentType != null) {
            request.contentType(runContext.render(this.contentType));
        }

        if (this.headers != null) {
            request.headers(this.headers);
        }

        return request;
    }

    protected String[] tags(HttpRequest<String> request, HttpResponse<String> response) {
        ArrayList<String> tags = new ArrayList<>(
            Arrays.asList("request.method", request.getMethodName())
        );

        if (response != null) {
            tags.addAll(
                Arrays.asList("response.code", String.valueOf(response.getStatus().getCode()))
            );
        }

        return tags.toArray(String[]::new);
    }
}
