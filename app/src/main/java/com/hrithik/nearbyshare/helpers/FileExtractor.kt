package com.hrithik.nearbyshare.helpers

import android.content.Context
import java.io.File
import kotlin.collections.ArrayList

object FileExtractor {

    suspend fun getNthVideoFile(n:Int):File?{
        val list = getSortedVideoFiles()
        if(n<=0 || n>list.size) return null
        return list[n-1]
    }

    private suspend fun getSortedVideoFiles() :  MutableList<File>{
        val filesList = FilesFetcher.fetchVideoFiles()
        return sortByDateFromLastToFirst(filesList)
    }

    private fun sortByDateFromLastToFirst(list : ArrayList<String>): MutableList<File>{
        val fileList: MutableList<File> = ArrayList()
        list.forEach { fileList.add(File(it)) }
        fileList.sortBy { it.lastModified() }
        return fileList.reversed().toMutableList()
    }
}