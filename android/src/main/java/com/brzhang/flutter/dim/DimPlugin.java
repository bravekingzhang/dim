package com.brzhang.flutter.dim;

import android.os.Environment;
import android.util.Log;

import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMConnListener;
import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMImageElem;
import com.tencent.imsdk.TIMLogLevel;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageListener;
import com.tencent.imsdk.TIMRefreshListener;
import com.tencent.imsdk.TIMSdkConfig;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.TIMUserConfig;
import com.tencent.imsdk.TIMUserStatusListener;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.imsdk.ext.message.TIMConversationExt;
import com.tencent.imsdk.ext.message.TIMManagerExt;

import java.util.List;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * DimPlugin
 */
public class DimPlugin implements MethodCallHandler, EventChannel.StreamHandler {
    private static final String TAG = "DimPlugin";
    private Registrar registrar;
    private EventChannel.EventSink eventSink;

    public DimPlugin(Registrar registrar) {
        this.registrar = registrar;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel =
                new MethodChannel(registrar.messenger(), "dim_method");
        final EventChannel eventChannel =
                new EventChannel(registrar.messenger(), "dim_event");
        final DimPlugin dimPlugin =
                new DimPlugin(registrar);
        channel.setMethodCallHandler(dimPlugin);
        eventChannel.setStreamHandler(dimPlugin);

    }

