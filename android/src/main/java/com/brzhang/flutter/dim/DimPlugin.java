package com.brzhang.flutter.dim;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMConnListener;
import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMElem;
import com.tencent.imsdk.TIMFriendshipManager;
import com.tencent.imsdk.TIMGroupMemberInfo;
import com.tencent.imsdk.TIMImageElem;
import com.tencent.imsdk.TIMLocationElem;
import com.tencent.imsdk.TIMLogLevel;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageListener;
import com.tencent.imsdk.TIMRefreshListener;
import com.tencent.imsdk.TIMSdkConfig;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.TIMUserConfig;
import com.tencent.imsdk.TIMUserProfile;
import com.tencent.imsdk.TIMUserStatusListener;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.imsdk.ext.message.TIMConversationExt;
import com.tencent.imsdk.ext.message.TIMManagerExt;
import com.tencent.imsdk.ext.sns.TIMAddFriendRequest;
import com.tencent.imsdk.ext.sns.TIMDelFriendType;
import com.tencent.imsdk.ext.sns.TIMFriendAddResponse;
import com.tencent.imsdk.ext.sns.TIMFriendResponseType;
import com.tencent.imsdk.ext.sns.TIMFriendResult;
import com.tencent.imsdk.ext.sns.TIMFriendshipManagerExt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    private TIMMessageListener timMessageListener;

    public DimPlugin(Registrar registrar) {
        this.registrar = registrar;
        timMessageListener = new TIMMessageListener() {
            @Override
            public boolean onNewMessages(List<TIMMessage> list) {
                if (list != null && list.size() > 0) {
                    List<Message> messages = new ArrayList<>();
                    for (TIMMessage timMessage : list) {
                        messages.add(new Message(timMessage));
                    }
                    eventSink.success(new Gson().toJson(messages, new TypeToken<Collection<Message>>() {
                    }.getType()));
                } else {
                    eventSink.success("[]");
                }
                return false;
            }
        };
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
        } else if (call.method.equals("im_logout")) {
            TIMManager.getInstance().logout(new TIMCallBack() {
                @Override
                public void onError(int code, String s) {
                    result.error(code + "", s, s);
                }

                @Override
                public void onSuccess() {
                    result.success("logout success");
                }
            });
        } else if (call.method.equals("im_login")) {
            if (!TextUtils.isEmpty(TIMManager.getInstance().getLoginUser())) {
                result.error("login failed. ", "user is login", "user is already login ,you should login out first");
                return;
            }
            int appid = call.argument("sdkAppId");
            String identifier = call.argument("identifier");
            String userSig = call.argument("userSig");
            //初始化 SDK 基本配置
            TIMSdkConfig config = new TIMSdkConfig(appid)
                    .enableCrashReport(false)
                    .enableLogPrint(true)
                    .setLogLevel(TIMLogLevel.DEBUG)
                    .setAccoutType("792")
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
            TIMManager.getInstance().removeMessageListener(timMessageListener);
            TIMManager.getInstance().addMessageListener(timMessageListener);


            // identifier为用户名，userSig 为用户登录凭证
            TIMManager.getInstance().login(identifier, userSig, new TIMCallBack() {
                @Override
                public void onError(int code, String desc) {
                    //错误码 code 和错误描述 desc，可用于定位请求失败原因
                    //错误码 code 列表请参见错误码表
                    Log.d(TAG, "login failed. code: " + code + " errmsg: " + desc);
                    result.error(code + "", desc, desc);
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
                    result.error(code + "", desc, desc);
                }

                @Override
                public void onSuccess() {
                    //登出成功
                    result.success("logout success");
                }
            });
        } else if (call.method.equals("getConversations")) {
            List<TIMConversation> list = TIMManagerExt.getInstance().getConversationList();
            if (list != null && list.size() > 0) {
                result.success(new Gson().toJson(list, new TypeToken<Collection<TIMConversation>>() {
                }.getType()));
            } else {
                result.success("[]");
            }
        } else if (call.method.equals("delConversation")) {
            String identifier = call.argument("identifier");
            TIMManagerExt.getInstance().deleteConversation(TIMConversationType.C2C, identifier);
            result.success("delConversation success");
        } else if (call.method.equals("getMessages")) {
            String identifier = call.argument("identifier");
            int count = call.argument("count");
            Log.e(TAG, "获取" + count + "条数据");
            int type = call.argument("ctype");
//            TIMMessage lastMsg = call.argument("lastMsg");
            //获取会话扩展实例
            TIMConversation con = TIMManager.getInstance().getConversation(type == 2 ? TIMConversationType.Group : TIMConversationType.C2C, identifier);
            TIMConversationExt conExt = new TIMConversationExt(con);

//获取此会话的消息
            conExt.getMessage(count, //获取此会话最近的 100 条消息
                    null, //不指定从哪条消息开始获取 - 等同于从最新的消息开始往前
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
                            if (msgs != null && msgs.size() > 0) {
                                List<Message> messages = new ArrayList<>();
                                for (TIMMessage timMessage : msgs) {
                                    messages.add(new Message(timMessage));
                                }
                                result.success(new Gson().toJson(messages, new TypeToken<Collection<Message>>() {
                                }.getType()));
                            } else {
                                result.success("[]");
                            }
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
                    result.error(code + "", desc, desc);
                }

                @Override
                public void onSuccess(TIMMessage msg) {//发送消息成功
                    Log.e(TAG, "SendMsg ok");
                    result.success("SendMsg ok");
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
                    result.error(code + "", desc, desc);
                }

                @Override
                public void onSuccess(TIMMessage msg) {//发送消息成功
                    Log.e(TAG, "SendMsg ok");
                    result.success("SendMsg ok");
                }
            });
        } else if (call.method.equals("sendLocation")) {

            String identifier = call.argument("identifier");
            double lat = call.argument("lat");
            double lng = call.argument("lng");
            String desc = call.argument("desc");

            TIMConversation conversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, identifier);
            //构造一条消息
            TIMMessage msg = new TIMMessage();

