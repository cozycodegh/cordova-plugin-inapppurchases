/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/** cordova-plugin-inapppurchases MIT © 2023 cozycode.ca **/

package com.alexdisler_github_cozycode.inapppurchases;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
//import com.google.common.collect.ImmutableList;
import org.json.JSONException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.provider.Settings;
import android.app.Activity;
import android.net.Uri;

import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.BillingClient;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient.FeatureType;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams;
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams;
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams.ReplacementMode;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;

import static com.alexdisler_github_cozycode.inapppurchases.InAppBilling.BILLING_API_VERSION;
import static com.alexdisler_github_cozycode.inapppurchases.InAppBilling.OK;
import static com.alexdisler_github_cozycode.inapppurchases.InAppBilling.INVALID_ARGUMENTS;
import static com.alexdisler_github_cozycode.inapppurchases.InAppBilling.UNABLE_TO_INITIALIZE;
import static com.alexdisler_github_cozycode.inapppurchases.InAppBilling.BILLING_NOT_INITIALIZED;
import static com.alexdisler_github_cozycode.inapppurchases.InAppBilling.UNKNOWN_ERROR;
import static com.alexdisler_github_cozycode.inapppurchases.InAppBilling.USER_CANCELLED;
import static com.alexdisler_github_cozycode.inapppurchases.InAppBilling.BAD_RESPONSE_FROM_SERVER;
import static com.alexdisler_github_cozycode.inapppurchases.InAppBilling.VERIFICATION_FAILED;
import static com.alexdisler_github_cozycode.inapppurchases.InAppBilling.ITEM_UNAVAILABLE;
import static com.alexdisler_github_cozycode.inapppurchases.InAppBilling.ITEM_ALREADY_OWNED;
import static com.alexdisler_github_cozycode.inapppurchases.InAppBilling.ITEM_NOT_OWNED;
import static com.alexdisler_github_cozycode.inapppurchases.InAppBilling.CONSUME_FAILED;
import static com.alexdisler_github_cozycode.inapppurchases.InAppBilling.GOOGLE_PLAY_KEY_ERROR;
import com.alexdisler_github_cozycode.inapppurchases.IabNext;

//adding

/**
 * Billing Client Helper - PurchasesUpdatedListener
 *  => every billing connection has one, even if do not update purchases
 **/
public class IabHelper implements PurchasesUpdatedListener {

    protected static final String TAG = "google.payments Helper ";
    public static final int QUERY_SKU_DETAILS_BATCH_SIZE = 20;

    //Debug logging
    boolean mDebugLog = false;
    String mDebugTag = "IabHelper";
    boolean mExtraDebugLoggingEnabled = false; //SET TO FALSE for app store, asks for more permissions set these in your androidManfiest.xml too
    boolean mSkipPurchaseVerification = false;

    // Is setup done?
    boolean mSetupDone = false;
    // Has this object been disposed of? (If so, we should ignore callbacks, etc)
    boolean mDisposed = false;
    // Is an asynchronous operation in progress?
    // (only one at a time can be in progress)
    boolean mAsyncInProgress = false;
    // (for logging/debugging)
    // if mAsyncInProgress == true, what asynchronous operation is in progress?
    String mAsyncOperation = "";
    // Context we were passed during initialization
    Context mContext;
    // Connection to the service
    // ServiceConnection mServiceConn;
    BillingClient mBillingClient;
    // Subscription support - possible it could be disabled while regular in-app purchases are still enabled
    boolean mSubscriptionsSupported = false;

    // The request code used to launch purchase flow
    int mRequestCode;
    // The item type of the current purchase flow
    String mPurchasingItemType;
    // Public key for verifying signature, in base64 encoding
    String mSignatureBase64 = null;
    
    // next after billing flow
    IabNext mPurchaseNext;
    
    // Billing response codes
    public static final int BILLING_RESPONSE_RESULT_OK = 0;

    // IAB Helper error codes
    public static final int IABHELPER_ERROR_BASE = -1000;
    public static final int IABHELPER_REMOTE_EXCEPTION = -1001;
    public static final int IABHELPER_BAD_RESPONSE = -1002;
    public static final int IABHELPER_VERIFICATION_FAILED = -1003;
    public static final int IABHELPER_SEND_INTENT_FAILED = -1004;
    public static final int IABHELPER_USER_CANCELLED = -1005;
    public static final int IABHELPER_UNKNOWN_PURCHASE_QUERY = -1006;
    public static final int IABHELPER_MISSING_TOKEN = -1007;
    public static final int IABHELPER_UNKNOWN_ERROR = -1008;
    public static final int IABHELPER_SUBSCRIPTIONS_NOT_AVAILABLE = -1009;
    public static final int IABHELPER_INVALID_CONSUMPTION = -1010;
    public static final int IABHELPER_BAD_ARGUMENT = -1011;
    public static final int IABHELPER_BILLING_UNEXPECTED_DISCONNECT = -1012;
    public static final int IABHELPER_RECEIVED_ERROR = -1013;

