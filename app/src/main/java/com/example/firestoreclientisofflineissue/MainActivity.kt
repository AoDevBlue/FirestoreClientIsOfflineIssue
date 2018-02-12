package com.example.firestoreclientisofflineissue

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * Created by matthieuesnault on 12/02/2018.
 */
class MainActivity: AppCompatActivity() {

    companion object {
        private const val TEST_TAG = "FirestoreTest"
    }

    private lateinit var firestore: FirebaseFirestore
    private var waitingForResponse = false
    private val disposables = CompositeDisposable()
    private var currentToast: Toast? = null

    private val connectivityManager: ConnectivityManager by lazy(LazyThreadSafetyMode.NONE) {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFirestoreInstance()

        // Use a button to trigger the call
        val callButton = findViewById<Button>(R.id.firestore_call_button)
        callButton.setOnClickListener {
            readValue()
        }
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    private fun initFirestoreInstance() {
        // This is done using dependency injection in our app, but let's simplify
        val firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()

        firestore = FirebaseFirestore.getInstance().apply {
            this.firestoreSettings = firestoreSettings
        }

        FirebaseFirestore.setLoggingEnabled(true)
    }

    private fun networkConnectivityIsEnabled(): Boolean {
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    private fun readValue() {
        if (!networkConnectivityIsEnabled()) {
            logAndToast("No connectivity")
            return
        }

        if (waitingForResponse) {
            logAndToast("Already waiting for a response")
            return
        }

        waitingForResponse = true
        logAndToast("Starting a new call")

        // The call is wrapped in a Rx Single to match the way we query Firestore in our app, including the schedulers
        readValueSingle()
            .doFinally { waitingForResponse = false } // This is not elegant, but this is a quick test
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { logAndToast("Read the test value: $it") },
                onError = { logAndToast("Error while reading the value: ${it.message}") }
            )
            .addTo(disposables)
    }

    private fun readValueSingle(): Single<String> {
        return Single.create<String> { subscriber ->
            val document = firestore
                .collection("test_collection")
                .document("test_document")

            document.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val value = task.result["test_key"] as? String
                    if (value != null) {
                        subscriber.onSuccess(value)
                    } else {
                        subscriber.onError(IllegalArgumentException("The test_key value is invalid"))
                    }
                } else {
                    subscriber.onError(task.exception ?: IllegalStateException("Failed with no exception"))
                }
            }
        }
    }

    private fun logAndToast(text: String) {
        currentToast?.cancel()
        currentToast = Toast.makeText(this, text, Toast.LENGTH_SHORT).apply { show() }
        Log.d(TEST_TAG, text)
    }
}