    @Override
    public void onMethodCall(MethodCall call, final Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("im_login")) {
            int appid = call.argument("sdkAppId");
            String identifier = call.argument("identifier");
            String userSig = call.argument("userSig");
            //初始化 SDK 基本配置
            TIMSdkConfig config = new TIMSdkConfig(appid)
                    .enableCrashReport(false)
                    .enableLogPrint(true)
                    .setLogLevel(TIMLogLevel.DEBUG)
                    .setLogPath(Environment.getExternalStorageDirectory().getPath() + "/justfortest/");
            //初始化 SDK
            TIMManager.getInstance().init(registrar.context(), config);
            //将用户配置与通讯管理器进行绑定
            TIMUserConfig userConfig = new TIMUserConfig()
                    //设置群组资料拉取字段
                    //设置资料关系链拉取字段
                    //设置用户状态变更事件监听器
                    .setUserStatusListener(new TIMUserStatusListener() {
                        @Override
                        public void onForceOffline() {
                            //被其他终端踢下线
                            Log.i(TAG, "onForceOffline");
                        }

                        @Override
                        public void onUserSigExpired() {
                            //用户签名过期了，需要刷新 userSig 重新登录 SDK
                            Log.i(TAG, "onUserSigExpired");
                        }
                    })//设置连接状态事件监听器
                    .setConnectionListener(new TIMConnListener() {
                        @Override
                        public void onConnected() {
                            Log.i(TAG, "onConnected");
                        }

                        @Override
                        public void onDisconnected(int code, String desc) {
                            Log.i(TAG, "onDisconnected");
                        }

                        @Override
                        public void onWifiNeedAuth(String name) {
                            Log.i(TAG, "onWifiNeedAuth");
                        }
                    })//设置会话刷新监听器
                    .setRefreshListener(new TIMRefreshListener() {
                        @Override
                        public void onRefresh() {
                            Log.i(TAG, "onRefresh");
                        }

                        @Override
                        public void onRefreshConversation(List<TIMConversation> conversations) {
                            Log.i(TAG, "onRefreshConversation, conversation size: " + conversations.size());
                        }
                    });
            TIMManager.getInstance().setUserConfig(userConfig);

            TIMManager.getInstance().addMessageListener(new TIMMessageListener() {
                @Override
                public boolean onNewMessages(List<TIMMessage> list) {
                    eventSink.success(list);
                    return false;
                }
            });


            // identifier为用户名，userSig 为用户登录凭证
            TIMManager.getInstance().login(identifier, userSig, new TIMCallBack() {
                @Override
                public void onError(int code, String desc) {
                    //错误码 code 和错误描述 desc，可用于定位请求失败原因
                    //错误码 code 列表请参见错误码表
                    Log.d(TAG, "login failed. code: " + code + " errmsg: " + desc);
                    result.error("login failed. code", code + "", desc);
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "login succ");
                    result.success("login succ");
                }
            });
        } else if (call.method.equals("sdkLogout")) {
            //登出
            TIMManager.getInstance().logout(new TIMCallBack() {
                @Override
                public void onError(int code, String desc) {

                    //错误码 code 和错误描述 desc，可用于定位请求失败原因
                    //错误码 code 列表请参见错误码表
                    Log.d(TAG, "logout failed. code: " + code + " errmsg: " + desc);
                    result.error("logout failed. code", code + "", desc);
                }

                @Override
                public void onSuccess() {
                    //登出成功
                    result.success("logout success");
                }
            });
        } else if (call.method.equals("getConversations")) {
            List<TIMConversation> list = TIMManagerExt.getInstance().getConversationList();
            result.success(list);
        } else if (call.method.equals("delConversation")) {
            String identifier = call.argument("identifier");
            TIMManagerExt.getInstance().deleteConversation(TIMConversationType.C2C, identifier);
            result.success("delConversation success");
        } else if (call.method.equals("getMessages")) {
            String identifier = call.argument("identifier");
            TIMMessage lastMsg = call.argument("lastMsg");
            //获取会话扩展实例
            TIMConversation con = TIMManager.getInstance().getConversation(TIMConversationType.C2C, identifier);
            TIMConversationExt conExt = new TIMConversationExt(con);

//获取此会话的消息
            conExt.getMessage(10, //获取此会话最近的 10 条消息
                    lastMsg, //不指定从哪条消息开始获取 - 等同于从最新的消息开始往前
                    new TIMValueCallBack<List<TIMMessage>>() {//回调接口
                        @Override
                        public void onError(int code, String desc) {//获取消息失败
                            //接口返回了错误码 code 和错误描述 desc，可用于定位请求失败原因
                            //错误码 code 含义请参见错误码表
                            Log.d(TAG, "get message failed. code: " + code + " errmsg: " + desc);
                        }

                        @Override
                        public void onSuccess(List<TIMMessage> msgs) {//获取消息成功
                            //遍历取得的消息
                            result.success(msgs);
                        }
                    });
        } else if (call.method.equals("sendTextMessages")) {
            String identifier = call.argument("identifier");
            String content = call.argument("content");
            TIMMessage msg = new TIMMessage();

            //添加文本内容
            TIMTextElem elem = new TIMTextElem();
            elem.setText(content);

            //将elem添加到消息
            if (msg.addElement(elem) != 0) {
                Log.d(TAG, "addElement failed");
                return;
            }
            TIMConversation conversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, identifier);
            //发送消息
            conversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {//发送消息回调
                @Override
                public void onError(int code, String desc) {//发送消息失败
                    //错误码 code 和错误描述 desc，可用于定位请求失败原因
                    //错误码 code 含义请参见错误码表
                    Log.d(TAG, "send message failed. code: " + code + " errmsg: " + desc);
                    result.error("send message failed. code: ", desc, code);
                }

                @Override
                public void onSuccess(TIMMessage msg) {//发送消息成功
                    Log.e(TAG, "SendMsg ok");
                    result.success(msg);
                }
            });
        } else if (call.method.equals("sendImageMessages")) {
            String identifier = call.argument("identifier");
            String iamgePath = call.argument("image_path");
            //构造一条消息
            TIMMessage msg = new TIMMessage();

//添加图片
            TIMImageElem elem = new TIMImageElem();
//            elem.setPath(Environment.getExternalStorageDirectory() + "/DCIM/Camera/1.jpg");
            elem.setPath(iamgePath);
//将 elem 添加到消息
            if (msg.addElement(elem) != 0) {
                Log.d(TAG, "addElement failed");
                return;
            }
            TIMConversation conversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, identifier);
//发送消息
            conversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {//发送消息回调
                @Override
                public void onError(int code, String desc) {//发送消息失败
                    //错误码 code 和错误描述 desc，可用于定位请求失败原因
                    //错误码 code 列表请参见错误码表
                    Log.d(TAG, "send message failed. code: " + code + " errmsg: " + desc);
                    result.error("send message failed. code: ", desc, code);
                }

                @Override
                public void onSuccess(TIMMessage msg) {//发送消息成功
                    Log.e(TAG, "SendMsg ok");
                    result.success("SendMsg ok");
                }
            });
        } else if (call.method.equals("post_data_test")) {
            Log.e(TAG, "onMethodCall() called with: call = [" + call + "], result = [" + result + "]");
            eventSink.success("hahahahha  I am from listener");
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        this.eventSink = eventSink;
    }

    @Override
    public void onCancel(Object o) {
        Log.e(TAG, "onCancel() called with: o = [" + o + "]");
    }
}
