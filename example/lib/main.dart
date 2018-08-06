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

  Future<void> login() async {
    try {
      var result = await _dim.imLogin(1400001533, "18681446372", "sfesfs");
//      var result = await _dim.postDataTest();
      print(result);
    } on PlatformException {
      print("登录  失败");
    }
  }

  void canCelListener() {
    if (_messageStreamSubscription != null) {
      _messageStreamSubscription.cancel();
    }
  }
}
