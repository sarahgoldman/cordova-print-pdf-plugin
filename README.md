Print PDF plugin for Cordova / PhoneGap
======================================================

This plugin brings up a native overlay to print a PDF document using [AirPrint](http://en.wikipedia.org/wiki/AirPrint) for iOS and Android Printing APIs on Android devices running 4.4 or later. **If you need to support printing on an Android device running 4.3 or less, checkout the `google-cloud-print` branch.

## Usage

```
window.plugins.PrintPDF.print(options);
```

The default options object
```
var options = {

	data: null, 	// print data, base64 string (required)

	title: '', 		// title of document

	dialogX: -1,	// if a dialog coord is not set, it defaults to -1.
					// the iOS method will fall back to center if it gets
	dialogY: -1,	// a dialog coord less than 0. (iPad only)

	success: null,	// success callback function

	error: null		// error callback function, returns json as a string.
	 				// parsed json format:
					// {success: [boolean], available: [boolean], error: [string], dismissed: [boolean]}
	
};
```

## Example:

```
var encodedString = 'base64encodedStringHere';
window.plugins.PrintPDF.print({
	data: encodedString,
	title: 'Print Document',
	success: function(){
		console.log('success');
	},
	error: function(data){
		data = JSON.parse(data);
		console.log('failed: ' + data.error);
	}
});
```