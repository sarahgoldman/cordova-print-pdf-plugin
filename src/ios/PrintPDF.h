//
//  PrintPDF.h
//  Print PDF
//
//  Created by Sarah Goldman (github.com/sarahgoldman) on 07/06/2015.
//  Copyright 2015 Sarah Goldman. All rights reserved.
//  MIT licensed
//

#import <Foundation/Foundation.h>


#import <Cordova/CDVPlugin.h>


@interface PrintPDF : CDVPlugin {
	NSString* successCallback;
	NSString* failCallback;
}

@property (nonatomic, copy) NSString* successCallback;
@property (nonatomic, copy) NSString* failCallback;

- (void)isPrintingAvailable: (CDVInvokedUrlCommand*)command;
- (void)printWithURL:(CDVInvokedUrlCommand*)command;
- (void)printWithData:(CDVInvokedUrlCommand*)command;

@end