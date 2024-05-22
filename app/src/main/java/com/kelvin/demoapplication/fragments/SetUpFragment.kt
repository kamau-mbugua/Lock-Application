package com.kelvin.demoapplication.fragments

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kelvin.demoapplication.AppBlockerService
import com.kelvin.demoapplication.InstalledApp
import com.kelvin.demoapplication.InstalledAppsAdapter
import com.kelvin.demoapplication.R
import com.kelvin.demoapplication.getAllInstalledApps
import com.kelvin.demoapplication.saveBlockedApps
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SetUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SetUpFragment : Fragment(), View.OnClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private var adapter: InstalledAppsAdapter? = null
    private val REQUEST_CODE_OVERLAY = 101
    private val REQUEST_CODE_USAGE_STATS = 102
    private val blockedApps: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_set_up, container, false)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        setUpOnClickListeners()
        searchView()
        controlVisibility()
    }

    private fun setUpOnClickListeners() {
        requireActivity().findViewById<Button>(R.id.button_custom).setOnClickListener(this)
    }

    private fun searchView() {
        val searchView = requireActivity().findViewById<EditText>(R.id.searchViewName)
        var job: Job? = null

        val recyclerView = requireActivity().findViewById<RecyclerView>(R.id.recycler_view)
        val progressBar = requireActivity().findViewById<ProgressBar>(R.id.progress_bar)
        var apps: List<InstalledApp>? = null
        adapter = InstalledAppsAdapter(apps ?: emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        adapter!!.onChecked = { isChecked, app ->
            if (isChecked) {
                blockedApps.add(app.packageName)
                controlVisibility()
                Toast.makeText(requireActivity(), "${app.name} is Added", Toast.LENGTH_SHORT)
                    .show()
            } else {
                blockedApps.remove(app.packageName)
//                blockedApps.minus(app.packageName)
                controlVisibility()
                Toast.makeText(requireActivity(), "${app.name} is Removed", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        progressBar.visibility = View.VISIBLE
        MainScope().launch {
            apps = getAllInstalledApps(requireActivity())
            adapter!!.updateData(apps!!)
            progressBar.visibility = View.GONE
        }

        job?.cancel()
        searchView.addTextChangedListener { queryText: Editable? ->
            job = MainScope().launch {
                delay(500)
                queryText?.let { search ->
                    if (search.toString().isNotEmpty()) {
                        val filteredApps =
                            apps?.filter { it.name.contains(search.toString(), ignoreCase = true) }
                        adapter!!.updateData(filteredApps ?: emptyList())
                    } else {
                        adapter!!.updateData(apps ?: emptyList())
                    }
                }
            }
        }
    }

    private fun controlVisibility() {
        if (!blockedApps.isNullOrEmpty()) {
            requireActivity().findViewById<Button>(R.id.button_custom).visibility = View.VISIBLE
        } else {
            requireActivity().findViewById<Button>(R.id.button_custom).visibility = View.GONE
        }
        requireActivity().findViewById<Button>(R.id.button_custom).text =
            "Block ${blockedApps.size} apps"
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(requireActivity(), { _, hourOfDay, minute ->
            val duration = hourOfDay * 60 * 60 * 1000 + minute * 60 * 1000
            startBlockingService(duration.toLong())
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
        timePickerDialog.show()
    }

    private fun startBlockingService(duration: Long) {
//        requireActivity().findViewById<TextView>(R.id.textView).text =
        "Blocking apps for ${duration / 1000 / 60} minutes"
        if (Settings.canDrawOverlays(requireActivity())) {
            val intent = Intent(requireActivity(), AppBlockerService::class.java).apply {
                putExtra("BLOCK_DURATION", duration)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireActivity().startForegroundService(intent)
            } else {
                requireActivity().startService(intent)
            }
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivityForResult(intent, REQUEST_CODE_OVERLAY)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_custom -> {
                val blocked: Set<String> = blockedApps.toSet()
                saveBlockedApps(blocked, requireActivity()).apply {
                    showTimePickerDialog()
                }
            }
        }
    }
}