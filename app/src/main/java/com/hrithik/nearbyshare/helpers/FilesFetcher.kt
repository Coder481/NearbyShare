package com.hrithik.nearbyshare.helpers

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore

object FilesFetcher {

    fun fetchVideoFiles(context : Context) : ArrayList<String>{
        val videoItemHashSet: HashSet<String> = HashSet()
        val projection = arrayOf(
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.Media.DISPLAY_NAME
        )
        val cursor: Cursor? = context.contentResolver
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
                    videoItemHashSet.add(it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)))
                } while (it.moveToNext())
                it.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return ArrayList(videoItemHashSet)
    }
}