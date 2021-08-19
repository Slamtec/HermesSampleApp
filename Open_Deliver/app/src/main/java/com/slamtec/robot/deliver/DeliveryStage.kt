package com.slamtec.robot.deliver

object DeliveryStage {
    const val IDLE = "IDLE"
    const val ON_DELIVERING = "ON_DELIVERING"
    const val ARRIVED_AT_TARGET = "ARRIVED_AT_TARGET"
    const val DELIVERY_CANCELLED = "DELIVERY_CANCELLED"
    const val BACK_TO_STATION = "BACK_TO_STATION"
    const val DEVICE_ERROR = "DEVICE_ERROR"
    const val GOING_HOME = "GOING_HOME"
}