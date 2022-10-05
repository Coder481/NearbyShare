package com.hrithik.nearbyshare.helpers

import android.content.Context
import java.io.File
import kotlin.collections.ArrayList

object SortFiles {

    fun getSortedVideoFiles(context : Context) :  MutableList<String>{
        val filesList = FilesFetcher.fetchVideoFiles(context)
        return sortByDateFromLastToFirst(filesList)
    }

    fun sortByDateFromLastToFirst(list : ArrayList<String>): MutableList<String>{
        val fileList: MutableList<File> = ArrayList()
        list.forEach { fileList.add(File(it)) }
        fileList.sortBy { it.lastModified() }

        val res = mutableListOf<String>()
        fileList.forEach { res.add(it.path) }
        return res.reversed().toMutableList()
    }
}