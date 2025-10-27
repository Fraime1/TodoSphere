package com.sofutil.todosw.eriger.data.utils

import android.content.Context
import android.os.Looper
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.sofutil.todosw.eriger.presentation.app.TodoSphereApp
import com.sofutil.todosw.eriger.presentation.app.TodoSphereAppsFlyerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


private const val TODO_SPHERE_APP_DEV = "ywRwLEQZoLB2waXZoWJWqM"
private const val TODO_SPHERE_LIN = "com.sofutil.todosw"
class TodoSphereAppsflyer(private val context: Context) {


    suspend fun init(): TodoSphereAppsFlyerState = withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            val appsflyer = AppsFlyerLib.getInstance()
            todoSphereSetDebufLogger(appsflyer)
            todoSphereMinTimeBetween(appsflyer)

            var isResumed = false
            fun safeResume(state: TodoSphereAppsFlyerState) {
                if (!isResumed) {
                    isResumed = true
                    cont.resume(state)
                }
            }

            appsflyer.init(
                TODO_SPHERE_APP_DEV,
                object : AppsFlyerConversionListener {
                    override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                        Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "onConversionDataSuccess: $p0")

                        val afStatus = p0?.get("af_status")?.toString() ?: "null"
                        if (afStatus == "Organic") {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    delay(5000)
                                    val api = todoSphereGetApi(
                                        "https://gcdsdk.appsflyer.com/install_data/v4.0/",
                                        null
                                    )
                                    val response = api.todoSphereGetClient(
                                        devkey = TODO_SPHERE_APP_DEV,
                                        deviceId = todoSphereGetAppsflyerId()
                                    ).awaitResponse()

                                    val resp = response.body()
                                    Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "After 5s: $resp")
                                    if (resp?.get("af_status") == "Organic") {
                                        safeResume(TodoSphereAppsFlyerState.TodoSphereError)
                                    } else {
                                        safeResume(
                                            TodoSphereAppsFlyerState.TodoSphereSuccess(resp)
                                        )
                                    }
                                } catch (d: Exception) {
                                    Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "Error: ${d.message}")
                                    safeResume(TodoSphereAppsFlyerState.TodoSphereError)
                                }
                            }
                        } else {
                            safeResume(TodoSphereAppsFlyerState.TodoSphereSuccess(p0))
                        }
                    }

                    override fun onConversionDataFail(p0: String?) {
                        Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "onConversionDataFail: $p0")
                        safeResume(TodoSphereAppsFlyerState.TodoSphereError)
                    }

                    override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                        Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "onAppOpenAttribution")
//                        safeResume(TodoSphereAppsFlyerState.TodoSphereError)
                    }

                    override fun onAttributionFailure(p0: String?) {
                        Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "onAttributionFailure: $p0")
//                        safeResume(TodoSphereAppsFlyerState.TodoSphereError)
                    }
                },
                context.applicationContext
            )

            appsflyer.start(context, TODO_SPHERE_APP_DEV, object : AppsFlyerRequestListener {
                override fun onSuccess() {
                    Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "AppsFlyer started")
                }

                override fun onError(p0: Int, p1: String) {
                    Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "AppsFlyer start error: $p0 - $p1")
                    safeResume(TodoSphereAppsFlyerState.TodoSphereError)
                }
            })
        }
    }

    private fun todoSphereGetAppsflyerId(): String {
        val appsflyrid = AppsFlyerLib.getInstance().getAppsFlyerUID(context) ?: ""
        Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "AppsFlyer: AppsFlyer Id = $appsflyrid")
        return appsflyrid
    }

    private fun todoSphereSetDebufLogger(appsflyer: AppsFlyerLib) {
        appsflyer.setDebugLog(true)
    }

    private fun todoSphereMinTimeBetween(appsflyer: AppsFlyerLib) {
        appsflyer.setMinTimeBetweenSessions(0)
    }

    private fun todoSphereGetApi(url: String, client: OkHttpClient?) : TodoSphereAppsApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }

}


interface TodoSphereAppsApi {
    @Headers("Content-Type: application/json")
    @GET(TODO_SPHERE_LIN)
    fun todoSphereGetClient(
        @Query("devkey") devkey: String,
        @Query("device_id") deviceId: String,
    ): Call<MutableMap<String, Any>?>
}