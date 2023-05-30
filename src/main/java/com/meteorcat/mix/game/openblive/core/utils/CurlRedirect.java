package com.meteorcat.mix.game.openblive.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.support.WebApplicationObjectSupport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodType;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * 服务器Http转发到指定服务器请求
 * @author MeteorCat
 */
public class CurlRedirect {

    /**
     * 返回信息
     */
    private String body = "";

    /**
     * 返回状态
     */
    private HttpStatus status = HttpStatus.BAD_GATEWAY;



    /**
     * 获取消息主体
     * @return String
     */
    public String getBody() {
        return body;
    }

    /**
     * 获取转发状态
     * @return HttpStatus
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * 构建返回数据
     * @param builder 构建器
     */
    public CurlRedirect(Builder builder){
        this.body = builder.body;
        this.status = builder.status;
    }

    @Override
    public String toString() {
        return "CurlRedirect{" +
                "body='" + body + '\'' +
                ", status=" + status +
                '}';
    }



    /**
     * 请求构建器
     */
    public static class Builder extends WebApplicationObjectSupport {

        /**
         * 请求URL
         */
        private String url;


        /**
         * 默认的请求方式: GET
         */
        private HttpMethod httpMethod = HttpMethod.GET;

        /**
         * 提交参数
         */
        private HashMap<String,Object> params;


        /**
         * 头信息
         */
        private HashMap<String,Object> headers;


        /**
         * 请求实例
         */
        private RestTemplate restTemplate;


        /**
         * 请求头对象
         */
        private HttpHeaders httpHeaders;


        /**
         * 返回信息
         */
        private String body = "";


        /**
         * 返回状态
         */
        private HttpStatus status = HttpStatus.BAD_GATEWAY;



        /**
         * 构建请求
         * @param url 请求链接
         */
        public Builder(String url){
            this.url = url;
        }


        /**
         * 追加参数
         * @param key 键
         * @param value 值
         * @return Builder
         */
        public Builder pushParam(String key,Object value){
            if(params == null){
                params = new LinkedHashMap<>(16);
            }
            params.put(key,value);
            return this;
        }


        /**
         * 追加参数
         * @param param Map<String,Object>
         * @return Builder
         */
        public Builder pushParams(Map<String,Object> param){
            if(params == null){
                params = new LinkedHashMap<>(16);
            }
            params.putAll(param);
            return this;
        }


        /**
         * 追加头
         * @param key 键
         * @param value 值
         * @return Builder
         */
        public Builder pushHeader(String key,String value){
            if(headers == null){
                headers = new LinkedHashMap<>(16);
            }
            headers.put(key,value);
            return this;
        }

        /**
         * 追加头
         * @param header 数据头
         * @return Builder
         */
        public Builder pushHeaders(Map<String,Object> header){
            if(headers == null){
                headers = new LinkedHashMap<>(16);
            }
            headers.putAll(header);
            return this;
        }



        /**
         * 获取构建的URL参数
         * @param param 参数
         * @return String
         */
        private String getUrlParam(Map<String,Object> param){
            List<String> keys = new ArrayList<>(param.keySet());
            Collections.sort(keys);
            StringBuilder urls = new StringBuilder();
            for(int i = 0; i< keys.size(); i++){
                String key = keys.get(i);
                String value = (String) params.get(key);
                value = URLEncoder.encode(value, StandardCharsets.UTF_8);
                if (i == keys.size() - 1){
                    urls.append(key).append("=").append(value);
                }else{
                    urls.append(key).append("=").append(value).append("&");
                }
            }
            return urls.toString();
        }


        /**
         * 将参数转化为HTTP支持的容器
         * @return MultiValueMap<String,Object>
         */
        private MultiValueMap<String,Object> setHttpMethodParams(){
            MultiValueMap<String,Object> params = new LinkedMultiValueMap<>();
            switch (this.httpMethod){
                case GET:
                    if(this.params != null){
                        String urls = getUrlParam(this.params);
                        this.url = this.url + "?" + urls;
                    }
                    break;
                case POST:
                    if(this.params != null){
                        // FORM提交
                        this.params.forEach((k,v)->{
                            params.put(k, Collections.singletonList(v));
                        });
                    }
                    break;
                default:break;
            }
            return params;
        }



        /**
         * 获取响应对象内容
         * @param type 头信息
         */
        public void getResponse(MediaType type){
            // 构建实例
            restTemplate = new RestTemplate();
            restTemplate.setErrorHandler(new RestErrorHandler());

            // 构建头
            httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(type);

            // 构建参数
            MultiValueMap<String,Object> params = this.setHttpMethodParams();
            responseExchange(params);
        }



