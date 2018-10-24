#import "DimPlugin.h"
#import <IMMessageExt/IMMessageExt.h>
#import <IMFriendshipExt/IMFriendshipExt.h>
#import "MJExtension.h"

@interface DimPlugin() <TIMConnListener, TIMUserStatusListener, TIMRefreshListener, TIMMessageListener, FlutterStreamHandler>
@property (nonatomic, strong) FlutterEventSink eventSink;

@end

@implementation DimPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel *channel = [FlutterMethodChannel
      methodChannelWithName:@"dim_method"
            binaryMessenger:[registrar messenger]];
  DimPlugin* instance = [[DimPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
    
    FlutterEventChannel *eventChannel = [FlutterEventChannel eventChannelWithName:@"dim_event" binaryMessenger:[registrar messenger]];
    [eventChannel setStreamHandler:instance];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {

  if ([@"getPlatformVersion" isEqualToString:call.method]) {
      result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  }else if([@"im_login" isEqualToString:call.method]) {
      int appidInt = (int)call.arguments[@"sdkAppId"];
      NSString *appid = [NSString stringWithFormat:@"%d", appidInt];
      NSString *identifier = (NSString *)(call.arguments[@"identifier"]);
      NSString *userSig = (NSString *)(call.arguments[@"userSig"]);

      //初始化 SDK 基本配置
      TIMSdkConfig *config = [TIMSdkConfig new];
      config.sdkAppId = [appid intValue];
      config.accountType = @"792";
      config.disableCrashReport = YES;
      config.connListener = self;

      //初始化 SDK
      [[TIMManager sharedInstance] initSdk:config];
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
            NSLog(@"Login Failed: %d->%@", code, err);
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
      NSArray *dictArray = [TIMConversation mj_keyValuesArrayWithObjectArray:conversationList];
      NSError *writeError = nil;
      NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dictArray options:NSJSONWritingPrettyPrinted error:&writeError];
      NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
      result(jsonString);
      
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
          result(@"send message ok");
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

  }else if([@"sendLocation" isEqualToString:call.method]){
      NSString *identifier = call.arguments[@"identifier"];
      double lat = [call.arguments[@"lat"] doubleValue];
      double lng = [call.arguments[@"lng"] doubleValue];
      NSString *desc = call.arguments[@"desc"];
      //构造一条消息
      TIMMessage *msg = [TIMMessage new];
      
      //添加图片
      TIMLocationElem *elem = [TIMLocationElem new];
      elem.latitude = lat;
      elem.longitude = lng;
      elem.desc = desc;
      if([msg addElem:elem] != 0){
          NSLog(@"addElement failed");
      }
      
      TIMConversation *conversation = [[TIMManager sharedInstance] getConversation:TIM_C2C receiver:identifier];
      [conversation sendMessage:msg succ:^{
          result(@"SendMsg ok");
      } fail:^(int code, NSString *msg) {
          result([NSString stringWithFormat:@"send message failed. code: %d desc:%@", code, msg]);
      }];
      
  }
  else if([@"post_data_test" isEqualToString:call.method]){
      
      NSLog(@"post_data_test invoke");
      self.eventSink(@"hahahahha  I am from listener");
      
  }else if([@"addFriend" isEqualToString:call.method]){
      
      NSMutableArray *users = [[NSMutableArray alloc] init];
      TIMAddFriendRequest *req = [[TIMAddFriendRequest alloc] init];
      req.identifier = call.arguments[@"identifier"];
      req.addWording = [NSString stringWithUTF8String:"请添加我"];
      [users addObject:req];
      [[TIMFriendshipManager sharedInstance] addFriend:users succ:^(NSArray *friends) {
          for(TIMFriendResult *res in friends){
              if(res.status != TIM_FRIEND_STATUS_SUCC){
                  result([NSString stringWithFormat:@"AddFriend succ: user=%@ status=%ld", res.identifier, (long)res.status]);
              }else{
                  result(res.identifier);
              }
          }
      } fail:^(int code, NSString *msg) {
          result([NSString stringWithFormat:@"msg:%@ code:%d", msg, code]);
      }];

  }else if([@"delFriend" isEqualToString:call.method]){
      
      NSMutableArray *delUsers = [[NSMutableArray alloc] init];
      [delUsers addObject:call.arguments[@"identifier"]];
      // TIM_FRIEND_DEL_BOTH 指定删除双向好友
      [[TIMFriendshipManager sharedInstance] delFriend: TIM_FRIEND_DEL_BOTH users:delUsers succ:^(NSArray* arr) {
          for (TIMFriendResult * res in arr) {
              if (res.status != TIM_FRIEND_STATUS_SUCC) {
                  result([NSString stringWithFormat:@"DelFriend failed: user=%@ status=%ld", res.identifier, (long)res.status]);
              }
              else {
                  result(res.identifier);
              }
          }
      } fail:^(int code, NSString * err) {
          result([NSString stringWithFormat:@"DelFriend failed: code=%d err=%@", code, err]);
      }];
      
  }else if([@"listFriends" isEqualToString:call.method]){
      
      [[TIMFriendshipManager sharedInstance] getFriendList:^(NSArray * arr) {
//          for (TIMUserProfile *profile in arr) {
//              NSLog(@"friend: %@", profile.identifier);
//          }
          NSArray *dictArray = [TIMUserProfile mj_keyValuesArrayWithObjectArray:arr];
          NSError *writeError = nil;
          NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dictArray options:NSJSONWritingPrettyPrinted error:&writeError];
          NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
          result(jsonString);
          
      }fail:^(int code, NSString * err) {
          NSLog(@"GetFriendList fail: code=%d err=%@", code, err);;
      }];
      
  }else if([@"opFriend" isEqualToString:call.method]){
      
      NSMutableArray *arr = [[NSMutableArray alloc] init];
      NSString *identifier = call.arguments[@"identifier"];
      NSString *opTypeStr = call.arguments[@"opTypeStr"];
      TIMFriendResponse *response = [[TIMFriendResponse alloc] init];
      response.identifier = identifier;
      if([opTypeStr isEqualToString:@"Y"]){
          response.responseType = TIM_FRIEND_RESPONSE_AGREE;
      }else{
          response.responseType = TIM_FRIEND_RESPONSE_REJECT;
      }
      [arr addObject:response];
      
      [[TIMFriendshipManager sharedInstance] addFriend:arr succ:^(NSArray *friends) {
          for (TIMFriendResult * res in friends) {
              result(res.identifier);
          }
      } fail:^(int code, NSString *msg) {
          result([NSString stringWithFormat:@"msg:%@ code:%d", msg, code]);
      }];
  }
  else {
    result(FlutterMethodNotImplemented);
  }
}


#pragma mark - FlutterStreamHandler
- (FlutterError*)onListenWithArguments:(id)arguments
                             eventSink:(FlutterEventSink)eventSink {
    self.eventSink = eventSink;
//    [[UIDevice currentDevice] setBatteryMonitoringEnabled:YES];
//    [self sendBatteryStateEvent];
//    [[NSNotificationCenter defaultCenter]
//     addObserver:self
//     selector:@selector(onBatteryStateDidChange:)
//     name:UIDeviceBatteryStateDidChangeNotification
//     object:nil];
    return nil;
}

- (FlutterError* _Nullable)onCancelWithArguments:(id _Nullable)arguments{
    return nil;
}

#pragma mark - TIMMessageListener
/**
 *  新消息回调通知
 *
 *  @param msgs 新消息列表，TIMMessage 类型数组
 */
- (void)onNewMessage:(NSArray*)msgs{
    if(msgs != nil && msgs.count > 0){
        NSArray *dictArray = [TIMConversation mj_keyValuesArrayWithObjectArray:msgs];
        NSError *writeError = nil;
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dictArray options:NSJSONWritingPrettyPrinted error:&writeError];
        NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        self.eventSink(jsonString);
    }else{
        self.eventSink(@"[]");
    }
}

@end
