#import "DimPlugin.h"
#import <IMMessageExt/IMMessageExt.h>

@interface DimPlugin() <TIMUserStatusListener, TIMRefreshListener, TIMMessageListener>
@end

@implementation DimPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"dim_method"
            binaryMessenger:[registrar messenger]];
  DimPlugin* instance = [[DimPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {

  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  }else if([@"im_login" isEqualToString:call.method]) {
    NSString *appid = call.arguments[@"sdkAppId"];
    NSString *identifier = call.arguments[@"identifier"];
    NSString *userSig = call.arguments[@"userSig"];

    //初始化 SDK 基本配置
    TIMSdkConfig *config = [TIMSdkConfig new];
      config.sdkAppId = [appid intValue];
    config.disableCrashReport = YES;
      config.disableLogPrint = NO;
      //初始化 SDK
      int initResult = [[TIMManager sharedInstance] initSdk:config];
      //将用户配置与通讯管理器进行绑定
      TIMUserConfig *userConfig = [TIMUserConfig new];
      userConfig.userStatusListener = self;
      userConfig.refreshListener = self;
      [[TIMManager sharedInstance] setUserConfig:userConfig];
      [[TIMManager sharedInstance] addMessageListener:self];
      
      
    TIMLoginParam *login_param = [[TIMLoginParam alloc ]init];
    // identifier 为用户名，userSig 为用户登录凭证
    // appidAt3rd 在私有帐号情况下，填写与 sdkAppId 一样
    login_param.identifier = identifier;
    login_param.userSig = userSig;
    login_param.appidAt3rd = appid;
    [[TIMManager sharedInstance] login: login_param succ:^(){
        result(@"Login Succ");
    } fail:^(int code, NSString * err) {
        NSLog([NSString stringWithFormat:@"Login Failed: %d->%@", code, err]);
        result([NSString stringWithFormat:@"Login Failed: %d->%@", code, err]);
    }];
  }else if([@"sdkLogout" isEqualToString:call.method]){
      [[TIMManager sharedInstance] logout:^{
          result(@"logout success");
      } fail:^(int code, NSString *msg) {
          [NSString stringWithFormat:@"logout failed. code %d desc %@", code, msg];
      }];
  }else if([@"getConversations" isEqualToString:call.method]){
      NSArray *conversationList = [[TIMManager sharedInstance] getConversationList];
      result(conversationList);
  }else if([@"delConversation" isEqualToString:call.method]){
      NSString *identifier = call.arguments[@"identifier"];
      [[TIMManager sharedInstance] deleteConversation:TIM_C2C receiver:identifier];
      result(@"delConversation success");
  }else if([@"getMessages" isEqualToString:call.method]){
      NSString *identifier = call.arguments[@"identifier"];
      TIMMessage *lastMsg = call.arguments[@"lastMsg"];
      TIMConversation *con = [[TIMManager sharedInstance] getConversation:TIM_C2C receiver:identifier];
      [con getMessage:10 last:lastMsg succ:^(NSArray *msgs) {
          result(msgs);
      } fail:^(int code, NSString *msg) {
          result([NSString stringWithFormat:@"get message failed. code: %d msg: %@", code, msg]);
      }];
  }else if([@"sendTextMessages" isEqualToString:call.method]){
      NSString *identifier = call.arguments[@"identifier"];
      NSString *content = call.arguments[@"content"];
      TIMMessage *msg = [TIMMessage new];
      
      //添加文本内容
      TIMTextElem *elem = [TIMTextElem new];
      elem.text = content;
      
      //将elem添加到消息
      if([msg addElem:elem] != 0){
          NSLog(@"addElement failed");
          return;
      }
      TIMConversation *conversation = [[TIMManager sharedInstance] getConversation:TIM_C2C receiver:identifier];
      //发送消息
      [conversation sendMessage:msg succ:^{
          result(msg);
      } fail:^(int code, NSString *msg) {
          result([NSString stringWithFormat:@"send message failed. code: %d desc:%@", code, msg]);
      }];
  }else if([@"sendImageMessages" isEqualToString:call.method]){
      NSString *identifier = call.arguments[@"identifier"];
      NSString *iamgePath = call.arguments[@"image_path"];
      //构造一条消息
      TIMMessage *msg = [TIMMessage new];
      
      //添加图片
      TIMImageElem *elem = [TIMImageElem new];
      elem.path = iamgePath;
      if([msg addElem:elem] != 0){
          NSLog(@"addElement failed");
      }
      
      TIMConversation *conversation = [[TIMManager sharedInstance] getConversation:TIM_C2C receiver:identifier];
      [conversation sendMessage:msg succ:^{
          result(@"SendMsg ok");
      } fail:^(int code, NSString *msg) {
          result([NSString stringWithFormat:@"send message failed. code: %d desc:%@", code, msg]);
      }];

  }else if([@"post_data_test" isEqualToString:call.method]){
//      eventSink.success("hahahahha  I am from listener");
  }
  else {
    result(FlutterMethodNotImplemented);
  }
}

@end
