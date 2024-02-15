package com.example.spyproject




import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.spyproject.databinding.ActivityMainBinding
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var mqttAndroidClient: MqttAndroidClient
    private val serverUri = "tcp://broker.example.com:1883"
    private var countDownTimer: CountDownTimer? = null
    private val initialTimeInMillis: Long = 60000 // Temps initial en millisecondes (60 secondes ici)
    //private lateinit var mqttClient: MqttAndroidClient
    lateinit var mqttClient:MqttAndroidClient
    val topic="test"
    // val br: BroadcastReceiver =  ;


    companion object {
        const val TAG = "AndroidMqttClient"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.chrono.format = "%s"
        startCountdown()
        val serverURI = "mqtt://mqtt-dashboard.com" //:1883
        //mqttClient = MqttAndroidClient(this, serverURI, "kotlin_client")


        connectMqtt()
   //     publish("my messssssaaaaaaagggge");
//        this.publish("test","test from android");

    }

    fun connectMqtt() {
        var clientId = MqttClient.generateClientId()
        mqttClient = MqttAndroidClient(applicationContext,"tcp://mqtt-dashboard.com", clientId)
        try {
            var token: IMqttToken = mqttClient.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "connection success")
                    subscribe()
                }
                override fun onFailure(asyncActionToken: IMqttToken?,    exception: Throwable?) {
                    Log.d("MQTT", exception.toString());
                    Log.d("MQTT", "connection failure")
                }
            }
            mqttClient.setCallback( object:MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.d("MQTT","connection lost")
                }
                override fun messageArrived(topic: String?, message:  MqttMessage?) {
                    Log.d("MQTT", "message arrived on topic "+topic+ "  message:"+message?.toString())
                }
                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d("MQTT", "deliveryComplete")
                }
            })
        } catch (e: MqttException) {
        }
    }

    fun subscribe(){
        var token=mqttClient.subscribe(topic,1)
        token.actionCallback=object:IMqttActionListener{
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("MQTT", "subscribe success")
            }
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.d("MQTT", "subscribe failed")
            }
        }
    }

    fun publish(payload:String){
        val message = MqttMessage()
        message.payload = payload.toByteArray()
        message.qos = 1
        message.isRetained = false
        mqttClient.publish(topic, message, null,  object:IMqttActionListener{
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("MQTT", "publish success")
            }
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.d("MQTT", "publish failed")
            }
        })
    }

    fun unsubscribe(){
        //mqttClient.unsubsribe(topic)
    }

    fun disconnectMqtt(){
        try{
            var token= mqttClient.disconnect()
            token?.actionCallback=object:IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT","disconnect success")
                }
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("MQTT", "disconnect failed");
                }
            }
        }catch (e:MqttException){
        }
    }
    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(initialTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                binding.chrono.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                // Animation à exécuter lorsque le compte à rebours est terminé
                // Par exemple, vous pouvez démarrer une animation ici
                // Pour cet exemple, je vais simplement afficher un message
                binding.chrono.text = "Terminé!"
            }
        }

        countDownTimer?.start()
    }

    fun connect(context: Context) {
        val serverURI = "mqtt://mqtt-dashboard.com" //:1883
        mqttClient = MqttAndroidClient(context, serverURI, "kotlin_client")
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(TAG, "Receive message: ${message.toString()} from topic: $topic")
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
        val options = MqttConnectOptions()
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Connection failure")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    fun subscribe(topic: String, qos: Int = 1) {
        try {
            mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unsubscribe(topic: String) {
        try {
            mqttClient.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Unsubscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to unsubscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "$msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }




    override fun onDestroy() {
        super.onDestroy()
        // Assurez-vous d'annuler le CountDownTimer lors de la destruction de l'activité
        countDownTimer?.cancel()
    }

}