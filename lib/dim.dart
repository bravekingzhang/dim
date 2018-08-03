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

  ///获取会话列表
  Future<dynamic> getConversations() async {
    return await _methodChannel.invokeMethod('getConversations');
  }

  ///退出IM
  Future<dynamic> sdkLogout() async {
    return await _methodChannel.invokeMethod('sdkLogout');
  }

  ///删除会话
  Future<dynamic> delConversation(String identifier) async {
    return await _methodChannel.invokeMethod(
        'delConversation', <String, String>{'identifier': identifier});
  }

  ///获取一个会话的消息，可以流式查询
  Future<dynamic> getMessages(String identifier, dynamic lastMsg) async {
    return await _methodChannel.invokeMethod('getMessages',
        <String, dynamic>{'identifier': identifier, 'lastMsg': lastMsg});
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

  ///测试使用eventChannel推送数据过来
  Future<dynamic> postDataTest() async {
    return await _methodChannel.invokeMethod("post_data_test");
  }

  dynamic _parseBatteryState(event) {
    return event;
  }
}
