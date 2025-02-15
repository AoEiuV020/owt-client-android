package owt.p2pandsfu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;

import java.util.Objects;

public class ScreenRecordingService extends Service {
    private static final String channelId = "ScreenRecording";//渠道id

    public ScreenRecordingService() {
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, ScreenRecordingService.class);
        ContextCompat.startForegroundService(context, starter);
    }

    public static void stop(Context context) {
        Intent starter = new Intent(context, ScreenRecordingService.class);
        context.stopService(starter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Notification createNotification(Context ctx) {
        Notification.Builder builder;
        builder = new Notification.Builder(ctx, channelId);
        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(ctx.getString(R.string.title_screen_sharing));
        builder.setContentText("");
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setAutoCancel(true);
        builder.setShowWhen(true);
        builder.setSmallIcon(android.R.drawable.stat_notify_chat);
        return builder.build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createChannel(Context ctx) {
        NotificationManager nm = Objects.requireNonNull(ContextCompat.getSystemService(ctx, NotificationManager.class));
        if (nm.getNotificationChannel(channelId) != null) {
            return;
        }
        //创建通知渠道
        CharSequence name = ctx.getString(R.string.title_screen_sharing);
        String description = ctx.getString(R.string.title_screen_sharing);
        int importance = NotificationManager.IMPORTANCE_MIN; //重要性级别 开启通知，不会弹出，但没有提示音，状态栏中无显示
        NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
        mChannel.setDescription(description);//渠道描述
        mChannel.setVibrationPattern(new long[]{0});//震动频率
        mChannel.enableLights(false);//是否显示通知指示灯
        mChannel.enableVibration(false);//是否振动
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);//设置在锁屏界面上显示这条通知
        mChannel.setLightColor(Color.GREEN);//如果显示的话就为绿色
        mChannel.setName(name);
        mChannel.setSound(null, null);
        nm.createNotificationChannel(mChannel);//创建通知渠道
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(this);
            startForeground(1, createNotification(this));
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
