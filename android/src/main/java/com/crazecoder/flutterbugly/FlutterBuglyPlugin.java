package com.crazecoder.flutterbugly;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.crazecoder.flutterbugly.bean.BuglyInitResultInfo;
import com.crazecoder.flutterbugly.utils.JsonUtil;
import com.crazecoder.flutterbugly.utils.MapUtil;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;

import io.flutter.BuildConfig;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterBuglyPlugin
 */
public class FlutterBuglyPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    private Result result;
    private boolean isResultSubmitted = false;
    private static MethodChannel channel;
    @SuppressLint("StaticFieldLeak")
    private static Activity activity;
    private FlutterPluginBinding flutterPluginBinding;


    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        channel = new MethodChannel(registrar.messenger(), "crazecoder/flutter_bugly");
        FlutterBuglyPlugin plugin = new FlutterBuglyPlugin();
        channel.setMethodCallHandler(plugin);
        activity = registrar.activity();
    }

    @Override
    public void onMethodCall(final MethodCall call, @NonNull final Result result) {
        isResultSubmitted = false;
        this.result = result;
        switch (call.method) {
            case "initBugly":
                if (call.hasArgument("appId")) {
                    String appId = call.argument("appId").toString();

                    CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(activity.getApplicationContext());

                    if (call.hasArgument("channel")) {
                        String channel = call.argument("channel");
                        if (!TextUtils.isEmpty(channel))
                            strategy.setAppChannel(channel);
                    }

                    Bugly.init(activity.getApplicationContext(), appId, BuildConfig.DEBUG, strategy);

                    result(getResultBean(true, appId, "Bugly 初始化成功"));
                } else {
                    result(getResultBean(false, null, "Bugly appId不能为空"));
                }
                break;
            case "setUserId":
                if (call.hasArgument("userId")) {
                    String userId = call.argument("userId");
                    CrashReport.setUserId(activity.getApplicationContext(), userId);
                }
                result(null);
                break;
            case "setUserTag":
                if (call.hasArgument("userTag")) {
                    Integer userTag = call.argument("userTag");
                    if (userTag != null)
                        CrashReport.setUserSceneTag(activity.getApplicationContext(), userTag);
                }
                result(null);
                break;
            case "putUserData":
                if (call.hasArgument("key") && call.hasArgument("value")) {
                    String userDataKey = call.argument("key");
                    String userDataValue = call.argument("value");
                    CrashReport.putUserData(activity.getApplicationContext(), userDataKey, userDataValue);
                }
                result(null);
                break;
            case "setAppChannel":
                String channel = call.argument("channel");
                if (!TextUtils.isEmpty(channel)) {
                    CrashReport.setAppChannel(activity.getApplicationContext(), channel);
                }
                result(null);
                break;
            case "postCatchedException":
                postException(call);
                result(null);
                break;
            default:
                result.notImplemented();
                isResultSubmitted = true;
                break;
        }

    }

    private void postException(MethodCall call) {
        String message = "";
        String detail = null;
        if (call.hasArgument("crash_message")) {
            message = call.argument("crash_message");
        }
        if (call.hasArgument("crash_detail")) {
            detail = call.argument("crash_detail");
        }
        if (TextUtils.isEmpty(detail)) return;
        CrashReport.postException(8, "Flutter Exception", message, detail, null);

    }

    private void result(Object object) {
        if (result != null && !isResultSubmitted) {
            if (object == null) {
                result.success(null);
            } else {
                result.success(JsonUtil.toJson(MapUtil.deepToMap(object)));
            }
            isResultSubmitted = true;
        }
    }

    private BuglyInitResultInfo getResultBean(boolean isSuccess, String appId, String msg) {
        BuglyInitResultInfo bean = new BuglyInitResultInfo();
        bean.setSuccess(isSuccess);
        bean.setAppId(appId);
        bean.setMessage(msg);
        return bean;
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        this.flutterPluginBinding = binding;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        flutterPluginBinding = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "crazecoder/flutter_bugly");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }
}