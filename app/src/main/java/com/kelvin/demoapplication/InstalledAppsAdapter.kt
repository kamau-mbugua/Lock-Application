package com.kelvin.demoapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InstalledAppsAdapter(private var apps: List<InstalledApp>) :
    RecyclerView.Adapter<InstalledAppsAdapter.ViewHolder>() {

    var onChecked: ((Boolean, InstalledApp) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val packageName: TextView = view.findViewById(R.id.package_name)
        val app_name: TextView = view.findViewById(R.id.app_name)
        val version: TextView = view.findViewById(R.id.version)
        val checkbox: CheckBox = view.findViewById(R.id.checkbox)
    }

    fun updateData(newApps: List<InstalledApp>) {
        apps = newApps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_installed_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.icon.setImageDrawable(app.icon)
        holder.packageName.text = app.packageName
        holder.version.text = app.version
        holder.app_name.text = app.name
        holder.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // App is checked
                onChecked?.invoke(isChecked, app)
            } else {
                // App is unchecked
                onChecked?.invoke(isChecked, app)
            }
        }
    }

    override fun getItemCount() = apps.size
}