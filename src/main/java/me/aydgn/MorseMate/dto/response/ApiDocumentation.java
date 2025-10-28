package me.aydgn.MorseMate.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiDocumentation {
    private String title;
    private String version;
    private String description;
    private String baseUrl;
    private List<EndpointGroup> endpointGroups;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EndpointGroup {
        private String name;
        private String description;
        private List<Endpoint> endpoints;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Endpoint {
        private String method;
        private String path;
        private String summary;
        private String description;
        private boolean requiresAuth;
        private List<String> requiredRoles;
        private List<Parameter> parameters;
        private RequestBody requestBody;
        private Map<String, Response> responses;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Parameter {
        private String name;
        private String in; // path, query, header
        private String type;
        private boolean required;
        private String description;
        private String example;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RequestBody {
        private String contentType;
        private String schema;
        private String example;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String description;
        private String example;
    }
}
