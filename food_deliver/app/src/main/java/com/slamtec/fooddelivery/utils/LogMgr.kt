package com.slamtec.fooddelivery.utils

import android.annotation.SuppressLint
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object LogMgr {
    private const val TAG = "LogMgr"
    private var LEVEL = 0
    private const val ERROR = 5
    private const val WARN = 4
    private const val INFO = 3
    private const val DEBUG = 2
    private const val VERBOSE = 1

    private const val SDCARD_LOG_FILE_SAVE_DAYS = 7
    private var LOG_PATH_SDCARD_DIR = "${Environment.getExternalStorageDirectory()}/foodDeliver/log"

    @SuppressLint("SimpleDateFormat")
    private val myLogSdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // 日志的输出格式

    @SuppressLint("SimpleDateFormat")
    private val logfile: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd") // 日志文件格式
    private val MAX_FILE_LENGTH = 1 * 1024 * 1024
    private val FILE_NAME = "Log"
    private val MAX_SAVE_SIZE = 200 * 1024 * 1024
    private val MAX_SD_SIZE = 500 * 1024 * 1024

    fun v(message: String) {
        if (LEVEL <= VERBOSE) {
            val stackTraceElement =
                    Thread.currentThread().stackTrace[3]
            val tag = getDefaultTag(stackTraceElement)
            Log.v(tag, getLogInfo(stackTraceElement) + message)
        }
    }

    fun v(t: String, message: String) {
        var tag = t
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

    fun d(t: String, message: String) {
        var tag = t
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
            saveLogFile(tag + "  " + getLogInfo(stackTraceElement) + message)
        }
    }

    fun i(t: String, message: String) {
        var tag = t
        if (LEVEL <= INFO) {
            val stackTraceElement =
                    Thread.currentThread().stackTrace[3]
            tag = "slamtec-$tag"
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement)
            }
            Log.i(tag, getLogInfo(stackTraceElement) + message)
            saveLogFile(tag + "  " + getLogInfo(stackTraceElement) + message)
        }
    }

    fun w(message: String) {
        if (LEVEL <= WARN) {
            val stackTraceElement =
                    Thread.currentThread().stackTrace[3]
            val tag = getDefaultTag(stackTraceElement)
            Log.w(tag, getLogInfo(stackTraceElement) + message)
            saveLogFile(tag + "  " + getLogInfo(stackTraceElement) + message)
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
            saveLogFile(tag + "  " + getLogInfo(stackTraceElement) + message)
        }
    }

    fun e(message: String) {
        if (LEVEL <= ERROR) {
            val stackTraceElement =
                    Thread.currentThread().stackTrace[3]
            val tag = getDefaultTag(stackTraceElement)
            Log.e(tag, getLogInfo(stackTraceElement) + message)
            saveLogFile(tag + "  " + getLogInfo(stackTraceElement) + message)
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
            saveLogFile(tag + "  " + getLogInfo(stackTraceElement) + message)
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

    private fun saveLogFile(message: String) {
        val fileDir = File(LOG_PATH_SDCARD_DIR + File.separator + logfile.format(Date()))
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        var file = File(fileDir, "$FILE_NAME-0.txt")
        //实测经常会大于50，调整到150
        for (index in 0 until 150) {
            file = File(fileDir, "$FILE_NAME-$index.txt")
            if (file.exists() && file.length() >= MAX_FILE_LENGTH) {
                //Log.d(TAG,"file length : ${file.length()} ，is too large，new  a file to save")
                file = File(fileDir, "$FILE_NAME-${index + 1}.txt")
                if (file.exists() && file.length() >= MAX_FILE_LENGTH) {
                    continue
                }
            } else if (!file.exists()) {
                file.createLogFile()
                break
            } else {
                break
            }
        }

        try {
            if (file.exists() && file.length() <= MAX_FILE_LENGTH) {
                val ps = PrintStream(FileOutputStream(file, true))
                ps.append(
                        """${simpleDateFormat("\n\nyyyy-MM-dd HH:mm:ss")}   $message""".trimIndent()
                ) // 往文件里写入字符串
                ps.close()
            } else {
                val ps = PrintStream(FileOutputStream(file))
                file.createLogFile()
                ps.println(
                        """
    ${myLogSdf.format(Date())}
    $message
    """.trimIndent()
                ) // 往文件里写入字符串
                ps.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun simpleDateFormat(format: String): String {
        return SimpleDateFormat(format).format(Date())
    }

    /**
     * 只在创建新文件的时候check，避免每次写Log都调用该方法
     */
    private fun File.createLogFile() {
        if (isLogSizeMax()) {
            Log.e(TAG, "file is too large ，delete sth")
            delFile()
        }
        createNewFile()
    }

    /**
     * 开始删除log
     */
    private fun delFile() {
        val rootFile = File(LOG_PATH_SDCARD_DIR)
        if (rootFile.exists()) {
            deleteLogs(rootFile)
        }
    }

    /**
     * 删除目录下的log
     */
    private fun deleteLogs(rootFile: File) {
        for (i in 0 until SDCARD_LOG_FILE_SAVE_DAYS) {
            val needDelTime = getDateBefore(SDCARD_LOG_FILE_SAVE_DAYS - i)?.time
            //删除
            for (logDir in rootFile.listFiles()) {
                needDelTime?.let {
                    if (needDelTime >= stringToLong(logDir.name, logfile)) {
                        deleteLogDir(logDir)
                    }
                }
            }
            if (!isLogSizeMax()) {
                //跳出循环
                return
            }
        }
    }

    /**
     * 判断剩余空间如果小于了 max sd size 或者 文件目录大小大于 max save size，则达到了最大值，不写入log
     */
    private fun isLogSizeMax(): Boolean {
        return getFolderSize(File(LOG_PATH_SDCARD_DIR)) >= MAX_SAVE_SIZE || checkSDFreeSpace()
    }

    /**
     * 剩余空间不小于 sd_max_size
     */
    private fun checkSDFreeSpace(): Boolean {
        return Environment.getExternalStorageDirectory().freeSpace <= MAX_SD_SIZE
    }

    private fun deleteLogDir(logDir: File) {
        if (logDir.isDirectory) {
            for (log in logDir.listFiles()) {
                log.delete()
            }
            if (logDir.listFiles().isEmpty()) {
                logDir.delete()
            }
        } else {
            logDir.delete()
        }
    }


    /**
     * 获取文件夹的大小，最大不超过 max size
     */
    private fun getFolderSize(file: File): Long {
        var size: Long = 0
        try {
            val fileList = file.listFiles()
            for (i in fileList.indices) {
                size = if (fileList[i].isDirectory) {
                    size + getFolderSize(fileList[i])
                } else {
                    size + fileList[i].length()
                }
                if (size >= MAX_SAVE_SIZE) {
                    //文件超过最大值的时候，就不用遍历了，直接return
                    return size
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    fun stringToLong(strTime: String?, formatType: SimpleDateFormat?): Long {
        val date: Date? = formatType?.parse(strTime) // String类型转成date类型
        return date?.let { it.time } ?: 0
    }

    /**
     * 判断当前时间和最大保存天数的差值，得到应该删掉的date
     */
    private fun getDateBefore(days: Int): Date? {
        val nowTime = Date()
        val now: Calendar = Calendar.getInstance()
        now.time = nowTime
        now.set(Calendar.DATE, now.get(Calendar.DATE) - days)
        return now.time
    }

    class FileFilter(prefix: String) : FilenameFilter {
        var prefix2: String? = ""

        init {
            this.prefix2 = prefix
        }

        override fun accept(dir: File?, name: String?): Boolean {
            return name!!.startsWith(prefix2!!)
        }
    }

}