package se.mo.xarbetemonolitisk.monitoring;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private final RequestMetricsCollector metricsCollector;

    public RequestLoggingFilter(RequestMetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.nanoTime();
        log.info("Incoming request: method={} path={}", request.getMethod(), request.getRequestURI());

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationNanos = System.nanoTime() - start;
            double durationMs = durationNanos / 1_000_000.0;
            metricsCollector.record(durationNanos);
            log.info("Completed request: method={} path={} status={} responseTimeMs={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(),
                    String.format("%.2f", durationMs));
        }
    }
}
