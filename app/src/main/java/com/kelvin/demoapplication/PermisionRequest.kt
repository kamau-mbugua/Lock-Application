package com.kelvin.demoapplication

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

fun requestUsageStatsPermission(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    context.startActivity(intent)
}



fun getForegroundApp(context: Context): String? {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val beginTime = endTime - 1000 * 60 // Check usage in the last minute
    val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime)

    var recentUsageStats: UsageStats? = null
    for (usageStats in usageStatsList) {
        if (recentUsageStats == null || usageStats.lastTimeUsed > recentUsageStats.lastTimeUsed) {
            recentUsageStats = usageStats
        }
    }
    return recentUsageStats?.packageName
}

fun getBackgroundApp(context: Context): String? {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val beginTime = endTime - 1000 * 60 // Check usage in the last minute
    val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime)

    var recentUsageStats: UsageStats? = null
    for (usageStats in usageStatsList) {
        if (recentUsageStats == null || usageStats.lastTimeUsed > recentUsageStats.lastTimeUsed) {
            recentUsageStats = usageStats
        }
    }
    return recentUsageStats?.packageName
}

fun hasUsageStatsPermission(context: Context): Boolean {
    val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOpsManager.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
    } else {
        appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

fun getInstalledApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    val packages = pm.getInstalledApplications(0)
    return packages.filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }.map {
        val appName = it.loadLabel(pm).toString()
        val packageName = it.packageName
        val icon = it.loadIcon(pm)
        val version = pm.getPackageInfo(packageName, 0).versionName
        InstalledApp(packageName, icon, version, appName)
    }
}


fun getAllInstalledApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
    return packages.filter {
        // Filter to get only non-system apps
        (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0
    }.map {
        val appName = it.loadLabel(pm).toString()
        val packageName = it.packageName
        val icon = it.loadIcon(pm)
        val version = pm.getPackageInfo(packageName, 0).versionName ?: "N/A"
        InstalledApp(packageName, icon, version, appName)
    }
}
fun saveBlockedApps(blockedApps: Set<String>, context: Context) {
    val sharedPreferences = context.getSharedPreferences("blocked_apps", Context.MODE_PRIVATE)
    with (sharedPreferences.edit()) {
        putStringSet("blocked_apps", blockedApps)
        apply()
    }
}

fun getBlockedApps(context: Context): Set<String> {
    val sharedPreferences = context.getSharedPreferences("blocked_apps", Context.MODE_PRIVATE)
    return sharedPreferences.getStringSet("blocked_apps", emptySet()) ?: emptySet()
}