/**
 * @constructor
 */
var PrintPDF = function () {
	this.BASE64_METHOD = 'printWithData';
	this.IS_AVAILABLE_METHOD = 'isPrintingAvailable';
	this.CLASS = 'PrintPDF';
};

PrintPDF.prototype.print = function(options) {
	
	options = options || {};
		
	var data = options.data; // print data, base64 string (required)
	
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
		
		args.push(dialogX);
		args.push(dialogY);
	
    } else {
	
		args.push(title);
		
	}
	
	// make the call
    cordova.exec(successCallback, errorCallback, this.CLASS, this.BASE64_METHOD, args);
		
};

PrintPDF.prototype.isPrintingAvailable = function (options) {
	
	options = options || {};
	
	// make sure callbacks are functions or reset to null
	var successCallback = (options.success && typeof(options.success) === 'function') ? options.success : this.defaultCallback; 
	
	var errorCallback = (options.error && typeof(options.error) === 'function') ? options.error : this.defaultCallback;
	
    cordova.exec(successCallback, errorCallback, this.CLASS, this.IS_AVAILABLE_METHOD, []);

};

PrintPDF.prototype.defaultCallback = function (e) {
	console.log(e);
};

// Plug in to Cordova
cordova.addConstructor(function () {
    if (!window.Cordova) {
        window.Cordova = cordova;
    };

    if (!window.plugins) window.plugins = {};
    window.plugins.PrintPDF = new PrintPDF();
});
