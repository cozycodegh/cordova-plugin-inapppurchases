/** cordova-plugin-inapppurchases MIT © 2023 cozycode.ca **/

package com.alexdisler_github_cozycode.inapppurchases;

import android.app.Activity;
import android.content.Context;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;

/**
 * IabNext
 * link asynchronous calls with error messages
 **/
//abstract
public class IabNext  {
    
    protected IabNext thisNext;
    protected IabNext mNext;
    protected InAppBilling inAppBilling;
    protected Activity activityContext;
    protected CallbackContext callbackContext;
    protected JSONArray args;
    protected String action;
    private int mFailErrorCode;
    private String mFailAppendMessage = "";
    private String mArgsProductId;
    private String mArgsSubscriptionUpgradeProductId = "";
    private int mArgsSubscriptionReplacementMode = -1;
    private boolean mArgsConsumable;
    private List<String> mArgsProductIds;
    public IabInventory tempInv;
    public String name = "next";
    
    //main
    public IabNext(InAppBilling theInAppBilling, Activity theAcivityContext, CallbackContext theCallbackContext, JSONArray theArgs, String theAction){
        thisNext = this;
        inAppBilling = theInAppBilling;
        activityContext = theAcivityContext;
        callbackContext = theCallbackContext;
        args = theArgs;
        action = theAction;
        mFailErrorCode = inAppBilling.UNKNOWN_ERROR;
    }
    public IabNext(IabNext nextNext){
        thisNext = this;
        mNext = nextNext;
        inAppBilling = mNext.inAppBilling;
        activityContext = mNext.activityContext;
        callbackContext = mNext.callbackContext;
        args = mNext.args;
        action = mNext.action;
        mFailErrorCode = inAppBilling.UNKNOWN_ERROR;
    }
    //set different error message
    public void setErrorInfo(int errorCode, String appendStr){
        mFailErrorCode = errorCode;
        mFailAppendMessage = appendStr;
    }
    
    //next - extend to next call to link async calls with error checking
    //public abstract void OnNext();
    //public abstract void OnNext(IabResult result);
    public void OnNext(){}
    public void OnNext(IabResult result){
        if (inAppBilling == null) return;
        if (inAppBilling.iabHelper == null) return;
        inAppBilling.iabHelper.logWarning("InAppBilling next not implemented");
        inAppBilling.iabHelper.flagEndAsync();
        callbackContext.error(inAppBilling.makeError(inAppBilling.UNKNOWN_ERROR, "Developer Billing Error: InAppBilling's OnNext not implemented"));
    }
    public void OnNext(IabResult result, IabInventory inventory){
        if (inAppBilling == null) return;
        if (inAppBilling.iabHelper == null) return;
        inAppBilling.iabHelper.logWarning("InAppBilling next not implemented");
        inAppBilling.iabHelper.flagEndAsync();
        callbackContext.error(inAppBilling.makeError(inAppBilling.UNKNOWN_ERROR, "Developer Billing Error: InAppBilling's OnNext was not implemented"));
    }
    //error - error back to cordova
    public void OnError(boolean endAsync, int errorCode, String appendStr){
        setErrorInfo(errorCode, appendStr);
        if (endAsync) inAppBilling.iabHelper.flagEndAsync();
        callbackContext.error(inAppBilling.makeError(mFailErrorCode,mFailAppendMessage));
    }
    public void OnError(int errorCode, IabResult result){ //finished operation and do not need to dispose
        mFailErrorCode = errorCode;
        OnError(result);
    }
    public void OnError(IabResult result){ //finished operation and do not need to dispose
        if (inAppBilling.iabHelper != null){
            inAppBilling.iabHelper.logError("Error during "+action+": "+result.getMessage());
            inAppBilling.iabHelper.flagEndAsync();
            //this.callbackContext.error(inAppBilling.makeError(mFailAppendMessage, mFailErrorCode, result));
            this.callbackContext.error(inAppBilling.makeError(result));
        } else {
            this.callbackContext.error(result.getMessage());
        }
    }
    public boolean checkResultFail(IabResult result){
        if (result.isFailure()){
            OnError(result);
            return true;
        }
        return false;
    }
    
