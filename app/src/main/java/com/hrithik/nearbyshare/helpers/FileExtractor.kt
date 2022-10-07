package com.hrithik.nearbyshare.helpers

import java.io.File
import kotlin.collections.ArrayList

/**
 * Helper class to sort files by last modified date and return Nth recent file
 */
object FileExtractor {

    fun getNthVideoFile(n:Int):File?{
        val list = getSortedVideoFiles()
        if(n<=0 || n>list.size) return null
        return list[n-1]
    }

    private fun getSortedVideoFiles() :  MutableList<File>{
        val filesList = FilesFetcher.fetchVideoFiles()
        return sortByDateFromLastToFirst(filesList)
    }

    // Sorting the video files' list by their lastModified date
    private fun sortByDateFromLastToFirst(list : ArrayList<String>): MutableList<File>{
        val fileList: MutableList<File> = ArrayList()
        list.forEach { fileList.add(File(it)) }
        fileList.sortBy { it.lastModified() }
        return fileList.reversed().toMutableList()
    }
}