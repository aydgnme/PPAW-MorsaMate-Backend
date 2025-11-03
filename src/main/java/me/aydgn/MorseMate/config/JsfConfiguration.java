package me.aydgn.MorseMate.config;

import jakarta.faces.webapp.FacesServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ServletContextAware;

import jakarta.servlet.ServletContext;

/**
 * JSF Configuration for Spring Boot.
 * Registers FacesServlet and configures JSF.
 */
@Configuration
public class JsfConfiguration implements ServletContextAware {

    @Bean
    public ServletRegistrationBean<FacesServlet> facesServletRegistration() {
        ServletRegistrationBean<FacesServlet> registration = new ServletRegistrationBean<>(
                new FacesServlet(), "*.xhtml");
        registration.setName("Faces Servlet");
        registration.setLoadOnStartup(1);
        return registration;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        // JSF Parameters
        servletContext.setInitParameter("jakarta.faces.PROJECT_STAGE", "Development");
        servletContext.setInitParameter("jakarta.faces.FACELETS_REFRESH_PERIOD", "0");
        servletContext.setInitParameter("primefaces.THEME", "admin");
        servletContext.setInitParameter("primefaces.FONT_AWESOME", "true");
        servletContext.setInitParameter("primefaces.MOVE_SCRIPTS_TO_BOTTOM", "true");
        servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration", "true");
    }
}
