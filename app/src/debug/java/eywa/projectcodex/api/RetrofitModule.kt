package eywa.projectcodex.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import java.io.IOException
import javax.inject.Singleton

// cd C:\Users\Ewa\Downloads\codex-api
// gradlew run

// T:\Programs\Android\Sdk\platform-tools\adb reverse tcp:8080 tcp:8080

object CodexRetrofit {
    internal const val BASE_URL = "http://localhost:8080/"

    suspend fun <Data> handleCall(apiCall: suspend () -> Data) =
            try {
                DataState.Success(apiCall())
            }
            catch (e: IOException) {
                DataState.Error(e)
            }
}

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {
    @Singleton
    @Provides
    fun providesRetrofit() = Retrofit
            .Builder()
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(CodexRetrofit.BASE_URL)
            .build()

    @Singleton
    @Provides
    fun providesScoresApi(retrofit: Retrofit) = retrofit.create(ScoresRf.Api::class.java)
}
