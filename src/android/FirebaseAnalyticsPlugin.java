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
        } else if ("logEcommerceEvent".equals(action)) {
            logEcommerceEvent(callbackContext, args.getString(0), args.getJSONObject(1));

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

    /*
    private void logEcommerceEvent(CallbackContext callbackContext, String name, JSONObject params) throws JSONException {
        // Define product with relevant parameters

        Bundle product1 = new Bundle();
        product1.putString( Param.ITEM_ID, "sku1234"); // ITEM_ID or ITEM_NAME is required
        product1.putString( Param.ITEM_NAME, "Donut Friday Scented T-Shirt");
        product1.putString( Param.ITEM_CATEGORY, "Apparel/Men/Shirts");
        product1.putString( Param.ITEM_VARIANT, "Blue");
        product1.putString( Param.ITEM_BRAND, "Google");
        product1.putDouble( Param.PRICE, 29.99 );
        product1.putString( Param.CURRENCY, "USD" ); // Item-level currency unused today
        product1.putLong( Param.QUANTITY, 1 );

        Bundle product2 = new Bundle();
        product2.putString( Param.ITEM_ID, "sku5678");
        product2.putString( Param.ITEM_NAME, "Android Workout Capris");
        product2.putString( Param.ITEM_CATEGORY, "Apparel/Women/Pants");
        product2.putString( Param.ITEM_VARIANT, "Black");
        product2.putString( Param.ITEM_BRAND, "Google");
        product2.putDouble( Param.PRICE, 39.99 );
        product2.putString( Param.CURRENCY, "USD" ); // Item-level currency unused today
        product2.putLong( Param.QUANTITY, 1 );

        // Prepare ecommerce bundle

        ArrayList items = new ArrayList();
        items.add(product1);
        items.add(product2);

        Bundle ecommerceBundle = new Bundle();
        ecommerceBundle.putParcelableArrayList( "items", items );

        // Set relevant transaction-level parameters

        ecommerceBundle.putString( Param.TRANSACTION_ID, "T12345" );
        ecommerceBundle.putString( Param.AFFILIATION, "Google Store - Online" );
        ecommerceBundle.putDouble( Param.VALUE, 37.39 );        // Revenue
        ecommerceBundle.putDouble( Param.TAX, 2.85 );
        ecommerceBundle.putDouble( Param.SHIPPING, 5.34 );
        ecommerceBundle.putString( Param.CURRENCY, "USD" );
        ecommerceBundle.putString( Param.COUPON, "SUMMER2017" );

        // Log ecommerce_purchase event with ecommerce bundle

        this.firebaseAnalytics.logEvent( Event.ECOMMERCE_PURCHASE, ecommerceBundle );
    }*/
    
     private void logPurchases(JSONArray products, JSONObject transactionOptions, CallbackContext callbackContext) {
        try {
            // create a list of items(products) needs to be added to ecommerceBundle
            ArrayList items = new ArrayList();

            if (transactionOptions.getString("TRANSACTION_ID").isEmpty()
                    || transactionOptions.getString("AFFILIATION").isEmpty()
                    || transactionOptions.getString("CURRENCY").isEmpty()) {
                callbackContext.error("Required param TRANSACTION_ID or AFFILIATION or VALUE or CURRENCY is empty");
                return;
            }

            for (int i = 0, size = products.length(); i < size; i++) {
                JSONObject product = products.getJSONObject(i);
                if (product.getString("ITEM_ID").isEmpty() || product.getString("ITEM_NAME").isEmpty()) {
                    callbackContext.error("Required param ITEM_ID or ITEM_NAME or QUANTITY is empty");
                    return;
                } else {
                    Bundle a = this.getProductBundle(product, i, true);
                    items.add(a);
                }
            }
            Bundle ecommerceBundle = new Bundle();
            ecommerceBundle.putParcelableArrayList("items", items);

            // Set relevant transaction-level parameters

            ecommerceBundle.putString(Param.TRANSACTION_ID, transactionOptions.getString("TRANSACTION_ID"));
            ecommerceBundle.putString(Param.AFFILIATION, transactionOptions.getString("AFFILIATION"));
            ecommerceBundle.putDouble(Param.VALUE, transactionOptions.getDouble("VALUE")); // Revenue
            ecommerceBundle.putString(Param.CURRENCY, transactionOptions.getString("CURRENCY"));

            // if(!transactionOptions.isNull("TAX")) {
            // ecommerceBundle.putDouble(Param.TAX, transactionOptions.getDouble("TAX"));
            // }
            // if(!transactionOptions.isNull("SHIPPING")) {
            // ecommerceBundle.putDouble(Param.SHIPPING,
            // transactionOptions.getDouble("SHIPPING"));
            // }
            // if(!transactionOptions.isNull("COUPON")) {
            // ecommerceBundle.putString(Param.COUPON,
            // transactionOptions.getString("COUPON"));
            // }

            ecommerceBundle.putDouble(Param.TAX, transactionOptions.getDouble("TAX"));
            ecommerceBundle.putDouble(Param.SHIPPING, transactionOptions.getDouble("SHIPPING"));
            ecommerceBundle.putString(Param.COUPON, transactionOptions.getString("COUPON"));

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
}
