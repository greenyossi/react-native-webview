package com.reactnativecommunity.webview;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.ThemedReactContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Nullable;

public class JavaScriptInterface {
  private ThemedReactContext context;

  public JavaScriptInterface(ThemedReactContext context) {
    this.context = context;
  }

  private void sendEvent(String eventName, @Nullable WritableMap params) {
    this.context
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  @JavascriptInterface
  public void getBase64FromBlobData(String base64Data) throws IOException {
    convertBase64StringToPdfAndStoreIt(base64Data);
  }

  @JavascriptInterface
  public void logError(String error) {
    Log.e("JSError", error);
  }

  public static String getBase64StringFromBlobUrl(String blobUrl){
      return "javascript: var xhr = new XMLHttpRequest();" +
        "xhr.open('GET', '" + blobUrl + "', true);" +
        "xhr.setRequestHeader('Content-type','application/json');" +
        "xhr.responseType = 'blob';" +
        "xhr.onload = function(e) {" +
        "    if (this.status == 200) {" +
        "        var blobPdf = this.response;" +
        "        var reader = new FileReader();" +
        "        reader.readAsDataURL(blobPdf);" +
        "        reader.onloadend = function() {" +
        "            base64data = reader.result;" +
        "            Android.getBase64FromBlobData(base64data);" +
        "        }" +
        "    }" +
        "};" +
        "xhr.onerror = function(e) {" +
        "    Android.logError(JSON.stringify(e));" +
        "};" +
        "xhr.send();";
  }

  private void convertBase64StringToPdfAndStoreIt(String base64PDf) throws IOException {
    final File dwldsPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/document.pdf");

    byte[] pdfAsBytes = Base64.decode(base64PDf.replaceFirst("^data:application/json;base64,", ""), 0);

    FileOutputStream os;

    os = new FileOutputStream(dwldsPath, false);
    os.write(pdfAsBytes);
    os.flush();

    if(dwldsPath.exists()) {
      WritableMap params = Arguments.createMap();
      params.putString("filePath", dwldsPath.getAbsolutePath());
      sendEvent("PDFDownloaded", params);
    }
  }
}
