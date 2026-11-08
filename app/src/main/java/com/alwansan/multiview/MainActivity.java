package com.alwansan.multiview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public final class MainActivity extends Activity {
    private static final int MAX_PANES = 20;
    private static final int FILE_REQUEST = 42;
    private static final String HOME = "https://www.google.com/";
    private final ArrayList<Pane> panes = new ArrayList<>();
    private LinearLayout root;
    private FrameLayout viewport;
    private LinearLayout topBar;
    private LinearLayout bottomBar;
    private TextView title;
    private android.webkit.ValueCallback<Uri[]> fileCallback;
    private int layoutMode = 0; // 0 grid, 1 vertical, 2 horizontal, 3 focus
    private int active = 0;
    private boolean dark = true;
    private boolean keepAwake = false;
    private boolean booting = true;
    private final String[] modeLabels = {"Grid", "Vertical", "Horizontal", "Focus"};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        viewport = new FrameLayout(this);
        topBar = new LinearLayout(this);
        bottomBar = new LinearLayout(this);
        root.addView(topBar, new LinearLayout.LayoutParams(-1, dp(57)));
        root.addView(viewport, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(bottomBar, new LinearLayout.LayoutParams(-1, dp(59)));
        setContentView(root);
        if (Build.VERSION.SDK_INT >= 30) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                root.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });
            root.requestApplyInsets();
        } else {
            root.setFitsSystemWindows(true);
        }
        restoreSession();
        booting = false;
        if (panes.isEmpty()) {
            addPane(HOME);
            addPane("about:blank");
        }
        active = Math.max(0, Math.min(active, panes.size()-1));
        rebuildChrome();
        renderLayout();
        applyAwake();
    }

    private int dp(float px) { return Math.round(getResources().getDisplayMetrics().density * px); }
    private int bg() { return Color.parseColor(dark ? "#0D1423" : "#F3F6FB"); }
    private int cardColor() { return Color.parseColor(dark ? "#1C2940" : "#FFFFFF"); }
    private int ink() { return Color.parseColor(dark ? "#EAF2FF" : "#172338"); }
    private int muted() { return Color.parseColor(dark ? "#A4B3CB" : "#607086"); }
    private int accent() { return Color.parseColor("#4A83F7"); }
    private GradientDrawable rounded(int color, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(radius));
        return g;
    }
    private Button button(String text, Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(13);
        b.setAllCaps(false);
        b.setTextColor(ink());
        b.setPadding(dp(4), 0, dp(4), 0);
        b.setBackground(rounded(cardColor(), 11));
        b.setOnClickListener(v -> action.run());
        return b;
    }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }

    private void rebuildChrome() {
        root.setBackgroundColor(bg());
        if (Build.VERSION.SDK_INT >= 26) {
            getWindow().getDecorView().setSystemUiVisibility(dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        getWindow().setStatusBarColor(bg());
        getWindow().setNavigationBarColor(bg());
        topBar.removeAllViews();
        topBar.setPadding(dp(10), dp(4), dp(10), dp(4));
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        title = new TextView(this);
        title.setText("MultiView");
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextSize(19);
        title.setTextColor(ink());
        title.setSingleLine(true);
        title.setOnClickListener(v -> selectFocusedPane());
        topBar.addView(title, new LinearLayout.LayoutParams(0,-1,1));
        Button minus=button("−", () -> { if(panes.size()>1) removePane(panes.size()-1); });
        topBar.addView(minus, new LinearLayout.LayoutParams(dp(40),-1));
        Button count=button(Integer.toString(panes.size()), this::chooseCount);
        topBar.addView(count, new LinearLayout.LayoutParams(dp(44),-1));
        Button plus=button("+", () -> { if(panes.size()<MAX_PANES) addPane("about:blank"); else toast("20 panes maximum"); });
        topBar.addView(plus, new LinearLayout.LayoutParams(dp(40),-1));

        bottomBar.removeAllViews();
        bottomBar.setPadding(dp(5), dp(6), dp(5), dp(6));
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        navButton(modeLabels[layoutMode], () -> new AlertDialog.Builder(this).setTitle("Arrangement")
            .setItems(new String[]{"Grid", "Vertical Stack", "Horizontal Strip", "Single Focus"},
                (dialog, which) -> { layoutMode=which; renderLayout(); rebuildChrome(); saveSession(); }).show(), 1.5f);
        navButton("Links", this::showUrlDistribution, 1f);
        navButton("★", this::showBookmarks, 0.7f);
        navButton("History", this::showHistory, 1f);
        navButton(dark ? "☀" : "☾", () -> { dark = !dark; rebuildChrome(); renderLayout(); saveSession(); }, 0.7f);
        navButton("⋮", this::showMainMenu, 0.6f);
    }
    private void navButton(String text, Runnable run, float weight) {
        Button b = button(text, run);
        b.setTextSize(12);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, weight);
        lp.setMargins(dp(3), 0, dp(3), 0);
        bottomBar.addView(b, lp);
    }
    private void selectFocusedPane() {
        String[] options=new String[panes.size()];
        for(int i=0;i<panes.size();i++) {
            String url=panes.get(i).web.getUrl();
            options[i]="View "+(i+1)+" — "+(url==null?"New tab":url);
        }
        new AlertDialog.Builder(this).setTitle("Switch active view")
            .setItems(options,(dialog,which)->{active=which;renderLayout();saveSession();})
            .show();
    }
    private void showMainMenu() {
        String[] actions = {"Keep screen awake: " + (keepAwake ? "ON" : "OFF"), "Reload all panes now", "Clear browsing history", "Clear cookies and cache", "About / limitations"};
        new AlertDialog.Builder(this).setTitle("MultiView options").setItems(actions, (d,which) -> {
            if(which==0) { keepAwake=!keepAwake; applyAwake(); saveSession(); toast(keepAwake ? "Screen stays awake" : "Normal sleep"); }
            if(which==1) for(Pane p:panes) p.web.reload();
            if(which==2) new AlertDialog.Builder(this).setMessage("Clear local visited-page list?").setPositiveButton("Clear",(di,w)->getPreferences(0).edit().remove("history").apply()).setNegativeButton("Cancel",null).show();
            if(which==3) new AlertDialog.Builder(this)
                .setTitle("Clear browsing data")
                .setMessage("This signs you out of websites in every normal pane. Continue?")
                .setPositiveButton("Clear", (di,w)->{
                    CookieManager.getInstance().removeAllCookies(ok->CookieManager.getInstance().flush());
                    for(Pane p:panes) p.web.clearCache(true);
                    getPreferences(0).edit().remove("history").apply();
                    toast("Cookies and cache cleared");
                }).setNegativeButton("Cancel",null).show();
            if(which==4) new AlertDialog.Builder(this).setTitle("MultiView Browser")
                .setMessage("Prototype 0.1.0\nNative independent WebViews. Android may reclaim renderers under memory pressure. Normal panes share the Android WebView cookie profile; private per-pane accounts are not yet supported.").setPositiveButton("OK",null).show();
        }).show();
    }
    private void applyAwake() {
        if(keepAwake) getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private void chooseCount() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(Integer.toString(panes.size()));
        new AlertDialog.Builder(this).setTitle("Number of views (1–20)").setView(input)
            .setPositiveButton("Apply", (d,w) -> {
                try {
                    int n=Integer.parseInt(input.getText().toString());
                    n=Math.max(1,Math.min(MAX_PANES,n));
                    while(panes.size()<n) addPane("about:blank");
                    while(panes.size()>n) removePane(panes.size()-1);
                } catch(NumberFormatException ex) { toast("Enter a number from 1 to 20"); }
            }).setNegativeButton("Cancel",null).show();
    }
    private void addPane(String url) {
        Pane pane = new Pane(url);
        panes.add(pane);
        if(!booting){ active=panes.size()-1; renderLayout(); rebuildChrome(); saveSession(); }
    }
    private void removePane(int index) {
        if (panes.size()<=1 || index<0 || index>=panes.size()) return;
        Pane pane=panes.remove(index);
        ViewGroup parent=(ViewGroup)pane.card.getParent();
        if(parent!=null) parent.removeView(pane.card);
        pane.web.stopLoading();
        pane.web.destroy();
        active=Math.min(active,panes.size()-1);
        renderLayout(); rebuildChrome(); saveSession();
    }
    private void movePane(Pane pane,int direction) {
        int index=panes.indexOf(pane), target=index+direction;
        if(target<0||target>=panes.size()) return;
        Collections.swap(panes,index,target);
        active=target;
        renderLayout(); saveSession();
    }
    private void setActive(Pane pane) {
        active=panes.indexOf(pane);
        if(layoutMode==3) renderLayout();
        else for(Pane p:panes) p.styleCard();
        saveSession();
    }
    // Rebuild only the layout CONTAINERS: each Pane.card and its existing WebView
    // are moved without reload or destroy. This is the central retention contract.
    private void renderLayout() {
        if (viewport==null || panes.isEmpty()) return;
        for(Pane p:panes) {
            ViewGroup old=(ViewGroup)p.card.getParent();
            if(old!=null) old.removeView(p.card);
        }
        viewport.removeAllViews();
        viewport.setBackgroundColor(bg());
        if(layoutMode==3) {
            Pane p=panes.get(Math.max(0,Math.min(active,panes.size()-1)));
            p.styleCard();
            FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(-1,-1);
            fp.setMargins(dp(5),dp(5),dp(5),dp(5));
            viewport.addView(p.card,fp);
            return;
        }
        if(layoutMode==2) {
            HorizontalScrollView scroll=new HorizontalScrollView(this);
            scroll.setFillViewport(true);
            LinearLayout lane=new LinearLayout(this);
            lane.setOrientation(LinearLayout.HORIZONTAL);
            scroll.addView(lane,new android.widget.HorizontalScrollView.LayoutParams(-2,-1));
            viewport.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
            for(Pane p:panes){
                p.styleCard();
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(318),-1);
                lp.setMargins(dp(5),dp(5),dp(5),dp(5));
                lane.addView(p.card,lp);
            }
            return;
        }
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout column=new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(column,new ScrollView.LayoutParams(-1,-2));
        viewport.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        int widthDp=Math.round(getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density);
        int columns = layoutMode==1 ? 1 : (widthDp>=850 ? 3 : widthDp>=380 ? 2 : 1);
        LinearLayout row=null;
        for(int i=0;i<panes.size();i++){
            if(i%columns==0) {
                row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
                column.addView(row,new LinearLayout.LayoutParams(-1,dp(columns==1 ? 390 : 360)));
            }
            Pane p=panes.get(i);p.styleCard();
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);
            lp.setMargins(dp(5),dp(5),dp(5),dp(5));
            row.addView(p.card,lp);
        }
    }
    private void showUrlDistribution() {
        final EditText input=new EditText(this);
        input.setMinLines(3); input.setMaxLines(6);
        input.setHint("One URL per line, or one URL for all panes");
        new AlertDialog.Builder(this).setTitle("Distribute URLs").setView(input)
            .setPositiveButton("Apply", (d,w) -> {
                String[] urls=input.getText().toString().trim().split("\\s+");
                if(urls.length==1 && !urls[0].isEmpty()) {
                    for(Pane p:panes) p.navigate(urls[0]);
                } else {
                    for(int i=0;i<panes.size() && i<urls.length;i++) panes.get(i).navigate(urls[i]);
                }
            }).setNegativeButton("Cancel",null).show();
    }
    private void showBookmarks() {
        JSONArray arr=readArray("bookmarks");
        String[] items=new String[arr.length()];
        for(int i=0;i<items.length;i++) items[i]=arr.optString(i);
        new AlertDialog.Builder(this).setTitle("Bookmarks").setItems(items,(d,w)->panes.get(active).navigate(items[w]))
            .setPositiveButton("Save current", (d,w) -> bookmark(panes.get(active).web.getUrl()))
            .setNegativeButton("Close",null).show();
    }
    private void bookmark(String url) {
        if(url==null||!url.startsWith("http")) return;
        JSONArray old=readArray("bookmarks");
        for(int i=0;i<old.length();i++) if(url.equals(old.optString(i))) {toast("Already bookmarked");return;}
        old.put(url);
        getPreferences(0).edit().putString("bookmarks",old.toString()).apply();
        toast("Saved");
    }
    private void showHistory() {
        JSONArray arr=readArray("history");
        int n=Math.min(arr.length(),50);
        String[] items=new String[n];
        for(int i=0;i<n;i++) items[i]=arr.optString(arr.length()-1-i);
        new AlertDialog.Builder(this).setTitle("Recent pages").setItems(items,(d,w)->panes.get(active).navigate(items[w]))
            .setPositiveButton("Close",null).show();
    }
    private void recordHistory(String url) {
        if(url==null||!url.startsWith("http"))return;
        JSONArray old=readArray("history"), next=new JSONArray();
        int begin=Math.max(0,old.length()-198);
        for(int i=begin;i<old.length();i++) next.put(old.optString(i));
        if(next.length()==0 || !url.equals(next.optString(next.length()-1)))next.put(url);
        getPreferences(0).edit().putString("history",next.toString()).apply();
    }
    private JSONArray readArray(String key) {
        try { return new JSONArray(getPreferences(0).getString(key,"[]")); }
        catch(Exception e){return new JSONArray();}
    }
    private String normalize(String text) {
        String value=text.trim();
        if(value.isEmpty())return "about:blank";
        if(value.startsWith("about:")||value.startsWith("https://")||value.startsWith("http://"))return value;
        if(!value.contains(" ") && value.contains("."))return "https://"+value;
        return "https://www.google.com/search?q="+Uri.encode(value);
    }
    private void restoreSession() {
        try {
            JSONObject state=new JSONObject(getPreferences(0).getString("session","{}"));
            layoutMode=Math.max(0,Math.min(3,state.optInt("layout",0)));
            dark=state.optBoolean("dark",true);
            keepAwake=state.optBoolean("awake",false);
            active=state.optInt("active",0);
            JSONArray urls=state.optJSONArray("urls");
            if(urls!=null) for(int i=0;i<Math.min(urls.length(),MAX_PANES);i++) {
                String url=urls.optString(i,"about:blank");
                addPane(url.startsWith("http")||url.equals("about:blank") ? url : "about:blank");
            }
        } catch(Exception ignored){}
    }
    // Persist URLs/layout, not raw cookies or credentials. OS process death requires
    // a new navigation; retaining webviews while this Activity lives avoids it.
    private void saveSession() {
        if(booting)return;
        try {
            JSONObject state=new JSONObject();
            JSONArray urls=new JSONArray();
            for(Pane pane:panes) {
                String url=pane.web.getUrl();
                urls.put(url==null?pane.initialUrl:url);
            }
            state.put("urls",urls);state.put("layout",layoutMode);
            state.put("active",active);state.put("dark",dark);state.put("awake",keepAwake);
            getPreferences(0).edit().putString("session",state.toString()).apply();
        } catch(Exception ignored){}
    }
    @Override public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        renderLayout();
    }
    @Override protected void onStop(){ saveSession();super.onStop(); }
    @Override public void onBackPressed() {
        if(panes.size()>0 && panes.get(active).web.canGoBack())panes.get(active).web.goBack();
        else super.onBackPressed();
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==FILE_REQUEST && fileCallback!=null) {
            fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode,data));
            fileCallback=null;
        }
    }
    private void chooseFile(android.webkit.ValueCallback<Uri[]> callback,WebChromeClient.FileChooserParams params) {
        if(fileCallback!=null)fileCallback.onReceiveValue(null);
        fileCallback=callback;
        try { startActivityForResult(params.createIntent(),FILE_REQUEST); }
        catch(Exception e){fileCallback.onReceiveValue(null);fileCallback=null;toast("No file picker available");}
    }
    private void download(String url, String disposition, String mime, String userAgent) {
        if(url==null || !(url.startsWith("https://")||url.startsWith("http://"))) {toast("Unsupported download link");return;}
        try {
            DownloadManager.Request req=new DownloadManager.Request(Uri.parse(url));
            req.setTitle(URLUtil.guessFileName(url,disposition,mime));
            req.setMimeType(mime);
            req.addRequestHeader("User-Agent",userAgent);
            String cookies=CookieManager.getInstance().getCookie(url);
            if(cookies!=null)req.addRequestHeader("Cookie",cookies);
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,URLUtil.guessFileName(url,disposition,mime));
            ((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(req);
            toast("Download started");
        }catch(Exception e){toast("Download error: "+e.getClass().getSimpleName());}
    }
    private void screenshot(Pane pane) {
        int w=pane.web.getWidth(),h=pane.web.getHeight();
        if(w<=0||h<=0){toast("View not ready");return;}
        try {
            Bitmap bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
            pane.web.draw(new Canvas(bitmap));
            if(Build.VERSION.SDK_INT>=29) {
                ContentValues values=new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME,"multiview-"+System.currentTimeMillis()+".png");
                values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+"/MultiView");
                Uri out=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
                if(out==null)throw new IllegalStateException("MediaStore insert");
                try(OutputStream stream=getContentResolver().openOutputStream(out)) {
                    if(stream==null || !bitmap.compress(Bitmap.CompressFormat.PNG,100,stream))throw new IllegalStateException("PNG");
                }
            }else{
                java.io.File dir=getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if(dir==null)throw new IllegalStateException("Storage");
                try(OutputStream stream=new java.io.FileOutputStream(new java.io.File(dir,"multiview-"+System.currentTimeMillis()+".png"))) {
                    bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                }
            }
            bitmap.recycle();
            toast("Screenshot saved");
        }catch(Exception e){toast("Capture failed: "+e.getClass().getSimpleName());}
        catch(OutOfMemoryError e){toast("Not enough memory for screenshot");}
    }
    private final class Pane {
        final String initialUrl;
        final LinearLayout card;
        final LinearLayout header;
        final WebView web;
        final EditText address;
        final TextView number;
        boolean desktop=false;
        Pane(String url) {
            initialUrl=url;
            card=new LinearLayout(MainActivity.this);
            card.setOrientation(LinearLayout.VERTICAL);
            header=new LinearLayout(MainActivity.this);
            header.setOrientation(LinearLayout.VERTICAL);
            card.addView(header,new LinearLayout.LayoutParams(-1,dp(88)));
            web=new WebView(MainActivity.this);
            web.setBackgroundColor(Color.WHITE);
            card.addView(web,new LinearLayout.LayoutParams(-1,0,1));
            address=new EditText(MainActivity.this);
            number=new TextView(MainActivity.this);
            configureWeb();
            styleCard();
            web.loadUrl(initialUrl);
        }
        private void configureWeb() {
            WebSettings s=web.getSettings();
            s.setJavaScriptEnabled(true);
            s.setDomStorageEnabled(true);
            s.setDatabaseEnabled(true);
            s.setSupportZoom(true);
            s.setBuiltInZoomControls(true);
            s.setDisplayZoomControls(false);
            s.setUseWideViewPort(true);
            s.setLoadWithOverviewMode(true);
            s.setSupportMultipleWindows(false);
            s.setJavaScriptCanOpenWindowsAutomatically(false);
            s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
            s.setAllowFileAccess(false);
            s.setAllowFileAccessFromFileURLs(false);
            s.setAllowUniversalAccessFromFileURLs(false);
            if(Build.VERSION.SDK_INT>=26)s.setSafeBrowsingEnabled(true);
            CookieManager.getInstance().setAcceptCookie(true);
            CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);
            web.setWebViewClient(new WebViewClient(){
                @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    Uri uri=request.getUrl();
                    String scheme=uri.getScheme();
                    if("http".equalsIgnoreCase(scheme)||"https".equalsIgnoreCase(scheme)||"about".equalsIgnoreCase(scheme))return false;
                    try { startActivity(new Intent(Intent.ACTION_VIEW,uri)); } catch(Exception e){toast("No handler for this link");}
                    return true;
                }
                @Override public void onPageStarted(WebView view,String url,android.graphics.Bitmap favicon) {
                    if(!address.hasFocus())address.setText(url);
                    recordHistory(url);
                }
                @Override public void onPageFinished(WebView view,String url) {
                    if(!address.hasFocus())address.setText(url);
                    saveSession();
                }
                @Override public boolean onRenderProcessGone(WebView view, android.webkit.RenderProcessGoneDetail detail) {
                    toast("Web renderer was reclaimed — restoring page");
                    // A killed renderer cannot be reused. Replace only this pane, never
                    // destroy unrelated live panes. Deferred until the next build.
                    int index=panes.indexOf(Pane.this);
                    if(index>=0) {
                        String current=view.getUrl()==null?initialUrl:view.getUrl();
                        panes.remove(index);
                        ViewGroup parent=(ViewGroup)card.getParent();
                        if(parent!=null)parent.removeView(card);
                        view.destroy();
                        Pane replacement=new Pane(current);
                        panes.add(index,replacement);
                        active=index;renderLayout();saveSession();
                    }
                    return true;
                }
            });
            web.setWebChromeClient(new WebChromeClient(){
                @Override public boolean onShowFileChooser(WebView view,android.webkit.ValueCallback<Uri[]> callback,FileChooserParams params) {
                    chooseFile(callback,params);return true;
                }
            });
            web.setDownloadListener((url,userAgent,contentDisposition,mimetype,contentLength)->
                download(url,contentDisposition,mimetype,userAgent));
        }
        private Button iconButton(String label,Runnable click,LinearLayout into) {
            Button b=button(label,click);
            b.setTextSize(12);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(38),1);
            lp.setMargins(dp(1),dp(1),dp(1),dp(1));
            into.addView(b,lp);
            return b;
        }
        void styleCard() {
            card.setBackground(rounded(cardColor(),12));
            header.removeAllViews();
            header.setPadding(dp(4),dp(3),dp(4),0);
            LinearLayout upper=new LinearLayout(MainActivity.this);
            upper.setOrientation(LinearLayout.HORIZONTAL);
            upper.setGravity(Gravity.CENTER_VERTICAL);
            header.addView(upper,new LinearLayout.LayoutParams(-1,dp(41)));
            number.setText(String.valueOf(panes.indexOf(this)+1));
            number.setTextSize(13);
            number.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            number.setGravity(Gravity.CENTER);
            number.setTextColor(Color.WHITE);
            number.setBackground(rounded(panes.indexOf(this)==active?accent():muted(),9));
            upper.addView(number,new LinearLayout.LayoutParams(dp(28),dp(30)));
            address.setSingleLine(true);
            address.setTextSize(12);
            address.setTextColor(ink());
            address.setHintTextColor(muted());
            address.setHint("URL or search");
            address.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
            address.setImeOptions(EditorInfo.IME_ACTION_GO);
            address.setSelectAllOnFocus(true);
            address.setBackground(rounded(bg(),8));
            address.setText(web.getUrl()==null?initialUrl:web.getUrl());
            address.setOnFocusChangeListener((v,focus)->{if(focus)setActive(this);});
            address.setOnEditorActionListener((v,action,event)->{
                if(action==EditorInfo.IME_ACTION_GO){navigate(address.getText().toString());address.clearFocus();return true;}
                return false;
            });
            LinearLayout.LayoutParams inputLp=new LinearLayout.LayoutParams(0,dp(33),1);
            inputLp.setMargins(dp(5),0,dp(4),0);
            upper.addView(address,inputLp);
            Button more=button("⋮",()->paneMenu());
            upper.addView(more,new LinearLayout.LayoutParams(dp(30),dp(35)));
            LinearLayout tools=new LinearLayout(MainActivity.this);
            header.addView(tools,new LinearLayout.LayoutParams(-1,dp(41)));
            iconButton("‹",()->{setActive(this);if(web.canGoBack())web.goBack();},tools);
            iconButton("›",()->{setActive(this);if(web.canGoForward())web.goForward();},tools);
            iconButton("↻",()->{setActive(this);web.reload();},tools);
            iconButton("⌂",()->{setActive(this);navigate(HOME);},tools);
            iconButton("▣",()->{setActive(this);layoutMode=3;renderLayout();rebuildChrome();saveSession();},tools);
        }
        void navigate(String text){web.loadUrl(normalize(text));setActive(this);saveSession();}
        void paneMenu() {
            setActive(this);
            String[] actions={"Bookmark page","Desktop mode: "+(desktop?"ON":"OFF"),"Save screenshot","Copy address","Share link","Move left/up","Move right/down","Close pane"};
            new AlertDialog.Builder(MainActivity.this).setTitle("View "+(panes.indexOf(this)+1))
                .setItems(actions,(d,which)->{
                    String url=web.getUrl();
                    switch(which){
                        case 0: bookmark(url);break;
                        case 1:
                            desktop=!desktop;
                            WebSettings s=web.getSettings();
                            s.setUserAgentString(desktop?
                                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36":
                                WebSettings.getDefaultUserAgent(MainActivity.this));
                            s.setUseWideViewPort(true);web.reload();break;
                        case 2: screenshot(this);break;
                        case 3:
                            ((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("URL",url==null?"":url));
                            toast("Copied");break;
                        case 4:
                            Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,url);
                            startActivity(Intent.createChooser(send,"Share page"));break;
                        case 5: movePane(this,-1);break;
                        case 6: movePane(this,1);break;
                        case 7:removePane(panes.indexOf(this));break;
                    }
                }).show();
        }
    }
}
