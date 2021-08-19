package com.slamtec.robot.deliver.utils

import android.text.TextUtils
import android.util.Log

object LogMgr {
    private var LEVEL = 0
    private const val ERROR = 5
    private const val WARN = 4
    private const val INFO = 3
    private const val DEBUG = 2
    private const val VERBOSE = 1

    fun v(message: String) {
        if (LEVEL <= VERBOSE) {
            val stackTraceElement =
                Thread.currentThread().stackTrace[3]
            val tag = getDefaultTag(stackTraceElement)
            Log.v(tag, getLogInfo(stackTraceElement) + message)
        }
    }

    fun v(tag: String, message: String) {
        var tag = tag
        if (LEVEL <= VERBOSE) {
            val stackTraceElement =
                Thread.currentThread().stackTrace[3]
            tag = "slamtec-$tag"
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement)
            }
            Log.v(tag, getLogInfo(stackTraceElement) + message)
        }
    }

    fun d(message: String) {
        if (LEVEL <= DEBUG) {
            val stackTraceElement =
                Thread.currentThread().stackTrace[3]
            val tag = getDefaultTag(stackTraceElement)
            Log.d(tag, getLogInfo(stackTraceElement) + message)
        }
    }

    fun d(tag: String, message: String) {
        var tag = tag
        if (LEVEL <= DEBUG) {
            val stackTraceElement =
                Thread.currentThread().stackTrace[3]
            tag = "slamtec-$tag"
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement)
            }
            Log.d(tag, getLogInfo(stackTraceElement) + message)
        }
    }

    fun i(message: String) {
        if (LEVEL <= INFO) {
            val stackTraceElement =
                Thread.currentThread().stackTrace[3]
            val tag = getDefaultTag(stackTraceElement)
            Log.i(tag, getLogInfo(stackTraceElement) + message)
        }
    }

    fun i(tag: String, message: String) {
        var tag = tag
        if (LEVEL <= INFO) {
            val stackTraceElement =
                Thread.currentThread().stackTrace[3]
            tag = "slamtec-$tag"
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement)
            }
            Log.i(tag, getLogInfo(stackTraceElement) + message)
        }
    }

    fun w(message: String) {
        if (LEVEL <= WARN) {
            val stackTraceElement =
                Thread.currentThread().stackTrace[3]
            val tag = getDefaultTag(stackTraceElement)
            Log.w(tag, getLogInfo(stackTraceElement) + message)
        }
    }

    fun w(tag: String, message: String) {
        var tag = tag
        if (LEVEL <= WARN) {
            val stackTraceElement =
                Thread.currentThread().stackTrace[3]
            tag = "slamtec-$tag"
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement)
            }
            Log.w(tag, getLogInfo(stackTraceElement) + message)
        }
    }

    fun e(message: String) {
        if (LEVEL <= ERROR) {
            val stackTraceElement =
                Thread.currentThread().stackTrace[3]
            val tag = getDefaultTag(stackTraceElement)
            Log.e(tag, getLogInfo(stackTraceElement) + message)
        }
    }

    fun e(tag: String?, message: String) {
        var tag = tag
        if (LEVEL <= ERROR) {
            val stackTraceElement =
                Thread.currentThread().stackTrace[3]
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement)
            }
            Log.e(tag, getLogInfo(stackTraceElement) + message)
        }
    }

    /**
     * Get default tag name
     */
    private fun getDefaultTag(stackTraceElement: StackTraceElement): String {
        val fileName = stackTraceElement.fileName
        val stringArray =
            fileName.split("\\.".toRegex()).toTypedArray()
        val tag = stringArray[0]
        return "slamtec-$tag"
    }

    /**
     * get stack info
     */
    private fun getLogInfo(stackTraceElement: StackTraceElement): String {
        var logInfoStringBuilder: StringBuilder? = StringBuilder()
        val lineNumber = stackTraceElement.lineNumber
        logInfoStringBuilder!!.append("[ ")
        logInfoStringBuilder.append("lineNumber =$lineNumber")
        logInfoStringBuilder.append(" ] ")
        return logInfoStringBuilder.toString()
    }

}