package com.example.spyproject




import android.os.Bundle
import android.os.CountDownTimer
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


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var mqttAndroidClient: MqttAndroidClient
    private val serverUri = "tcp://broker.example.com:1883"
    private var countDownTimer: CountDownTimer? = null
    private val initialTimeInMillis: Long = 60000 // Temps initial en millisecondes (60 secondes ici)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chrono.format = "%s"
        startCountdown()

     /*   val clientId = MqttClient.generateClientId()
        mqttAndroidClient = MqttAndroidClient(applicationContext, serverUri, clientId)

        // Configuration de la connexion MQTT
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isCleanSession = true



        // Connexion au broker MQTT
        mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                // Connexion réussie
                subscribeToTopic("topic/example")
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                // Échec de la connexion
            }
        })



        // Callback pour recevoir les messages
        mqttAndroidClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                // Connexion perdue
            }


            override fun messageArrived(topic: String, message: MqttMessage) {
                // Nouveau message reçu
                val payload = String(message.payload)
                // Faites quelque chose avec le message reçu
                // Traitez le message reçu depuis le serveur Node.js avec XBee API
                handleXBeeMessage(payload)
            }

            private fun handleXBeeMessage(xbeeMessage: String) {
                // Traitez le message XBee ici
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                // Message délivré
            }
        })*/
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

  /*  private fun subscribeToTopic(topic: String) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // Abonnement réussi
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // Échec de l'abonnement
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    // Méthode pour publier un message
    private fun publishMessage(topic: String, payload: String) {
        try {
            val message = MqttMessage()
            message.payload = payload.toByteArray()
            mqttAndroidClient.publish(topic, message)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    // Fonction pour envoyer un message au serveur Node.js via MQTT
    private fun sendMessageToServer(message: String) {
        val serverTopic = "topic/to/server"  // Remplacez par le sujet que votre serveur Node.js écoute
        publishMessage(serverTopic, message)
    }*/
}