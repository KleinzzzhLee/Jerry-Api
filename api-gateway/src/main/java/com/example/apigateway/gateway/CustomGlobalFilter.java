package com.example.apigateway.gateway;


import com.api.common.model.entity.InterfaceInfo;
import com.api.common.model.entity.User;
import com.api.common.service.*;
import com.example.apiclientsdk.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> list = Arrays.asList("/127.0.0.1");

    @DubboReference
    private InnerUserService userService;

    @DubboReference
    private InnerInterfaceInfoService interfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService userInterfaceInfoService;

    /**
     *
     * @param exchange exchange对象封装了请求的所有信息，request, response, session等等
     * @param chain 网关内部的过滤器链条， 使用chain.filter(exchange) 继续向下一个请求传递
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("进入gateway的CustomGlobalFilter");
        // 通过路由转发过来 ServerWebExchange中封装了请求的所有信息

        ServerHttpRequest request = exchange.getRequest();
        log.info("请求的唯一标识" + request.getId());
        log.info("请求URI：" + request.getURI());
        log.info("请求路径：" + request.getPath().value());
        log.info("请求方法：" + request.getMethod());
        log.info("请求参数：" + request.getQueryParams());
        log.info("请求来源地址：" + request.getRemoteAddress());

        // 请求地址的来源
        InetAddress address = request.getRemoteAddress().getAddress();
        String addr = address.toString();

        // 3、 对黑白名单进行交接, 请求的发出路径

//        if(!list.contains(addr)) {
//            ServerHttpResponse response = exchange.getResponse();
//            // 设置响应的状态码
//            response.setStatusCode(HttpStatus.FORBIDDEN);
//            return  response.setComplete();
//        }

        // 4、用户鉴权 只是简单的通过了 appKey 和 appSecret
        // 这些信息保存在请求头中
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String appKey = headers.getFirst("appKey");
//        String appSecret = headers.getFirst("appSecret");
        String sign = headers.getFirst("sign");
        String timestamp = headers.getFirst("timestamp");
        String random = headers.getFirst("random");
        String body = headers.getFirst("body");

        // todo 从数据库中查询比较, 查到封装的appSecret
        User invokeUser = userService.getInvokeUser(appKey);
        if(invokeUser == null){
           return handleNoAuth(exchange.getResponse());
        }
        // todo 需要利用redis 进行查询 唯一标识是否存在
        if(random == null || Long.parseLong(random) > 1111111) {
            return handleNoAuth(exchange.getResponse());
        }
        // todo 时间和当前时间的差距不能大于5分钟
        if(timestamp == null) {
            return handleNoAuth(exchange.getResponse());
        }
        Long currentTime = System.currentTimeMillis() / 1000;
        Long sendTime = Long.parseLong(timestamp) / 1000;
        final Long FIVE_MINUTES = 60 * 5L;
        if((currentTime - sendTime) > FIVE_MINUTES) {
            return handleNoAuth(exchange.getResponse());
        }
        // todo 判断签名是否一致 从数据库中获取到 appSecret （总感觉存在问题）
        // 判断 签名是否一致，
        Map<String, String> map = new HashMap<>();
        map.put("appKey", appKey);
        map.put("body", body);
        map.put("random", random);
        map.put("timestamp", timestamp);
        String signCalculate = SignUtil.getSign(map, invokeUser.getAppSecret());
        if(sign == null || !sign.equals(signCalculate)) {
            return handleNoAuth(exchange.getResponse());
        }

        // 5、判断接口是否存在
        // todo 从数据库中查询模拟接口是否存在，以及请求类型是否匹配
        String uri = exchange.getRequest().getURI().toString();
        String method = exchange.getRequest().getMethod().toString();
        InterfaceInfo interfaceInfo = interfaceInfoService.getInterfaceInfo(uri, method);
        if(interfaceInfo == null) {
                return handleInvokeError(exchange.getResponse());
        }

        // 6、请求转发, 调用模拟接口
//        Mono<Void> filter = chain.filter(exchange);
        return handleResponse(exchange, chain, invokeUser.getId(), interfaceInfo.getId());
//        // 7、响应日志
//        log.info("调用成功" + exchange.getResponse().getStatusCode());
        // todo 判断是否还有调用次数

        // todo  8、 修改用户的调用次数， 远程调用实现
        /**
         * - 1、用户向接口服务器发出请求，接口服务器收到后，直接将请求发送给网关
         * - 2、记录请求日志
         * - 3、对用户进行黑白名单的校验
         * - 4、用户鉴权
         *   - 网关进行API签名认证，判断本次请求是否安全
         *   - 判断用户剩余的请求次数是否足够
         * - 5、判断该接口是否存在
         *   - 判断该请求参数是否传输足够
         * - **6、请求转发，调用模拟接口**
         * - 7、记录相应日志
         * - 8、调用成功，修改用户的剩余调用次数
         * - 9、调用失败，返回一个规范的错误码
         */
        // 9、 调用失败，返回一个规范的错误码
        // 转入handleResponse
//        if(exchange.getResponse().getStatusCode() != HttpStatus.OK) {
//            return handleInvokeError(exchange.getResponse());
//        }
    }

    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain,
                                     Long userId, Long interfaceInfoId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();

            HttpStatus statusCode = originalResponse.getStatusCode();

            if(statusCode == HttpStatus.OK){
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        //log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            //
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                // todo 8、 调用成功。调用次数 - 1
                                try {
                                    userInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                } catch (Exception e) {
                                    log.error("invoke error");
                                }

                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                // 构建日志
                                StringBuilder sb2 = new StringBuilder(200);
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                String data = new String(content, StandardCharsets.UTF_8);//data
                                sb2.append(data);

                                log.info("响应结果"  + data);//log.info("<-- {} {}\n", originalResponse.getStatusCode(), data);

                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            // 8、响应失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange);//降级处理返回数据
        }catch (Exception e){
            log.error("网关处理异常.\n" + e);
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * 这是发生异常的处理，这里没有用全局异常处理器
     */
    Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
}
