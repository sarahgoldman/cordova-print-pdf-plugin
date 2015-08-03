//
//  PrintPDF.m
//  Print PDF
//
//  Created by Sarah Goldman (github.com/sarahgoldman) on 07/06/2015.
//  Copyright 2015 Sarah Goldman. All rights reserved.
//  MIT licensed
//
#import "NSData+Base64.h"

#import "PrintPDF.h"

@interface PrintPDF (Private)
- (BOOL) isPrintServiceAvailable;
@end

@implementation PrintPDF

@synthesize successCallback, failCallback;

// Plugin Functions
 - (void) isPrintingAvailable:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:([self isPrintServiceAvailable] ? YES : NO)];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) printWithData:(CDVInvokedUrlCommand *)command
{
    NSString *pdfDataString = [command.arguments objectAtIndex:0];
    
    NSData *pdfData = [NSData dataFromBase64String:pdfDataString];

    if (![self isPrintServiceAvailable]){
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"{\"success\": false, \"available\": false}"];
	    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
 
    UIPrintInteractionController *printInteraction = [UIPrintInteractionController sharedPrintController];
    
    if (!printInteraction){
        return;
    }
    
    if ([UIPrintInteractionController isPrintingAvailable]){
        //Set the printer settings
        UIPrintInfo *printInfo = [UIPrintInfo printInfo];
        printInfo.outputType = UIPrintInfoOutputGeneral;
        printInteraction.printInfo = printInfo;
        printInteraction.showsPageRange = YES;
        printInteraction.printingItem = pdfData;
        
        void (^completionHandler)(UIPrintInteractionController *, BOOL, NSError *) =
        ^(UIPrintInteractionController *printController, BOOL completed, NSError *error) {
            CDVPluginResult* pluginResult = nil;
            if (!completed || error) {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"{\"success\": false, \"available\": true, \"error\": \"%@\"}", error.localizedDescription]];
            }
            else{
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"{\"success\": true}"];
            }
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        };
        
        if([UIDevice currentDevice].userInterfaceIdiom == UIUserInterfaceIdiomPad) {
            
            CGRect bounds = self.webView.bounds;

			NSInteger dialogX = [[command.arguments objectAtIndex:1] integerValue];
			NSInteger dialogY = [[command.arguments objectAtIndex:2] integerValue];
			
			if (dialogX < 0) {
				dialogX = (bounds.size.width / 2);
			}
			
			if (dialogY < 0) {
				dialogY = (bounds.size.width / 2);
			}
            
            [printInteraction presentFromRect:CGRectMake(dialogX, dialogY, 0, 0) inView:self.webView animated:YES completionHandler:completionHandler];
            
        }
        else {
            [printInteraction presentAnimated:YES completionHandler:completionHandler];
        }
        
    }
    
}

- (BOOL) isPrintServiceAvailable
{
    
    Class myClass = NSClassFromString(@"UIPrintInteractionController");
    if (myClass) {
        UIPrintInteractionController *controller = [UIPrintInteractionController sharedPrintController];
        return (controller != nil) && [UIPrintInteractionController isPrintingAvailable];
    }
    
    
    return NO;
}

@end
