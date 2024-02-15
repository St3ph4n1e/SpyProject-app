
package com.example.spyproject

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.spyproject.databinding.ActivityMainBinding
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val clientId = "AndroidClient"
    private var countDownTimer: CountDownTimer? = null
    private val initialTimeInMillis: Long = 60000 // Temps initial en millisecondes (60 secondes ici)
    private lateinit var mqttAndroidClient: MqttAndroidClient
    private val serverUri = "ws://test.mosquitto.org:8080"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chrono.format = "%s"
        //startCountdown()

        mqttAndroidClient = MqttAndroidClient(applicationContext, serverUri, clientId)

        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                if (reconnect) {
                    Log.d("MQTT","Reconnexion réussie à $serverURI")
                } else {
                    // Connexion initiale réussie
                    Log.d("MQTT","Connexion initiale réussie à $serverURI")
                    subscribeToTopic("presence")
                }
            }

            override fun connectionLost(cause: Throwable) {
                Log.e("MQTT", "Connexion perdue : ${cause.message}")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                val payload = String(message.payload)
                Log.d("MQTT", "Nouveau message reçu sur le sujet $topic : $payload")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                Log.d("MQTT", "Message délivré : ${token.message}")
            }
        })

        connectToMqtt()
    }

    private fun connectToMqtt() {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isCleanSession = true

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // Cette méthode ne sera pas appelée directement. La connexion complète est gérée par MqttCallbackExtended.
                    Log.d("MQTT", "Connexion réussie : $asyncActionToken")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.e("MQTT", "Échec de la connexion : ${asyncActionToken.exception}")
                    exception.printStackTrace()
                }
            })
        } catch (e: Exception) {
            Log.e("MQTT", "Exception lors de la tentative de connexion : ${e.message}", e)
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

    override fun onDestroy() {
        super.onDestroy()
        // Assurez-vous d'annuler le CountDownTimer lors de la destruction de l'activité
        countDownTimer?.cancel()
    }

    private fun subscribeToTopic(topic: String) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.d("MQTT", "Abonnement réussi au sujet : $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.e("MQTT", "Échec de l'abonnement au sujet : $topic, Exception : ${exception.message}")
                }
            })
        } catch (e: MqttException) {
            Log.e("MQTT", "Exception lors de l'abonnement au sujet : $topic, Exception : ${e.message}")
        }
    }

}