    //Inventory and args
    public String getArgsProductId(){
        return getArgsProductId(false);
    }
    public String getArgsProductId(boolean force){
        if (mArgsProductId != null) return mArgsProductId;
        String productId;
        if (args.length() == 0){
            if (force){
                inAppBilling.iabHelper.flagEndAsync();
                callbackContext.error(inAppBilling.makeError(inAppBilling.INVALID_ARGUMENTS,"Invalid Product ID Argument - Missing Product Id Argument"));
            }
            return null;
        } else {
            try {
                productId = args.getString(0);
                //if (args.length() > 1) { developerPayload = args.getString(1); }
            } catch (JSONException e) {
                inAppBilling.iabHelper.flagEndAsync();
                callbackContext.error(inAppBilling.makeError(inAppBilling.INVALID_ARGUMENTS,"Unreadable Product ID "+e.toString()));
                return null;
            }
        }
        mArgsProductId = productId;
        return mArgsProductId;
    }
    public boolean getArgsConsumable(){
        return getArgsConsumable(false);
    }
    public boolean getArgsConsumable(boolean force){
        if (mArgsConsumable) return mArgsConsumable;
        if (args.length() < 2){
            inAppBilling.iabHelper.logError("No args consumable was found");
            if (force){
                inAppBilling.iabHelper.flagEndAsync();
                callbackContext.error(inAppBilling.makeError(inAppBilling.INVALID_ARGUMENTS, "Missing 2nd argument, consume"));
                return false;
            }
            return false;
        }
        boolean consumable;
        try {
            consumable = (Boolean) args.get(1);
            //if (args.length() > 1) { developerPayload = args.getString(1); }
        } catch (JSONException e) {
            inAppBilling.iabHelper.flagEndAsync();
            callbackContext.error(inAppBilling.makeError(inAppBilling.INVALID_ARGUMENTS, "Invalid consume argument"+e.toString()));
            return false;
        }
        mArgsConsumable = consumable;
        return mArgsConsumable;
    }
    public String getArgsSubscriptionUpgradeProductId(){
        return getArgsSubscriptionUpgradeProductId(false);
    }
    public String getArgsSubscriptionUpgradeProductId(boolean force){
        if (mArgsSubscriptionUpgradeProductId != "") return mArgsSubscriptionUpgradeProductId;
        if (args.length() < 2){
            if (force){
                inAppBilling.iabHelper.logError("No args subscription replacement product id was found");
                inAppBilling.iabHelper.flagEndAsync();
                callbackContext.error(inAppBilling.makeError(inAppBilling.INVALID_ARGUMENTS, "Missing 2nd argument, subscription product id to replace"));
                return "";
            }
            return "";
        }
        String subscriptionUpgradeProductId;
        try {
            subscriptionUpgradeProductId = (String) args.get(1);
            //if (args.length() > 1) { developerPayload = args.getString(1); }
        } catch (JSONException e) {
            inAppBilling.iabHelper.flagEndAsync();
            callbackContext.error(inAppBilling.makeError(inAppBilling.INVALID_ARGUMENTS, "Invalid subscription product id argument"+e.toString()));
            return "";
        }
        mArgsSubscriptionUpgradeProductId = subscriptionUpgradeProductId;
        return mArgsSubscriptionUpgradeProductId;
    }
    public int getArgsSubscriptionReplacementMode(){
        return getArgsSubscriptionReplacementMode(false);
    }
    public int getArgsSubscriptionReplacementMode(boolean force){
        if (mArgsSubscriptionReplacementMode != -1) return mArgsSubscriptionReplacementMode;
        if (args.length() < 3){
            if (force){
                inAppBilling.iabHelper.logError("No args subscription replacement mode was found");
                inAppBilling.iabHelper.flagEndAsync();
                callbackContext.error(inAppBilling.makeError(inAppBilling.INVALID_ARGUMENTS, "Missing 3rd argument, subscription replacement mode"));
                return -1;
            }
            return -1;
        }
        int subscriptionReplacementMode;
        try {
            subscriptionReplacementMode = (Integer) args.get(2);
            //if (args.length() > 1) { developerPayload = args.getString(1); }
        } catch (JSONException e) {
            inAppBilling.iabHelper.flagEndAsync();
            callbackContext.error(inAppBilling.makeError(inAppBilling.INVALID_ARGUMENTS, "Invalid subscription replacement mode argument"+e.toString()));
            return -1;
        }
        mArgsSubscriptionReplacementMode = subscriptionReplacementMode;
        return mArgsSubscriptionReplacementMode;
    }
    public List<String> getArgsProductIds(){
        return getArgsProductIds(false);
    }
    public List<String> getArgsProductIds(boolean force){
        if (mArgsProductIds != null) return mArgsProductIds;
        List<String> argsProductIds = new ArrayList<String>();
        for (int i = 0; i < args.length(); i++) {
            try {
                String productId = args.getString(i);
                if (productId.equals("")) break;
                argsProductIds.add(productId);
                if (inAppBilling.iabHelper != null) inAppBilling.iabHelper.logInfo("read in product id #"+Integer.toString(i)+": \"" + args.getString(i)+"\"");
            } catch (JSONException e) {
                inAppBilling.iabHelper.flagEndAsync();
                callbackContext.error(inAppBilling.makeError(inAppBilling.INVALID_ARGUMENTS,"Invalid Product ID (#"+Integer.toString(i)+")"));
                return null;
            }
        }
        mArgsProductIds = argsProductIds;
        if (force){
            boolean empty = false;
            if (mArgsProductIds == null) empty = true;
            else if (mArgsProductIds.size() == 0) empty = true;
            if (empty){
                inAppBilling.iabHelper.flagEndAsync();
                callbackContext.error(inAppBilling.makeError(inAppBilling.INVALID_ARGUMENTS,"Invalid Product ID List Argument - Missing Product Ids in List Argument"));
                return null;
            }
        }
        return mArgsProductIds;
    }
    public List<String> getAllProductIds(){
        return inAppBilling.iabHelperInventory.combineProductIdLists(
            inAppBilling.iabHelperInventory.getAllProductIds(),getArgsProductIds());
    }
    
    @Override
    public String toString(){
        return "InAppBilling Next ("+name+") { on: "+action+" for args: "+args.toString()+" }";
    }
}
