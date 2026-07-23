package io.github.zapolyarnydev.ptktimetable

import android.app.Application

class PtkApplication : Application() {
    val container by lazy { AppContainer(this) }
}
