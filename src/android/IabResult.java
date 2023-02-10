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

import com.android.billingclient.api.BillingClient.BillingResponseCode;

/**
 * Represents the result of an in-app billing operation.
 * A result is composed of a response code (an integer) and possibly a
 * message (String). You can get those by calling
 * {@link #getResponse} and {@link #getMessage()}, respectively. You
 * can also inquire whether a result is a success or a failure by
 * calling {@link #isSuccess()} and {@link #isFailure()}.
 */
public class IabResult {
    
    int mIabCode;
    String mMessage;
    int mResponseCode;
    String mResponseMessage;

    public IabResult(int iabCode, String message) {
        setResult(iabCode,message);
    }
    public IabResult(int iabCode, String message, String responseMessage) {
        setResult(iabCode,message);
        mResponseMessage = responseMessage;
    }
    public IabResult(int iabCode, int responseCode, String message, String responseMessage) {
        setResult(iabCode,message);
        mResponseCode = responseCode;
        mResponseMessage = responseMessage;
    }
    public IabResult(){
        mResponseCode = BillingResponseCode.OK;
        mMessage = "Billing query was successful";
    }
    private void setResult(int iabCode, String message){
        mIabCode = iabCode;
        mMessage = message;
        String iabHelperMessage = IabHelper.getIabHelperErrorMessage(mIabCode);
        if (iabHelperMessage != null){
            if (mMessage.length() == 0) mMessage = iabHelperMessage;
            else mMessage = iabHelperMessage+" - "+mMessage;
        }
    }
    
    public int getIabCode() { return mIabCode; }
    public int getResponseCode() { return mResponseCode; }
    public String getMessage() { return mMessage; }
    public boolean hasResponseMessage() { return mResponseMessage != null; }
    public String getResponseMessage() { return mResponseMessage; }
    public boolean isSuccess() { return mResponseCode == BillingResponseCode.OK; }
    public boolean isFailure() { return !isSuccess(); }
    public String toString() { return "IabResult: " + getMessage(); }
}

