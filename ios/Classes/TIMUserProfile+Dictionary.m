//
//  TIMUserProfile+Dictionary.m
//  dim
//
//  Created by junshao on 2018/8/28.
//

#import "TIMUserProfile+Dictionary.h"
#import <IMFriendshipExt/IMFriendshipExt.h>

@implementation TIMUserProfile (Dictionary)
-(NSDictionary *)dictionary {
    return [NSDictionary dictionaryWithObjectsAndKeys:
            self.identifier, @"identifier",
            self.nickname, @"nickname",
            self.remark, @"remark",
            self.allowType, @"allowType",
            self.faceURL, @"faceURL",
            self.selfSignature, @"selfSignature",
            
            nil];
    
    
    /**
     *  用户identifier
     */
    @property(nonatomic,strong) NSString* identifier;
    
    /**
     *  用户昵称
     */
    @property(nonatomic,strong) NSString* nickname;
    
    /**
     *  用户备注（最大96字节，获取自己资料时，该字段为空）
     */
    @property(nonatomic,strong) NSString* remark;
    
    /**
     *  好友验证方式
     */
    @property(nonatomic,assign) TIMFriendAllowType allowType;
    
    /**
     * 用户头像
     */
    @property(nonatomic,strong) NSString* faceURL;
    
    /**
     *  用户签名
     */
    @property(nonatomic,strong) NSData* selfSignature;
    
    /**
     *  好友性别
     */
    @property(nonatomic,assign) TIMGender gender;
    
    /**
     *  好友生日
     */
    @property(nonatomic,assign) uint32_t birthday;
    
    /**
     *  好友区域
     */
    @property(nonatomic,strong) NSData* location;
    
    /**
     *  好友语言
     */
    @property(nonatomic,assign) uint32_t language;
    
    /**
     *  等级
     */
    @property(nonatomic,assign) uint32_t level;
    
    /**
     *  角色
     */
    @property(nonatomic,assign) uint32_t role;
    
    /**
     *  好友分组名称 NSString* 列表
     */
    @property(nonatomic,strong) NSArray* friendGroups;
    
    /**
     *  自定义字段集合,key是NSString类型,value是NSData类型或者NSNumber类型
     *  (key值按照后台配置的字符串传入)
     */
    @property(nonatomic,strong) NSDictionary* customInfo;
}
@end

