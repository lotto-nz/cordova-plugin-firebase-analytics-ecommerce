#import "FirebaseAnalyticsPlugin.h"
@import Firebase;


@implementation FirebaseAnalyticsPlugin

- (void)pluginInitialize {
    NSLog(@"Starting Firebase Analytics plugin");
    
    [FIROptions defaultOptions].deepLinkURLScheme = [FIROptions defaultOptions].bundleID;
    
    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }
}

- (void)logEvent:(CDVInvokedUrlCommand *)command {
    NSString* name = [command.arguments objectAtIndex:0];
    NSDictionary* parameters = [command.arguments objectAtIndex:1];
    
    [FIRAnalytics logEventWithName:name parameters:parameters];
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) logPurchases:(CDVInvokedUrlCommand *)command {
    NSArray *params = [command argumentAtIndex:0];
    NSDictionary *transactionDetails = [command argumentAtIndex:1];
    //    NSLog(@"params = %@", params);
    int len = (int)params.count;
    NSMutableArray *items = [[NSMutableArray alloc] init];
    for(int i=0; i < len; i++){
        NSDictionary *product = [self getProductDictionaryWithQuantity:[params objectAtIndex:i]];
        [items addObject:product];
    }
    
    // Prepare ecommerce dictionary.
    NSArray *productData = [items copy];
    //NSNumber *revenue = [NSNumber numberWithDouble:[[transactionDetails objectForKey:@"value"] doubleValue]];
    NSDictionary *ecommerce = @{
                                @"items": productData,
                                kFIRParameterItemList: [self checkAndFormatToString:[transactionDetails objectForKey:@"item_list"]],
                                kFIRParameterTransactionID : [self checkAndFormatToString:[transactionDetails objectForKey:@"transaction_id"]],
                                kFIRParameterAffiliation : [self checkAndFormatToString:[transactionDetails objectForKey:@"affiliation"]],
                                kFIRParameterValue : [NSNumber numberWithDouble:[[transactionDetails objectForKey:@"value"] doubleValue]],
                                kFIRParameterCurrency : [self checkAndFormatToString:[transactionDetails objectForKey:@"currency"]]
                                };
    // Log ecommerce_purchase event with ecommerce dictionary.
    [FIRAnalytics logEventWithName:kFIREventEcommercePurchase parameters:ecommerce];
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
}

- (void)setUserId:(CDVInvokedUrlCommand *)command {
    NSString* id = [command.arguments objectAtIndex:0];
    
    [FIRAnalytics setUserID:id];
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setUserProperty:(CDVInvokedUrlCommand *)command {
    NSString* name = [command.arguments objectAtIndex:0];
    NSString* value = [command.arguments objectAtIndex:1];
    
    [FIRAnalytics setUserPropertyString:value forName:name];
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setEnabled:(CDVInvokedUrlCommand *)command {
    bool enabled = [[command.arguments objectAtIndex:0] boolValue];
    
    [[FIRAnalyticsConfiguration sharedInstance] setAnalyticsCollectionEnabled:enabled];
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setCurrentScreen:(CDVInvokedUrlCommand *)command {
    NSString* name = [command.arguments objectAtIndex:0];
    
    [FIRAnalytics setScreenName:name screenClass:nil];
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (NSDictionary *) getProductDictionaryWithQuantity: (NSDictionary*) options {
    NSDictionary *defaultProduct = [NSDictionary dictionaryWithObjectsAndKeys:
                                    kFIRParameterItemID, @"item_id",
                                    kFIRParameterItemName, @"item_name",
                                    kFIRParameterItemCategory, @"item_category",
                                    kFIRParameterCurrency, @"currency",
                                    kFIRParameterPrice, @"price",
                                    kFIRParameterQuantity, @"quantity", nil];
    return [self createFirebaseParamFormat:options withDefault:defaultProduct];
}

- (NSString *) checkAndFormatToString: (id) data {
    if ([data isKindOfClass:[NSString class]]) {
        return data;
    } else {
        return [NSString stringWithFormat:@"%@", data];
    }
}

- (NSDictionary *)createFirebaseParamFormat:(NSDictionary *)params withDefault:(NSDictionary *)defaultProduct
{
    NSMutableDictionary *mappedParams = [NSMutableDictionary dictionaryWithDictionary:params];
    [defaultProduct enumerateKeysAndObjectsUsingBlock:^(NSString *original, NSString *new, BOOL *stop) {
        id data = [params objectForKey:original];
        if (data) {
            [mappedParams removeObjectForKey:original];
            [mappedParams setObject:data forKey:new];
        }
    }];
    
    NSDictionary *dictionary = [mappedParams copy];
    NSMutableDictionary *output = [NSMutableDictionary dictionaryWithCapacity:dictionary.count];
    [dictionary enumerateKeysAndObjectsUsingBlock:^(id key, id data, BOOL *stop) {
        [output removeObjectForKey:key];
        key = [key stringByReplacingOccurrencesOfString:@" " withString:@"_"];
        if ([data isKindOfClass:[NSNumber class]]) {
            data = [NSNumber numberWithDouble:[data doubleValue]];
            [output setObject:data forKey:key];
        } else {
            [output setObject:[NSString stringWithFormat:@"%@", data] forKey:key];
        }
    }];
    
    return [output copy];
}

@end
