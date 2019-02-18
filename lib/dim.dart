import 'dart:async';
import 'package:flutter/services.dart';

class Dim {
  factory Dim() {
    if (_instance == null) {
      final MethodChannel methodChannel = const MethodChannel('dim_method');
      final EventChannel eventChannel = const EventChannel('dim_event');
      _instance = new Dim.private(methodChannel, eventChannel);
    }
    return _instance;
  }

  Dim.private(this._methodChannel, this._eventChannel);

  final MethodChannel _methodChannel;

  final EventChannel _eventChannel;

  Future<String> get platformVersion async {
    final String version =
        await _methodChannel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Dim _instance;

  Stream<dynamic> _listener;

  Stream<dynamic> get onMessage {
    if (_listener == null) {
      _listener = _eventChannel
          .receiveBroadcastStream()
          .map((dynamic event) => _parseBatteryState(event));
    }
    return _listener;
  }

  ///im登录
  Future<dynamic> imLogin(int appid, String identifier, String sig) async {
    return await _methodChannel.invokeMethod("im_login", <String, dynamic>{
      'sdkAppId': appid,
      'identifier': identifier,
      'userSig': sig,
    });
  }

  ///im登出
  Future<dynamic> imLogout() async {
    return await _methodChannel.invokeMethod("im_logout");
  }

  ///获取会话列表
  Future<dynamic> getConversations() async {
    return await _methodChannel.invokeMethod('getConversations');
  }

  ///删除会话
  Future<dynamic> delConversation(String identifier) async {
    return await _methodChannel.invokeMethod(
        'delConversation', <String, String>{'identifier': identifier});
  }

  ///获取一个会话的消息，暂不支持流式查询
  ///identifier 会话id
  ///count 获取消息数量 ,默认50条
  ///ctype 1 私信，2群聊  ,默认是私信
  Future<dynamic> getMessages(String identifier,
      [int count = 50, int ctype = 1]) async {
    return await _methodChannel.invokeMethod('getMessages', <String, dynamic>{
      'identifier': identifier,
      'count': count,
      'ctype': ctype
    });
  }

  ///发送文本消息
  Future<dynamic> sendTextMessages(String identifier, String content) async {
    return await _methodChannel.invokeMethod('sendTextMessages',
        <String, dynamic>{'identifier': identifier, 'content': content});
  }

  ///发送图片消息
  ///imagePath   eg for android : Environment.getExternalStorageDirectory() + "/DCIM/Camera/1.jpg"
  Future<dynamic> sendImageMessages(String identifier, String imagePath) async {
    return await _methodChannel.invokeMethod('sendImageMessages',
        <String, dynamic>{'identifier': identifier, 'image_path': imagePath});
  }

  ///发送位置消息
  ///eg：
  ///lat 113.93
  ///lng 22.54
  ///desc 腾讯大厦
  Future<dynamic> sendLocationMessages(
      String identifier, double lat, double lng, String desc) async {
    return await _methodChannel.invokeMethod('sendLocation', <String, dynamic>{
      'identifier': identifier,
      'lat': lat,
      'lng': lng,
      'desc': desc,
    });
  }

  ///添加好友
  ///
  Future<dynamic> addFriend(String identifier) async {
    return await _methodChannel
        .invokeMethod("addFriend", <String, dynamic>{'identifier': identifier});
  }

  ///删除好友
  ///
  Future<dynamic> delFriend(String identifier) async {
    return await _methodChannel
        .invokeMethod("delFriend", <String, dynamic>{'identifier': identifier});
  }

  ///获取好友列表
  ///
  Future<dynamic> listFriends(String identifier) async {
    return await _methodChannel.invokeMethod(
        "listFriends", <String, dynamic>{'identifier': identifier});
  }

  ///处理好友的请求，接受/拒绝
  ///opTypeStr 接受传 Y
  ///opTypeStr 拒绝传 N
  Future<dynamic> opFriend(String identifier, String opTypeStr) async {
    return await _methodChannel.invokeMethod("opFriend",
        <String, dynamic>{'identifier': identifier, 'opTypeStr': opTypeStr});
  }

  ///获取用户资料
  ///param user is a list ["usersf1","jiofoea2"]
  Future<dynamic> getUsersProfile(List<String> users) async {
    return await _methodChannel
        .invokeMethod("getUsersProfile", <String, dynamic>{'users': users});
  }

  ///测试使用eventChannel推送数据过来
  Future<dynamic> postDataTest() async {
    return await _methodChannel.invokeMethod("post_data_test");
  }

  dynamic _parseBatteryState(event) {
    return event;
  }
}
