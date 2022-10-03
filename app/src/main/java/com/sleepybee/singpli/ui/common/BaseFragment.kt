package com.sleepybee.singpli.ui.common

import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

open class BaseFragment :Fragment(){

    protected val mainScope = CoroutineScope(Dispatchers.Main)
    protected val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
}