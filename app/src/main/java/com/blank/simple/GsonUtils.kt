package com.blank.simple

import com.google.gson.Gson
import java.io.InputStreamReader

fun <T> GsonToBean(reader: InputStreamReader, clas: Class<T>): T {
    val gson = Gson()
    return gson.fromJson(reader, clas)
}
