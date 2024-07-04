package com.dd.deardiary.shareddata

interface BaseView {
    fun internalServer()

    fun onUnknownError(error: String?)

    fun onTimeout()

    fun onNetworkError()

    fun onConnectionError()
}