//添加位置信息
            TIMLocationElem elem = new TIMLocationElem();
            elem.setLatitude(lat);   //设置纬度
            elem.setLongitude(lng);   //设置经度
            elem.setDesc(desc);

//将elem添加到消息
            if (msg.addElement(elem) != 0) {
                Log.d(TAG, "addElement failed");
                return;
            }
//发送消息
            conversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {//发送消息回调
                @Override
                public void onError(int code, String desc) {//发送消息失败
                    //错误码 code 和错误描述 desc，可用于定位请求失败原因
                    //错误码 code 含义请参见错误码表
                    Log.d(TAG, "send message failed. code: " + code + " errmsg: " + desc);
                }

                @Override
                public void onSuccess(TIMMessage msg) {//发送消息成功
                    Log.e(TAG, "Send location ok");
                }
            });

        } else if (call.method.equals("post_data_test")) {
            Log.e(TAG, "onMethodCall() called with: call = [" + call + "], result = [" + result + "]");
            eventSink.success("hahahahha  I am from listener");
        } else if (call.method.equals("addFriend")) {
//创建请求列表
            List<TIMAddFriendRequest> reqList = new ArrayList<TIMAddFriendRequest>();
//添加好友请求
            String identifier = call.argument("identifier");
            TIMAddFriendRequest req = new TIMAddFriendRequest(identifier);
//            req.setAddrSource("DemoApp");
            req.setAddWording("请添加我");
//            req.setRemark("Cat");
            reqList.add(req);
//申请添加好友
            //申请添加好友
            TIMFriendshipManagerExt.getInstance().addFriend(reqList, new TIMValueCallBack<List<TIMFriendResult>>() {
                @Override
                public void onError(int code, String desc) {
                    //错误码 code 和错误描述 desc，可用于定位请求失败原因
                    //错误码 code 列表请参见错误码表
                    result.error(code + "", desc, desc);
                }

                @Override
                public void onSuccess(List<TIMFriendResult> timFriendResults) {
                    if (timFriendResults != null && timFriendResults.size() > 0) {
                        result.success(timFriendResults.get(0).getIdentifer());
                    }
                }
            });
        } else if (call.method.equals("delFriend")) {
            //双向删除好友 test_user
            String identifier = call.argument("identifier");
            TIMFriendshipManagerExt.DeleteFriendParam param = new TIMFriendshipManagerExt.DeleteFriendParam();
            param.setType(TIMDelFriendType.TIM_FRIEND_DEL_BOTH)
                    .setUsers(Collections.singletonList(identifier));

            TIMFriendshipManagerExt.getInstance().delFriend(param, new TIMValueCallBack<List<TIMFriendResult>>() {
                @Override
                public void onError(int code, String desc) {
                    //错误码 code 和错误描述 desc，可用于定位请求失败原因
                    //错误码 code 列表请参见错误码表
                    result.error(code + "", desc, desc);
                }

                @Override
                public void onSuccess(List<TIMFriendResult> timFriendResults) {
                    if (timFriendResults != null && timFriendResults.size() > 0) {
                        result.success(timFriendResults.get(0).getIdentifer());
                    }
                }
            });
        } else if (call.method.equals("listFriends")) {
            //获取好友列表
            TIMFriendshipManagerExt.getInstance().getFriendList(new TIMValueCallBack<List<TIMUserProfile>>() {
                @Override
                public void onError(int code, String desc) {
                    //错误码 code 和错误描述 desc，可用于定位请求失败原因
                    //错误码 code 列表请参见错误码表
                    result.error(code + "", desc, desc);
                }

                @Override
                public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                    if (timUserProfiles != null && timUserProfiles.size() > 0) {

                        result.success(new Gson().toJson(timUserProfiles, new TypeToken<Collection<TIMUserProfile>>() {
                        }.getType()));
                    } else {
                        result.success("[]");//返回一个空的json array
                    }
                }
            });
        } else if (call.method.equals("opFriend")) {//好友申请
            //获取好友列表
            String identifier = call.argument("identifier");
            String opTypeStr = call.argument("opTypeStr");
            TIMFriendAddResponse timFriendAddResponse = new TIMFriendAddResponse(identifier);
            if (opTypeStr.toUpperCase().trim().equals("Y")) {
                timFriendAddResponse.setType(TIMFriendResponseType.Agree);
            } else {
                timFriendAddResponse.setType(TIMFriendResponseType.Reject);
            }
            TIMFriendshipManagerExt.getInstance().addFriendResponse(timFriendAddResponse, new TIMValueCallBack<TIMFriendResult>() {
                @Override
                public void onError(int i, String s) {
                    result.error(s, String.valueOf(i), null);
                }

                @Override
                public void onSuccess(TIMFriendResult timFriendResult) {
                    result.success(timFriendResult.getIdentifer());
                }
            });
        } else if (call.method.equals("getUsersProfile")) {
            List<String> users = call.argument("users");
            TIMFriendshipManager.getInstance().getUsersProfile(users, new TIMValueCallBack<List<TIMUserProfile>>() {
                @Override
                public void onError(int code, String desc) {
                    //错误码 code 和错误描述 desc，可用于定位请求失败原因
                    //错误码 code 列表请参见错误码表
                    Log.e(TAG, "getUsersProfile failed: " + code + " desc");
                    result.error(desc, String.valueOf(code), null);
                }

                @Override
                public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                    Log.e(TAG, "getUsersProfile succ");
                    if (timUserProfiles != null && timUserProfiles.size() > 0) {
                        List<User> userList = new ArrayList<>();
                        for (TIMUserProfile res : timUserProfiles) {
                            userList.add(new User(res));
                            Log.e(TAG, "identifier: " + res.getIdentifier() + " nickName: " + res.getNickName()
                                    + " remark: " + res.getRemark());
                        }
                        result.success(new Gson().toJson(userList, new TypeToken<Collection<User>>() {
                        }.getType()));
                    } else {
                        result.success("[]");
                    }

                }
            });
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


    class Message {
        TIMUserProfile senderProfile;
        TIMConversation timConversation;
        TIMGroupMemberInfo timGroupMemberInfo;
        TIMElem message;


        Message(TIMMessage timMessage) {
            senderProfile = timMessage.getSenderProfile();
            timConversation = timMessage.getConversation();
            message = timMessage.getElement(0);
            timGroupMemberInfo = timMessage.getSenderGroupMemberProfile();
        }
    }

    class User {
        private String identifier = "";
        private String nickName = "";
        private String remark = "";
        private String faceUrl = "";
        private String selfSignature = "";
        private long gender = 0L;
        private long birthday = 0L;
        private String location = "";

        public User(TIMUserProfile timUserProfile) {
            this.identifier = timUserProfile.getIdentifier();
            this.nickName = timUserProfile.getNickName();
            this.remark = timUserProfile.getRemark();
            this.faceUrl = timUserProfile.getFaceUrl();
            this.selfSignature = timUserProfile.getSelfSignature();
            this.gender = timUserProfile.getGender() == null ? 1 : timUserProfile.getGender().getValue();
            this.birthday = timUserProfile.getBirthday();
            this.location = timUserProfile.getLocation();
        }
    }
}
