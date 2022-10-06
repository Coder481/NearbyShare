package com.hrithik.nearbyshare.helpers

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.google.android.gms.common.util.IOUtils.copyStream
import com.hrithik.nearbyshare.MyApp
import java.io.*


object FilesFetcher {

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

    fun saveVideoFile(byteArray: ByteArray?){
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

    fun saveFileFromUri(uri: Uri?, context: Context) {
        uri?.let{
//            val sourceFilename: String = it.path ?: return@let
//            Log.e("Saving File From Uri",sourceFilename)
//            var bis: BufferedInputStream? = null
//            var bos: BufferedOutputStream? = null
            val myDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "ReceivedFile"
            )
            myDir.mkdirs()
            try {
                val fName = "${System.currentTimeMillis()}.mp4"
                val fos = FileOutputStream(File(myDir, fName))
                copyStream(context.contentResolver.openInputStream(it)!!,fos)
//                bis = context.contentResolver.openInputStream(it)
//                bis = BufferedInputStream(FileInputStream(it))
//                bos = BufferedOutputStream(fos)
//                val buf = ByteArray(1024)
//                bis.read(buf)
//                do { bos.write(buf) } while (bis.read(buf) != -1)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("FilesFetcher","->>$e")
            } finally {
                /*try {
                    bis?.close()
                    bos?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e("FilesFetcher2","->>$e")
                }*/
            }
        }

    }
}