package co.theportman.way_of_the_goat

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
actual val isDebugBuild: Boolean get() = Platform.isDebugBinary
