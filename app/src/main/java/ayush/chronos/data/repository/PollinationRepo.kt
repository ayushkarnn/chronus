package ayush.chronos.data.repository

import com.app.chronus.network.PollinationsApi
import javax.inject.Inject

class PollinationRepo @Inject constructor(
    private val api: PollinationsApi
) {
    suspend fun getData(prompt: String): String? {
        return api.getWish(prompt)
    }
}
