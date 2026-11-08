package com.alwansan.multiview;

public class WebAppearanceTest {
    private static void eq(int wanted,int actual){
        if(wanted!=actual)throw new AssertionError(wanted+" != "+actual);
    }
    public static void main(String[] args){
        eq(10,WebAppearance.clampZoom(0));
        eq(10,WebAppearance.clampZoom(10));
        eq(50,WebAppearance.clampZoom(51));
        eq(100,WebAppearance.clampZoom(99));
        eq(200,WebAppearance.clampZoom(200));
        eq(200,WebAppearance.clampZoom(900));
        if(!WebAppearance.script(50,false).contains("zoom='50%'"))throw new AssertionError("Missing CSS zoom");
        if(!WebAppearance.script(110,true).contains("multiview-force-dark"))throw new AssertionError("Missing dark CSS");
        if(!WebAppearance.script(110,false).contains("element.remove()"))throw new AssertionError("Missing dark cleanup");
        System.out.println("WebAppearance tests passed");
    }
}
