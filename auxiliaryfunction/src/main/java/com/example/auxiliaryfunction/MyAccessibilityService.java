package com.example.auxiliaryfunction;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by admin on 2018/3/22.
 * 辅助功能开发服务类
 */

public class MyAccessibilityService extends AccessibilityService {


    private boolean isMainPage = true;//是否是微信首页
    private boolean isOpenWeiChatPage = false;//是否打开聊天页面
    private boolean isOpenRP = false;//是否点击打开红包
    private boolean isOpenRPDetail = false;//是否拆开红包
    private AccessibilityNodeInfo rpNode;//红包节点


    /**
     * 系统会在成功连接上你的服务的时候调用这个方法，在这个方法里你可以做一下初始化工作，
     * 例如设备的声音震动管理，也可以调用setServiceInfo()进行配置工作。
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    /**
     * 通过这个函数可以接收系统发送来的AccessibilityEvent，接收来的AccessibilityEvent是经过过滤的，
     * 过滤是在配置工作时设置的
     * 页面变化回调事件
     *
     * @param event event.getEventType() 当前事件的类型;
     *              event.getClassName() 当前类的名称;
     *              event.getSource() 当前页面中的节点信息；
     *              event.getPackageName() 事件源所在的包名
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //接收事件类型
        int eventType = event.getEventType();
        CharSequence eventClassName = event.getClassName();

        logInfo("eventClassName === "+eventClassName);

        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                logInfo("通知栏变化");

                //捕获通知栏事件
                List<CharSequence> texts = event.getText();//获取所有通知文本
                if (texts.isEmpty()) {
                    break;
                }
                for (CharSequence text : texts) {
                    String content = text.toString();
                    //通过“[微信红包]”文本来匹配是否是微信红包
                    if (content.contains(Constant.WECHAT_HONGBAO_NOTI_TEXT)) {
                        logInfo("通知栏有红包");
                        openWeiChatPage(event);
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                logInfo("窗口状态变化");

                //当窗口变化时
                if(isOpenWeiChatPage){
                    String classNameStateChanged = event.getClassName().toString();
                    if (classNameStateChanged.equals(Constant.LAUCHER)) {
                        //如果打开了微信页面，则执行找红包
                        findTheRP();
                    }
                }
                if (isOpenRP && Constant.LUCKEY_MONEY_RECEIVER.equals(event.getClassName().toString())) {
                    //如果打开了抢红包页面
                    AccessibilityNodeInfo rootNode1 = getRootInActiveWindow();
                    if (findOpenBtn(rootNode1)) {
                        //找到拆开按钮
                        logInfo("拆红包成功");
                        isOpenRPDetail = true;
//                        backHome(this);
                    }
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                logInfo("窗口视图滚动");

                //监听窗口滚动，当前处于聊天窗口时

                //判断是否是微信聊天页面类
                isOpenWeiChatPage = false;
                String classNameScrolled = event.getClassName().toString();
                if (classNameScrolled.equals("android.widget.ListView")) {
                    findTheRP();
                }
                if (isOpenRP && Constant.LUCKEY_MONEY_RECEIVER.equals(event.getClassName().toString())) {
                    //如果打开了抢红包页面
                    AccessibilityNodeInfo rootNode1 = getRootInActiveWindow();
                    if (findOpenBtn(rootNode1)) {
                        //找到拆开按钮
                        logInfo("拆红包成功");
                        isOpenRPDetail = true;
//                        backHome(this);
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                logInfo("窗口内容改变");

                //监听窗口内容改变
                //判断是否是微信聊天页面类
                isOpenWeiChatPage = false;
                String classNames = event.getClassName().toString();
                if (classNames.equals("android.widget.ListView")) {
                    findTheRP();
                }
                if (isOpenRP && Constant.LUCKEY_MONEY_RECEIVER.equals(event.getClassName().toString())) {
                    //如果打开了抢红包页面
                    AccessibilityNodeInfo rootNode1 = getRootInActiveWindow();
                    if (findOpenBtn(rootNode1)) {
                        //找到拆开按钮
                        logInfo("拆红包成功");
                        isOpenRPDetail = true;
//                        backHome(this);
                    }
                }
                break;
            default:
                break;
        }
        release();
    }

    /**
     * 这个在系统想要中断AccessibilityService返给的响应时会调用。在整个生命周期里会被调用多次。
     */
    @Override
    public void onInterrupt() {

    }

    /**
     * 在系统将要关闭这个AccessibilityService会被调用。在这个方法中进行一些释放资源的工作。
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    /**
     * 打开微信详情页
     */
    private void openWeiChatPage(AccessibilityEvent event) {
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();

            isOpenWeiChatPage = true;//设置从通知栏打开微信聊天页状态

            //打开通知栏的intent，跳转到聊天页面
            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }


    /***
     * 从根目录开始找红包
     *
     */
    private void findTheRP() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            if (findRP(rootNode)) {
                isOpenRP = true;
            }
            if (rpNode != null) {
                rpNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /**
     * 循环节点树，找到红包并打开
     */
    private boolean findRP(AccessibilityNodeInfo rootNode) {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);

            logInfo("class 名称是：" + nodeInfo.getClassName());

            if (nodeInfo == null) {
                continue;
            }

            if(nodeInfo.getText()!=null){
                logInfo("控件文本内容是：" + nodeInfo.getText());

                if ("android.widget.TextView".equals(nodeInfo.getClassName())) {
                    if (Constant.WECHAT_HONGBAO_GET_TEXT.equals(nodeInfo.getText())) {
                        //找到待领取红包
                        isOpenRP = true;
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);//点击红包
                        return true;
                    }
                }
            }
            if (findRP(nodeInfo)) {
                if ("android.widget.LinearLayout".equals(nodeInfo.getClassName())) {
                    rpNode = nodeInfo;//把红包那个节点传出去
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 找到红包上开的按钮
     */
    private boolean findOpenBtn(AccessibilityNodeInfo rootNode) {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
            if ("android.widget.Button".equals(nodeInfo.getClassName())) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);//点击拆开红包
                return true;
            }
            findOpenBtn(nodeInfo);//继续找红包
        }
        return false;
    }

    /**
     * 返回桌面
     */
    private void backHome(Context context) {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        home.addCategory(Intent.CATEGORY_HOME);

        logInfo("================返回桌面=================");

        context.startActivity(home);
    }

    /**
     * 释放系统资源
     */
    private void release() {
        if (rpNode != null) {
            rpNode = null;
        }
    }


    /**
     * 打印信息
     */
    public void logInfo(String info) {
        Log.i(Constant.TAG, info);
    }

}
