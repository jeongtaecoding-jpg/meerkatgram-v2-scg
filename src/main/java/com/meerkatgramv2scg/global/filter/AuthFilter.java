package com.meerkatgramv2scg.global.filter;

import com.meerkatgramv2scg.global.jwt.JwtConfig;
import com.meerkatgramv2scg.global.jwt.JwtProvider;
import com.meerkatgramv2scg.global.response.GlobalRes;
import com.meerkatgramv2scg.global.response.constant.CustomResponseCode;
import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

/**
 * SCG 전역에서 사용될 인증 처리 필터
 *   - 모든 라우팅 요청에 대해서 JWT검증 및 사용자 식별 헤더를 하위 서비스로 전파시키는 기능
 */
@Component
@RequiredArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {

    private final JwtProvider jwtProvider;
    private final JwtConfig jwtConfig;
    private final ObjectMapper objectMapper;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        try {
        // HTTP Header에서 Access Token 추출
        Optional<String> optionalToken = jwtProvider.extractAccessToken(exchange);

        // Access Token이 없을 경우, 인증 과정을 건너뛰고 다음 필터로 진행
        if (optionalToken.isEmpty()) {
            return chain.filter(exchange);
        }

        // JWT 파싱 및 검증
            Claims claims = jwtProvider.extractClaims(optionalToken.get());

        // 하위서비스로 전달한 Http Header 셋팅
            ServerHttpRequest serverRequest = exchange.getRequest().mutate()
                .headers(httpHeaders -> httpHeaders.remove(jwtConfig.headerKey()))  // 불필요해진 토큰 외부 노출을 막기위해 제거
                .header("X-User-Id", claims.getSubject())  // 유저 ID 셋팅
                .header("X-User-Role", claims.get("role", String.class))
                .build();

        return chain.filter(exchange.mutate().request(serverRequest).build());

        } catch (Exception e) {
            return this.unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(CustomResponseCode.INVALID_TOKEN_ERROR.getHttpStatus());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes = objectMapper.writeValueAsBytes(GlobalRes.from(CustomResponseCode.INVALID_TOKEN_ERROR));
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    /**
     * 필터의 실행 순서 결정
     *   - Gateway의 기본 라우팅(0)보다 먼저 실행되어야 하므로 -1을 설정
     * @return
     */

    @Override
    public int getOrder() {
        return -1;
    }
}
