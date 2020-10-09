package com.websocketdemo.demo.WebSocket;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;


@Component
@ServerEndpoint(value = "/WebSocketLink/{userinfo}")
public class WebSocketServer {
    private static int onlineCount = 0;
    private static ConcurrentHashMap<String,UserEntity> root = new ConcurrentHashMap<>();
    private UserEntity userEntity;

    /**
     *
     * 和前端进行连接
     *
     */
    @OnOpen
    public void onOpen(@PathParam("userinfo")String userinfo,Session session) throws IOException, EncodeException {
        String decodedUserinfo = decodeBase64(userinfo);
        JSONObject job = JSONObject.parseObject(decodedUserinfo);
        String userId = job.getString("userId");
        String nickname = job.getString("nickname");
        String avatar = job.getString("avatar");
        UserEntity userEntity = new UserEntity(userId, avatar, nickname, session);
        this.userEntity = userEntity;
        root.put(userId,userEntity);
        addOnlineCount();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type",0);//type==0,表示谁进来了
        jsonObject.put("userId",userId);
        jsonObject.put("nickname",nickname);
        jsonObject.put("avatar",avatar);
        //告诉前端谁进入会话了
        for (String key:root.keySet()){
            if (!key.equals(userId)) {
                root.get(key).getSession().getBasicRemote().sendText(jsonObject.toString());//向其他人推送谁进来了（除了自己）
            }
        }
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("type",-1);
        JSONArray jarr = new JSONArray();
        for (String key:root.keySet()){
            jarr.add(root.get(key).getUserinfo());//将所有进来的用户放在数组中
        }
        jsonObject1.put("onlineUsers",jarr);
        session.getBasicRemote().sendText(jsonObject1.toString());//向自己推送谁进来了

    }

    /**
     *
     * 接受前端的消息然后处理发送方式（群发/私聊）
     *
     */
    @OnMessage
    public void onMessage(String message) throws IOException {
        JSONObject job = JSONObject.parseObject(message);
        int type = (int) job.get("type");

        if (type == 2) {//type==2,表示群发
            for (String key : root.keySet()) {
                root.get(key).getSession().getBasicRemote().sendText(message);
            }
            return;
        }

        if (type == 3){//type==3,表示私聊
            int index = 0;
            for (String key : root.keySet()) {//toId:接受方id；fromId:发送方id
                if (key.equals(job.get("toId")) || key.equals(job.get("fromId"))) {//双方都接受消息
                    index ++;
                    root.get(key).getSession().getBasicRemote().sendText(message);
                }
                if (index == 2){//发送给双方两个人后就结束
                    return;
                }
            }
        }

    }



    @OnClose
    public void onClose() throws IOException {
        root.remove(userEntity.getUserId());
        subOnlineCount();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type",1);//type==1,表示谁出去了
        jsonObject.put("userId",userEntity.getUserId());
        jsonObject.put("nickname",userEntity.getNickname());
        jsonObject.put("avatar",userEntity.getAvatar());
        for (String key:root.keySet()){
            root.get(key).getSession().getBasicRemote().sendText(jsonObject.toString());
        }
    }

    @OnError
    public void onError(Throwable error){
        error.printStackTrace();
    }


    /**
     * Base64解密
     */
    private static String decodeBase64(String str) throws UnsupportedEncodingException {
        byte[] decoded = Base64.getDecoder().decode(str);
        String decodeStr =  new String(decoded);
        return URLDecoder.decode(decodeStr,"utf-8");
    }

    /**
     * 加锁防止并发出现数目问题
     * */
    private static synchronized int getOnlineCount() {
        return onlineCount;
    }

    private static synchronized void addOnlineCount() {
        onlineCount++;
    }

    private static synchronized void subOnlineCount() {
        onlineCount--;
    }
}
