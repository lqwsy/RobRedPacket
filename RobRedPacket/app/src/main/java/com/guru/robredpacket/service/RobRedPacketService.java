package com.guru.robredpacket.service;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by admin on 2018/4/3.
 * 自动抢红包辅助功能类
 */

public class RobRedPacketService extends AccessibilityService {


    private boolean isNotiChatPage = false;//从通知栏跳转到聊天页面
    private boolean isClickRedPacket = false;//是否点击待领红包


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

        int eventType = event.getEventType();
        CharSequence eventClassName = event.getClassName();
        logInfo("eventClassName === "+eventClassName.toString());

        switch (eventType){
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                logInfo("通知栏红包");
                List<CharSequence> texts = event.getText();
                if(texts.isEmpty()){
                    break;
                }
                for(CharSequence text : texts){
                    //通过文字匹配，如果是恶作剧就没办法
                    if (text.toString().contains("[微信红包]")){
                        try {
                            Notification notification = (Notification)event.getParcelableData();
                            notification.contentIntent.send();//跳转到聊天页面
                            isNotiChatPage = true;
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                logInfo("窗口[状态]发生改变");
                //处理从通知栏跳转到聊天页面的红包
                if(isNotiChatPage){
                    //判断当前是否是微信聊天页面
                    if("com.tencent.mm.ui.LauncherUI".equals(eventClassName)){
                        clickRedPacket();
                    }
                }
                if(isClickRedPacket && "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(eventClassName)){
                    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    robRedPacket(nodeInfo);
                }
                if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(eventClassName)){
                    turnBack();
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                logInfo("内容改变监听");
                clickRedPacket();
                if(isClickRedPacket && "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(eventClassName)){
                    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    robRedPacket(nodeInfo);
                }
                if(isClickRedPacket && "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(eventClassName)){
                    turnBack();
                }
                break;
            default:
                break;

        }

    }


    /**
     * 点击红包
     * */
    private void clickRedPacket(){
        logInfo("点击红包");
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if(accessibilityNodeInfo!=null){
            List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText("领取红包");
            if(nodeInfoList.size()!=0){
                logInfo("找到领取红包节点 个数为"+nodeInfoList.size());
                for(int i=nodeInfoList.size()-1;i>=0;i--){
                    logInfo("className===" + nodeInfoList.get(i).getClassName());
                    if("android.widget.TextView".equals(nodeInfoList.get(i).getClassName())){
                        AccessibilityNodeInfo nodeInfo = nodeInfoList.get(i);
                        if (nodeInfo!=null){
                            logInfo("点击领取红包");
                            nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);//只能点击父元素linearlayout
                            isClickRedPacket = true;
                            isNotiChatPage = false;
                        }
                    }
                }
            }
        }
    }

    /**
     * 点击开字抢红包
     * */
    private void robRedPacket(AccessibilityNodeInfo nodeInfo){
        logInfo("打开红包");
        for(int i=0;i<nodeInfo.getChildCount();i++){
            logInfo("抢红包className === "+nodeInfo.getChild(i).getClassName());
            AccessibilityNodeInfo nodeInfoChild = nodeInfo.getChild(i);
            if ("android.widget.Button".equals(nodeInfoChild.getClassName())){
                nodeInfoChild.performAction(AccessibilityNodeInfo.ACTION_CLICK);//点击开按钮打开红包
            }
            robRedPacket(nodeInfoChild);
        }
    }

    /**
     * 从红包详情页返回
     * */
    private void turnBack(){
        List<AccessibilityNodeInfo> nodeInfoList = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hx");
        for(int j=nodeInfoList.size()-1;j>=0;j--){
            if(nodeInfoList.get(j).isClickable()){
                logInfo("红包详情页点击返回");
                isClickRedPacket = false;
                nodeInfoList.get(j).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }


    /**
     * 系统会在成功连接上你的服务的时候调用这个方法，在这个方法里你可以做一下初始化工作，
     * 例如设备的声音震动管理，也可以调用setServiceInfo()进行配置工作。
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        logInfo("系统链接服务成功");
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
     * 打印后台信息
     * */
    private void logInfo(String content){
        Log.i("rob-red-packet",content);
    }

}
