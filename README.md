# dim

封装的一个腾讯云im，以便于flutter开发者可以方便继承im到自己的应用中

## 使用之前注意事项


开发者需要到腾讯云上申请一个appid，申请[地址](https://console.cloud.tencent.com/avc)

申请成功之后，平台会分配一个appid给到开发者。

1、sig的获取，sig一般就是开发者自己的后台开发同学提供，可以参考腾讯云文档实现sig申请。

2、都准备ok了，就可以登录imsdk了。

android 端配置你的项目中，android 目录app下面的 AndroidManifest.xml，内容参考这里
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.coinseast">

    <!-- The INTERNET permission is required for development. Specifically,
         flutter needs it to communicate with the running application
         to allow setting breakpoints, to provide hot reload, etc.
    -->
    <uses-permission android:name="android.permission.INTERNET"/>

    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
    <uses-permission android:name="android.permission.GET_TASKS" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.READ_LOGS" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <!-- io.flutter.app.FlutterApplication is an android.app.Application that
         calls FlutterMain.startInitialization(this); in its onCreate method.
         In most cases you can leave this as-is, but you if you want to provide
         additional functionality it is fine to subclass or reimplement
         FlutterApplication and put your custom class here. -->
    <application
        android:name="io.flutter.app.FlutterApplication"
        android:label="coins_east"
        android:icon="@mipmap/ic_launcher">
        <activity
            android:name=".MainActivity"
            android:launchMode="singleTop"
            android:theme="@style/LaunchTheme"
            android:configChanges="orientation|keyboardHidden|keyboard|screenSize|locale|layoutDirection|fontScale|screenLayout|density"
            android:hardwareAccelerated="true"
            android:windowSoftInputMode="adjustResize">
            <!-- This keeps the window background of the activity showing
                 until Flutter renders its first frame. It can be removed if
                 there is no splash screen (such as the default splash screen
                 defined in @style/LaunchTheme). -->
            <meta-data
                android:name="io.flutter.app.android.SplashScreenUntilFirstFrame"
                android:value="true" />
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>

        <!-- 【必须】消息收发service -->
        <service
            android:name="com.tencent.qalsdk.service.QalService"
            android:exported="true"
            android:process=":QALSERVICE" >
        </service>
        <service
            android:name="com.tencent.qalsdk.service.QalAssistService"
            android:exported="false"
            android:process=":QALSERVICE" >
        </service>

        <!-- 【必须】 离线消息广播接收器 -->
        <receiver
            android:name="com.tencent.qalsdk.QALBroadcastReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="com.tencent.qalsdk.broadcast.qal" />
            </intent-filter>
        </receiver>
        <receiver
            android:name="com.tencent.qalsdk.core.NetConnInfoCenter" android:process=":QALSERVICE">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.TIME_SET" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.TIMEZONE_CHANGED" />
            </intent-filter>

            <!-- ImSDK 3.0.2 后添加 -->
            <intent-filter>
                <action android:name="com.tencent.qalsdk.service.TASK_REMOVED" />
            </intent-filter>
        </receiver>
    </application>
</manifest>

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