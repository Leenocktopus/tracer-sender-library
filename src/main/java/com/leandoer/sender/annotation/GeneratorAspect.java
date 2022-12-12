package com.leandoer.sender.annotation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leandoer.sender.TraceRequest;
import com.leandoer.sender.TracerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class GeneratorAspect {

    private final RestTemplate restTemplate;
    private final TracerConfig tracerConfig;
    private final ObjectMapper objectMapper;

    @Around("@annotation(generator)")
    private Object processRngOutput(ProceedingJoinPoint joinPoint, Generator generator) throws Throwable {
        Object returnValue = joinPoint.proceed();
        checkApplicationPresent();
        validateInput(returnValue, generator.type());
        sendTrace(generator, returnValue);
        return returnValue;
    }

    private void checkApplicationPresent() {
        if (tracerConfig.getApplication() == null || tracerConfig.getApplication().isEmpty() || tracerConfig.getApplication().isBlank()) {
            throw new RuntimeException("Please provide tracer.config.application parameter in the .properties or .yaml file");
        }
    }

    private void validateInput(Object returnValue, PayloadType payloadType) {
        if ((returnValue instanceof String) && payloadType == PayloadType.BINARY_STRING) {
            if (returnValue.toString().length() < 3) {
                throw new RuntimeException("Return type of @Generator annotated method with PayloadType.BINARY_STRING should have length more than 3");
            }
        } else if (((returnValue instanceof Long) || (returnValue instanceof Integer)) && payloadType == PayloadType.NUMBER) {
            if (Math.abs((long) returnValue) < 4L) {
                throw new RuntimeException("Return type of @Generator annotated method with PayloadType.NUMBER should be more than 4");
            }
        } else {
            throw new RuntimeException("Return type of @Generator annotated method should be either Long, Integer or String and correspond to the `type` annotation variable");
        }
    }

    private void sendTrace(Generator generator, Object returnValue) throws JsonProcessingException {
        TraceRequest traceRequest = TraceRequest.builder()
                .value(returnValue.toString())
                .application(tracerConfig.getApplication().trim())
                .type(generator.type())
                .labels(Arrays.asList(Arrays.stream(generator.labels()).map(String::trim).toArray(String[]::new)))
                .generatedAt(now())
                .build();
        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(traceRequest), getHttpHeaders());
        String url = getUrl();
        restTemplate.postForObject(url, request, Void.class);
        log.info(format("Trace with value `%s` has been sent to tracer-app on %s", returnValue, url));
    }


    private String getUrl() {
        return format("%s%s", tracerConfig.getHost(), tracerConfig.getPath());
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(APPLICATION_JSON);
        return httpHeaders;
    }

}
