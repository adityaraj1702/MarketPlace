package com.adityastudios.buyerseller

import android.content.Context
import android.widget.Toast
import android.text.format.DateFormat
import java.util.Calendar
import java.util.Locale

//a class that will contain static functions, variables required in whole project
object Utils {
    fun toast(context: Context, message: String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

    fun getTimeStamp() : Long{
        return System.currentTimeMillis()
    }

    fun formatTimeStampDate(timestamp: Long):String{
        val calender = Calendar.getInstance(Locale.ENGLISH)
        calender.timeInMillis = timestamp
        return DateFormat.format("DD/MM/YYYY",calender).toString()
    }
}