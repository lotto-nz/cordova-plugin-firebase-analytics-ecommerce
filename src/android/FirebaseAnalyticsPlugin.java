package by.chemerisuk.cordova.firebase;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics.Event;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import java.util.ArrayList;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


public class FirebaseAnalyticsPlugin extends CordovaPlugin {
    private static final String TAG = "FirebaseAnalyticsPlugin";

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Firebase Analytics plugin");

        Context context = this.cordova.getActivity().getApplicationContext();

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("logEvent".equals(action)) {
            logEvent(callbackContext, args.getString(0), args.getJSONObject(1));

            return true;
        } else if ("logPurchases".equals(action)) {
            JSONArray products = args.getJSONArray(0);
            JSONObject transactionDetails = args.getJSONObject(1);
            logPurchases(callbackContext, products, transactionDetails);

            return true;
        } else if ("setUserId".equals(action)) {
            setUserId(callbackContext, args.getString(0));

            return true;
        } else if ("setUserProperty".equals(action)) {
            setUserProperty(callbackContext, args.getString(0), args.getString(1));

            return true;
        } else if ("setEnabled".equals(action)) {
            setEnabled(callbackContext, args.getBoolean(0));

            return true;
        } else if ("setCurrentScreen".equals(action)) {
            setCurrentScreen(callbackContext, args.getString(0));

            return true;
        }

        return false;
    }

    private void logEvent(CallbackContext callbackContext, String name, JSONObject params) throws JSONException {
        Bundle bundle = new Bundle();
        Iterator iter = params.keys();

        while (iter.hasNext()) {
            String key = (String) iter.next();
            Object value = params.get(key);

            if (value instanceof Integer || value instanceof Double) {
                bundle.putFloat(key, ((Number) value).floatValue());
            } else {
                bundle.putString(key, value.toString());
            }
        }

        this.firebaseAnalytics.logEvent(name, bundle);

        callbackContext.success();
    }

    // Code copied from cordova ionic ecommerce plugin - https://github.com/JABJustaBaby/cordova-plugins-firebaseecommerceanalytics/blob/master/src/android/FirebaseEcommerceAnalytics.java
    private void logPurchases(CallbackContext callbackContext, JSONArray products, JSONObject transactionOptions) {
        try {
            // create a list of items(products) needs to be added to ecommerceBundle
            ArrayList items = new ArrayList();

            if (transactionOptions.getString("transaction_id").isEmpty()
                    || transactionOptions.getString("affiliation").isEmpty()
                    || transactionOptions.getString("value").isEmpty()
                    || transactionOptions.getString("currency").isEmpty()) {
                callbackContext.error("Required param transaction_id or affiliation or value or currency is empty");
                return;
            }

            for (int i = 0, size = products.length(); i < size; i++) {
                JSONObject product = products.getJSONObject(i);
                if (product.getString("item_id").isEmpty()
                        || product.getString("item_name").isEmpty()) {
                    callbackContext.error("Required param item_id or item_name or quantiry is empty");
                    return;
                } else {
                    Bundle a = this.getProductBundle(product, i, true);
                    items.add(a);
                }
            }
            Bundle ecommerceBundle = new Bundle();
            ecommerceBundle.putParcelableArrayList("items", items);

            // Set relevant transaction-level parameters

            ecommerceBundle.putString(Param.ITEM_LIST, transactionOptions.getString("item_list"));
            ecommerceBundle.putString(Param.TRANSACTION_ID, transactionOptions.getString("transaction_id"));
            ecommerceBundle.putString(Param.AFFILIATION, transactionOptions.getString("affiliation"));
            ecommerceBundle.putDouble(Param.VALUE, transactionOptions.getDouble("value"));
            ecommerceBundle.putString(Param.CURRENCY, transactionOptions.getString("currency"));

            // Log ecommerce_purchase event with ecommerce bundle

            this.firebaseAnalytics.logEvent(Event.ECOMMERCE_PURCHASE, ecommerceBundle);
            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }
    
    private void setUserId(CallbackContext callbackContext, String userId) {
        this.firebaseAnalytics.setUserId(userId);

        callbackContext.success();
    }

    private void setUserProperty(CallbackContext callbackContext, String name, String value) {
        this.firebaseAnalytics.setUserProperty(name, value);

        callbackContext.success();
    }

    private void setEnabled(CallbackContext callbackContext, boolean enabled) {
        this.firebaseAnalytics.setAnalyticsCollectionEnabled(enabled);

        callbackContext.success();
    }

    private void setCurrentScreen(final CallbackContext callbackContext, final String screenName) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                firebaseAnalytics.setCurrentScreen(
                    cordova.getActivity(),
                    screenName,
                    null
                );

                callbackContext.success();
            }
        });
    }

    private Bundle getProductBundle(JSONObject product, int index, boolean quantity) {
        Bundle a = new Bundle();
        try {
            a.putString(Param.ITEM_ID, product.getString("item_id"));
            a.putString(Param.ITEM_NAME, product.getString("item_name"));
            if (!product.getString("item_category").isEmpty()) {
                a.putString(Param.ITEM_CATEGORY, product.getString("item_category"));
            }

            a.putDouble(Param.PRICE, product.getDouble("price"));

            if (!product.getString("currency").isEmpty()) {
                a.putString(Param.CURRENCY, product.getString("currency"));
            }

            // index = -1. its a product details page, no need for index or quantity
            if (index != -1) {
                if (!quantity) {
                    a.putLong(Param.INDEX, product.getLong("index"));

                } else {
                    a.putLong(Param.QUANTITY, product.getLong("quantity"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return a;
    }

}
