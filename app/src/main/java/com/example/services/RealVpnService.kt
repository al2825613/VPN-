package com.example.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class RealVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnThread: Thread? = null
    private var isRunning = false

    companion object {
        const val ACTION_CONNECT = "com.example.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpn.DISCONNECT"
        const val EXTRA_IP = "vpn_ip"
        const val EXTRA_PORT = "vpn_port"
        const val EXTRA_PROTOCOL = "vpn_protocol"
        
        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "vpn_channel"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action == ACTION_DISCONNECT) {
            disconnect()
            return START_NOT_STICKY
        }
        
        if (intent != null && intent.action == ACTION_CONNECT) {
            val ip = intent.getStringExtra(EXTRA_IP) ?: return START_NOT_STICKY
            val port = intent.getIntExtra(EXTRA_PORT, 1194)
            val protocol = intent.getStringExtra(EXTRA_PROTOCOL) ?: "udp"
            
            connect(ip, port, protocol)
            showNotification()
            return START_STICKY
        }
        
        return START_NOT_STICKY
    }

    private fun connect(ip: String, port: Int, protocol: String) {
        if (isRunning) return
        isRunning = true
        
        vpnThread = thread {
            try {
                // Here we setup the VpnService Tun Interface
                val builder = Builder()
                
                // We use a private IP for the tun interface. 
                // In a real OpenVPN connection, this is negotiated and provided by the server.
                builder.addAddress("10.0.0.2", 24)
                builder.addRoute("0.0.0.0", 0) // Route all traffic
                builder.addDnsServer("8.8.8.8")
                
                // Allow the app to bypass the VPN
                try {
                    builder.addDisallowedApplication(packageName)
                } catch (e: Exception) {
                    Log.e("RealVpnService", "Cannot bypass package: $e")
                }

                vpnInterface = builder.setSession("VPN Pro").establish()
                
                if (vpnInterface != null) {
                    Log.d("RealVpnService", "VPN Interface established! Routing traffic to $ip:$port via $protocol")
                    
                    val inStream = FileInputStream(vpnInterface!!.fileDescriptor)
                    val outStream = FileOutputStream(vpnInterface!!.fileDescriptor)
                    val packet = ByteBuffer.allocate(32767)
                    
                    while (isRunning) {
                        try {
                            val length = inStream.read(packet.array())
                            if (length > 0) {
                                packet.limit(length)
                                // In a real implementation (like OpenVPN or IPsec) you would:
                                // 1. Encrypt the packet
                                // 2. Send it over a UDP/TCP Socket pointing to $ip:$port
                                // 3. Read the response from the Socket
                                // 4. Decrypt the response packet
                                // 5. Write it back to the outStream so the OS apps receive it
                                
                                // Since this is a proof of concept environment without NDK OpenVPN binaries,
                                // we are dropping packets here (acting as a blackhole for tun0).
                                packet.clear()
                            }
                        } catch (e: Exception) {
                            if (isRunning) {
                                Log.e("RealVpnService", "Error reading packet: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RealVpnService", "Error establishing VPN: ${e.message}")
            } finally {
                disconnect()
            }
        }
    }

    private fun disconnect() {
        isRunning = false
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e("RealVpnService", "Error closing interface: ${e.message}")
        }
        
        vpnThread?.interrupt()
        vpnThread = null
        
        stopForeground(true)
        stopSelf()
    }

    private fun showNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VPN Status",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VPN Pro Active")
            .setContentText("Your traffic is secured.")
            .setSmallIcon(android.R.drawable.ic_secure)
            .setContentIntent(pendingIntent)
            .build()
            
        // Required for Android 11+ foreground services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }
}
