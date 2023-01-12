## Error Statements

These are the *error objects* that will be caught in the inAppPurchases promise chain.

Errors will either be in string format for unexpected errors, or an error object will be returned with some or all of these fields:
```
- error["code"]         //error code (always included in plugin error objects) 
- error["message"]      //error message (always included in plugin error objects)
- error["responseCode"] //another error code from Google Play billing response 
- error["iabText"]      //another error message from the billing api description of the error
- error["iabCode"]      //error code from billing api usage
```

## List of common in app purchase errors with billing: 

### Android:
- `TOO MANY REQUESTS`     - wait for the current request to finish before calling again (onResume might trigger this error, catch it there, and set timeout to query again later - or ignore it. Fast button presses and more than 1 call to the plugin before the first call has finished will also bring up this error, use the promise chaining to do one after the other)
- `GOOGLE PLAY KEY ERROR` - make sure your www/manifest.json file has your correct key with no spaces
- `IAB Helper Error`      - Unexpected billing disconnect - the call disconnected at some point during the operation, check purchases again to confirm if any went through
- `UKNOWN ERROR`          - "runtime" billing error with more information 

### iOS:
- `Unknown product identifier`        - wait up to one hour for products to be added to the appstore billing api when you add them to an app for the first time 
        - wait for the first request to getAllProductInfo to complete before calling buy or restore in iOS
- `Cannot connect to iTunes Store`    - if a buy fails with this message, it could have been cancelled by the user

### more:
- `invalid argument`      - calls to the plugin were not given the correct arguments

#### Android gives out Billing response errors:
- **see [purchase errors](purchase.md#Some of the Possible Error Messages)**
- some errors can be retried automatically, otherwise could display an error message that the purchase did not go through and let them re-try themselves with another button press 

## Testing Tips

see [testing tips](../README.md#Testing Tips) to handle other testing errors 

[go to main](../README.md)
