Print PDF plugin for Cordova / PhoneGap
======================================================

This plugin brings up a native overlay to print a PDF document using [AirPrint](http://en.wikipedia.org/wiki/AirPrint) for iOS and [Google Cloud Print](http://www.google.com/landing/cloudprint/) for Android.

## Usage

**with public link to PDF**

```printWithURL( url, title, successCallback, failCallback )```

Example:

```
var url = 'http://www.sushirockva.com/media/docs/Sushi-Rock-Dinner.pdf';
var title = 'Sushi Rock Menu';
window.plugins.PrintPDF.printWithURL(url,title,function(){
	console.log('success');
},function(){
	console.log('fail');
});
```

**with PDF converted to base64 encoded string**

```printWithData( base64String, title, successCallback, failCallback )```

Example:

```
var data = 'base64encodedStringHere';
var title = 'Sushi Rock Menu';
window.plugins.PrintPDF.printWithData(data,title,function(){
	console.log('success');
},function(){
	console.log('fail');
});
```