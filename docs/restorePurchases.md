# inAppPurchases.restorePurchases()

## Usage:
```js
inAppPurchases.restorePurchases().then(function (purchases) {
    // access purchase information
    // update your purchases applied to the app
}).catch (function(err){
    // view or handle billing or api error messages
});
```
<p align="center">
<img src="restore_button.png" alt="put a restore button somewhere" width="300" align="center" />
</p>

## Description:
 - get a list of purchased products 
 - logs in and retrieves purchases for the user
 - useful to attempt to complete uncompleted purchases, once they are no longer pending
 - call from `onDeviceReady` (see example code: [`inAppPurchases.getAllProductInfo(productIds)`](getAllProductInfo.md#Example))
 - call from `onResume` (but check for errors, and timeout to try again later if it interferes with another call)
 - call from a `restore button` that the user can press to get back their purchases if they log in on a new device (required by App store)
 - note: will list pending purchases, check the pending state before rewarding the purchase
    
## Parameters:
None

## Returns:
Array of purchases,
Each purchase object contains information about the purchase and its state:
```js
purchases[i]["productId"]       // string: the product id
purchases[i]["purchaseTime"]    // int: timestamp of purchase
purchases[i]["purchaseId"]      // string: id assigned by the app store when it was bought, called the Google order ID or Appstore transaction id
purchases[i]["purchaseToken"]   // string: Android only, token value for completed purchases, use to handle subscriptions
purchases[i]["quantity"]        // int: number of purchased consumables, for non-consumables and subscriptions always returns 1 
purchases[i]["verified"]        // boolean: receipt signature was verified (stops modded or pirated versions of an app from enabling fake purchases - may error instead when tampered, optional, is done for Android locally)
purchases[i]["pending"]         // boolean: Android only, purchase is pending (not paid for yet), wait for user to complete cash payment, then run inAppPurchases.completePurchase(productId) to complete (acknowledge and consume) the purchase
purchases[i]["completed"]       // boolean: has been acknowledged or consumed, unacknowledged purchases will be returned after a few days in Android. unconsumed purchases will not be available for repurchase until they are completed, this field is useful if a call to completePurchase fails because it lets your app know of any purchases that still need to be completed 
```

## Example
```js
function onRestoreButtonPressOrUpdate(){
    inAppPurchases.restorePurchases().then( function(purchases){
        for (var i=0; i<purchases.length; i++){
            if (purchases[i]["pending"]) continue;
            if (!purchases[i]["completed"]) inAppPurchases
                .completePurchase(purchases[i]["productId"])
                .catch(function(err){});
            // handle purchases:
            if (purchases[i]["productId"] == "ads_remove_id") removeAds();
        }
    }).catch (function(err){
        // view or handle billing or api error messages
    });
}
```

## Tips:
- returns pending non-consumed purchases and unacknowledged purchases, useful if the completePurchase call fails to re-run it later 
   (eg. for Android slow card purchase, which would be completed later by calling `onRestoreButtonPressOrUpdate` from `onResume`)
- call from a `restore button` press
- call from `onDeviceReady` to load purchases into the app
- call from `onResume` to load new purchases and updates
- check for errors and try again later with setTimeout if another call was in progress 
- since the API returns a promise, it is asynchronous... call it and continue with other actions, use it in promise chaining, or await for it to finish from within an async function to wait for the call to finish. 

## Notes:

(note from previous API: current state is no longer needed as cancelled and refunded purchases are no longer returned here by Google)

<hr/>

<p align="center">

[go to main](../README.md#plugin-usage)

</p>
