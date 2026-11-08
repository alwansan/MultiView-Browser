package com.alwansan.multiview;

/** WebView page appearance. CSS zoom may be overridden by websites; darkening is opt-in. */
final class WebAppearance {
    private WebAppearance(){}
    static int clampZoom(int value){
        return Math.max(10,Math.min(200,Math.round(value/10f)*10));
    }
    static String script(int zoom,boolean dark){
        int pct=clampZoom(zoom);
        // No user-provided JS or untrusted strings are interpolated into this script.
        return "(function(){try{"
            +"document.documentElement.style.zoom='"+pct+"%';"
            +"var element=document.getElementById('multiview-force-dark');"
            +(dark
                ?"if(!element){element=document.createElement('style');"
                  +"element.id='multiview-force-dark';"
                  +"document.documentElement.appendChild(element);}"
                  +"element.textContent='html{filter:invert(1) hue-rotate(180deg)!important;"
                  +"background-color:#ffffff!important} img,video,canvas,picture"
                  +"{filter:invert(1) hue-rotate(180deg)!important}';"
                :"if(element)element.remove();")
            +"}catch(e){}})();";
    }
}
