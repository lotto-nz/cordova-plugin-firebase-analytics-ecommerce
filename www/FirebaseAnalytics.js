var exec = require("cordova/exec");
var PLUGIN_NAME = "FirebaseAnalytics";

module.exports = {
    logEvent: function(name, params) {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, PLUGIN_NAME, "logEvent", [name, params || {}]);
        });
    },

    logPurchases: function(products, transactionDetails) {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, PLUGIN_NAME, 'logPurchases', [products, transactionDetails] || {});
        });
    
        // logPurchases: function(name, params) {
        //     return new Promise(function(resolve, reject) {
        //         exec(resolve, reject, PLUGIN_NAME, "logEcommerceEvent", [name, params || {}]);
        //     });
        // },

        //logPurchases = function (products, transactionDetails, successCallback, errorCallback) {
            // const defaultTransactionObject = {
            //     TRANSACTION_ID: 'string',
            //     AFFILIATION: 'string',
            //     VALUE: 'number',
            //     TAX: 'number',
            //     SHIPPING: 'number',
            //     CURRENCY: 'string',
            // };
            // let defaultProductKeysArray = Object.keys(defaultTransactionObject);
            //exec(resolve, reject, PLUGIN_NAME, 'logPurchases', [products, transactionDetails] || {});

            //if (validateObject(defaultTransactionObject, defaultProductKeysArray, transactionDetails, errorCallback)) {
                
            //}
        //}
    },
    setUserId: function(userId) {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, PLUGIN_NAME, "setUserId", [userId]);
        });
    },
    setUserProperty: function(name, value) {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, PLUGIN_NAME, "setUserProperty", [name, value]);
        });
    },
    setEnabled: function(enabled) {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, PLUGIN_NAME, "setEnabled", [enabled]);
        });
    },
    setCurrentScreen: function(name) {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, PLUGIN_NAME, "setCurrentScreen", [name]);
        });
    }
};
