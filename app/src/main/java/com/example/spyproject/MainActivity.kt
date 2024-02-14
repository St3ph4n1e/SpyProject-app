package com.example.spyproject



import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spyproject.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var mqttAndroidClient: MqttAndroidClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        clientId += System.currentTimeMillis()
        mqttAndroidClient = MqttAndroidClient(applicationContext, serverUri, clientId)
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                if (reconnect) {
                    addToHistory("Reconnected: $serverURI")
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic()
                } else {
                    addToHistory("Connected: $serverURI")
                }
            }

            override fun connectionLost(cause: Throwable?) {
                addToHistory("The Connection was lost.")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                addToHistory("Incoming message: " + String(message.payload))
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {}
        })
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false
        addToHistory("Connecting: $serverUri")
        mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                val disconnectedBufferOptions = DisconnectedBufferOptions()
                disconnectedBufferOptions.isBufferEnabled = true
                disconnectedBufferOptions.bufferSize = 100
                disconnectedBufferOptions.isPersistBuffer = false
                disconnectedBufferOptions.isDeleteOldestMessages = false
                mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                subscribeToTopic()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                addToHistory("Failed to connect: $serverUri")
            }
        })
    }

    private fun addToHistory(mainText: String) {
        Timber.d(mainText)
        @SuppressLint("SimpleDateFormat")
        val timestamp = SimpleDateFormat("HH:mm.ss.SSS").format(Date(System.currentTimeMillis()))

        Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }

    fun subscribeToTopic() {
        mqttAndroidClient.subscribe(subscriptionTopic, QoS.AtMostOnce.value, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                addToHistory("Subscribed! $subscriptionTopic")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                addToHistory("Failed to subscribe $exception")
            }
        })

        // THIS DOES NOT WORK!
        mqttAndroidClient.subscribe(subscriptionTopic, QoS.AtMostOnce.value) { topic, message ->
            Timber.d("Message arrived $topic : ${String(message.payload)}")
            addToHistory("Message arrived $message")
        }
    }

    private fun publishMessage() {
        val message = MqttMessage()
        message.payload = publishMessage.toByteArray()
        if (mqttAndroidClient.isConnected) {
            mqttAndroidClient.publish(publishTopic, message)
            addToHistory("Message Published >$publishMessage<")
            if (!mqttAndroidClient.isConnected) {
                addToHistory(mqttAndroidClient.bufferedMessageCount.toString() + " messages in buffer.")
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Not connected", Snackbar.LENGTH_SHORT).setAction("Action", null).show()
        }
    }

    companion object {
        private const val serverUri = "tcp://broker.hivemq.com:1883"
        private const val subscriptionTopic = "exampleAndroidTopic"
        private const val publishTopic = "exampleAndroidPublishTopic"
        private const val publishMessage = "Hello World"
        private var clientId = "BasicSample"
    }



}