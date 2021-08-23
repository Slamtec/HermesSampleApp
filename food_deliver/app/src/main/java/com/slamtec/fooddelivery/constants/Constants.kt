package com.slamtec.fooddelivery.constants

/**
 * File   : Constants
 * Author : Qikun.Xiong
 * Date   : 2021/8/10 5:16 PM
 */
object Constants {
    const val AGENT_URL = "http://127.0.0.1:1448/api/"

    /**
     *  0-待放入菜品,1-选择餐桌号,2-已选择餐桌号，待取餐,3-到达餐桌,4-已取餐,5-取餐超时,6-送餐超时
     */
    const val READY_PUT_FOOD = 0
    const val CHOOSE_TABLE = 1
    const val CHOSEN_TABLE = 2
    const val ARRIVED_TABLE = 3
    const val HAVE_TAKEN_FOOD = 4
    const val TAKEN_FOOD_TIME_OUT = 5
    const val DELIVERING_FOOD_TIME_OUT = 6

    /**
     *
     * 跨楼层移动，和跨楼层回桩
     */
    const val MOVE_ACTION_NAME = "slamtec.agent.actions.MultiFloorMoveAction"
    const val BACK_HOME_NAME = "slamtec.agent.actions.MultiFloorBackHomeAction"

    /**
     * 系统参数
     */
    const val MAX_SPEED_KEY = "base.max_moving_speed"
}