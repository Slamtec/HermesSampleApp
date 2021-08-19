package com.slamtec.robot.deliver.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.slamtec.robot.deliver.R
import java.util.*

class RoomNumberView : LinearLayout {
    private var mNo1: TextView
    private var mNo2: TextView
    private var mNo3: TextView
    private var mNo4: TextView
    private var mNo5: TextView
    private var mTextViewList: Array<TextView>
    private var mInputRoomNum = Stack<Char>()
    private var mLen = 5


    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val view = View.inflate(context, R.layout.view_roomnumber, this)
        mNo1 = view.findViewById(R.id.tv_room_1) as TextView
        mNo1.setBackgroundResource(R.drawable.input_on)
        mNo2 = view.findViewById(R.id.tv_room_2) as TextView
        mNo3 = view.findViewById(R.id.tv_room_3) as TextView
        mNo4 = view.findViewById(R.id.tv_room_4) as TextView
        mNo5 = view.findViewById(R.id.tv_room_5) as TextView
        mTextViewList = arrayOf(mNo1, mNo2, mNo3, mNo4, mNo5)
    }

    fun inputNumber(num: Char) {
        if (mInputRoomNum.size < mLen) {
            mInputRoomNum.push(num)
        }
        refreshNumber()
    }

    fun deleteNumber() {
        if (!mInputRoomNum.empty()) {
            mInputRoomNum.pop()
        }
        refreshNumber()
    }


    fun getRoomNumber(): String {
        return String(mInputRoomNum.toCharArray())
    }

    fun clearNumber() {
        mInputRoomNum.clear()
        refreshNumber()
    }

    private fun refreshNumber() {
        for (index in mTextViewList.indices) {
            if (index < mInputRoomNum.size) {
                mTextViewList[index].text = mInputRoomNum[index].toString()
            } else {
                mTextViewList[index].text = ""
            }
            if (index + 1 == mInputRoomNum.size) {
                mTextViewList[index].setBackgroundResource(R.drawable.input_on)
            } else {
                mTextViewList[index].setBackgroundResource(R.drawable.input_default)
            }
        }
    }

    fun setNumberLength(len: Int) {
        when (len) {
            3 -> {
                mNo4.visibility = View.GONE
                mNo5.visibility = View.GONE
                mTextViewList = arrayOf(mNo1, mNo2, mNo3)
                refreshNumber()
                mLen = len
            }
            4 -> {
                mNo4.visibility = View.VISIBLE
                mNo5.visibility = View.GONE
                mTextViewList = arrayOf(mNo1, mNo2, mNo3, mNo4)
                refreshNumber()
                mLen = len
            }
            5 -> {
                mNo4.visibility = View.VISIBLE
                mNo5.visibility = View.VISIBLE
                mTextViewList = arrayOf(mNo1, mNo2, mNo3, mNo4, mNo5)
                refreshNumber()
                mLen = len
            }
            else -> {
                mLen = 4
                mNo4.visibility = View.VISIBLE
                mNo5.visibility = View.GONE
                mTextViewList = arrayOf(mNo1, mNo2, mNo3, mNo4)
                refreshNumber()
            }
        }
    }

}