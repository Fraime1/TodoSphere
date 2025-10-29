package com.sofutil.todosw.eriger.presentation.app

import android.app.Application
import android.util.Log
import android.view.WindowManager
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.sofutil.todosw.eriger.presentation.di.todoSphereModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


sealed interface TodoSphereAppsFlyerState {
    data object TodoSphereDefault : TodoSphereAppsFlyerState
    data class TodoSphereSuccess(val todoSphereData: MutableMap<String, Any>?) :
        TodoSphereAppsFlyerState
    data object TodoSphereError : TodoSphereAppsFlyerState
}

interface TodoSphereAppsApi {
    @Headers("Content-Type: application/json")
    @GET(TODO_SPHERE_LIN)
    fun todoSphereGetClient(
        @Query("devkey") devkey: String,
        @Query("device_id") deviceId: String,
    ): Call<MutableMap<String, Any>?>
}
private const val TODO_SPHERE_APP_DEV = "ywRwLEQZoLB2waXZoWJWqM"
private const val TODO_SPHERE_LIN = "com.sofutil.todosw"
class TodoSphereApp : Application() {
    private var todoSphereIsResumed = false

    override fun onCreate() {
        super.onCreate()

        val appsflyer = AppsFlyerLib.getInstance()
        todoSphereSetDebufLogger(appsflyer)
        todoSphereMinTimeBetween(appsflyer)


        appsflyer.init(
            TODO_SPHERE_APP_DEV,
            object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                    Log.d(TODO_SPHERE_MAIN_TAG, "onConversionDataSuccess: $p0")

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
                                Log.d(TODO_SPHERE_MAIN_TAG, "After 5s: $resp")
                                if (resp?.get("af_status") == "Organic") {
                                    todoSafeResume(TodoSphereAppsFlyerState.TodoSphereError)
                                } else {
                                    todoSafeResume(
                                        TodoSphereAppsFlyerState.TodoSphereSuccess(resp)
                                    )
                                }
                            } catch (d: Exception) {
                                Log.d(TODO_SPHERE_MAIN_TAG, "Error: ${d.message}")
                                todoSafeResume(TodoSphereAppsFlyerState.TodoSphereError)
                            }
                        }
                    } else {
                        todoSafeResume(TodoSphereAppsFlyerState.TodoSphereSuccess(p0))
                    }
                }

                override fun onConversionDataFail(p0: String?) {
                    Log.d(TODO_SPHERE_MAIN_TAG, "onConversionDataFail: $p0")
                    todoSafeResume(TodoSphereAppsFlyerState.TodoSphereError)
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                    Log.d(TODO_SPHERE_MAIN_TAG, "onAppOpenAttribution")
                }

                override fun onAttributionFailure(p0: String?) {
                    Log.d(TODO_SPHERE_MAIN_TAG, "onAttributionFailure: $p0")
                }
            },
            this
        )

        appsflyer.start(this, TODO_SPHERE_APP_DEV, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d(TODO_SPHERE_MAIN_TAG, "AppsFlyer started")
            }

            override fun onError(p0: Int, p1: String) {
                Log.d(TODO_SPHERE_MAIN_TAG, "AppsFlyer start error: $p0 - $p1")
                todoSafeResume(TodoSphereAppsFlyerState.TodoSphereError)
            }
        })
        
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@TodoSphereApp)
            modules(
                listOf(
                    todoSphereModule
                )
            )
        }
    }

    private fun todoSafeResume(state: TodoSphereAppsFlyerState) {
        if (!todoSphereIsResumed) {
            todoSphereIsResumed = true
            todoSphereConversionFlow.value = state
        }
    }

    private fun todoSphereGetAppsflyerId(): String {
        val appsflyrid = AppsFlyerLib.getInstance().getAppsFlyerUID(this) ?: ""
        Log.d(TODO_SPHERE_MAIN_TAG, "AppsFlyer: AppsFlyer Id = $appsflyrid")
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

    companion object {
        var todoSphereInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        val todoSphereConversionFlow: MutableStateFlow<TodoSphereAppsFlyerState> = MutableStateFlow(
            TodoSphereAppsFlyerState.TodoSphereDefault
        )
        var TODO_SPHERE_FB_LI: String? = null
        const val TODO_SPHERE_MAIN_TAG = "TodoSphereMainTag"
    }
}