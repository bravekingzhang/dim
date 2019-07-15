import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:dim/dim.dart';

import 'dart:math';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  Dim _dim = new Dim();

  String _result = "";

  List<dynamic> _users = List();

  //在另外一个手机上测试改变下用户，靠这里了
  int _currentUser = 1;

  int appid = 1400215656;

  StreamSubscription<dynamic> _messageStreamSubscription;

  @override
  void initState() {
    super.initState();
    initPlatformState();
    _users.add({
      'username': 'hoolly1',
      'sig':
          "eJxlj8FPgzAche-8FYTrjLbQgngbbkEmHIgzZF4Iwg*obLTSssmM-7sRNTbxXb8v7*W9G6ZpWtv44bIoSz72KleTAMu8MS1kXfxBIViVFyp3huofhDfBBsiLWsEwQ0wptRHSHVZBr1jNfoyW8-1*wpogqy6fV74bCEI2pi51dYU1M0zWu9soXWUp37Q0CKYoCGJVPi3ASRkbty*CyCQeC7dZc1igFK5OUbvchMdyyvz7tqulF3qrJT3Is-vYvI4nz92hwc-EHQnpM0k6bVKxA-xe8sk19oij0SMMkvF*FmyEKbYd9BXL*DA*AfzTXb0_"
    });
    _users.add({
      'username': 'hoolly2',
      'sig':
          "eJxlj8FOg0AURfd8BWFbIzMDM60mLhqUhNpqacUFGwKdgb4yDATGltb470bUSOLbnpN773s3TNO0Xpbb63S3q9*UTvS5EZZ5a1rIuvqDTQM8SXXitPwfFH0DrUjSXIt2gJhSShAaO8CF0pDDj7GvaynPZCR0vEyGlu8EFyGCKaNsrEAxwNVD5AXhPeHk4D1uTq7wudL9pVB9PJmjCmTZ*pn0Q8*3N9sc8qciKBbsGOBYnEo2E5nM9yxmr5FcVwd7MWEdrHHozLPnVbS0w7tRpYZK-L50487wdDrefBRtB7UaBIIwxcRBX2cZH8YnH3NdyQ__"
    });
    _users.add({
      'username': 'hoolly3',
      'sig':
          "eJxlj11PgzAARd-5FU2fjbZA2ccbwW0QYbqMEeSlAdpB2YQGyiYx-ncjakbifT0n9*Z*aAAAGPr7*zTPm75WVA2SQ7AEEMG7G5RSMJoqarTsH*TvUrScpkfF2xFiQoiO0NQRjNdKHMWvUTbN*TwYE6FjJzqu-DSYCOmYWMSaKqIYYbB6dbyd40XeY6i8uHpyS5*blzBKtv1zvtrGa9Zd1yeLWb0hrzxyba*0g83eVpsgr5wC6Vl8KHYvWZUmTvdQJ2o4ZHXH0eD77tywJ5NKvPG-SwtzjmeL2YReeNuJph4FHWGCdQN9B2qf2hc3HF5a"
    });
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
        print(
            "我监听到数据了$onData,需要在这里判断是你是消息列表还是需要刷新会话的请求。会话的请求是一个空的列表[],消息列表是有内容的");
      });
    }
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
          title: Text('当前账号' + _users[_currentUser]["username"]),
        ),
        body: new Center(
          child: CustomScrollView(
            primary: false,
            slivers: <Widget>[
              SliverPersistentHeader(
                delegate: _SliverAppBarDelegate(
                    minHeight: 30,
                    maxHeight: 200,
                    child: Container(
                      margin: EdgeInsets.all(10),
                      padding: EdgeInsets.all(4),
                      decoration: BoxDecoration(
                          border: Border.all(),
                          borderRadius: BorderRadius.all(Radius.circular(5))),
                      child: SingleChildScrollView(
                        child: Text(_result.isEmpty ? "这里显示输出结果" : _result),
                      ),
                    )),
                pinned: true,
              ),
              SliverPadding(
                padding: const EdgeInsets.all(10.0),
                sliver: SliverGrid.count(
                  crossAxisSpacing: 10.0,
                  mainAxisSpacing: 10.0,
                  crossAxisCount: 4,
                  children: <Widget>[
                    RaisedButton(
                      onPressed: () {
                        init();
                      },
                      child: Text('初始化'),
                    ),
                    RaisedButton(
                      onPressed: () {
                        login();
                      },
                      child: Text('登录'),
                    ),
                    RaisedButton(
                      onPressed: () {
                        logout();
                      },
                      child: Text('登出'),
                    ),
                    RaisedButton(
                      onPressed: () {
                        postData();
                      },
                      child: Text('测试发送数据'),
                    ),
//                    RaisedButton(
//                      onPressed: () {
//                        canCelListener();
//                      },
//                      child: Text('取消监听'),
//                    ),
                    RaisedButton(
                      onPressed: () {
                        sendTextMsg();
                      },
                      child: Text('发文本'),
                    ),
                    RaisedButton(
                      onPressed: () {
                        sendImageMsg();
                      },
                      child: Text('发图片'),
                    ),
                    RaisedButton(
                      onPressed: () {
                        sendLocationMsg();
                      },
                      child: Text('发位置'),
                    ),
                    RaisedButton(
                      onPressed: () {
                        getMessages();
                      },
                      padding: EdgeInsets.all(0),
                      child: Text('历史消息'),
                    ),
                    RaisedButton(
                      onPressed: () {
                        getUserInfo();
                      },
                      child: Text('拿资料'),
                    ),
                    RaisedButton(
                      padding: EdgeInsets.all(0),
                      onPressed: () {
                        setUserInfo();
                      },
                      child: Text('设置资料'),
                    ),
                    RaisedButton(
                      padding: EdgeInsets.all(0),
                      onPressed: () {
                        getConversations();
                      },
                      child: Text('会话列表'),
                    ),
                  ],
                ),
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
      setState(() {
        this._result = result;
      });
      print(result);
    } on PlatformException {
      print("listen  失败");
    }
  }

  Future<void> sendTextMsg() async {
    try {
      var result = await _dim.sendTextMessages(
          _users[_users.length - _currentUser - 1]['username'], "haahah");
      print(result);
      setState(() {
        this._result = result;
      });
    } on PlatformException {
      print("发送消息失败");
      setState(() {
        this._result = "发送消息失败";
      });
    }
  }

  Future<void> sendImageMsg() async {
    try {
      var result = await _dim.sendImageMessages(
          _users[_users.length - _currentUser - 1]['username'],
          "tyyhuiijkoi.png");
      print(result);
      setState(() {
        this._result = result;
      });
    } on PlatformException {
      print("发送图片消息失败");
      setState(() {
        this._result = "发送图片消息失败";
      });
    }
  }

  Future<void> sendLocationMsg() async {
    try {
      var result = await _dim.sendLocationMessages(
          _users[_users.length - _currentUser - 1]['username'],
          113.93,
          22.54,
          "腾讯大厦");
      print(result);
      setState(() {
        this._result = result;
      });
    } on PlatformException {
      print("发送位置消息失败");
      setState(() {
        this._result = "发送位置消息失败";
      });
    }
  }

  ///测试化测试，这里传自己应用的appid
  Future<void> init() async {
    try {
      var result = await _dim.init(appid);
      print(result);
      setState(() {
        this._result = result;
      });
    } on PlatformException {
      print("初始化失败");
    }
  }

  ///第一个测试账号
  Future<void> login() async {
    try {
      var result = await _dim.imLogin(
          _users[_currentUser]['username'], _users[_currentUser]['sig']);
      print(result);
      setState(() {
        this._result = result;
      });
    } on PlatformException {
      print("登录  失败");
    }
  }

  Future<void> logout() async {
    try {
      var result = await _dim.imLogout();
      print(result);
      setState(() {
        this._result = result;
      });
    } on PlatformException {
      print("登出  失败");
    }
  }

  Future<dynamic> getMessages() async {
    try {
      var result = await _dim.getMessages(
        _users[_users.length - _currentUser - 1]['username'],
      );
      print(result);
      setState(() {
        this._result = result;
      });
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
      users.add(_users[_users.length - _currentUser - 1]['username']);
      var result = await _dim.getUsersProfile(users);
      print(result);
      setState(() {
        this._result = result;
      });
    } on PlatformException {
      print("获取个人资料失败");
    }
  }

  void setUserInfo() async {
    try {
      var result = await _dim.setUsersProfile(
          1, "hz", "https://www.brzhang.club/images/hz.png");
      print(result);
      setState(() {
        this._result = result;
      });
    } on PlatformException {
      print("获取个人资料失败");
    }
  }

  void getConversations() async {
    try {
      var result = await _dim.getConversations();
      print(result);
      setState(() {
        this._result = result;
      });
    } on PlatformException {
      print("获取会话列表失败");
    }
  }
}

class _SliverAppBarDelegate extends SliverPersistentHeaderDelegate {
  _SliverAppBarDelegate({
    @required this.minHeight,
    @required this.maxHeight,
    @required this.child,
  });

  final double minHeight;
  final double maxHeight;
  final Widget child;

  @override
  double get minExtent => minHeight;

  @override
  double get maxExtent => max(maxHeight, minHeight);

  @override
  Widget build(
      BuildContext context, double shrinkOffset, bool overlapsContent) {
    return new SizedBox.expand(child: child);
  }

  @override
  bool shouldRebuild(_SliverAppBarDelegate oldDelegate) {
    return maxHeight != oldDelegate.maxHeight ||
        minHeight != oldDelegate.minHeight ||
        child != oldDelegate.child;
  }
}
