package com.lavos.mappackage

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import com.lavos.MonitorBroadcast
import com.lavos.app.Pref


class SendBrod {

    companion object{
        var monitorNotiID:Int = 201
        fun sendBrod(context: Context){
            if(Pref.user_id.toString().length > 0){
                //var notificationManager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
                //notificationManager.cancel(monitorNotiID)

                val intent: Intent = Intent(context, MonitorBroadcast::class.java)
                intent.putExtra("notiId", monitorNotiID)
                intent.putExtra("fuzedLoc", "Fuzed Stop")
                context.sendBroadcast(intent)
            }
        }

        fun stopBrod(context: Context){
            if (monitorNotiID != 0){
                if(MonitorBroadcast.player!=null){
                    MonitorBroadcast.player.stop()
                    MonitorBroadcast.player=null
                    MonitorBroadcast.vibrator.cancel()
                    MonitorBroadcast.vibrator=null
                }
                var notificationManager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(monitorNotiID)
            }
        }
    }

}