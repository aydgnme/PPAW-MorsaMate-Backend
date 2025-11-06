package me.aydgn.MorseMate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.ApiDocumentation;
import me.aydgn.MorseMate.service.ApiDocumentationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
