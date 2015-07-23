Print PDF plugin for Cordova / PhoneGap
======================================================

This plugin brings up a native overlay to print a PDF document using [AirPrint](http://en.wikipedia.org/wiki/AirPrint) for iOS and [Google Cloud Print](http://www.google.com/landing/cloudprint/) for Android.

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

	error: null		// error callback function, argument format:
					// {success: [boolean], available: [boolean], error: [string]}
	
};
```

## Example:

```
var encodedString = 'base64encodedStringHere';
window.plugins.PrintPDF.print({
	data: encodedString,
	title: 'Sushi Rock Menu',
	success: function(){
		console.log('success');
	},
	error: function(data){
		console.log('failed: ' + data.error);
	}
});
```