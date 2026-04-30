package ru.practicum.ewm.filter;

import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.*;

@Component
public class BadQueryParamFilter implements Filter {

    private static final Set<String> PARAMS_TO_FILTER = Set.of("rangeStart", "rangeEnd");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        boolean needsFiltering = PARAMS_TO_FILTER.stream()
                .anyMatch(paramName -> httpRequest.getParameterMap().containsKey(paramName));

        if (needsFiltering) {
            chain.doFilter(new CustomRequestWrapper(httpRequest), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private static class CustomRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String[]> modifiedParams;

        public CustomRequestWrapper(HttpServletRequest request) {
            super(request);
            this.modifiedParams = new HashMap<>(request.getParameterMap());

            PARAMS_TO_FILTER.forEach(modifiedParams::remove);
        }

        @Override
        public String getParameter(String name) {
            String[] values = modifiedParams.get(name);
            return values != null && values.length > 0 ? values[0] : null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return Collections.unmodifiableMap(modifiedParams);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(modifiedParams.keySet());
        }

        @Override
        public String[] getParameterValues(String name) {
            return modifiedParams.get(name);
        }
    }
}