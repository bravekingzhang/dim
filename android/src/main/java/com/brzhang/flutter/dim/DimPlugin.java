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
import com.tencent.imsdk.TIMFriendGenderType;
import com.tencent.imsdk.TIMFriendshipManager;
import com.tencent.imsdk.TIMGroupEventListener;
import com.tencent.imsdk.TIMGroupMemberInfo;
import com.tencent.imsdk.TIMGroupTipsElem;
import com.tencent.imsdk.TIMImageElem;
import com.tencent.imsdk.TIMLocationElem;
import com.tencent.imsdk.TIMLogLevel;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageListener;
import com.tencent.imsdk.TIMRefreshListener;
import com.tencent.imsdk.TIMSdkConfig;
import com.tencent.imsdk.TIMSoundElem;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.TIMUserConfig;
import com.tencent.imsdk.TIMUserProfile;
import com.tencent.imsdk.TIMUserStatusListener;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.imsdk.ext.message.TIMConversationExt;
import com.tencent.imsdk.ext.message.TIMManagerExt;
import com.tencent.imsdk.ext.message.TIMUserConfigMsgExt;
import com.tencent.imsdk.friendship.TIMDelFriendType;
import com.tencent.imsdk.friendship.TIMFriend;
import com.tencent.imsdk.friendship.TIMFriendRequest;
import com.tencent.imsdk.friendship.TIMFriendResponse;
import com.tencent.imsdk.friendship.TIMFriendResult;
import com.tencent.imsdk.log.QLog;
import com.tencent.imsdk.session.SessionWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
        final DimPlugin dimPlugin = new DimPlugin(registrar);
        channel.setMethodCallHandler(dimPlugin);
        eventChannel.setStreamHandler(dimPlugin);
    }

    @Override
    public void onMethodCall(MethodCall call, final Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("init")) {
            int appid = call.argument("sdkAppId");
            //初始化 IM SDK 基本配置
            //判断是否是在主线程
            if (SessionWrapper.isMainProcess(registrar.context())) {
                TIMSdkConfig config = new TIMSdkConfig(appid)
                        .enableLogPrint(true)
                        .setLogLevel(TIMLogLevel.DEBUG)
                        .setLogPath(Environment.getExternalStorageDirectory().getPath() + "/justfortest/");

                //初始化 SDK
                TIMManager.getInstance().init(registrar.context(), config);


                //基本用户配置,在登录前，通过通讯管理器 TIMManager 的接口 setUserConfig 将用户配置与当前通讯管理器进行绑定
                TIMUserConfig userConfig = new TIMUserConfig()
                        //设置用户状态变更事件监听器
                        .setUserStatusListener(new TIMUserStatusListener() {
                            @Override
                            public void onForceOffline() {
                                //被其他终端踢下线
                                Log.i(TAG, "onForceOffline");
                            }

                            @Override
                            public void onUserSigExpired() {
                                //用户签名过期了，需要刷新 userSig 重新登录 IM SDK
                                Log.i(TAG, "onUserSigExpired");
                            }
                        })
                        //设置连接状态事件监听器
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
                        })
                        //设置群组事件监听器
                        .setGroupEventListener(new TIMGroupEventListener() {
                            @Override
                            public void onGroupTipsEvent(TIMGroupTipsElem elem) {
                                Log.i(TAG, "onGroupTipsEvent, type: " + elem.getTipsType());
                            }
                        })
                        //设置会话刷新监听器
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

                //消息扩展用户配置
                userConfig = new TIMUserConfigMsgExt(userConfig)
                        .enableAutoReport(true)
                        //开启消息已读回执
                        .enableReadReceipt(true);
                //将用户配置与通讯管理器进行绑定
                TIMManager.getInstance().setUserConfig(userConfig);

                TIMManager.getInstance().removeMessageListener(timMessageListener);
                TIMManager.getInstance().addMessageListener(timMessageListener);

                result.success("init succ");
            } else {
                result.success("init failed ,not in main process");
            }
        } else if (call.method.equals("im_login")) {
            if (!TextUtils.isEmpty(TIMManager.getInstance().getLoginUser())) {
                result.error("login failed. ", "user is login", "user is already login ,you should login out first");
                return;
            }
            String identifier = call.argument("identifier");
            String userSig = call.argument("userSig");
            // identifier为用户名，userSig 为用户登录凭证
            TIMManager.getInstance().login(identifier, userSig, new TIMCallBack() {
                @Override
                public void onError(int code, String desc) {
                    result.error(desc, String.valueOf(code), null);
                }

                @Override
                public void onSuccess() {
                    result.success("login succ");
                }
            });
        } else if (call.method.equals("im_logout")) {
            TIMManager.getInstance().logout(new TIMCallBack() {
                @Override
                public void onError(int code, String desc) {
                    result.error(desc, String.valueOf(code), null);
                }

                @Override
                public void onSuccess() {
                    result.success("logout success");
                }
            });
        } else if (call.method.equals("sdkLogout")) {
            //登出
            TIMManager.getInstance().logout(new TIMCallBack() {
                @Override
                public void onError(int code, String desc) {
                    Log.d(TAG, "logout failed. code: " + code + " errmsg: " + desc);
                    result.error(desc, String.valueOf(code), null);
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
                            Log.d(TAG, "get message failed. code: " + code + " errmsg: " + desc);
                            result.error(desc, String.valueOf(code), null);
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
                    Log.d(TAG, "send message failed. code: " + code + " errmsg: " + desc);
                    result.error(desc, String.valueOf(code), null);
                }

                @Override
                public void onSuccess(TIMMessage msg) {//发送消息成功
                    Log.e(TAG, "sendTextMessages ok");
                    result.success("sendTextMessages ok");
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
                    Log.d(TAG, "send message failed. code: " + code + " errmsg: " + desc);
                    result.error(desc, String.valueOf(code), null);
                }

                @Override
                public void onSuccess(TIMMessage msg) {//发送消息成功
                    Log.e(TAG, "SendMsg ok");
                    result.success("SendMsg ok");
                }
            });
        } else if (call.method.equals("sendSoundMessages")) {
            String identifier = call.argument("identifier");
            String sound_path = call.argument("sound_path");
            int duration = call.argument("duration");
            //构造一条消息
            TIMMessage msg = new TIMMessage();

//添加图片
            TIMSoundElem elem = new TIMSoundElem();
//            elem.setPath(Environment.getExternalStorageDirectory() + "/DCIM/Camera/1.jpg");
            elem.setPath(sound_path);
            elem.setDuration(duration);
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
                    Log.d(TAG, "send message failed. code: " + code + " errmsg: " + desc);
                    result.error(desc, String.valueOf(code), null);
                }

                @Override
                public void onSuccess(TIMMessage msg) {//发送消息成功
                    Log.e(TAG, "sendSoundMessages ok");
                    result.success("sendSoundMessages ok");
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
                    result.error(desc, String.valueOf(code), null);
                }

                @Override
                public void onSuccess(TIMMessage msg) {//发送消息成功
                    Log.e(TAG, "Send location ok");
                    result.success("sendLocation ok");
                }
            });

        } else if (call.method.equals("post_data_test")) {
            Log.e(TAG, "onMethodCall() called with: call = [" + call + "], result = [" + result + "]");
            eventSink.success("hahahahha  I am from listener");
        } else if (call.method.equals("addFriend")) {
            //创建请求列表
            //添加好友请求
            String identifier = call.argument("identifier");
            TIMFriendRequest timFriendRequest = new TIMFriendRequest(identifier);
            timFriendRequest.setAddWording("请添加我!");
            timFriendRequest.setAddSource("android");
            TIMFriendshipManager.getInstance().addFriend(timFriendRequest, new TIMValueCallBack<TIMFriendResult>() {
                @Override
                public void onError(int code, String desc) {
                    result.error(desc, String.valueOf(code), null);
                }

                @Override
                public void onSuccess(TIMFriendResult timFriendResult) {
                    result.success("addFriend success");
                }
            });
        } else if (call.method.equals("delFriend")) {
            //双向删除好友 test_user
            String identifier = call.argument("identifier");

            List<String> identifiers = new ArrayList<>();
            identifiers.add(identifier);
            TIMFriendshipManager.getInstance().deleteFriends(identifiers, TIMDelFriendType.TIM_FRIEND_DEL_BOTH, new TIMValueCallBack<List<TIMFriendResult>>() {
                @Override
                public void onError(int code, String desc) {
                    result.error(desc, String.valueOf(code), null);
                }

                @Override
                public void onSuccess(List<TIMFriendResult> timUserProfiles) {
                    result.success("deleteFriends success");
                }
            });
        } else if (call.method.equals("listFriends")) {
            TIMFriendshipManager.getInstance().getFriendList(new TIMValueCallBack<List<TIMFriend>>() {
                @Override
                public void onError(int code, String desc) {
                    result.error(desc, String.valueOf(code), null);
                }

                @Override
                public void onSuccess(List<TIMFriend> timFriends) {
                    List<TIMUserProfile> userList = new ArrayList<>();
                    for (TIMFriend timFriend : timFriends) {
                        userList.add(timFriend.getTimUserProfile());
                    }
                    result.success(new Gson().toJson(userList, new TypeToken<Collection<TIMUserProfile>>() {
                    }.getType()));
                }
            });
        } else if (call.method.equals("opFriend")) {//好友申请
            //获取好友列表
            String identifier = call.argument("identifier");
            String opTypeStr = call.argument("opTypeStr");
            TIMFriendResponse timFriendAddResponse = new TIMFriendResponse();
            timFriendAddResponse.setIdentifier(identifier);
            if (opTypeStr.toUpperCase().trim().equals("Y")) {
                timFriendAddResponse.setResponseType(TIMFriendResponse.TIM_FRIEND_RESPONSE_AGREE_AND_ADD);
            } else {
                timFriendAddResponse.setResponseType(TIMFriendResponse.TIM_FRIEND_RESPONSE_REJECT);
            }
            TIMFriendshipManager.getInstance().doResponse(timFriendAddResponse, new TIMValueCallBack<TIMFriendResult>() {
                @Override
                public void onError(int i, String s) {
                    result.error(s, String.valueOf(i), null);
                }

                @Override
                public void onSuccess(TIMFriendResult timFriendResult) {
                    result.success(timFriendResult.getIdentifier());
                }
            });
        } else if (call.method.equals("getUsersProfile")) {
            List<String> users = call.argument("users");
            //获取用户资料
            TIMFriendshipManager.getInstance().getUsersProfile(users, true, new TIMValueCallBack<List<TIMUserProfile>>() {
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
                        result.success(new Gson().toJson(timUserProfiles, new TypeToken<Collection<TIMUserProfile>>() {
                        }.getType()));
                    } else {
                        result.success("[]");
                    }

                }
            });
        } else if (call.method.equals("setUsersProfile")) {

            String nick = call.argument("nick");
            int gender = call.argument("gender");
            String faceUrl = call.argument("faceUrl");
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put(TIMUserProfile.TIM_PROFILE_TYPE_KEY_NICK, nick);
            profileMap.put(TIMUserProfile.TIM_PROFILE_TYPE_KEY_GENDER, gender==1?TIMFriendGenderType.GENDER_MALE:TIMFriendGenderType.GENDER_FEMALE);
            profileMap.put(TIMUserProfile.TIM_PROFILE_TYPE_KEY_FACEURL, faceUrl);
            TIMFriendshipManager.getInstance().modifySelfProfile(profileMap, new TIMCallBack() {
                @Override
                public void onError(int code, String desc) {
                    Log.e(TAG, "modifySelfProfile failed: " + code + " desc" + desc);
                    result.error(desc, String.valueOf(code), null);
                }

                @Override
                public void onSuccess() {
                    Log.e(TAG, "modifySelfProfile success");
                    result.success("setUsersProfile succ");
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
            timMessage.getSenderProfile(new TIMValueCallBack<TIMUserProfile>() {
                @Override
                public void onError(int i, String s) {

                }

                @Override
                public void onSuccess(TIMUserProfile timUserProfile) {
                    senderProfile = timUserProfile;
                }
            });
            timConversation = timMessage.getConversation();
            message = timMessage.getElement(0);
            timGroupMemberInfo = timMessage.getSenderGroupMemberProfile();
        }
    }
}
