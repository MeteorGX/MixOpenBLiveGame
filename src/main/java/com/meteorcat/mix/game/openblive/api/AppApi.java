package com.meteorcat.mix.game.openblive.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteorcat.mix.game.openblive.config.OpenBLiveConfig;
import com.meteorcat.mix.game.openblive.core.utils.CurlRedirect;
import com.meteorcat.mix.game.openblive.core.utils.JsonRespond;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.TreeMap;

/**
 * 挂载哔哩哔哩直播的API接口
 * @see <a href="https://open-live.bilibili.com/document/eba8e2e1-847d-e908-2e5c-7a1ec7d9266f">Api列表</a>
 * @author MeteorCat
 */
@RestController
@RequestMapping("/app")
public class AppApi extends BaseApi {

    /**
     * 开放平台配置
     */
    @Autowired
    OpenBLiveConfig config;


    /**
     * JSON解析器
     */
    ObjectMapper mapper = new ObjectMapper();


    /**
     * 创建游戏房间: /v2/app/start
     * @param code [主播身份码]
     * @return JSON
     */
    @RequestMapping("/create")
    public Object create(@RequestParam(name = "code",defaultValue = "") String code){
        // 要求提供身份码
        if(code.isBlank()){
            return JsonRespond.of(HttpStatus.NOT_FOUND,"参数错误(Code)");
        }

        // 转发哔哩哔哩网关请求
        Long id = config.getAppId();
        Map<String,Object> body = new TreeMap<>(){{
            put("code",code);
            put("app_id",id);
        }};

        // 解析参数
        String bodyJson;
        try{
            bodyJson = mapper.writeValueAsString(body);
        }catch (JsonProcessingException e){
            e.printStackTrace();
            return JsonRespond.of(HttpStatus.INTERNAL_SERVER_ERROR,"服务器错误(Json)");
        }
        if(bodyJson.isBlank()){
            return JsonRespond.of(HttpStatus.INTERNAL_SERVER_ERROR,"服务器错误(Json)");
        }

        // 响应参数
        final String key = config.getAppKey();
        final String secret = config.getAppSecret();
        final String url = config.getApiUrl();
        final String path = "/v2/app/start";
        final String address = String.format("%s%s",url,path);
        CurlRedirect response = redirect(key,secret,address,bodyJson);
        if(!response.getStatus().equals(HttpStatus.OK)){
            return JsonRespond.of(HttpStatus.INTERNAL_SERVER_ERROR,"服务器错误(Origin)");
        }

        // 将转发过来的数据重新打包给前端
        try{
            JsonNode node = mapper.readTree(response.getBody());
            return JsonRespond.of(HttpStatus.OK,"操作完成",node);
        }catch (JsonProcessingException exception){
            return JsonRespond.of(HttpStatus.INTERNAL_SERVER_ERROR,"服务器错误(Parse)");
        }
    }


    /**
     * 删除游戏房间: /v2/app/end
     * @param gameId 场次id
     * @return JSON
     */
    @RequestMapping("/remove")
    public Object remove(@RequestParam(name = "game_id",defaultValue = "") String gameId){
        // 要求提供身份码
        if(gameId.isBlank()){
            return JsonRespond.of(HttpStatus.NOT_FOUND,"参数错误(Param)");
        }

        // 转发哔哩哔哩网关请求
        Long id = config.getAppId();
        Map<String,Object> body = new TreeMap<>(){{
            put("app_id",id);
            put("game_id",gameId);
        }};


        // 解析参数
        String bodyJson;
        try{
            bodyJson = mapper.writeValueAsString(body);
        }catch (JsonProcessingException e){
            e.printStackTrace();
            return JsonRespond.of(HttpStatus.INTERNAL_SERVER_ERROR,"服务器错误(Json)");
        }
        if(bodyJson.isBlank()){
            return JsonRespond.of(HttpStatus.INTERNAL_SERVER_ERROR,"服务器错误(Json)");
        }


        // 响应参数
        final String key = config.getAppKey();
        final String secret = config.getAppSecret();
        final String url = config.getApiUrl();
        final String path = "/v2/app/end";
        final String address = String.format("%s%s",url,path);
        CurlRedirect response = redirect(key,secret,address,bodyJson);
        if(!response.getStatus().equals(HttpStatus.OK)){
            return JsonRespond.of(HttpStatus.INTERNAL_SERVER_ERROR,"服务器错误(Origin)");
        }

        // 将转发过来的数据重新打包给前端
        try{
            JsonNode node = mapper.readTree(response.getBody());
            return JsonRespond.of(HttpStatus.OK,"操作完成",node);
        }catch (JsonProcessingException exception){
            return JsonRespond.of(HttpStatus.INTERNAL_SERVER_ERROR,"服务器错误(Parse)");
        }
    }


}
