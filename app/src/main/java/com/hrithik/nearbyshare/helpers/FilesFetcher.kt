package com.hrithik.nearbyshare.helpers

import android.database.Cursor
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.google.android.gms.nearby.connection.Payload
import com.hrithik.nearbyshare.MyApp
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 *  Helper class to get and save files from and in storage
 */
object FilesFetcher {

    // Return list of all videos available
    fun fetchVideoFiles() : ArrayList<String>{
        val videoItemHashSet: HashSet<String> = HashSet()
        val projection = arrayOf(
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.Media.DISPLAY_NAME
        )
        val cursor: Cursor? = MyApp.getContext().contentResolver
            .query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )
        cursor?.let{
            try {
                it.moveToFirst()
                do {
                    val file = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    videoItemHashSet.add(file)
                    Log.d("Files Fetcher: ", file)
                } while (it.moveToNext())
                it.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return ArrayList(videoItemHashSet)
    }

    fun saveVideoFileFromBytes(byteArray: ByteArray?){
        byteArray?.let{
            val myDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "ReceivedFile"
            )

            myDir.mkdirs()

            try {
                val fName = "${System.currentTimeMillis()}.mp4"
                val fos = FileOutputStream(File(myDir, fName))
                fos.write(it)
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun saveFileFromUri(payload : Payload) {
        val myDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "ReceivedFile"
        )
        myDir.mkdirs()
        try {
            val fName = "${System.currentTimeMillis()}.mp4"
            val fos = FileOutputStream(File(myDir, fName))
            val fis = FileInputStream(payload.asFile()?.asParcelFileDescriptor()?.fileDescriptor)
            fis.copyTo(fos)
            Log.e("FilesFetcher","File saved successfully")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("FilesFetcher","->>$e")
        }

    }
}