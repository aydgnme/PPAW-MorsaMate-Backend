package me.aydgn.MorseMate.controller;

import lombok.RequiredArgsConstructor;
import me.aydgn.MorseMate.dto.response.ApiDocumentation;
import me.aydgn.MorseMate.service.ApiDocumentationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/docs")
@RequiredArgsConstructor
public class ApiDocsViewController {

    private final ApiDocumentationService apiDocumentationService;
    @Value("${springdoc.api-docs.path:/v3/api-docs}")
    private String openApiDocsPath;

    @GetMapping
    public String docsPage(Model model) {
        ApiDocumentation documentation = apiDocumentationService.getApiDocumentation();
        model.addAttribute("documentation", documentation);
        model.addAttribute("openApiDocsPath", openApiDocsPath);
        return "api/docs";
    }
}
