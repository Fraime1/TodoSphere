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
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


private const val TODO_SPHERE_APP_DEV = "ywRwLEQZoLB2waXZoWJWqM"
private const val TODO_SPHERE_LIN = "com.sofutil.todosw"
class TodoSphereAppsflyer(private val context: Context) {


    fun init(
        todoSphereCallback: (TodoSphereAppsFlyerState) -> Unit
    ) {
        val appsflyer = AppsFlyerLib.getInstance()
        todoSphereSetDebufLogger(appsflyer)
        todoSphereMinTimeBetween(appsflyer)
        appsflyer.init(
            TODO_SPHERE_APP_DEV,
            object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                    Looper.prepare()
                    Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "AppsFlyer: onConversionDataSuccess")
                    Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "AppsFlyer: $p0")
                    Log.d(
                        TodoSphereApp.TODO_SPHERE_MAIN_TAG,
                        "AppsFlyer: af_status: ${p0?.get("af_status")}"
                    )
//                    todoSphereCallback(BubblePasswordAppsFlyerState.BubblePasswordSuccess(p0))
                    if (p0?.get("af_status") == "Organic") {
                        val corouteScope = CoroutineScope(Dispatchers.IO)
                        corouteScope.launch {
                            try {
                                delay(5000)
                                val api = todoSphereGetApi("https://gcdsdk.appsflyer.com/install_data/v4.0/", null)
                                val request = api.todoSphereGetClient(
                                    devkey = TODO_SPHERE_APP_DEV,
                                    deviceId = todoSphereGetAppsflyerId()
                                )
                                val response = request.awaitResponse()
                                Log.d(
                                    TodoSphereApp.TODO_SPHERE_MAIN_TAG,
                                    "AppsFlyer: Conversion after 5 seconds: ${response.body()}"
                                )
                                if (response.body()?.get("af_status") == "Organic") {
                                    todoSphereCallback(TodoSphereAppsFlyerState.TodoSphereError)
                                } else {
                                    todoSphereCallback(TodoSphereAppsFlyerState.TodoSphereSuccess(response.body()))
                                }
                            } catch (e: Exception) {
                                Log.d(
                                    TodoSphereApp.TODO_SPHERE_MAIN_TAG,
                                    "AppsFlyer: ${e.message}"
                                )
                                todoSphereCallback(TodoSphereAppsFlyerState.TodoSphereError)
                            }
                        }
                    } else {
                        todoSphereCallback(TodoSphereAppsFlyerState.TodoSphereSuccess(p0))
                    }
                }

                override fun onConversionDataFail(p0: String?) {
                    Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "AppsFlyer: onConversionDataFail: $p0")
                    todoSphereCallback(TodoSphereAppsFlyerState.TodoSphereError)
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                    Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "AppsFlyer: onAppOpenAttribution")
                    todoSphereCallback(TodoSphereAppsFlyerState.TodoSphereError)
                }

                override fun onAttributionFailure(p0: String?) {
                    Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "AppsFlyer: onAttributionFailure: $p0")
                    todoSphereCallback(TodoSphereAppsFlyerState.TodoSphereError)
                }
            },
            context.applicationContext
        )
        appsflyer.start(context, TODO_SPHERE_APP_DEV, object : AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "AppsFlyer: Start is Success")
            }

            override fun onError(p0: Int, p1: String) {
                Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "AppsFlyer: Start is Error")
                Log.d(TodoSphereApp.TODO_SPHERE_MAIN_TAG, "AppsFlyer: Error code: $p0, error message: $p1")
                todoSphereCallback(TodoSphereAppsFlyerState.TodoSphereError)
            }

        })
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