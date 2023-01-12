# inAppPurchases.getPurchases()
<!--
## Usage:

inAppPurchases.restorePurchases().then(function (purchases) {
    // view purchases
    // update your purchases applied to the app
}).catch (err){
    // view or handle error messages
}

## Description:
 - get a list of purchased products 
 - the same as restorePurchases except (* iOs Only? *) it does not prompt to log in, which is recommended by Apple to use at the first load up the app instead of restorePurchases
 - use on the start up of the device (onDeviceReady) and on app resume (onResume) to edit UI and include prices from the app store without prompting to log in
 - note: includes pending purchases, check the pending state before rewarding the purchase 
 - note: returns pending non-consumed purchases and unAcknowledged purchases, useful if the completePurchase call fails, to re-run it
 - note: does not return purchases that were failed, refunded, or cancelled, only pending and paid for purchases
    
## Parameters:
None

## Returns:
Array of purchases,
Each purchase object contains information about the purchase and its state:
purchases[i]["productId"]       // string: the product id
purchases[i]["productType"]     // string: "INAPP" (one-time purchase and consumables) or "SUBS" (subscriptions)
purchases[i]["purchaseTime"]    // int: timestamp of purchase
purchases[i]["purchaseId"]      // string: id assigned by the app store when it was bought, called the Google order ID or Appstore transaction id
purchases[i]["quantity"]        // int: number of purchased consumables (Android), for non-consumables and subscriptions always returns 1 
purchases[i]["pending"]         // boolean: purchase is pending (not paid for yet), wait for user to complete cash payment, then run inAppPurchases.completePurchase(productId) to complete (acknowledge and consume) the purchase
purchases[i]["completed"]       // boolean: has been acknowledged or consumed, unacknowledged purchases will be returned after a few days in Android. unconsumed purchases will not be available for repurchase until they are completed, this field is useful if a call to completePurchase fails because it lets your app know of any purchases that still need to be completed 
purchases[i]["receipt"]         // string: ios only, returns a reciept that can be used for purchase verification

## Example

function getPurchases(){
    inAppPurchases.getPurchases().then( function(purchases){
        for (var i=0; i<purchases.length; i++){
            if (purchases[i]["pending"]) continue;
            if (!purchases[i]["completed"]) inAppPurchase.completePurchase(purchases[i]["productId"]);
            // handle purchases:
            if (purchases[i]["productId"] == "ads_remove_id") removeAds();
        }
    });
}

## Tips:

- call from onDeviceReady and onResume events to load products and prices into your app
- useful to complete purchases that could not be completed right after buying
- since the API returns a promise, it is asynchronous... call it and continue with other actions, use it in promise chaining, or await for it to finish from within an async function to wait for the call to finish. 

## Notes:

(note from previous API: current state is no longer needed as cancelled and refunded purchases are no longer returned here by Google, but purchases can be in a pending state)
-->
[go to main](../README.md#PLUGIN USAGE)