    // Main
    public IabHelper(Context ctx, String base64PublicKey, boolean extra_debug) {
        mContext = ctx.getApplicationContext();
        mSignatureBase64 = base64PublicKey;
        mExtraDebugLoggingEnabled = extra_debug;
        checkSystemWritePermission();
        logInfo(TAG + " " +"IAB helper created.");
    }
    
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchasesList) {
        try {
            logInfo(TAG + " " +"IabHelper onPurchasesUpdated");
            checkSetupDone("handleActivityResult");
            
            IabResult successResult = failBillingResponseNotOk(billingResult, mPurchaseNext);
            if (purchasesList == null) {
                logError("Null data in IAB activity result.");
                if (mPurchaseNext != null) mPurchaseNext.OnError(BAD_RESPONSE_FROM_SERVER, new IabResult(IABHELPER_BAD_RESPONSE, "Null data in IAB result"));
                else logError("missing mPurchaseNext to complete call");
                return;
            }
            if (mPurchaseNext == null){
                logError("ERROR: no IabNext set in IabHelper to finish billing flow");
                return;
            }
            String productType = BillingClient.ProductType.INAPP;
            for (Purchase purchase : purchasesList) {
                String productId = purchase.getProducts().get(0);
                if (mPurchaseNext.inAppBilling.iabHelperInventory.hasDetails(productId)){
                    productType = mPurchaseNext.inAppBilling.iabHelperInventory.getDetails(productId).getProductId();
                    break;
                }
            }
            IabInventory newInv = new IabInventory(purchasesList,productType);
            //logInfo(newInv.toString());
            // Purchase verification
            if (verifyPurchases(newInv)) logInfo(TAG + " Verified purchases");
            else logError(TAG + " Purchase was not verified");
           
            mPurchaseNext.OnNext(successResult, newInv);
        } catch (Exception e) {
            if (mPurchaseNext != null) mPurchaseNext.OnError(new IabResult(IABHELPER_UNKNOWN_ERROR, "onPurchasesUpdate received some error: "+e));
            else logError("missing mPurchaseNext to complete call");
        }
    }
    /* Purchase Verification */
    // Only allow purchase verification to be skipped if we are debuggable
    public boolean skipPurchaseVerification(){
        return this.mSkipPurchaseVerification && ((mContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
    }
    public boolean verifyPurchases(IabInventory inv){
        return verifyPurchases(inv, null);
    }
    public boolean verifyPurchases(IabInventory inv, IabNext next){
       return verifyPurchases(inv.getAllOwnedPurchases(),next);
    }
    public boolean verifyPurchases(List<IabPurchase> purchases, IabNext next){
        boolean verified = true;
        if (verified) logInfo("IabHelper starting verification of list of purchases");
        for (IabPurchase p: purchases){
            verified = verified && verifyPurchases(p, next);
        }
        if (verified) logInfo("IabHelper verified list of purchases");
        else logInfo("IabHelper verification failed over list of purchases");
        return verified;
    }
    public boolean verifyPurchases(IabPurchase p){
        return verifyPurchases(p, null);
    }
    public boolean verifyPurchases(IabPurchase p, IabNext next){
        if (skipPurchaseVerification()) logInfo(TAG + " " +"IabHelper skipping purchase verification for " + p.getProductId());
        else logInfo(TAG + " " +"IabHelper starting purchase verification for " + p.getProductId());
        if (!p.verifyPurchase(mSignatureBase64)){
            String err = p.getPurhcaseVerifyFailMessage();
            logWarning(err);
            if (next != null) next.OnError(VERIFICATION_FAILED, new IabResult(IABHELPER_VERIFICATION_FAILED,err)); //set null to not error
            else logError("missing IabNext to complete call");
            return false;
        }
        return true;
    }

    /** Limit Billing Client to 1 asynchronous operation at a time **/
    //only one billing operation at a time (avoid multiple PurchasesUpdatedListener callbacks for a single event)
    public void flagStartAsync(String operation) {
        if (mAsyncInProgress) throw new IllegalStateException("Can't start async operation (" +
                operation + ") because another async operation(" + mAsyncOperation + ") is in progress.");
        mAsyncOperation = operation;
        mAsyncInProgress = true;
        logInfo(TAG + " " +"Starting async operation: " + operation);
    }
    public void flagEndAsync() {
        logInfo(TAG + " " +"Ending async operation: " + mAsyncOperation);
        mAsyncOperation = "";
        mAsyncInProgress = false;
    }
    
    /**
     * Dispose of object, releasing connection resources. It's very important to call this
     * method when you are done with this object. It will release any resources
     * used by it such as service connections. Naturally, once the object is
     * disposed of, it can't be used again.
     */
    public void dispose() {
        logInfo(TAG + " " +"Disposing.");
        mSetupDone = false;
//         if (mServiceConn != null) {
//             logInfo(TAG + " " +"Unbinding from service.");
//             if (mContext != null && mService != null) mContext.unbindService(mServiceConn);
//         }
        mDisposed = true;
        mContext = null;
//         mServiceConn = null;
//         mService = null;
        mBillingClient.endConnection();
    }
    private void checkNotDisposed() {
        if (mDisposed) throw new IllegalStateException("IabHelper was disposed of, so it cannot be used.");
    }
    
    /**
     * Logging
     **/
    // some devices disable writing to log
    private void checkSystemWritePermission() {
        if (!mExtraDebugLoggingEnabled) return;
        enableDebugLogging(true);
        if (Settings.System.canWrite(mContext)) {
            return;
        } else {
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + mContext.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }
    public void enableDebugLogging(boolean enable, String tag) {
        checkNotDisposed();
        mDebugLog = enable;
        mDebugTag = tag;
    }
    public void enableDebugLogging(boolean enable) {
        checkNotDisposed();
        mDebugLog = enable;
    }
    public void logInfo(String msg){
        Log.d(mDebugTag, "In-app billing info: " + msg);
        if (!mExtraDebugLoggingEnabled) return;
        System.out.println("In-app billing info: " + msg);
    }
    public void logError(String msg) {
        Log.e(mDebugTag, "In-app billing error: " + msg);
        if (!mExtraDebugLoggingEnabled) return;
        logInfo(mDebugTag + "In-app billing error: " + msg);
    }
    public void logWarning(String msg) {
        Log.w(mDebugTag, "In-app billing warning: " + msg);
        if (!mExtraDebugLoggingEnabled) return;
        logInfo(mDebugTag + "In-app billing warning: " + msg);
    }
    
    /* Errors */
    public IabResult getIabResultFromBillingResult(BillingResult billingResult){
        return getIabResultFromBillingResult(billingResult, "");
    }
    public IabResult getIabResultFromBillingResult(BillingResult billingResult, String addToErrorMsg){
        int responseCode = billingResult.getResponseCode();
        if (responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE){
            return new IabResult(IABHELPER_RECEIVED_ERROR, BillingClient.BillingResponseCode.BILLING_UNAVAILABLE, "Billing response error: A user billing error occurred during processing. "+addToErrorMsg,billingResult.getDebugMessage());
        } else if (responseCode == BillingClient.BillingResponseCode.DEVELOPER_ERROR){
            return new IabResult(IABHELPER_RECEIVED_ERROR, BillingClient.BillingResponseCode.DEVELOPER_ERROR, "Billing response error: Internal Error resulting from incorrect usage of the API. Possibly manifest.json key is incorrect an argument was not supplied when required... Can also happen when the app is not set up to run purchases from the Google Play Store settings."+addToErrorMsg,billingResult.getDebugMessage());
        } else if (responseCode == BillingClient.BillingResponseCode.ERROR){
            return new IabResult(IABHELPER_RECEIVED_ERROR, BillingClient.BillingResponseCode.ERROR, "Billing response error: Fatal error during the API action. "+addToErrorMsg,billingResult.getDebugMessage());
        } else if (responseCode == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED){
            return new IabResult(IABHELPER_RECEIVED_ERROR, BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED, "Billing response error: The requested feature is not supported by the Play Store on the current device. "+addToErrorMsg,billingResult.getDebugMessage());
        } else if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
            return new IabResult(IABHELPER_RECEIVED_ERROR, BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED, "Billing response error: The purchase failed because the item is already owned. "+addToErrorMsg,billingResult.getDebugMessage());
        } else if (responseCode == BillingClient.BillingResponseCode.ITEM_NOT_OWNED){
            return new IabResult(IABHELPER_RECEIVED_ERROR, BillingClient.BillingResponseCode.ITEM_NOT_OWNED, "Billing response error: Requested action on the item failed since it is not owned by the user. "+addToErrorMsg,billingResult.getDebugMessage());
        } else if (responseCode == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE){
            return new IabResult(IABHELPER_RECEIVED_ERROR, BillingClient.BillingResponseCode.ITEM_UNAVAILABLE, "Billing response error: The requested product is not available for purchase. "+addToErrorMsg,billingResult.getDebugMessage());
        } else if (responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED){
            return new IabResult(IABHELPER_RECEIVED_ERROR, BillingClient.BillingResponseCode.SERVICE_DISCONNECTED, "Billing response error: The app is not connected to the Play Store service via the Google Play Billing Library. "+addToErrorMsg,billingResult.getDebugMessage());
        } else if (responseCode == BillingClient.BillingResponseCode.SERVICE_TIMEOUT){
            return new IabResult(IABHELPER_RECEIVED_ERROR, BillingClient.BillingResponseCode.SERVICE_TIMEOUT, "Billing response error: The request has reached the maximum timeout before Google Play responds. "+addToErrorMsg,billingResult.getDebugMessage());
        } else if (responseCode == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE){
            return new IabResult(IABHELPER_RECEIVED_ERROR, BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE, "Billing response error: The service is currently unavailable. "+addToErrorMsg,billingResult.getDebugMessage());
        } else if (responseCode == BillingClient.BillingResponseCode.NETWORK_ERROR ){
            return new IabResult(IABHELPER_RECEIVED_ERROR, BillingClient.BillingResponseCode.NETWORK_ERROR , "Billing response error: There was a problem with the network connection between the device and Play systems. "+addToErrorMsg,billingResult.getDebugMessage());
        } else if (responseCode == BillingClient.BillingResponseCode.USER_CANCELED){
            return new IabResult(IABHELPER_RECEIVED_ERROR, BillingClient.BillingResponseCode.USER_CANCELED, "Billing response error: Transaction was canceled by the user. "+addToErrorMsg,billingResult.getDebugMessage());
        } else if (responseCode != BillingClient.BillingResponseCode.OK){
            return new IabResult(IABHELPER_UNKNOWN_ERROR, "Billing response error: An unknown billing error occurred. "+addToErrorMsg,billingResult.getDebugMessage());
        }
        return new IabResult();
    }
    public IabResult failBillingResponseNotOk(BillingResult billingResult, IabNext next){
        return failBillingResponseNotOk(billingResult, next, "");
    }
    public IabResult failBillingResponseNotOk(BillingResult billingResult, IabNext next, String addToErrorMsg){
        IabResult result = getIabResultFromBillingResult(billingResult, addToErrorMsg);
        logInfo(result.toString());
        if (result != null) {
            if (result.isFailure()){
                if (next != null) next.OnError(result);
                else logError("missing IabNext to complete call");
            }
            return result;
        }
        return new IabResult();
    }
    /**
     * Returns a human-readable description for the given response code.
     * @param code The response code
     * @return A human-readable string explaining the result code.
     *     It also includes the result code numerically.
     */
    public static String getIabHelperErrorMessage(int code) {
        String err = null;
        if (code == 0 || code == IABHELPER_REMOTE_EXCEPTION) err = "IAB Helper Error: Remote exception during initialization";
        else if (code == IABHELPER_BAD_RESPONSE) err = "IAB Helper Error: Bad response received";
        else if (code == IABHELPER_VERIFICATION_FAILED) err = "IAB Helper Error: Purchase signature verification failed";
        else if (code == IABHELPER_SEND_INTENT_FAILED) err = "IAB Helper Error: Send intent failed";
        else if (code == IABHELPER_USER_CANCELLED) err = "IAB Helper Error: User cancelled";
        else if (code == IABHELPER_UNKNOWN_PURCHASE_QUERY) err = "IAB Helper Error: Unknown purchase - make sure to query product ids";
        else if (code == IABHELPER_MISSING_TOKEN) err = "IAB Helper Error: Missing token";
        else if (code == IABHELPER_UNKNOWN_ERROR) err = "IAB Helper Error: Unknown error";
        else if (code == IABHELPER_SUBSCRIPTIONS_NOT_AVAILABLE) err = "IAB Helper Error: Subscriptions not available";
        else if (code == IABHELPER_INVALID_CONSUMPTION) err = "IAB Helper Error: Invalid consumption attempt";
        else if (code == IABHELPER_BAD_ARGUMENT) err = "IAB Helper Error: Invalid product id argument given";
        else if (code == IABHELPER_BILLING_UNEXPECTED_DISCONNECT) err = "IAB Helper Error: Unexpected billing disconnect";
        else if (code == IABHELPER_RECEIVED_ERROR) err = "IAB RECEIVED ERROR";
        return err;
    }
    // Checks that setup was done; if not, throws an exception.
    void checkSetupDone(String operation) {
        if (!mSetupDone) {
            logError("Illegal state for operation (" + operation + "): IAB helper is not set up.");
            throw new IllegalStateException("IAB helper is not set up. Can't perform operation: " + operation);
        }
    }
    /*/ Workaround to bug where sometimes response codes come as Long instead of Integer
    int getResponseCodeFromBundle(Bundle b) {
        Object o = b.get(RESPONSE_CODE);
        if (o == null) {
            logInfo(TAG + " " +"Bundle with null response code, assuming OK (known issue)");
            return BILLING_RESPONSE_RESULT_OK;
        }
        else if (o instanceof Integer) return ((Integer)o).intValue();
        else if (o instanceof Long) return (int)((Long)o).longValue();
        else {
            logError("Unexpected type for bundle response code.");
            logError(o.getClass().getName());
            throw new RuntimeException("Unexpected type for bundle response code: " + o.getClass().getName());
        }
    }
    // Workaround to bug where sometimes response codes come as Long instead of Integer
    int getResponseCodeFromIntent(Intent i) {
        Object o = i.getExtras().get(RESPONSE_CODE);
        if (o == null) {
            logError("Intent with no response code, assuming OK (known issue)");
            return BILLING_RESPONSE_RESULT_OK;
        }
        else if (o instanceof Integer) return ((Integer)o).intValue();
        else if (o instanceof Long) return (int)((Long)o).longValue();
        else {
            logError("Unexpected type for intent response code.");
            logError(o.getClass().getName());
            throw new RuntimeException("Unexpected type for intent response code: " + o.getClass().getName());
        }
    } */
    
    /**
     * Handles an activity result that's part of the purchase flow in in-app billing. If you
     * are calling {@link #launchPurchaseFlow}, then you must call this method from your
     * Activity's {@link android.app.Activity@onActivityResult} method. This method
     * MUST be called from the UI thread of the Activity.
     * @param requestCode The requestCode as you received it.
     * @param resultCode The resultCode as you received it.
     * @param data The data (Intent) as you received it.
     * @return Returns true if the result was related to a purchase flow and was handled;
     *     false if the result was not related to a purchase, in which case you should
     *     handle it normally.
     */
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        return true;
    }
    
    /** BILLING LIBRARY v5 **/
    
    // helper - some calls to not return when no products given - queryProductDetailsAsync
    public void errorOnEmptyProductList(IabNext next){
        next.OnError(INVALID_ARGUMENTS, new IabResult(IABHELPER_BAD_ARGUMENT, "please request with a list of product ids - "+next.getArgsProductIds().toString()));
    }
    
    /* Initialize billing client and connect */
    public void initializeBillingClientAsync(IabNext next){
        // If already set up, can't do it again.
        checkNotDisposed();
        if (mSetupDone) throw new IllegalStateException("IAB helper is already set up.");
        mBillingClient = BillingClient.newBuilder(mContext)
            .setListener(this)
            .enablePendingPurchases()
            .build();
        mBillingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    try {
                        if (mDisposed) return;
                        logInfo(TAG + " " +"Billing service connected.");
                        // check for in-app billing support
                        IabResult result = getIabResultFromBillingResult(billingResult,"Error checking for billing v" + BILLING_API_VERSION + " support.");
                        if (next.checkResultFail(result)) return;
                        logInfo(TAG + " " +"In-app billing version " + BILLING_API_VERSION + " supported for " + mContext.getPackageName());
                        if (mBillingClient.isReady()) logInfo(TAG + " " +"In-app billing ready");
                        else logWarning(TAG + " " +"In-app billing is not ready...");
                        // check for subscriptions support - could possibly be unsupported while in-app products are still supported
                        // response = mService.isBillingSupported(BILLING_API_VERSION, packageName, ITEM_TYPE_SUBS);
                        int response = mBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).getResponseCode();
                        if (response == BillingClient.BillingResponseCode.OK) {
                            logInfo(TAG + " " +"Subscriptions AVAILABLE.");
                            mSubscriptionsSupported = true;
                        } else {
                            logInfo(TAG + " " +"Subscriptions NOT AVAILABLE. Response: " + response);
                            mSubscriptionsSupported = false;
                        }
                        mSetupDone = true;
                        next.OnNext(new IabResult(BillingClient.BillingResponseCode.OK, "Setup successful.")); // next != null?
                    } catch (Exception e) {
                        logInfo("IAB BILLING ERROR: "+e);
                        next.OnError(new IabResult(IABHELPER_UNKNOWN_ERROR, e.toString()));
                        //throw new RuntimeException(e);
                    }
                }
                @Override
                public void onBillingServiceDisconnected() {
                    // Logic from ServiceConnection.onServiceDisconnected should be moved here.
                    logInfo(TAG + " " +"Billing service disconnected.");
                    next.OnError(new IabResult(IABHELPER_BILLING_UNEXPECTED_DISCONNECT, "Disconnected from Play Store on billing setup."));
                    // mService = null;
                }
        });
    }
    
    /* Get Available Product Details */
    //get inapp or subs
    public void getProductDetailsAsync(IabNext next, String productType){
        logInfo (TAG + " " + "Getting all "+productType+" product details"); //only returns for productType even when set all
        checkNotDisposed();
        
        List<String> productIdList = next.getArgsProductIds(); //or all productids
        if (productIdList == null || productIdList.size() == 0){
            errorOnEmptyProductList(next);
            return;
        }
        
        List<QueryProductDetailsParams.Product> productList = new ArrayList<QueryProductDetailsParams.Product>();
        for (String productId: productIdList){
            productList.add(QueryProductDetailsParams.Product.newBuilder()
                 .setProductId(productId)
                 .setProductType(productType)
                 .build());
        }
        
        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build();
        
        mBillingClient.queryProductDetailsAsync(
             queryProductDetailsParams, new ProductDetailsResponseListener() {
                 public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                     try {
                         logInfo (TAG + "PRODUCTDETAILS" + " " + "Received these product details: ");
                         //logInfo(productDetailsList.toString());
                         // check billingResult
                         IabResult successResult = failBillingResponseNotOk(billingResult, next);
                         // process returned productDetailsList
                         IabInventory newInv = new IabInventory(productDetailsList);
                         //logInfo(newInv.toString());
                         next.OnNext(successResult, newInv);
                     } catch (Exception e){
                         logInfo("IAB BILLING ERROR: "+e);
                         next.OnError(new IabResult(IABHELPER_UNKNOWN_ERROR, e.toString()));
                     }
                 }
             });
        logInfo (TAG + " " + "Sent request for "+productType);
    }
    //get inapp and subs
    public void getProductDetailsAsync(IabNext next){
        logInfo (TAG + " " + "Getting all product details");
        checkNotDisposed();

        IabNext nextCall1Complete = new IabNext(next){
            public void OnNext(IabResult result, IabInventory newInv){
                try {
                    IabNext nextCall2Complete = new IabNext(next){ //or this.mNext){
                        public void OnNext(IabResult result, IabInventory newInv){
                            try {
                                tempInv.addInventory(newInv);
                                inAppBilling.iabHelperInventory.overwriteProductDetailsInventory(tempInv);
                                mNext.OnNext(result);
                            } catch (Exception e){
                                logInfo("IAB BILLING ERROR: "+e);
                                next.OnError(new IabResult(IABHELPER_UNKNOWN_ERROR, e.toString()));
                            }
                        }
                    };
                    nextCall2Complete.tempInv = newInv;
                    getProductDetailsAsync(nextCall2Complete, BillingClient.ProductType.SUBS);
                } catch (Exception e){
                    logInfo("IAB BILLING ERROR: "+e);
                    next.OnError(new IabResult(IABHELPER_UNKNOWN_ERROR, e.toString()));
                }
            }
        };
        
        getProductDetailsAsync(nextCall1Complete, BillingClient.ProductType.INAPP);
    }
    
    /* Get Purchases Information */
    public void restorePurchasesAsync(IabNext next, String productType){
        logInfo (TAG + " " + "Getting all "+productType+" purchases (for restore)");
        checkNotDisposed();

        mBillingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
              .setProductType(productType)
              .build(),
            new PurchasesResponseListener() {
              public void onQueryPurchasesResponse(BillingResult billingResult, List<Purchase> purchasesList) {
                  try {
                      logInfo (TAG + "PURCHASES" + " " + "Received these purchase details: ");
                      //logInfo(purchasesList.toString());
                      // check billingResult
                      IabResult successResult = failBillingResponseNotOk(billingResult, next);
                      // process returned productDetailsList
                      IabInventory newInv = new IabInventory(purchasesList,productType);
                      //logInfo(newInv.toString());
                      // Purchase verification
                      if (verifyPurchases(newInv)) logInfo(TAG + " Verified purchases");
                      else logError(TAG + " Purchases were not verified");
                      next.OnNext(successResult, newInv);
                  } catch (Exception e){
                      logInfo("BILLING ERROR: "+e);
                      next.OnError(new IabResult(IABHELPER_UNKNOWN_ERROR, e.toString()));
                  }
              }
            }
        );
    }
    public void restorePurchasesAsync(IabNext next){
        logInfo (TAG + " " + "Getting all purchases (for restore)");
        checkNotDisposed();

        IabNext nextCall1Complete = new IabNext(next){
            public void OnNext(IabResult result, IabInventory newInv){
                try {
                    IabNext nextCall2Complete = new IabNext(next){ //or this.mNext){
                        public void OnNext(IabResult result, IabInventory newInv){
                            try {
                                tempInv.addInventory(newInv);
                                inAppBilling.iabHelperInventory.overwritePurchaseInventory(tempInv);
                                mNext.OnNext(result);
                            } catch (Exception e){
                                logInfo("IAB BILLING ERROR: "+e);
                                next.OnError(new IabResult(IABHELPER_UNKNOWN_ERROR, e.toString()));
                            }
                        }
                    };
                    nextCall2Complete.tempInv = newInv;
                    restorePurchasesAsync(nextCall2Complete, BillingClient.ProductType.SUBS);
                } catch (Exception e){
                    logInfo("IAB BILLING ERROR: "+e);
                    next.OnError(new IabResult(IABHELPER_UNKNOWN_ERROR, e.toString()));
                }
            }
        };
        
        restorePurchasesAsync(nextCall1Complete, BillingClient.ProductType.INAPP);
    }
    
    /* Purchase a Product */
    public void launchBillingFlowAsync(IabNext next, String productId){
        launchBillingFlowAsync(next, productId, "", -1);
    }
    public void launchBillingFlowAsync(IabNext next, String productId, String upgradeProductId, int upgradeReplacementMode){
        logInfo (TAG + " " + "Launching billing flow for "+productId);
        checkNotDisposed();

        if (! next.inAppBilling.iabHelperInventory.hasDetails(productId)){
            next.OnError(INVALID_ARGUMENTS, new IabResult(IABHELPER_UNKNOWN_PURCHASE_QUERY, "Trying to purchase unrecognized product id: "+productId));
            return;
        }
        IabProductDetails purchaseIabProductDetails = next.inAppBilling.iabHelperInventory.getDetails(productId);
        ProductDetails purchaseProductDetails = purchaseIabProductDetails.getProductDetails();
        String firstOfferToken = purchaseIabProductDetails.getOfferToken();
        ProductDetailsParams.Builder paramBuilder = ProductDetailsParams.newBuilder().setProductDetails(purchaseProductDetails);
        if (firstOfferToken != null) paramBuilder.setOfferToken(firstOfferToken);
        List<ProductDetailsParams> productDetailsParamsList = new ArrayList<ProductDetailsParams>();
        productDetailsParamsList.add(paramBuilder.build());
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build();
        
        //subscription upgrade
        if (upgradeReplacementMode != -1 && !upgradeProductId.equals("")){
            if (! next.inAppBilling.iabHelperInventory.hasPurchase(upgradeProductId)){
                next.OnError(INVALID_ARGUMENTS, new IabResult(IABHELPER_UNKNOWN_PURCHASE_QUERY, "Trying to upgrade a subscription for a product id that was not purchased: "+upgradeProductId));
                return;
            }
            if (upgradeReplacementMode == -1){ //default
                upgradeReplacementMode = BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE;
            }
            if (upgradeReplacementMode == BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE){
                logInfo (TAG + " " + "Subscription replacement mode: CHARGE_FULL_PRICE");
            } else if (upgradeReplacementMode == BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE){
                logInfo (TAG + " " + "Subscription replacement mode: CHARGE_PRORATED_PRICE");
            } else if(upgradeReplacementMode == BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_PRORATED_PRICE){
                logInfo (TAG + " " + "Subscription replacement mode: CHARGE_PRORATED_PRICE");
            } else if(upgradeReplacementMode == BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.DEFERRED){
                logInfo (TAG + " " + "Subscription replacement mode: DEFERRED");
            } else if(upgradeReplacementMode == BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.UNKNOWN_REPLACEMENT_MODE){
                logInfo (TAG + " " + "Subscription replacement mode: UNKNOWN_REPLACEMENT_MODE");
            } else if(upgradeReplacementMode == BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITHOUT_PRORATION){
                logInfo (TAG + " " + "Subscription replacement mode: WITHOUT_PRORATION");
            } else if(upgradeReplacementMode == BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITH_TIME_PRORATION){
                logInfo (TAG + " " + "Subscription replacement mode: WITH_TIME_PRORATION");
            } else {
                logWarning (TAG + " " + "Unrecognized subscription upgrade replacement mode: "+Integer.toString(upgradeReplacementMode));
            }
            IabPurchase iabPurchase = next.inAppBilling.iabHelperInventory.getPurchase(upgradeProductId);
            String upgradePurchaseToken = iabPurchase.getPurchaseToken();
            SubscriptionUpdateParams subscriptionUpdateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                           .setOldPurchaseToken(upgradePurchaseToken)
                           .setSubscriptionReplacementMode(upgradeReplacementMode)
                           .build();
            billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .setSubscriptionUpdateParams(subscriptionUpdateParams)
                .build();
        }
        
        // Launch the billing flow
        mPurchaseNext = next;
        BillingResult billingResult = mBillingClient.launchBillingFlow(next.activityContext, billingFlowParams);
    }
    
    /* Complete a Purchase */
    public void launchPurchaseCompletionAsync(IabNext next, String productId){
        logInfo (TAG + " " + "Launching purchase completion for "+productId);
        checkNotDisposed();

        if (! next.inAppBilling.iabHelperInventory.hasDetails(productId)){
            next.OnError(INVALID_ARGUMENTS, new IabResult(IABHELPER_UNKNOWN_PURCHASE_QUERY, "Trying to complete purchase of unrecognized product id: "+productId));
            return;
        }
        IabProductDetails purchaseIabProductDetails = next.inAppBilling.iabHelperInventory.getDetails(productId);
        if (! next.inAppBilling.iabHelperInventory.hasPurchase(productId)){
            next.OnError(INVALID_ARGUMENTS, new IabResult(IABHELPER_UNKNOWN_PURCHASE_QUERY, "Trying to complete purchase that wasn't made of product id: "+productId));
            return;
        }
        IabPurchase iabPurchase = next.inAppBilling.iabHelperInventory.getPurchase(productId);
        if (iabPurchase.getPending()){
            next.OnError(CONSUME_FAILED, new IabResult(IABHELPER_INVALID_CONSUMPTION, "Trying to complete a purchase that is still pending: "+productId));
            return;
        }
        if (! verifyPurchases(iabPurchase)){
            next.OnError(VERIFICATION_FAILED, new IabResult(IABHELPER_VERIFICATION_FAILED, "Trying to complete purchase of unverified purchase product id: "+productId));
            return;
        }
        boolean consume = BillingClient.ProductType.INAPP.equals(purchaseIabProductDetails.getProductType()) && iabPurchase.getIsConsumable();
        if (iabPurchase.getCompleted() && ! consume){ //already completed - unless now want to force consuming it
            next.OnNext(new IabResult());
            return;
        }
        
        //acknowledgePurchaseAsync(next, purchaseIabProductDetails, iabPurchase);
        if (consume) consumePurchaseAsync(next, purchaseIabProductDetails, iabPurchase); //for consumables and non-consumables will consume and acknowledge
        else acknowledgePurchaseAsync(next, purchaseIabProductDetails, iabPurchase);
    }
    
    /* Consume a purchase */
    public void consumePurchaseAsync(IabNext next, IabProductDetails purchaseIabProductDetails, IabPurchase iabPurchase){
        logInfo (TAG + " " + "Consuming purchase "+purchaseIabProductDetails.getProductId());
        
        ConsumeParams consumeParams =
            ConsumeParams.newBuilder()
            .setPurchaseToken(iabPurchase.getPurchaseToken())
            .build();
            
        mBillingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                try {
                    logInfo (TAG + "PURCHASES" + " " + "Received consume response for "+purchaseIabProductDetails.getProductId());
                    // check billingResult
                    IabResult successResult = failBillingResponseNotOk(billingResult, next);
                    // handle
                    iabPurchase.updatePurchaseCompletion();
                    //logInfo(next.inAppBilling.iabHelperInventory.toString());
                    next.OnNext(successResult);
                } catch (Exception e){
                    logInfo("BILLING ERROR: "+e);
                    next.OnError(new IabResult(IABHELPER_UNKNOWN_ERROR, e.toString()));
                }
            }});
            
    }

    /* Acknowledge a purchase */
    public void acknowledgePurchaseAsync(IabNext next, IabProductDetails purchaseIabProductDetails, IabPurchase iabPurchase){
        logInfo (TAG + " " + "Acknowledging purchase "+purchaseIabProductDetails.getProductId());
            
        AcknowledgePurchaseParams acknowledgePurchaseParams =
            AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(iabPurchase.getPurchaseToken())
            .build();
            
        mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                try {
                    logInfo (TAG + "PURCHASES" + " " + "Received acknowledge response for "+purchaseIabProductDetails.getProductId());
                    // check billingResult
                    IabResult successResult = failBillingResponseNotOk(billingResult, next);
                    // handle
                    iabPurchase.updatePurchaseCompletion();
                    //logInfo(next.inAppBilling.iabHelperInventory.toString());
                    next.OnNext(successResult);
                } catch (Exception e){
                    logInfo("BILLING ERROR: "+e);
                    next.OnError(new IabResult(IABHELPER_UNKNOWN_ERROR, e.toString()));
                }
            }
        });
    }

    public String toString(){
        String ret = "IabHelper {\n";
        ret += "Async in progress: "+Boolean.toString(mAsyncInProgress)+"\n";
        ret += "Async operation: "+mAsyncOperation+"\n";
        ret += "}";
        return ret;
    }
    
}
