package com.fivestars.edynamosample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Build.MANUFACTURER
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.github.pedrovgs.lynx.LynxConfig
import com.magtek.mobile.android.mtlib.*
import org.json.JSONException
import org.json.JSONObject
import com.github.pedrovgs.lynx.LynxActivity



class MainActivity : AppCompatActivity() {

    private val TAG = "EdynamoPlugin"
    private var previousConnectionState: MTConnectionState? = null
    private lateinit var mtscra: MTSCRA
    private val mscraHandler = Handler(SCRAHandlerCallback())
    // We track whether a card is inserted.  This is based on tracking the card inserted and removed
    // events.
    private var cardInserted: Boolean = false
    private var mProgress: Int = 0x00
    private var mEvent: Int = 0x00

    private val br = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.i(TAG, "Got intent: " + action!!)

            when (action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> mtscra.openDevice()
                UsbManager.ACTION_USB_DEVICE_DETACHED -> mtscra.closeDevice()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mtscra = MTSCRA(applicationContext, mscraHandler)
        mtscra.setConnectionType(MTConnectionType.USB)
        mtscra.openDevice()

        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(br, filter)


        val lynxConfig = LynxConfig()
        lynxConfig.maxNumberOfTracesToShow = 4000

        val lynxActivityIntent = LynxActivity.getIntent(this, lynxConfig)
        startActivity(lynxActivityIntent)
    }

    private inner class SCRAHandlerCallback : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                MTSCRAEvent.OnDataReceived -> {
                    Log.i(TAG, "MTSCRAEvent.OnDataReceived")
                    try {
                    } catch (e: JSONException) {
                        Log.e(TAG, "JSON error while handling card swipe: $e")
                    }
                }

                MTSCRAEvent.OnDeviceConnectionStateChanged -> handleConnectionStateChange(msg.obj as MTConnectionState)

                MTEMVEvent.OnTransactionResult -> {
                    Log.i(TAG, "MTSCRAEvent.OnTransactionResult")
                }

                MTEMVEvent.OnTransactionStatus -> {
                    Log.i(TAG, "MTSCRAEvent.OnTransactionStatus")
                    handleTransactionStatus(msg.obj as ByteArray)

                }

                MTEMVEvent.OnARQCReceived -> {
                    Log.i(TAG, "MTSCRAEvent.OnARQCReceived")

                }

                MTEMVEvent.OnUserSelectionRequest -> {
                    Log.i(TAG, "MTEMVEvent.OnUserSelectionRequest")

                }
            }

            return true
        }
    }

    private fun handleTransactionStatus(data: ByteArray) {
        mEvent = data[0] as Int
        mProgress = data[2] as Int
        Log.i(TAG, "Event: " + TLVParser.getHexString(byteArrayOf(mEvent.toByte())))
        Log.i(TAG, "Progress: " + TLVParser.getHexString(byteArrayOf(mProgress.toByte())))

        when (mEvent) {
            TransactionStatusEvent.TRANSACTION_PROGRESS_CHANGE -> if (mProgress == TransactionStatusProgress.SELECTING_APPLICATION) {
                // EMV process has begun

            }

            TransactionStatusEvent.CARD_ERROR -> {
                Log.i(TAG, "EMV transaction failed")

            }

            TransactionStatusEvent.TIMEOUT -> Log.i(TAG, "EMV transaction timed out")

            TransactionStatusEvent.TRANSACTION_TERMINATED -> {
                Log.i(TAG, "EMV transaction terminated")

            }

            TransactionStatusEvent.CARD_INSERTED -> {
                Log.i(TAG, "Card inserted into emv slot")
                updateCardInsert(true)
            }

            TransactionStatusEvent.CARD_REMOVED -> {
                // This event does not always seem to fire, but we will do the following when
                // the event does occur
                Log.i(TAG, "Card removed from emv slot")
                updateCardInsert(false)
            }

            TransactionStatusEvent.WAITING_FOR_USER_RESPONSE -> {
                // When we did not explicitly get a remove card event, the case where we are
                // waiting for the user to insert the card tells us the card is not in the slot.
                Log.i(TAG, "Waiting for user response")
                if (mProgress == TransactionStatusProgress.WAITING_FOR_USER_TO_INSERT_CARD) {
                    updateCardInsert(false)
                }
            }
        }
    }

    private fun updateCardInsert(state: Boolean) {
        if (cardInserted != state) {
            sendUpdate()
        }
        cardInserted = state
    }

    private fun getStatus() {
        Log.i(TAG, "Getting Reader Status")
        try {
            val payload = getStatusPayload()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting reader status.")
        }

    }

    private fun getStatusPayload(): JSONObject {
        val payload = JSONObject()

        val isConnected = mtscra.isDeviceConnected
        payload.put("deviceConnected", isConnected) // Done
        payload.put("cardInserted", if (isConnected) cardInserted else null) // Done
        payload.put("batteryLevel", mtscra.batteryLevel)
        payload.put("usbPermission", getMagTekUsbPermission())

        return payload
    }

    private fun handleConnectionStateChange(state: MTConnectionState) {
        Log.i(TAG, "Previous connection state: $previousConnectionState")
        Log.i(TAG, "Current connection state: $state")

        if (previousConnectionState != state) {
            sendUpdate()
        }

        if (previousConnectionState == MTConnectionState.Connecting && state == MTConnectionState.Connected) {


        }

        previousConnectionState = state
    }

    /**
     * This method checks if we have USB permission to the magtek device.  If we can not
     * explicitly get the permission, we will return null.  Otherwise we will return the
     * permission we get back.
     */
    private fun getMagTekUsbPermission(): Boolean? {
        var hasPermission: Boolean? = null

        val usbManager =
            getSystemService(Context.USB_SERVICE) as UsbManager

        if (usbManager == null) {
            Log.w(TAG, "Unable to get UsbManager.")
            return hasPermission
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.w(TAG, "Android Platform Version must be at least: " + Build.VERSION_CODES.LOLLIPOP)
            return hasPermission
        }

        val deviceList = usbManager.deviceList

        for (usbDevice in deviceList.values) {
            if (usbDevice.manufacturerName != null && usbDevice.manufacturerName!!.equals(
                    MANUFACTURER,
                    ignoreCase = true
                )
            ) {
                hasPermission = usbManager.hasPermission(usbDevice)
                // Log if USB manager explicitly reports no permission.
                if (!hasPermission) {
                    Log.i(TAG, "USB Manager reporting no permission to reader.")
                }
                return hasPermission
            }
        }

        Log.w(TAG, "Reader is not connected to device.")
        return hasPermission
    }

    private fun sendUpdate() {
        val jsonObject = getStatusPayload()
        Log.e("darran", "event stream: $jsonObject")
    }
}
