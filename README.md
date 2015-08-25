Print PDF plugin for Cordova / PhoneGap
======================================================

This plugin brings up a native overlay to print a PDF document using [AirPrint](http://en.wikipedia.org/wiki/AirPrint) for iOS and Android Printing APIs on Android devices running 4.4 or later. **If you need to support printing on an Android device running 4.3 or less, checkout the `google-cloud-print` branch.**

## Usage

### print()

Use the `print` function to print an encoded document via a native printing interface.

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

	success: null,	// success callback function, argument is a json string.
	 				// parsed json format:
					// {success: true}

	error: null		// error callback function, argument is a json string.
	 				// parsed json format:
					// {success: [boolean], available: [boolean], error: [string], dismissed: [boolean]}
	
};
```

### dismiss()

Use the `dismiss` function to programmatically dismiss the print dialog (iOS only). There are no options that can be passed in. When the dialog is dismissed it will trigger the error callback that was set for the `print()` function with the `dismissed` parameter set to `true`.

```
window.plugins.PrintPDF.dismiss();
```

### isPrintingAvailable()

Use the `isPrintingAvailable` function to check if native printing is supported and available.

```
window.plugins.PrintPDF.isPrintingAvailable(options);
```

The default options object
```
var options = {

	success: null	// success callback function, argument is a boolean.
					// e.g. function(isAvailable) {
					// 			console.log('printing is available: '+ isAvailable);
					// }
	
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