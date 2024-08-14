package eywa.projectcodex.api

import eywa.projectcodex.common.utils.DoNotObfuscate
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import javax.inject.Inject

object ScoresRf {
    interface Api {
        @GET("/scores")
        suspend fun getScores(): List<Score>
    }

    class Service @Inject constructor(private val api: Api) {
        suspend fun getScores() = CodexRetrofit.handleCall { api.getScores() }
    }

    @DoNotObfuscate
    @Serializable
    data class Score(
            val name: String,
            val hits: Int,
            val score: Int,
            val golds: Int,
            val arrowsShot: Int,
    )
}
