package com.alwansan.multiview;
public class GridSizingTest {
    private static void eq(int want,int got){if(want!=got)throw new AssertionError(want+" != "+got);}
    private static void yes(boolean actual){if(!actual)throw new AssertionError("Expected true");}
    private static void no(boolean actual){if(actual)throw new AssertionError("Expected false");}
    public static void main(String[] args) {
        eq(1,GridSizing.columns(300,5));
        eq(2,GridSizing.columns(393,5));
        eq(3,GridSizing.rows(5,GridSizing.columns(393,5)));
        eq(3,GridSizing.columns(800,5));
        eq(4,GridSizing.columns(1024,12));
        eq(1,GridSizing.columns(393,1));
        eq(3,GridSizing.rows(5,2));
        yes(GridSizing.shouldFit(true,5,600,3,160));
        no(GridSizing.shouldFit(true,5,300,3,160));
        no(GridSizing.shouldFit(true,20,1200,10,80));
        no(GridSizing.shouldFit(false,5,900,3,160));
        eq(200,GridSizing.rowHeight(600,3));
        System.out.println("GridSizing tests passed");
    }
}
