package com.example.n54guru.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives USB device attached broadcasts so the OBD2 adapter can be
 * auto-detected when plugged in. The actual permission grant and device
 * opening is handled in [com.example.n54guru.services.OBD2Service].
 */
class UsbAttachmentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            // Foreground service or activity will pick the device up via UsbManager
            // when the user opens the connection wizard.
        }
    }
}
