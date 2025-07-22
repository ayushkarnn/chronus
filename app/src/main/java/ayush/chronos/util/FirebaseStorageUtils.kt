package ayush.chronos.util

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

suspend fun uploadImageToFirebaseStorage(
    imageUri: Uri,
    userId: String
): String {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("reminders/$userId/${UUID.randomUUID()}.png")
    val uploadTask = imageRef.putFile(imageUri).await()
    return imageRef.downloadUrl.await().toString()
}