        /**
         * 解码GZIP
         * @param stream 输入流
         * @return Byte[]
         */
        private byte[] unGZIP(InputStream stream){
            byte[] bytes = null;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try{
                try(GZIPInputStream gzipInputStream = new GZIPInputStream(stream)){
                    byte[] buffer = new byte[4096];
                    int len = -1;
                    while ((len = gzipInputStream.read(buffer,0,buffer.length)) != -1){
                        byteArrayOutputStream.write(buffer,0,len);
                    }
                    bytes = byteArrayOutputStream.toByteArray();
                }finally {
                    byteArrayOutputStream.close();
                };
            }catch (IOException e){
                logger.error(e);
            }
            return bytes;
        }




        /**
         * 执行请求
         * @param params 参数对象
         */
        private void responseExchange(MultiValueMap<String,Object> params){
            if(headers != null){
                for (Map.Entry<String,Object> header :headers.entrySet()) {
                    httpHeaders.set(header.getKey(),header.getValue().toString());
                }
            }

            // 构建参数对象
            HttpEntity<MultiValueMap<String,Object>> entity = new HttpEntity<>(params,httpHeaders);

            // 发起请求
            ResponseEntity<byte[]> response = this.restTemplate.exchange(this.url,this.httpMethod,entity,byte[].class);
            String encoding = response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
            this.status = response.getStatusCode();
            if(response.getBody() == null){
                return ;
            }
            // 处理响应
            final String gzipName = "gzip";
            if(gzipName.equals(encoding)){
                byte[] bytes = unGZIP(new ByteArrayInputStream(response.getBody()));
                this.body = new String(bytes);
            }else{
                this.body = new String(response.getBody());
            }
        }


        /**
         * 执行请求
         * @param json JSON字符串对象
         */
        private void responseExchange(String json){
            if(headers != null){
                for (Map.Entry<String,Object> header :headers.entrySet()) {
                    httpHeaders.set(header.getKey(),header.getValue().toString());
                }
            }

            // 构建参数对象
            HttpEntity<String> entity = new HttpEntity<>(json,httpHeaders);

            // 发起请求
            ResponseEntity<byte[]> response = this.restTemplate.exchange(this.url,this.httpMethod,entity,byte[].class);
            String encoding = response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
            this.status = response.getStatusCode();
            if(response.getBody() == null){
                return ;
            }
            // 处理响应
            final String gzipName = "gzip";
            if(gzipName.equals(encoding)){
                byte[] bytes = unGZIP(new ByteArrayInputStream(response.getBody()));
                this.body = new String(bytes);
            }else{
                this.body = new String(response.getBody());
            }
        }


        /**
         * GET请求
         * @return HttpRequestHandler
         */
        public CurlRedirect get(){
            this.httpMethod = HttpMethod.GET;
            this.getResponse(MediaType.APPLICATION_FORM_URLENCODED);
            return new CurlRedirect(this);
        }

        /**
         * POST请求
         * @return HttpRequestHandler
         */
        public CurlRedirect post(){
            this.httpMethod = HttpMethod.POST;
            this.getResponse(MediaType.APPLICATION_FORM_URLENCODED);
            return new CurlRedirect(this);
        }


        /**
         * 自定义请求类型
         * @param method 请求方法
         * @return CurlRedirect
         */
        public CurlRedirect json(HttpMethod method,String body){
            // 构建实例
            this.httpMethod = method;
            restTemplate = new RestTemplate();
            restTemplate.setErrorHandler(new RestErrorHandler());

            // 构建头
            httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.setAccept(new ArrayList<>(1){{add(MediaType.APPLICATION_JSON);}});

            // 构建参数请求
            responseExchange(body);
            return new CurlRedirect(this);
        }

    }





    /**
     * 错误回调错误
     */
    public static class RestErrorHandler implements ResponseErrorHandler {


        /**
         * 确认是否为错误请求
         * @param clientHttpResponse 远程响应
         * @return boolean
         * @throws IOException 异常
         */
        @Override
        public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
            int status = clientHttpResponse.getRawStatusCode();
            HttpStatus httpStatus = HttpStatus.resolve(status);
            return (httpStatus != null ? httpStatus.isError() : hasError(status) );
        }


        /**
         * 确认状态码是否错误
         * @param status 状态码
         * @return boolean
         */
        protected boolean hasError(int status){
            HttpStatus.Series series = HttpStatus.Series.resolve(status);
            return (series == HttpStatus.Series.CLIENT_ERROR || series == HttpStatus.Series.SERVER_ERROR);
        }


        /**
         * 拦截自定义处理回调
         * @param clientHttpResponse 远程响应
         * @throws IOException 异常
         */
        @Override
        public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {}
    }

}
