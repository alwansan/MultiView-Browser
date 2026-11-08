package com.alwansan.multiview;

/** Pure, independently testable rules for adaptive multi-pane layouts. */
final class GridSizing {
    private GridSizing(){}
    static int columns(int widthDp,int count) {
        if(count<=1)return 1;
        if(widthDp>=900 && count>=8)return 4;
        if(widthDp>=680 && count>=3)return 3;
        return widthDp>=320?2:1;
    }
    static int rows(int count,int columns) {
        return (Math.max(0,count)+Math.max(1,columns)-1)/Math.max(1,columns);
    }
    static boolean shouldFit(boolean requested,int count,int availableHeightPx,int rows,int minimumTilePx) {
        return requested && count>=1 && count<=6 && rows>0 &&
            availableHeightPx>=rows*minimumTilePx;
    }
    static int rowHeight(int availableHeightPx,int rows) {
        return Math.max(1,availableHeightPx/Math.max(1,rows));
    }
}
