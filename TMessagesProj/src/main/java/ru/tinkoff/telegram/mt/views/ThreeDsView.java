package ru.tinkoff.telegram.mt.views;

import android.content.Context;
import android.net.http.SslError;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.util.EncodingUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author a.shishkin1
 */


public class ThreeDsView extends WebView {

    private String redirectUrl;
    private Callback callback;

    public ThreeDsView(Context context) {
        super(context);
        init();
    }

    public ThreeDsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThreeDsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }



    private void init() {
        getSettings().setJavaScriptEnabled(true);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        addJavascriptInterface(new HtmlThief(), "HtmlThief");
        setWebViewClient(new ThreeDSecWebViewClient());
    }


    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public void start(String url, String merchantData, String requestSecretCode) {
        if (redirectUrl == null) {
            throw new NullPointerException("redirect_url not setted");
        }
        String postData = getPostData(merchantData, requestSecretCode);
        postUrl(url, EncodingUtils.getBytes(postData, "BASE64"));
    }

    private String getPostData(String merchantData, String requestSecretCode) {
        String params = "TermUrl=%s&MD=%s&PaReq=%s";
        String md = null;
        String paReq = null;
        try {
            md = URLEncoder.encode(merchantData, "UTF-8");
            paReq = URLEncoder.encode(requestSecretCode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("ThreeDsFragment", "Unsupported encoding UTF-8", e);
        }
        return String.format(params, redirectUrl, md, paReq);
    }


    private class HtmlThief {
        @JavascriptInterface
        public void stealHtml(String html) {
                String paRes = new PaResParser().getPaRes(html);
                if(callback != null) {
                    callback.onPaResReady(paRes);
                }
        }
    }

    private class ThreeDSecWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (url.equals(redirectUrl)) {
                loadUrl("javascript:HtmlThief.stealHtml(document.getElementsByTagName('html')[0].innerHTML);");
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if(callback != null) {
                callback.onReceivedSslError(view.getContext(), handler, error);
            }

        }
    }

    private static class PaResParser {

        private static final String TAG = PaResParser.class.getSimpleName();

        public String getPaRes(String html) {
            if (TextUtils.isEmpty(html) || !html.contains("PaRes")) {
                return null;
            }

            String pares = null;
            InputStream is = null;
            try {
                is = new ByteArrayInputStream(html.getBytes(Xml.Encoding.UTF_8.name()));

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setValidating(false);
                factory.setFeature(Xml.FEATURE_RELAXED, true);
                factory.setNamespaceAware(true);
                XmlPullParser xmlPullParser = factory.newPullParser();
                xmlPullParser.setInput(is, Xml.Encoding.UTF_8.name());
                pares = readXml(xmlPullParser);

            } catch (XmlPullParserException | IOException e) {
                Log.e(TAG, e.toString(), e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return pares;
        }

        private String readXml(XmlPullParser parser) throws IOException, XmlPullParserException {
            String paresValue = null;
            int eventType;
            do {
                eventType = parser.next();
                if (eventType == XmlPullParser.START_TAG && parser.getAttributeCount() != -1) {
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        String attrName = parser.getAttributeName(i);
                        String attrValue = parser.getAttributeValue(parser.getNamespace(), attrName);
                        if (attrValue.equalsIgnoreCase("PaRes")) {
                            paresValue = parser.getAttributeValue(parser.getNamespace(), "value");
                            return paresValue;
                        }
                    }
                }
            } while (eventType != XmlPullParser.END_DOCUMENT);
            return paresValue;
        }



    }



    public interface Callback {
        void onReceivedSslError(Context context, SslErrorHandler handler, SslError error);
        void onPaResReady(String paRes);
    }


}
