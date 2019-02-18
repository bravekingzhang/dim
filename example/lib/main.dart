import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:dim/dim.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  Dim _dim = new Dim();
  String _platformVersion = 'Unknown';

  StreamSubscription<dynamic> _messageStreamSubscription;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await _dim.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }
    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    if (_messageStreamSubscription == null) {
      _messageStreamSubscription = _dim.onMessage.listen((dynamic onData) {
        print("我监听到数据了$onData");
      });
    }

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  void dispose() {
    // TODO: implement dispose
    super.dispose();
    //flutter 这里应该页面退出栈会调用，但是如果这个是根页面，日志是打不出来的。
    canCelListener();
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: const Text('Plugin example app'),
        ),
        body: new Center(
          child: new Column(
            children: <Widget>[
              new Text('Running on: $_platformVersion\n'),
              RaisedButton(
                onPressed: () {
                  login();
                },
                child: Text('登录imsdk'),
              ),
              RaisedButton(
                onPressed: () {
                  logout();
                },
                child: Text('登出imsdk'),
              ),
              RaisedButton(
                onPressed: () {
                  postData();
                },
                child: Text('测试发送数据'),
              ),
              RaisedButton(
                onPressed: () {
                  canCelListener();
                },
                child: Text('取消监听'),
              ),
              RaisedButton(
                onPressed: () {
                  sendTextMsg();
                },
                child: Text('发送文本消息'),
              ),
              RaisedButton(
                onPressed: () {
                  sendImageMsg();
                },
                child: Text('发送图片消息'),
              ),
              RaisedButton(
                onPressed: () {
                  sendLocationMsg();
                },
                child: Text('发送位置消息'),
              ),
              RaisedButton(
                onPressed: () {
                  getMessages();
                },
                child: Text('拿到历史消息'),
              ),
              RaisedButton(
                onPressed: () {
                  getUserInfo();
                },
                child: Text('获取个人资料'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> postData() async {
    try {
      var result = await _dim.postDataTest();
      print(result);
    } on PlatformException {
      print("listen  失败");
    }
  }

//  Future<void> login() async {
//    try {
//      var result = await _dim.imLogin(1400119955, "18681446372", "eJxlz11PwjAUgOH7-YpmtxjTj32AdwuCMIcGJkq9WWbXlkbsSul0avjvhkniEs-t856cnG8PAOA-ZPllyVjdaFe4T8N9cAV86F-8oTGqKkpXEFv9Q94aZXlRCsdthygMQwxhv1EV104JdS7snvTwUL0W3YXf7QBChGKI4n6iZIeLCR3Pl9dGrMUwj1iyqvSNXNMmmyU5HQ6mVjwumjs5truYbkcbTeR8m6RCRLssu0*pfVrepvmgbdhzFr1MP7gLZ-ir3qxkPGlZsB-1Tjr1xs-vBBBHJA5wT9*5PahadwGGKESYwNP43tH7ARZeXFI_");
////      var result = await _dim.postDataTest();
//      print(result);
//    } on PlatformException {
//      print("登录  失败");
//    }
//  }

  Future<void> sendTextMsg() async {
    try {
      var result = await _dim.sendTextMessages("rq2", "haahah");
      print(result);
    } on PlatformException {
      print("发送消息失败");
    }
  }

  Future<void> sendImageMsg() async {
    try {
      var result = await _dim.sendImageMessages("rq2", "tyyhuiijkoi.png");
      print(result);
    } on PlatformException {
      print("发送图片消息失败");
    }
  }

  Future<void> sendLocationMsg() async {
    try {
      var result =
          await _dim.sendLocationMessages("rq2", 113.93, 22.54, "腾讯大厦");
      print(result);
    } on PlatformException {
      print("发送位置消息失败");
    }
  }

  Future<void> login() async {
    try {
//      var result = await _dim.imLogin(1400117017, "rq3",
//          "eJxlz11PwjAUgOH7-YpmtxjTj32AdwuCMIcGJkq9WWbXlkbsSul0avjvhkniEs-t856cnG8PAOA-ZPllyVjdaFe4T8N9cAV86F-8oTGqKkpXEFv9Q94aZXlRCsdthygMQwxhv1EV104JdS7snvTwUL0W3YXf7QBChGKI4n6iZIeLCR3Pl9dGrMUwj1iyqvSNXNMmmyU5HQ6mVjwumjs5truYbkcbTeR8m6RCRLssu0*pfVrepvmgbdhzFr1MP7gLZ-ir3qxkPGlZsB-1Tjr1xs-vBBBHJA5wT9*5PahadwGGKESYwNP43tH7ARZeXFI_");
      var result = await _dim.imLogin(1400117017, "rq2",
          "eJxlz01Pg0AQgOE7v4Ls2ejMwnaxSQ9Sq60W09o2QS*ElKGsHxS2SxGM-92ITSRxrs87mcynZds2W89X5-F2u69yE5mmIGYPbQbs7A*LQiVRbCJHJ-*QPgqlKYpTQ7pDFEJwgH6jEsqNStWp0CXv4SF5jboLv9suAKIElP1E7ToMJsvx7HpuKGhhJ1J3WU-Tl0E1fp6oMPPC9SNlOHiqFhftBmdhvLhSvi-L*yn4spG5vinfNGUplu2x8bmHCbmeaaugvn248zb1aNQ7adQ7nd5xgaO8dLyeHkkf1D7vAg4okDvwM8z6sr4BAlVcKw__");
      print(result);
    } on PlatformException {
      print("登录  失败");
    }
  }

  Future<void> logout() async {
    try {
      var result = await _dim.imLogout();
      print(result);
    } on PlatformException {
      print("登出  失败");
    }
  }

  Future<dynamic> getMessages() async {
    try {
      var result = await _dim.getMessages(
        "rq3",
      );
      print(result);
    } on PlatformException {}
  }

  void canCelListener() {
    if (_messageStreamSubscription != null) {
      _messageStreamSubscription.cancel();
    }
  }

  void getUserInfo() async {
    try {
      List<String> users = List();
      users.add("jiumi_1");
      var result = await _dim.getUsersProfile(users);
      print(result);
    } on PlatformException {
      print("获取个人资料失败");
    }
  }
}
