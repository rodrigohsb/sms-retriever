package br.com.rodrigohsb.smsretriever

import android.app.Application
import android.util.Log

/**
 * @rodrigohsb
 */
class AppApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        Log.i("AppApplication","appSignatures = ${AppSignatureHelper(this).appSignatures}")

    }
}