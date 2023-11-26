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

import com.android.billingclient.api.Purchase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an in-app billing purchase.
 */
public class IabPurchase {
    
    Purchase mPurchase;
    
    String mProductType;  //inapp or subs
    String mProductId;
    long mPurchaseTime;
    int mQuantity;
    int mPurchaseState;
    String mOrderId;
    
    //verify and complete: consume/acknowledge
    String mSignature;
    String mOriginalJson;
    String mPurchaseToken;
    
    //state info
    boolean mAcknowledged;
    boolean mCompleted;
    boolean mVerified;
    String mVerifyFailMessage;
    boolean mIsConsumable = false; //manually set to true
    
    //more
    String mPackageName;
    String mDeveloperPayload;
    int mHashCode;
    
    public IabPurchase(Purchase p, String productType){
        mPurchase = p;
        mProductType = productType;
        mProductId = p.getProducts().get(0); //only supporting purchasing 1 at a time, possible to order multiple inapp
        mPurchaseTime = p.getPurchaseTime();
        mOrderId = p.getOrderId();
        
        mSignature = p.getSignature();
        mOriginalJson = p.getOriginalJson();
        mPurchaseToken = p.getPurchaseToken();
        
        mPurchaseState = p.getPurchaseState();
        mQuantity = p.getQuantity();
        mPackageName = p.getPackageName();
        mDeveloperPayload = p.getDeveloperPayload();
        mHashCode = p.hashCode();
        
        mAcknowledged = p.isAcknowledged();
        mCompleted = (p.isAcknowledged() || (mDeveloperPayload != null && mDeveloperPayload.length() > 0)); //only acknowledging with consume command for consumables.....
    }
    
    public void updatePurchaseCompletion(boolean completed){
        //isacknowledged, developer payload
        mCompleted = completed;
    }
    public void updatePurchaseCompletion(){
        mCompleted = true;
    }
    
    // Verify signature
    public boolean verifyPurchase(String iabSignatureBase64){
        boolean verified = true;
        try {
            if (!IabSecurity.verifyPurchase(iabSignatureBase64, mOriginalJson, mSignature)) {
                mVerifyFailMessage = "Purchase signature verification FAILED for product id " + mProductId; //only supporting purchasing 1 at a time, possible to order multiple inapp;
                if (IabSecurity.hasDecodeFailMessage()) mVerifyFailMessage += ", DECODING Google Billing Key failed: "+IabSecurity.getFailDecodeMessage();
                else if (IabSecurity.hasVerifyFailMessage()) mVerifyFailMessage += ", VERIFYING purchase failed: "+IabSecurity.getFailVerifyMessage();
                verified = false;
            }
        } catch (Exception err) {
            mVerifyFailMessage = "ERROR encountered during signature verification: "+err;
            verified = false;
        }
        mVerified = verified;
        return mVerified;
    }
    
    public String getProductType() { return mProductType; }
    public String getProductId() { return mProductId; }
    public long getPurchaseTime() { return mPurchaseTime; }
    public String getOrderId() { return mOrderId; }
    public String getSignature() { return mSignature; }
    public String getOriginalJson() { return mOriginalJson; }
    public String getPurchaseToken() { return mPurchaseToken; }
    public boolean getCompleted() { return mCompleted; }
    public int getPurchaseState() { return mPurchaseState; }
    public boolean getPending(){ return getPurchaseState() != Purchase.PurchaseState.PURCHASED; }
    public int getQuantity(){ return mQuantity; }
    public String getPackageName() { return mPackageName; }
    public String getDeveloperPayload() { return mDeveloperPayload; }
    public int getHashCode() { return mHashCode; }
    public boolean getPurchaseVerified(){ return mVerified; }
    public String getPurhcaseVerifyFailMessage(){ return mVerifyFailMessage; }
    public boolean getPurchaseAcknowledged(){ return mAcknowledged; }
    public boolean getIsConsumable(){ return mIsConsumable; }
    public void setIsConsumable(boolean consumable){ mIsConsumable = consumable; }
    
    public JSONObject getDetailsJSON() throws JSONException {
        JSONObject detailsJson = new JSONObject();
        detailsJson.put("productId", getProductId());
        detailsJson.put("productType", getProductType());
        detailsJson.put("purchaseTime", getPurchaseTime());
        detailsJson.put("purchaseToken", getPurchaseToken());
        detailsJson.put("purchaseId", getOrderId());
        detailsJson.put("quantity", getQuantity());
        detailsJson.put("verified", getPurchaseVerified());
        detailsJson.put("pending", getPending());
        detailsJson.put("completed", getCompleted());
        return detailsJson;
    }
    
    @Override
    public String toString() {
        return "IabPurchase { \n"
        + " mProductType: " + mProductType  + "\n"
        + " mProductId: " + mProductId + "\n"
        + " mPurchaseTime: " + Long.toString(mPurchaseTime) + "\n"
        + " mPurchaseId/OrderId: " + mOrderId + "\n"
        + " mCompleted: " + Boolean.toString(mCompleted) + "\n"
        + " mSignature: " + mSignature + "\n"
        + " mOriginalJson: " + mOriginalJson.toString() + "\n"
        + " mPurchaseToken: " + mPurchaseToken + "\n"
        + " mPurchaseState: " + Integer.toString(mPurchaseState) + "\n"
        + " mQuantity: " + Integer.toString(mQuantity) + "\n"
        + " mPackageName: " + mPackageName + "\n"
        + " mDeveloperPayload: " + mDeveloperPayload + "\n"
        + " mHashCode: " + Integer.toString(mHashCode) + "\n"
        + " mVerified: " + Boolean.toString(mVerified) + "\n"
        + " mVerifyFailMessage: " + mVerifyFailMessage + "\n"
        + "}\n";
        //         if(mPurchase != null) {
        //             return "IabPurchase:" + mPurchase;
        //         } else {
        //             return "IabPurchase Product:" + mPurchase;
        //         }
    }
    
}
