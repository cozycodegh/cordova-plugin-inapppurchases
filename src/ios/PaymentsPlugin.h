/*!
 *
 * Author: Alex Disler (alexdisler.com)
 * github.com/alexdisler/cordova-plugin-inapppurchase
 *
 * Licensed under the MIT license. Please see README for more information.
 *
 */

/** Modifications: cordova-plugin-inapppurchases MIT © 2023 cozycode.ca  **/

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>

@interface PaymentsPlugin : CDVPlugin

- (void)billingGetAllProductInfo:(CDVInvokedUrlCommand *)command;
- (void)billingGetPurchases:(CDVInvokedUrlCommand *)command;
- (void)billingRestorePurchases:(CDVInvokedUrlCommand *)command;
- (void)billingPurchase:(CDVInvokedUrlCommand *)command;
- (void)billingGetReceipt:(CDVInvokedUrlCommand *)command;

@end
