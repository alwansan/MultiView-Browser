package com.alwansan.multiview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;
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
import android.widget.ProgressBar;
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
    private static final String DESKTOP_UA="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36";
    private final ArrayList<Pane> panes = new ArrayList<>();
    private final ArrayList<LinearLayout> gridRows = new ArrayList<>();
    private LinearLayout root;
    private FrameLayout viewport;
    private LinearLayout topBar;
    private LinearLayout bottomBar;
    private TextView title;
    private android.webkit.ValueCallback<Uri[]> fileCallback;
    private int layoutMode = 0; // 0 grid, 1 vertical, 2 horizontal, 3 focus
    private int active = 0;
    private boolean dark = true;
    private boolean fitGrid = true;
    private int defaultZoom = 100;
    private boolean defaultSiteDark = false;
    private boolean keepAwake = false;
    private boolean booting = true;
    private final int[] modeLabels = {R.string.fit_grid, R.string.vertical, R.string.horizontal, R.string.focus};
    private String t(int id){return getString(id);}
    @Override protected void attachBaseContext(Context base){
        if(Build.VERSION.SDK_INT<33){
            String code=base.getSharedPreferences("multiview_language",Context.MODE_PRIVATE).getString("locale","");
            if(!code.isEmpty()){
                Configuration config=new Configuration(base.getResources().getConfiguration());
                config.setLocale(new Locale(code));
                base=base.createConfigurationContext(config);
            }
        }
        super.attachBaseContext(base);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        viewport = new FrameLayout(this);
        topBar = new LinearLayout(this);
        bottomBar = new LinearLayout(this);
        root.addView(topBar, new LinearLayout.LayoutParams(-1, dp(62)));
        root.addView(viewport, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(bottomBar, new LinearLayout.LayoutParams(-1, dp(58)));
        viewport.addOnLayoutChangeListener((v,left,top,right,bottom,oldLeft,oldTop,oldRight,oldBottom) -> {
            if (right-left != oldRight-oldLeft || bottom-top != oldBottom-oldTop) updateGridRowHeights();
        });
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
    private int bg() { return Color.parseColor(dark ? "#101625" : "#F2F5FC"); }
    private int cardColor() { return Color.parseColor(dark ? "#1E2B43" : "#FFFFFF"); }
    private int ink() { return Color.parseColor(dark ? "#EAF2FF" : "#172338"); }
    private int muted() { return Color.parseColor(dark ? "#A4B3CB" : "#607086"); }
    private int accent() { return Color.parseColor("#5A90FF"); }
    private int surface() { return Color.parseColor(dark ? "#152034" : "#E6ECF8"); }
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
        b.setPadding(dp(3), 0, dp(3), 0);
        b.setMinimumHeight(0);
        b.setMinHeight(0);
        b.setMinimumWidth(0);
        b.setMinWidth(0);
        b.setContentDescription(text);
        b.setBackground(rounded(cardColor(), 11));
        b.setOnClickListener(v -> action.run());
        return b;
    }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }

    private void rebuildChrome() {
        root.setBackgroundColor(bg());
        getWindow().getDecorView().setSystemUiVisibility(dark ? 0 :
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().setStatusBarColor(bg());
        getWindow().setNavigationBarColor(bg());
        topBar.removeAllViews();
        topBar.setPadding(dp(12), dp(6), dp(12), dp(5));
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = new TextView(this);
        name.setText("MultiView");
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        name.setTextSize(19);
        name.setTextColor(ink());
        name.setSingleLine(true);
        brand.addView(name, new LinearLayout.LayoutParams(-1,dp(25)));
        TextView detail = new TextView(this);
        detail.setText(panes.size()+" "+t(R.string.live_views)+"  ·  "+
            (layoutMode==0 ? (fitGrid?t(R.string.fit_grid):t(R.string.scroll_grid)) : t(modeLabels[layoutMode])));
        detail.setTextSize(10);
        detail.setLetterSpacing(0.07f);
        detail.setTextColor(muted());
        detail.setSingleLine(true);
        brand.addView(detail,new LinearLayout.LayoutParams(-1,dp(18)));
        title = name;
        brand.setOnClickListener(v -> selectFocusedPane());
        topBar.addView(brand,new LinearLayout.LayoutParams(0,-1,1));

        Button minus=button("−", () -> { if(panes.size()>1) removePane(active); });
        minus.setContentDescription("Close active view");
        topBar.addView(minus,new LinearLayout.LayoutParams(dp(39),dp(41)));
        Button count=button(String.valueOf(panes.size()), this::chooseCount);
        count.setContentDescription("Change number of views");
        LinearLayout.LayoutParams countLp=new LinearLayout.LayoutParams(dp(43),dp(41));
        countLp.setMargins(dp(5),0,dp(5),0);
        topBar.addView(count,countLp);
        Button plus=button("+", () -> {
            if(panes.size()<MAX_PANES) addPane("about:blank");
            else toast(t(R.string.max_views));
        });
        plus.setContentDescription("New view");
        topBar.addView(plus,new LinearLayout.LayoutParams(dp(39),dp(41)));

        bottomBar.removeAllViews();
        bottomBar.setPadding(dp(5),dp(6),dp(5),dp(6));
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        navButton("◫ "+t(R.string.layout),this::showLayouts,1.15f);
        navButton("± "+t(R.string.zoom),()->showZoomDialog(panes.get(active)),1.0f);
        navButton("↻ "+t(R.string.all),this::confirmReloadAll,0.9f);
        navButton("▤ "+t(R.string.views),this::selectFocusedPane,1.05f);
        navButton("★ "+t(R.string.saved),this::showSavedMenu,1.0f);
        navButton("⋮ "+t(R.string.more),this::showMainMenu,1.0f);
    }
    private void navButton(String label,Runnable run,float weight) {
        Button b=button(label,run);
        b.setTextSize(11);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,weight);
        lp.setMargins(dp(2),0,dp(2),0);
        bottomBar.addView(b,lp);
    }
    private void showLayouts() {
        new AlertDialog.Builder(this).setTitle(t(R.string.layout_title))
            .setSingleChoiceItems(new String[]{
                t(R.string.layout_fit),t(R.string.layout_scroll),t(R.string.layout_vertical),
                t(R.string.layout_horizontal),t(R.string.layout_focus)
            }, layoutMode==0?(fitGrid?0:1):layoutMode+1,
            (d,choice)->{
                if(choice<2){layoutMode=0;fitGrid=choice==0;}
                else layoutMode=choice-1;
                renderLayout();rebuildChrome();saveSession();d.dismiss();
            }).setNegativeButton(t(R.string.close),null).show();
    }
    private void showSavedMenu() {
        new AlertDialog.Builder(this).setTitle(t(R.string.library))
            .setItems(new String[]{t(R.string.bookmarks),t(R.string.history)},(d,which)->{
                if(which==0)showBookmarks();else showHistory();
            }).show();
    }
    private void selectFocusedPane() {
        String[] options=new String[panes.size()];
        for(int i=0;i<panes.size();i++) {
            String url=panes.get(i).web.getUrl();
            String label=panes.get(i).pageTitle;
            if(label==null || label.isEmpty() || "about:blank".equals(label)) label=url;
            options[i]=t(R.string.view)+" "+(i+1)+" — "+(label==null?t(R.string.new_view):label);
        }
        new AlertDialog.Builder(this).setTitle(t(R.string.switch_view))
            .setItems(options,(dialog,which)->{active=which;renderLayout();saveSession();})
            .show();
    }
    private void confirmReloadAll(){
        new AlertDialog.Builder(this).setTitle(t(R.string.reload_all_title))
            .setMessage(t(R.string.reload_warning))
            .setPositiveButton(t(R.string.reload),(d,w)->{
                for(Pane p:panes)p.web.reload();
                toast(t(R.string.reloading_all));
            })
            .setNegativeButton(t(R.string.cancel),null).show();
    }
    private void showZoomDialog(Pane pane){
        String[] levels=new String[20];
        for(int i=0;i<20;i++)levels[i]=((i+1)*10)+"%";
        final int[] chosen={Math.max(0,Math.min(19,pane.zoomPct/10-1))};
        new AlertDialog.Builder(this)
            .setTitle(t(R.string.zoom_title)+" · "+pane.zoomPct+"%")
            .setSingleChoiceItems(levels,chosen[0],(d,index)->chosen[0]=index)
            .setPositiveButton(t(R.string.apply_all_save),(d,w)->{
                int value=(chosen[0]+1)*10;
                defaultZoom=value;
                for(Pane p:panes){p.zoomPct=value;p.applyAppearance();}
                saveSession();toast(value+"% · "+t(R.string.applied_all));
            })
            .setNeutralButton(t(R.string.this_view),(d,w)->{
                pane.zoomPct=(chosen[0]+1)*10;
                pane.applyAppearance();saveSession();
            })
            .setNegativeButton(t(R.string.cancel),null).show();
    }
    private void setAllSitesDark(boolean enabled){
        defaultSiteDark=enabled;
        for(Pane p:panes){p.websiteDark=enabled;p.applyAppearance();}
        saveSession();
        toast(enabled?t(R.string.site_dark_on):t(R.string.site_dark_off));
    }
    private void showLanguagePicker(){
        new AlertDialog.Builder(this).setTitle(t(R.string.language))
            .setItems(new String[]{t(R.string.system_language),"العربية","English"},
                (dialog,which)->{
                    String code=which==1?"ar":which==2?"en":"";
                    new AlertDialog.Builder(this).setTitle(t(R.string.language))
                        .setMessage(t(R.string.language_restart_warning))
                        .setPositiveButton(t(R.string.apply),(d,w)->{
                            saveSession();
                            if(Build.VERSION.SDK_INT>=33){
                                android.app.LocaleManager manager=
                                    (android.app.LocaleManager)getSystemService(Context.LOCALE_SERVICE);
                                if(manager!=null)manager.setApplicationLocales(
                                    android.os.LocaleList.forLanguageTags(code));
                            }else{
                                getSharedPreferences("multiview_language",Context.MODE_PRIVATE)
                                    .edit().putString("locale",code).apply();
                                recreate();
                            }
                        })
                        .setNegativeButton(t(R.string.cancel),null).show();
                }).show();
    }
    private void copyAllUrls(){
        StringBuilder urls=new StringBuilder();
        for(Pane pane:panes){
            String url=pane.web.getUrl();
            if(url!=null && url.startsWith("http")){
                if(urls.length()>0)urls.append('\n');
                urls.append(url);
            }
        }
        if(urls.length()==0){toast(t(R.string.no_urls));return;}
        ((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(
            ClipData.newPlainText("MultiView URLs",urls.toString()));
        toast(t(R.string.copied_urls));
    }
    private void showMainMenu(){
        String[] actions={
            t(R.string.appearance)+" · "+(dark?t(R.string.light):t(R.string.dark)),
            t(R.string.keep_awake)+" · "+(keepAwake?t(R.string.on):t(R.string.off)),
            t(R.string.open_chatgpt),
            t(R.string.open_links),
            t(R.string.site_dark_all)+" · "+(defaultSiteDark?t(R.string.on):t(R.string.off)),
            t(R.string.language),
            t(R.string.copy_urls),
            t(R.string.clear_history),
            t(R.string.clear_cookies),
            t(R.string.about)
        };
        new AlertDialog.Builder(this).setTitle(t(R.string.settings))
            .setItems(actions,(dialog,which)->{
                switch(which){
                    case 0:
                        dark=!dark;rebuildChrome();renderLayout();saveSession();break;
                    case 1:
                        keepAwake=!keepAwake;applyAwake();saveSession();break;
                    case 2:
                        panes.get(active).navigate("https://chatgpt.com/");break;
                    case 3:
                        showUrlDistribution();break;
                    case 4:
                        setAllSitesDark(!defaultSiteDark);break;
                    case 5:
                        showLanguagePicker();break;
                    case 6:
                        copyAllUrls();break;
                    case 7:
                        new AlertDialog.Builder(this).setTitle(t(R.string.clear_history))
                            .setPositiveButton(t(R.string.clear),
                                (d,w)->getPreferences(0).edit().remove("history").apply())
                            .setNegativeButton(t(R.string.cancel),null).show();break;
                    case 8:
                        new AlertDialog.Builder(this).setTitle(t(R.string.clear_cookies))
                            .setMessage(t(R.string.clear_cookies_warning))
                            .setPositiveButton(t(R.string.clear),(d,w)->{
                                CookieManager.getInstance().removeAllCookies(ok->
                                    CookieManager.getInstance().flush());
                                for(Pane p:panes)p.web.clearCache(true);
                                getPreferences(0).edit().remove("history").apply();
                            }).setNegativeButton(t(R.string.cancel),null).show();break;
                    case 9:
                        new AlertDialog.Builder(this).setTitle(t(R.string.about))
                            .setMessage(t(R.string.about_text))
                            .setPositiveButton(t(R.string.ok),null).show();break;
                }
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
        new AlertDialog.Builder(this).setTitle(t(R.string.number_of_views)).setView(input)
            .setPositiveButton(t(R.string.apply), (d,w) -> {
                try {
                    int n=Integer.parseInt(input.getText().toString());
                    n=Math.max(1,Math.min(MAX_PANES,n));
                    resizePanes(n);
                } catch(NumberFormatException ex) { toast(t(R.string.count_input)); }
            }).setNegativeButton(t(R.string.cancel),null).show();
    }
    private void addPane(String url) {addPane(url,false);}
    private void addPane(String url,boolean desktop) {
        panes.add(new Pane(url,desktop,true));
        if(!booting){active=panes.size()-1;renderLayout();rebuildChrome();saveSession();}
    }
    private void resizePanes(int requested) {
        int target=Math.max(1,Math.min(MAX_PANES,requested));
        boolean wasBooting=booting;
        booting=true;
        while(panes.size()<target)panes.add(new Pane("about:blank",false,true));
        while(panes.size()>target)disposePane(panes.remove(panes.size()-1));
        booting=wasBooting;
        active=Math.max(0,Math.min(active,panes.size()-1));
        renderLayout();rebuildChrome();saveSession();
    }
    private void disposePane(Pane pane) {
        ViewGroup parent=(ViewGroup)pane.card.getParent();
        if(parent!=null)parent.removeView(pane.card);
        pane.card.removeView(pane.web);
        pane.web.stopLoading();
        pane.web.destroy();
    }
    private void removePane(int index) {
        if (panes.size()<=1 || index<0 || index>=panes.size()) return;
        Pane pane=panes.remove(index);
        disposePane(pane);
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
        int next=panes.indexOf(pane);
        if(next<0 || next==active)return;
        active=next;
        if(layoutMode==3) renderLayout();
        else for(Pane p:panes)p.styleCard();
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
        gridRows.clear();
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
        int columns = layoutMode==1 ? 1 : GridSizing.columns(widthDp,panes.size());
        LinearLayout row=null;
        for(int i=0;i<panes.size();i++){
            if(i%columns==0) {
                row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
                column.addView(row,new LinearLayout.LayoutParams(-1,dp(layoutMode==1?400:320)));
                if(layoutMode==0)gridRows.add(row);
            }
            Pane p=panes.get(i);p.styleCard();
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);
            lp.setMargins(dp(5),dp(5),dp(5),dp(5));
            row.addView(p.card,lp);
        }
        updateGridRowHeights();
    }
    // Change row dimensions only; this never rebuilds WebViews or pane headers.
    private void updateGridRowHeights() {
        if(layoutMode!=0 || gridRows.isEmpty() || viewport==null)return;
        int height=viewport.getHeight();
        boolean fit=GridSizing.shouldFit(fitGrid,panes.size(),height,gridRows.size(),dp(148));
        int tile=fit?GridSizing.rowHeight(height,gridRows.size()):dp(320);
        for(LinearLayout row:gridRows) {
            LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)row.getLayoutParams();
            if(lp.height!=tile) {
                lp.height=tile;
                row.setLayoutParams(lp);
            }
        }
    }
    private void showUrlDistribution() {
        EditText input=new EditText(this);
        input.setMinLines(3);input.setMaxLines(7);
        input.setHint(t(R.string.urls_hint));
        new AlertDialog.Builder(this).setTitle(t(R.string.urls_title)).setView(input)
            .setPositiveButton(t(R.string.apply),(dialog,which)->{
                String value=input.getText().toString().trim();
                if(value.isEmpty()){toast(t(R.string.urls_empty));return;}
                String[] urls=value.split("\\r?\\n");
                if(urls.length==1) {
                    String url=normalize(urls[0]);
                    for(Pane p:panes)p.web.loadUrl(url);
                }else{
                    for(int i=0;i<Math.min(urls.length,panes.size());i++){
                        if(!urls[i].trim().isEmpty())panes.get(i).web.loadUrl(normalize(urls[i]));
                    }
                }
                // Preserve selection and avoid rebuilding the entire layout for every URL.
                saveSession();
            }).setNegativeButton(t(R.string.cancel),null).show();
    }
    private void showBookmarks() {
        JSONArray arr=readArray("bookmarks");
        String[] items=new String[arr.length()];
        for(int i=0;i<items.length;i++) items[i]=arr.optString(i);
        new AlertDialog.Builder(this).setTitle(t(R.string.bookmarks_title)).setItems(items,(d,w)->panes.get(active).navigate(items[w]))
            .setPositiveButton(t(R.string.save_current), (d,w) -> bookmark(panes.get(active).web.getUrl()))
            .setNegativeButton(t(R.string.close),null).show();
    }
    private void bookmark(String url) {
        if(url==null||!url.startsWith("http")) return;
        JSONArray old=readArray("bookmarks");
        for(int i=0;i<old.length();i++) if(url.equals(old.optString(i))) {toast(t(R.string.already_bookmarked));return;}
        old.put(url);
        getPreferences(0).edit().putString("bookmarks",old.toString()).apply();
        toast(t(R.string.saved_to_bookmarks));
    }
    private void showHistory() {
        JSONArray arr=readArray("history");
        int n=Math.min(arr.length(),50);
        String[] items=new String[n];
        for(int i=0;i<n;i++) items[i]=arr.optString(arr.length()-1-i);
        new AlertDialog.Builder(this).setTitle(t(R.string.recent_pages)).setItems(items,(d,w)->panes.get(active).navigate(items[w]))
            .setPositiveButton(t(R.string.close),null).show();
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
            fitGrid=state.optBoolean("fitGrid",true);
            defaultZoom=WebAppearance.clampZoom(state.optInt("defaultZoom",100));
            defaultSiteDark=state.optBoolean("defaultSiteDark",false);
            active=state.optInt("active",0);
            JSONArray urls=state.optJSONArray("urls");
            JSONArray flags=state.optJSONArray("desktopFlags");
            JSONArray zooms=state.optJSONArray("zoomPercents");
            JSONArray siteDarks=state.optJSONArray("siteDarks");
            if(urls!=null)for(int i=0;i<Math.min(urls.length(),MAX_PANES);i++){
                String url=urls.optString(i,"about:blank");
                addPane(url.startsWith("http")||url.equals("about:blank")?url:"about:blank",
                    flags!=null && flags.optBoolean(i,false));
                Pane pane=panes.get(panes.size()-1);
                pane.zoomPct=zooms!=null?WebAppearance.clampZoom(zooms.optInt(i,defaultZoom)):defaultZoom;
                pane.websiteDark=siteDarks!=null?siteDarks.optBoolean(i,defaultSiteDark):defaultSiteDark;
                pane.applyAppearance();
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
            JSONArray desktops=new JSONArray();
            JSONArray zooms=new JSONArray();
            JSONArray siteDarks=new JSONArray();
            for(Pane pane:panes) {
                String url=pane.web.getUrl();
                urls.put(url==null?pane.initialUrl:url);
                desktops.put(pane.desktop);
                zooms.put(pane.zoomPct);
                siteDarks.put(pane.websiteDark);
            }
            state.put("urls",urls);
            state.put("desktopFlags",desktops);
            state.put("zoomPercents",zooms);
            state.put("siteDarks",siteDarks);
            state.put("defaultZoom",defaultZoom);
            state.put("defaultSiteDark",defaultSiteDark);
            state.put("layout",layoutMode);
            state.put("active",active);state.put("dark",dark);state.put("awake",keepAwake);
            state.put("fitGrid",fitGrid);
            getPreferences(0).edit().putString("session",state.toString()).apply();
        } catch(Exception ignored){}
    }
    @Override public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        renderLayout();
        rebuildChrome();
    }
    @Override protected void onStop(){ saveSession();super.onStop(); }
    @Override protected void onDestroy() {
        if(fileCallback!=null){fileCallback.onReceiveValue(null);fileCallback=null;}
        for(Pane p:panes) {
            ViewGroup parent=(ViewGroup)p.card.getParent();
            if(parent!=null)parent.removeView(p.card);
            p.card.removeView(p.web);
            p.web.stopLoading();
            p.web.destroy();
        }
        panes.clear();
        super.onDestroy();
    }
    @Override public void onBackPressed() {
        if(panes.size()>0 && panes.get(active).web.canGoBack())panes.get(active).web.goBack();
        else if(layoutMode==3 && panes.size()>1){
            layoutMode=0;renderLayout();rebuildChrome();saveSession();
        }else super.onBackPressed();
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
        catch(Exception e){fileCallback.onReceiveValue(null);fileCallback=null;toast(t(R.string.no_picker));}
    }
    private void download(String url, String disposition, String mime, String userAgent) {
        if(url==null || !(url.startsWith("https://")||url.startsWith("http://"))) {toast(t(R.string.download_unsupported));return;}
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
            toast(t(R.string.download_started));
        }catch(Exception e){toast(t(R.string.download_error)+": "+e.getClass().getSimpleName());}
    }
    private void screenshot(Pane pane) {
        int w=pane.web.getWidth(),h=pane.web.getHeight();
        if(w<=0||h<=0){toast(t(R.string.view_not_ready));return;}
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
            toast(t(R.string.screenshot_saved));
        }catch(Exception e){toast(t(R.string.capture_failed)+": "+e.getClass().getSimpleName());}
        catch(OutOfMemoryError e){toast(t(R.string.screenshot_oom));}
    }
    private final class Pane {
        final String initialUrl;
        final LinearLayout card;
        final LinearLayout header;
        final WebView web;
        final EditText address;
        final TextView number;
        ProgressBar loading;
        LinearLayout toolsRow;
        String pageTitle="";
        String findQuery="";
        boolean desktop=false;
        boolean headerBuilt=false;
        int zoomPct=defaultZoom;
        boolean websiteDark=defaultSiteDark;
        Pane(String url,boolean startDesktop,boolean autoLoad) {
            initialUrl=url;
            desktop=startDesktop;
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
            if(autoLoad)web.loadUrl(initialUrl);
        }
        private void configureWeb() {
            WebSettings s=web.getSettings();
            s.setJavaScriptEnabled(true);
            if(desktop)s.setUserAgentString(DESKTOP_UA);
            s.setDomStorageEnabled(true);
            s.setDatabaseEnabled(true);
            s.setSupportZoom(true);
            s.setBuiltInZoomControls(true);
            s.setDisplayZoomControls(false);
            s.setUseWideViewPort(true);
            s.setLoadWithOverviewMode(true);
            // Disable unexpected popups; pop-up windows are not supported.
            s.setSupportMultipleWindows(true);
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
                    if("mailto".equalsIgnoreCase(scheme)||"tel".equalsIgnoreCase(scheme)||
                       "sms".equalsIgnoreCase(scheme)){
                        try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}
                        catch(Exception e){toast(t(R.string.no_app_for_link));}
                    }else toast(t(R.string.scheme_blocked));
                    return true;
                }
                @Override public void onPageStarted(WebView view,String url,android.graphics.Bitmap favicon) {
                    if(!address.hasFocus())address.setText(url);
                    pageTitle=url;
                    recordHistory(url);
                }
                @Override public void onPageFinished(WebView view,String url) {
                    if(!address.hasFocus())address.setText(url);
                    applyAppearance();
                    saveSession();
                }
                @Override public boolean onRenderProcessGone(WebView view, android.webkit.RenderProcessGoneDetail detail) {
                    toast(t(R.string.renderer_restored));
                    // A killed renderer cannot be reused. Replace only this pane, never
                    // destroy unrelated live panes. Deferred until the next build.
                    int index=panes.indexOf(Pane.this);
                    if(index>=0) {
                        String current=view.getUrl()==null?initialUrl:view.getUrl();
                        panes.remove(index);
                        ViewGroup parent=(ViewGroup)card.getParent();
                        if(parent!=null)parent.removeView(card);
                        card.removeView(view);
                        view.destroy();
                        Pane replacement=new Pane(current,desktop,true);
                        replacement.zoomPct=zoomPct;
                        replacement.websiteDark=websiteDark;
                        panes.add(index,replacement);
                        replacement.applyAppearance();
                        active=index;renderLayout();saveSession();
                    }
                    return true;
                }
            });
            web.setWebChromeClient(new WebChromeClient(){
                @Override public boolean onCreateWindow(WebView source,boolean dialog,
                                                         boolean userGesture,android.os.Message resultMsg) {
                    if(!userGesture||panes.size()>=MAX_PANES){
                        if(userGesture)toast("Maximum 20 views");
                        return false;
                    }
                    Pane popup=new Pane("about:blank",false,false);
                    panes.add(popup);active=panes.size()-1;
                    renderLayout();rebuildChrome();saveSession();
                    WebView.WebViewTransport transport=(WebView.WebViewTransport)resultMsg.obj;
                    transport.setWebView(popup.web);
                    resultMsg.sendToTarget();
                    toast(t(R.string.popup_opened)+" "+panes.size());
                    return true;
                }
                @Override public void onProgressChanged(WebView view,int progress) {
                    if(loading!=null){
                        loading.setProgress(progress);
                        loading.setVisibility(progress>=100?View.INVISIBLE:View.VISIBLE);
                    }
                }
                @Override public void onReceivedTitle(WebView view,String text) {
                    if(text!=null && !text.isEmpty())pageTitle=text;
                }
                @Override public boolean onShowFileChooser(WebView view,android.webkit.ValueCallback<Uri[]> callback,FileChooserParams params) {
                    chooseFile(callback,params);return true;
                }
            });
            web.setDownloadListener((url,userAgent,contentDisposition,mimetype,contentLength)->
                download(url,contentDisposition,mimetype,userAgent));
        }
        void applyAppearance(){
            if(web==null)return;
            String current=web.getUrl();
            if(current==null)return;
            web.evaluateJavascript(WebAppearance.script(zoomPct,websiteDark),null);
        }
        private Button iconButton(String label,Runnable click,LinearLayout into) {
            Button b=button(label,click);
            b.setTextSize(12);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(38),1);
            lp.setMargins(dp(1),dp(1),dp(1),dp(1));
            into.addView(b,lp);
            return b;
        }
        // Header widgets are constructed ONCE. Layout switching only reparents the
        // Pane.card (which contains the live WebView) without rebuilding its children.
        // Re-adding number/address to another parent used to crash on app startup.
        void styleCard() {
            card.setBackground(rounded(cardColor(),12));
            if (!headerBuilt) {
                headerBuilt = true;
                header.setPadding(dp(4),dp(3),dp(4),0);
                LinearLayout upper=new LinearLayout(MainActivity.this);
                upper.setOrientation(LinearLayout.HORIZONTAL);
                upper.setGravity(Gravity.CENTER_VERTICAL);
                header.addView(upper,new LinearLayout.LayoutParams(-1,dp(41)));
                number.setTextSize(13);
                number.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                number.setGravity(Gravity.CENTER);
                number.setTextColor(Color.WHITE);
                upper.addView(number,new LinearLayout.LayoutParams(dp(28),dp(30)));

                address.setSingleLine(true);
                address.setTextSize(12);
                address.setHint(t(R.string.url_or_search));
                address.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
                address.setImeOptions(EditorInfo.IME_ACTION_GO);
                address.setSelectAllOnFocus(true);
                address.setText(web.getUrl()==null?initialUrl:web.getUrl());
                address.setOnFocusChangeListener((v,focus)->{if(focus)setActive(this);});
                address.setOnEditorActionListener((v,action,event)->{
                    if(action==EditorInfo.IME_ACTION_GO){
                        navigate(address.getText().toString());
                        address.clearFocus();
                        return true;
                    }
                    return false;
                });
                LinearLayout.LayoutParams inputLp=new LinearLayout.LayoutParams(0,dp(33),1);
                inputLp.setMargins(dp(5),0,dp(4),0);
                upper.addView(address,inputLp);
                Button more=button("⋮",()->paneMenu());
                upper.addView(more,new LinearLayout.LayoutParams(dp(30),dp(35)));

                LinearLayout tools=new LinearLayout(MainActivity.this);
                header.addView(tools,new LinearLayout.LayoutParams(-1,dp(41)));
                toolsRow=tools;
                iconButton("‹",()->{setActive(this);if(web.canGoBack())web.goBack();},tools);
                iconButton("›",()->{setActive(this);if(web.canGoForward())web.goForward();},tools);
                iconButton("↻",()->{setActive(this);web.reload();},tools);
                iconButton("⌂",()->{setActive(this);navigate(HOME);},tools);
                iconButton("%",()->showZoomDialog(this),tools);
                iconButton("▣",()->{setActive(this);layoutMode=3;renderLayout();rebuildChrome();saveSession();},tools);
                loading=new ProgressBar(MainActivity.this,null,android.R.attr.progressBarStyleHorizontal);
                loading.setMax(100);
                loading.setProgress(100);
                loading.setVisibility(View.INVISIBLE);
                header.addView(loading,new LinearLayout.LayoutParams(-1,dp(2)));
                number.setOnClickListener(v -> {
                    setActive(this);layoutMode=3;renderLayout();rebuildChrome();saveSession();
                });
                number.setContentDescription("Focus this view");
            }
            boolean compact=layoutMode==0 && fitGrid && panes.size()>=3 && panes.size()<=6;
            LinearLayout.LayoutParams headerLp=(LinearLayout.LayoutParams)header.getLayoutParams();
            int headerHeight=dp(compact?46:88);
            if(headerLp.height!=headerHeight){
                headerLp.height=headerHeight;header.setLayoutParams(headerLp);
            }
            if(toolsRow!=null)toolsRow.setVisibility(compact?View.GONE:View.VISIBLE);
            number.setText(String.valueOf(Math.max(0,panes.indexOf(this))+1));
            number.setBackground(rounded(panes.indexOf(this)==active?accent():muted(),9));
            address.setTextColor(ink());
            address.setHintTextColor(muted());
            address.setBackground(rounded(bg(),8));
            retintHeaderButtons(header);
        }
        private void retintHeaderButtons(ViewGroup group) {
            for(int i=0;i<group.getChildCount();i++) {
                View child=group.getChildAt(i);
                if(child instanceof Button) {
                    Button b=(Button)child;
                    b.setTextColor(ink());
                    b.setBackground(rounded(cardColor(),11));
                } else if(child instanceof ViewGroup) {
                    retintHeaderButtons((ViewGroup)child);
                }
            }
        }
        void navigate(String text){web.loadUrl(normalize(text));setActive(this);saveSession();}
        void showFindDialog(){
            EditText input=new EditText(MainActivity.this);
            input.setSingleLine(true);
            input.setHint(t(R.string.find_hint));
            input.setText(findQuery);
            new AlertDialog.Builder(MainActivity.this).setTitle(t(R.string.zoom_find))
                .setView(input)
                .setPositiveButton(t(R.string.find),(dialog,which)->{
                    findQuery=input.getText().toString().trim();
                    if(findQuery.isEmpty()){
                        web.clearMatches();
                    }else{
                        web.findAllAsync(findQuery);
                    }
                })
                .setNeutralButton(t(R.string.find_next),(dialog,which)->{
                    if(findQuery.isEmpty())toast(t(R.string.no_search));
                    else web.findNext(true);
                })
                .setNegativeButton(t(R.string.clear_matches),(dialog,which)->{
                    findQuery="";web.clearMatches();
                }).show();
        }
        void paneMenu(){
            setActive(this);
            String[] options={
                t(R.string.bookmark_page),
                t(R.string.desktop_mode)+" · "+(desktop?t(R.string.on):t(R.string.off)),
                t(R.string.site_dark_view)+" · "+(websiteDark?t(R.string.on):t(R.string.off)),
                t(R.string.zoom)+" · "+zoomPct+"%",
                t(R.string.screenshot),
                t(R.string.copy_address),
                t(R.string.share_link),
                t(R.string.duplicate_view),
                t(R.string.external_browser),
                t(R.string.zoom_in),
                t(R.string.zoom_out),
                t(R.string.move_before),
                t(R.string.move_after),
                t(R.string.zoom_find),
                t(R.string.find_next),
                t(R.string.clear_matches),
                t(R.string.scroll_top),
                t(R.string.close_view)
            };
            new AlertDialog.Builder(MainActivity.this)
                .setTitle(t(R.string.view)+" "+(panes.indexOf(this)+1))
                .setItems(options,(dialog,which)->{
                    String url=web.getUrl();
                    switch(which){
                        case 0:bookmark(url);break;
                        case 1:
                            desktop=!desktop;
                            web.getSettings().setUserAgentString(desktop?DESKTOP_UA:
                                WebSettings.getDefaultUserAgent(MainActivity.this));
                            web.reload();saveSession();break;
                        case 2:
                            websiteDark=!websiteDark;applyAppearance();saveSession();break;
                        case 3:
                            showZoomDialog(this);break;
                        case 4:screenshot(this);break;
                        case 5:
                            ((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(
                                ClipData.newPlainText("URL",url==null?"":url));
                            toast(t(R.string.copied));break;
                        case 6:
                            Intent share=new Intent(Intent.ACTION_SEND);
                            share.setType("text/plain");
                            share.putExtra(Intent.EXTRA_TEXT,url);
                            startActivity(Intent.createChooser(share,t(R.string.share_link)));break;
                        case 7:
                            if(panes.size()<MAX_PANES){
                                addPane(url==null?"about:blank":url,desktop);
                                Pane copy=panes.get(panes.size()-1);
                                copy.zoomPct=zoomPct;copy.websiteDark=websiteDark;
                                copy.applyAppearance();
                            }else toast(t(R.string.max_views));
                            break;
                        case 8:
                            if(url!=null&&(url.startsWith("https://")||url.startsWith("http://"))){
                                try{
                                    Intent external=new Intent(Intent.ACTION_VIEW,Uri.parse(url));
                                    external.addCategory(Intent.CATEGORY_BROWSABLE);
                                    startActivity(external);
                                }catch(Exception e){toast(t(R.string.no_browser));}
                            }else toast(t(R.string.no_urls));
                            break;
                        case 9:
                            zoomPct=WebAppearance.clampZoom(zoomPct+10);
                            applyAppearance();saveSession();break;
                        case 10:
                            zoomPct=WebAppearance.clampZoom(zoomPct-10);
                            applyAppearance();saveSession();break;
                        case 11:movePane(this,-1);break;
                        case 12:movePane(this,1);break;
                        case 13:showFindDialog();break;
                        case 14:
                            if(findQuery.isEmpty())showFindDialog();
                            else web.findNext(true);
                            break;
                        case 15:findQuery="";web.clearMatches();break;
                        case 16:web.scrollTo(0,0);break;
                        case 17:removePane(panes.indexOf(this));break;
                    }
                }).show();
        }
    }
}
