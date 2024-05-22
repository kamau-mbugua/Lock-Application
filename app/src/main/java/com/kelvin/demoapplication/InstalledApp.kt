package com.kelvin.demoapplication

import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val icon: Drawable,
    val version: String,
    val name: String
)