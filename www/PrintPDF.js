 /**
 * @constructor
 */
var PrintPDF = function () {
	this.METHOD = 'printDocument';
	this.IS_AVAILABLE_METHOD = 'isPrintingAvailable';
	this.DISMISS_METHOD = 'dismissPrintDialog';
	this.CLASS = 'PrintPDF';
};

PrintPDF.prototype.print = function(options) {

	options = options || {};

	var data = options.data; // print data, base64 string (required)

	var type = options.type || 'Data'; // type of document

	var title = options.title || 'Print Document'; // title of document

	var dialogX = options.dialogX || -1; // if a dialog coord is not set, default to -1.
										  // the iOS method will fall back to center on the
	var dialogY = options.dialogY || -1; // screen if it gets a dialog coord less than 0.

	// make sure callbacks are functions or reset to null
	var successCallback = (options.success && typeof(options.success) === 'function') ? options.success : this.defaultCallback;

	var errorCallback = (options.error && typeof(options.error) === 'function') ? options.error : this.defaultCallback;

	// make sure data is set
	if (!data) {
		if (errorCallback) {
			errorCallback({
				success: false,
				error: "Parameter 'data' is required."
			});
		}
		return false;
	}

	var args = [data];

	if (device.platform === "iOS") {
		args.push(type);
		args.push(dialogX);
		args.push(dialogY);

    } else {
		args.push(type);
		args.push(title);

	}

	// make the call
    cordova.exec(successCallback, errorCallback, this.CLASS, this.METHOD, args);

};

PrintPDF.prototype.isPrintingAvailable = function (callback) {

	// make sure callbacks are functions or reset to null
	var successCallback = (callback && typeof(callback) === 'function') ? callback : this.defaultCallback;

    cordova.exec(successCallback, null, this.CLASS, this.IS_AVAILABLE_METHOD, []);

};

PrintPDF.prototype.dismiss = function () {

	// Dismiss is only an iOS method because the dialog exists in the
	// same context as the cordova activity. In Android, when the
	// print activity starts, the cordova activity is paused.

	if (device.platform === "iOS") {

	    cordova.exec(null, null, this.CLASS, this.DISMISS_METHOD, []);

	}

};

PrintPDF.prototype.defaultCallback = null;

// Plug in to Cordova
cordova.addConstructor(function () {
    if (!window.Cordova) {
        window.Cordova = cordova;
    };

    if (!window.plugins) window.plugins = {};
    window.plugins.PrintPDF = new PrintPDF();
});
