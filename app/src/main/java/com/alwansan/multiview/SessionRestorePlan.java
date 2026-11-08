package com.alwansan.multiview;

/**
 * A cold start cannot resurrect WebView's DOM or JS heap. Load only likely
 * visible panes so 20 tabs don't all issue expensive navigation simultaneously.
 * Other panes remain visible as explicit tap-to-restore placeholders.
 */
final class SessionRestorePlan {
    private SessionRestorePlan(){}
    static boolean eager(int index,int active,int total,int layout,boolean fitGrid){
        if(index==active)return true;
        int count=layout==0 && fitGrid ? Math.min(6,total) : Math.min(2,total);
        return index>=0 && index<count;
    }
}
