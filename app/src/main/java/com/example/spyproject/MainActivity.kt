package com.example.spyproject

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.spyproject.databinding.ActivityMainBinding
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import java.util.UUID

// MqttManager
object MqttManager {

    private const val brokerUrl = "tcp://test.mosquitto.org:1883"
    private const val clientId = "votre_client_id"
    private const val topicToSubscribe = "votre_topic" // Changez cela avec le topic auquel vous souhaitez vous abonner

    private lateinit var internalMqttClient: MqttClient

    // Fonction de connexion
    fun connect(context: Context) {
        try {
            // Vérifier si le client MQTT est déjà connecté
            if (this::internalMqttClient.isInitialized && internalMqttClient.isConnected) {
                Log.d("MqttManager", "Le client MQTT est déjà connecté")
                return
            }

            // Générer un identifiant client unique
            val uniqueClientId = UUID.randomUUID().toString()
            internalMqttClient = MqttClient(brokerUrl, uniqueClientId, MqttDefaultFilePersistence(context.filesDir.absolutePath))

            // Connexion au broker
            val options = MqttConnectOptions()
            internalMqttClient.connect(options)

            Log.d("MqttManager", "Connexion MQTT réussie")

            // Appeler la fonction pour s'abonner à un topic
            subscribeToTopic(topicToSubscribe)

        } catch (e: MqttException) {
            Log.e("MqttManager", "Erreur lors de la connexion MQTT: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e("MqttManager", "Une erreur inattendue s'est produite lors de la connexion MQTT: ${e.message}")
            e.printStackTrace()
        }
    }




    // Fonction pour s'abonner à un topic
    private fun subscribeToTopic(topic: String) {
        try {
            internalMqttClient.subscribe(topic, 1)
            Log.d("MqttManager", "Abonné au topic: $topic")
        } catch (e: MqttException) {
            Log.e("MqttManager", "Erreur lors de l'abonnement au topic: $topic - ${e.message}")
            e.printStackTrace()
        }
    }

    fun disconnect() {
        internalMqttClient.disconnect()
    }

    fun publish(topic: String, message: String) {
        if (this::internalMqttClient.isInitialized && internalMqttClient.isConnected) {
            internalMqttClient.publish(topic, MqttMessage(message.toByteArray()))
        } else {
            // Gérer le cas où mqttClient est null ou n'est pas connecté
            Log.e("MqttManager", "MQTT Client is null or not connected.")
        }
    }

    fun subscribe(topic: String) {
        if (this::internalMqttClient.isInitialized && internalMqttClient.isConnected) {
            internalMqttClient.subscribe(topic)
        } else {
            // Gérer le cas où mqttClient est null ou n'est pas connecté
            Log.e("MqttManager", "MQTT Client is null or not connected.")
        }
    }

    fun setCallback(callback: MqttCallback) {
        if (this::internalMqttClient.isInitialized) {
            internalMqttClient.setCallback(callback)
        } else {
            // Gérer le cas où mqttClient est null
            Log.e("MqttManager", "MQTT Client is null.")
        }
    }
}

class MyMqttCallback : MqttCallback {
    override fun connectionLost(cause: Throwable?) {
        // Gérer la perte de connexion
        Log.e("MQTT", "Connexion perdue avec le broker MQTT")
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        // Gérer les messages reçus
        val payload = message?.payload?.toString(Charsets.UTF_8)
        Log.d("MQTT", "Message reçu sur le topic $topic : $payload")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        // Gérer la livraison complète du message
        Log.d("MQTT", "Livraison complète du message")
    }
}

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var countDownTimer: CountDownTimer? = null
    private val initialTimeInMillis: Long = 60000 // Temps initial en millisecondes (60 secondes ici)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chrono.format = "%s"
        startCountdown()

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            try {
                Log.d("MainActivity", "Avant la connexion MQTT")
                MqttManager.connect(this)
                Log.d("MainActivity", "Après la connexion MQTT")
            } catch (e: MqttException) {
                e.printStackTrace()
                // Gérer l'erreur de connexion MQTT ici
            }
        } else {
            // Gérer l'absence de connexion Internet
        }

        // Publier un message
        MqttManager.publish("topic", "Hello MQTT!")

        // Souscrire à un topic
        MqttManager.subscribe("topic")
    }

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(initialTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                binding.chrono.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.chrono.text = "Terminé!"
            }
        }

        countDownTimer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        MqttManager.disconnect()
    }
}
