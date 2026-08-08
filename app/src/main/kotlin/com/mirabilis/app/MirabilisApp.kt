package com.mirabilis.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Composition root (ADR-0004). The only module that knows the full DI graph. */
@HiltAndroidApp
class MirabilisApp : Application()
