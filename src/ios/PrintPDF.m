//
//  PrintPDF.m
//  Print PDF
//
//  Created by Sarah Goldman (github.com/sarahgoldman) on 07/06/2015.
//  Copyright 2015 Sarah Goldman. All rights reserved.
//  MIT licensed
//

#import "PrintPDF.h"

@interface PrintPDF (Private)
- (void) doPrint;
- (void) callbackWithFuntion:(NSString *)function withData:(NSString *)value;
- (BOOL) isPrintServiceAvailable;
@end

@implementation PrintPDF

@synthesize successCallback, failCallback, pdfUrlString;

/*
 Is printing available. Callback returns true/false if printing is available/unavailable.
 */
 - (void) isPrintingAvailable:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:([self isPrintServiceAvailable] ? YES : NO)];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) print:(CDVInvokedUrlCommand*)command
{
    
    self.pdfUrlString = [command.arguments objectAtIndex:0];
	NSURL *fileURL = [NSURL URLWithString:self.pdfUrlString];
	
	NSData *pdfData = [NSData dataWithContentsOfURL:fileURL];
    
    if (![self isPrintServiceAvailable]){
        [self callbackWithFuntion:self.failCallback withData: @"{success: false, available: false}"];
        
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
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"{success: false, available: true, error: \"%@\"}", error.localizedDescription]];
            }
            else{
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"{success: true}"];
            }
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        };
        
        
		[printInteraction presentAnimated:YES completionHandler:completionHandler];
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
