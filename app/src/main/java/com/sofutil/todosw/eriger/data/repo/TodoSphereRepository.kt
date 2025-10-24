package com.sofutil.todosw.eriger.data.repo

import android.util.Log
import com.sofutil.todosw.eriger.domain.model.TodoSphereEntity
import com.sofutil.todosw.eriger.domain.model.TodoSphereParam
import com.sofutil.todosw.eriger.presentation.app.TodoSphereApp.Companion.TODO_SPHERE_MAIN_TAG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TodoSphereApi {
    @Headers("Content-Type: application/json")
    @POST("config.php")
    fun getClient(
        @Body jsonString: JsonObject,
    ): Call<TodoSphereEntity>
}


private const val TODO_SPHERE_MAIN = "https://todosphhere.com/"
class TodoSphereRepository {

    suspend fun todoSphereGetClient(
        todoSphereParam: TodoSphereParam,
        todoSphereConversion: MutableMap<String, Any>?
    ): TodoSphereEntity? {
        val gson = Gson()
        val api = todoSphereGetApi(TODO_SPHERE_MAIN, null)

        val todoSphereJsonObject = gson.toJsonTree(todoSphereParam).asJsonObject
        todoSphereConversion?.forEach { (key, value) ->
            val element: JsonElement = gson.toJsonTree(value)
            todoSphereJsonObject.add(key, element)
        }
        return try {
            val todoSphereRequest: Call<TodoSphereEntity> = api.getClient(
                jsonString = todoSphereJsonObject,
            )
            val todoSphereResult = todoSphereRequest.awaitResponse()
            Log.d(TODO_SPHERE_MAIN_TAG, "Retrofit: Result code: ${todoSphereResult.code()}")
            if (todoSphereResult.code() == 200) {
                Log.d(TODO_SPHERE_MAIN_TAG, "Retrofit: Get request success")
                Log.d(TODO_SPHERE_MAIN_TAG, "Retrofit: Code = ${todoSphereResult.code()}")
                Log.d(TODO_SPHERE_MAIN_TAG, "Retrofit: ${todoSphereResult.body()}")
                todoSphereResult.body()
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            Log.d(TODO_SPHERE_MAIN_TAG, "Retrofit: Get request failed")
            Log.d(TODO_SPHERE_MAIN_TAG, "Retrofit: ${e.message}")
            null
        }
    }


    private fun todoSphereGetApi(url: String, client: OkHttpClient?) : TodoSphereApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }


}
