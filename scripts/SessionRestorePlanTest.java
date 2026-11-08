package com.alwansan.multiview;
public final class SessionRestorePlanTest {
    private static void check(boolean actual,boolean expected){
        if(actual!=expected)throw new AssertionError("Expected "+expected+" got "+actual);
    }
    public static void main(String[] args){
        for(int i=0;i<5;i++)check(SessionRestorePlan.eager(i,4,5,0,true),true);
        check(SessionRestorePlan.eager(9,14,20,0,true),false);
        check(SessionRestorePlan.eager(14,14,20,0,true),true);
        check(SessionRestorePlan.eager(0,19,20,3,false),true);
        check(SessionRestorePlan.eager(5,19,20,3,false),false);
        check(SessionRestorePlan.eager(19,19,20,3,false),true);
        System.out.println("Session restore planning tests passed");
    }
}
