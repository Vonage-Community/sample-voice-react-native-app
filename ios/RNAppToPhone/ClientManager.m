#import "ClientManager.h"
#import "EventEmitter.h"
#import <VonageClientSDKVoice/VonageClientSDKVoice.h>

@interface ClientManager ()
@property VGVoiceClient *client;
@end

@implementation ClientManager

RCT_EXPORT_MODULE();

+ (nonnull ClientManager *)shared {
  static ClientManager *sharedInstance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    sharedInstance = [ClientManager new];
  });
  return sharedInstance;
}

- (void)setupClient {
  VGVoiceClient.isUsingCallKit = NO;
  self.client = [[VGVoiceClient alloc] init];
  VGClientConfig *config = [[VGClientConfig alloc] initWithRegion:VGConfigRegionUS];
  [self.client setConfig:config];
}

RCT_EXPORT_METHOD(login:(NSString *)jwt) {
  [ClientManager.shared.client createSession:jwt
                                    callback:^(NSError * _Nullable error, NSString * _Nullable sessionId) {
    if (error != nil) {
      [ClientManager.shared.eventEmitter sendStatusEventWith:@"Error"];
      return;
    } else {
      [ClientManager.shared.eventEmitter sendStatusEventWith:@"Connected"];
    }
  }];
}

RCT_EXPORT_METHOD(makeCall:(NSString *)number) {
  [ClientManager.shared.client serverCall:@{@"to": number} callback:^(NSError * _Nullable error, VGCallId  _Nullable call) {
    if (error != nil) {
      [ClientManager.shared.eventEmitter sendCallStateEventWith:@"Error"];
      return;
    }
    [ClientManager.shared.eventEmitter sendCallStateEventWith:@"On Call"];
    [ClientManager.shared setCallId:call];
  }];
}

RCT_EXPORT_METHOD(endCall) {
  [ClientManager.shared.client hangup:ClientManager.shared.callId callback:^(NSError * _Nullable error) {
    ClientManager.shared.callId  = nil;
    [ClientManager.shared.eventEmitter sendCallStateEventWith:@"Idle"];
  }];
}

@end
