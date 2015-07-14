Print PDF plugin for Cordova / PhoneGap
======================================================

This plugin brings up a native overlay to print a PDF document using [AirPrint](http://en.wikipedia.org/wiki/AirPrint) for iOS and [Google Cloud Print](http://www.google.com/landing/cloudprint/) for Android.

## Usage

```
window.plugins.PrintPDF.print();
```

The default options object
```
var options = {

	type: null, 	// must be either 'url' or 'base64' (required)

	data: null, 	// print data, either a url string or base64 string (required)

	title: '', 		// title of document

	dialogX: -1,	// if a dialog coord is not set, it defaults to -1.
					// the iOS method will fall back to center if it gets
	dialogY: -1,	// a dialog coord less than 0. (iPad only)

	success: null,	// success callback function

	error: null		// error callback function, argument format:
					// {success: [boolean], available: [boolean], error: [string]}
	
};
```

## URL Example:

```
window.plugins.PrintPDF.print({
	type: 'url',
	data: 'http://www.sushirockva.com/media/docs/Sushi-Rock-Dinner.pdf',
	title: 'Sushi Rock Menu',
	success: function(){
		console.log('success');
	},
	error: function(data){
		console.log('failed: ' + data.error);
	}
});
```

## Base64 Example:

```
var encodedString = 'base64encodedStringHere';
window.plugins.PrintPDF.print({
	type: 'base64',
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