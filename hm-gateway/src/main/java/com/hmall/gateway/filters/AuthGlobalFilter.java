package com.hmall.gateway.filters;

import cn.hutool.core.text.AntPathMatcher;
import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.util.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter {
    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    private final AntPathMatcher ant = new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        1.获取请求头
        ServerHttpRequest request = (ServerHttpRequest) exchange.getRequest();
//        2.判断是否需要登录拦截
        if (isExclude(request.getPath().toString()))
            return chain.filter(exchange);
//        3.获取 token
        String token = null;
        List<String> headers = request.getHeaders().get("authorization");
        if (headers != null && !headers.isEmpty())
            token = headers.get(0).replace("Bearer ", "");
        Long userId;
        try {
            userId = jwtTool.parseToken(token);
        } catch (UnauthorizedException e) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        ServerWebExchange newExchange = exchange.mutate()
                .request(request.mutate()
                        .header("userId", userId
                                .toString())
                        .build()).build();
        return chain.filter(newExchange);

    }


    private boolean isExclude(String Path) {
        for(String pathPattern: authProperties.getExcludePaths()){
            if (ant.match(pathPattern,Path)) {
                return true;
            }

        }
        return false;
    }
}
