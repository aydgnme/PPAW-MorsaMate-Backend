package me.aydgn.MorseMate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.ApiDocumentation;
import me.aydgn.MorseMate.service.ApiDocumentationService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api-docs")
@RequiredArgsConstructor
@Slf4j
public class ApiDocumentationController {

    private final ApiDocumentationService apiDocumentationService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiDocumentation> getApiDocumentation() {
        log.info("Fetching API documentation");
        ApiDocumentation documentation = apiDocumentationService.getApiDocumentation();
        return ResponseEntity.ok(documentation);
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getApiDocumentationPage() throws IOException {
        log.info("Serving API documentation UI");
        ClassPathResource resource = new ClassPathResource("static/api-docs.html");
        String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return ResponseEntity.ok(html);
    }
}
