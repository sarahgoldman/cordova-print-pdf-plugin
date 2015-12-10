//
//  CDVPrintPDF.m
//  Print PDF
//
//  Created by Sarah Goldman (github.com/sarahgoldman) on 07/06/2015.
//  Copyright 2015 Sarah Goldman. All rights reserved.
//  MIT licensed
//
#import "NSData+Base64.h"

#import "CDVPrintPDF.h"
#import "CDVFile.h"

@interface CDVPrintPDF (Private)
- (BOOL) isPrintServiceAvailable;
@end

@implementation CDVPrintPDF

NSString * const KEY_TYPE_FILE = @"File";
NSString * const KEY_TYPE_DATA = @"Data";


@synthesize successCallback, failCallback, wasDismissed;

// Plugin Functions
 - (void) isPrintingAvailable:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:([self isPrintServiceAvailable] ? YES : NO)];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) printDocument:(CDVInvokedUrlCommand *)command
{
    //NSString *pdfDataString = [command.arguments objectAtIndex:0];
    NSString *pdfString = [command.arguments objectAtIndex:0];
    NSString *typeData = [command.arguments objectAtIndex:1];

    NSData *pdfData = nil;
    if (typeData != nil && [typeData isEqualToString:KEY_TYPE_FILE]) {
        CDVFilesystemURL * urlCdv = [CDVFilesystemURL fileSystemURLWithString:pdfString];
        CDVFile* filePlugin = [self.commandDelegate getCommandInstance:@"File"];
        NSString * filePath = [filePlugin filesystemPathForURL:urlCdv];
        NSFileManager *fileMgr = [NSFileManager defaultManager];
        if (![fileMgr fileExistsAtPath:filePath isDirectory:false]) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"{\"success\": false, \"available\": true, \"error\": \"File does not exist\" }"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            return;
        }
        pdfData = [NSData dataWithContentsOfFile:filePath];
    } else {
        pdfData = [NSData dataFromBase64String:pdfString];
    }

    self.wasDismissed = NO;

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
        printInteraction.delegate = self;

        void (^completionHandler)(UIPrintInteractionController *, BOOL, NSError *) =
        ^(UIPrintInteractionController *printController, BOOL completed, NSError *error) {
            CDVPluginResult* pluginResult = nil;
            if (!completed || error) {

                if (self.wasDismissed) {

                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"{\"success\": false, \"available\": true, \"dismissed\": true}"];

                } else {

                    if (error != nil) {
						pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"{\"success\": false, \"available\": true, \"error\": \"%@\", \"dismissed\": false}", error.localizedDescription]];
                    } else {
                        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"{\"success\": false, \"available\": true, \"dismissed\": false}"];
                    }

                }
            }
            else{
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"{\"success\": true}"];
            }
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        };

        if([UIDevice currentDevice].userInterfaceIdiom == UIUserInterfaceIdiomPad) {

            CGRect bounds = self.webView.bounds;

			NSInteger dialogX = [[command.arguments objectAtIndex:2] integerValue];
			NSInteger dialogY = [[command.arguments objectAtIndex:3] integerValue];

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

 - (void) dismissPrintDialog:(CDVInvokedUrlCommand*)command
{
    self.wasDismissed = YES;
    [[UIPrintInteractionController sharedPrintController] dismissAnimated:NO];
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

- (void)printInteractionControllerDidDismissPrinterOptions:(UIPrintInteractionController *)printInteractionController
{
    NSLog(@"DISMISSED");
    self.wasDismissed = YES;
}

@end
