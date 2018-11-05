# dim

封装的一个腾讯云im，以便于flutter开发者可以方便继承im到自己的应用中

## 使用之前注意事项


开发者需要到腾讯云上申请一个appid，申请[地址](https://console.cloud.tencent.com/avc)

申请成功之后，平台会分配一个appid给到开发者。

1、sig的获取，sig一般就是开发者自己的后台开发同学提供，可以参考腾讯云文档实现sig申请。

2、都准备ok了，就可以登录imsdk了。

登录代码参考这里：

```dart
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
```


```
ios端配置:
 
因为git上传文件必须要小于100m的限制，所以云im有些framework上传不了，因此需要开发者自己到[这里下载](http://dldir1.qq.com/hudongzhibo/im/IM_iOS_SDK_3.3.2.zip)。

下载好了zip包之后，把里面的这些framework copy到 `/Users/xxx/.pub-cache/hosted/pub.dartlang.org/dim-0.x.x/ios`
目录下（注意，运行fetch_libs.sh获取所有的framework）.

随后到你的工程的ios文件夹中执行 `pod install`

然后就可以跑起来了。

如果不可以跑起来，报了这样的错误。
```objectivec
 Undefined symbols for architecture x86_64:
             "operator new[](unsigned long, std::nothrow_t const&)", referenced from:
                 openbdh::BdhUpTransaction::initSegmentList() in ImSDK(bdhUpTransaction.o)
                 openbdh::BdhUpTransaction::getData(openbdh::DataTransInfo*) in ImSDK(bdhUpTransaction.o)
             "std::__1::__throw_system_error(int, char const*)", referenced from:
                 std::__1::unique_lock<std::__1::mutex>::unlock() in ImSDK(task_queue.o)
             "_uncompress", referenced from:
```

这说明你本地的一些库没有引用，用xcode打开你的ios工程，然后[参考这里](https://cloud.tencent.com/document/product/269/9147)
这里如何集成IMSDK写的很清楚，需要依赖系统的那些库。


注意：

1、引入的时候搜索你会发现.dylib现在变为了.tbd了。还有就是IOS模拟器跑不了，腾讯云没有提供X86的framework。
2、注意不要引入IMUGCExt.framework,TXRTMPSDK.framework。
3、注意，当你升级dim之后，cache对应的版本中没有这些库了，因此要在copy一份过去,可以直接从你之前的版本中copy，在到ios工程下执行`pod install`。

## 已有的功能

1、登录

2、登出

3、获取会话列表

4、删除一个会话

5、获取会话消息

6、发送图片消息

7、发送文本消息

8、发送地理位置消息