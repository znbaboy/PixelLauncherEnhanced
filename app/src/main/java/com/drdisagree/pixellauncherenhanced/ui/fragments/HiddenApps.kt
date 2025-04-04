package com.drdisagree.pixellauncherenhanced.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.pixellauncherenhanced.PLEnhanced.Companion.appContext
import com.drdisagree.pixellauncherenhanced.R
import com.drdisagree.pixellauncherenhanced.data.common.Constants.APP_BLOCK_LIST
import com.drdisagree.pixellauncherenhanced.data.config.RPrefs
import com.drdisagree.pixellauncherenhanced.data.model.AppInfoModel
import com.drdisagree.pixellauncherenhanced.databinding.FragmentHiddenAppsBinding
import com.drdisagree.pixellauncherenhanced.ui.adapters.AppListAdapter
import com.drdisagree.pixellauncherenhanced.utils.MiscUtils.setToolbarTitle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HiddenApps : Fragment() {

    private lateinit var binding: FragmentHiddenAppsBinding
    private var appList: List<AppInfoModel>? = null
    private var adapter: AppListAdapter? = null

    private val packageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            initAppList()
        }
    }
    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable) {
            if (binding.search.text.toString().trim().isNotEmpty()) {
                binding.clear.visibility = View.VISIBLE
                filterList(binding.search.text.toString().trim())
            } else {
                binding.clear.visibility = View.GONE
                filterList("")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHiddenAppsBinding.inflate(inflater, container, false)

        setToolbarTitle(
            requireContext(),
            R.string.fragment_hidden_apps_title,
            true,
            binding.header.toolbar,
            binding.header.collapsingToolbar
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAppList()
    }

    private fun initAppList() {
        binding.recyclerView.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.search.removeTextChangedListener(textWatcher)

        CoroutineScope(Dispatchers.IO).launch {
            appList = getAllLaunchableApps()
            adapter = AppListAdapter(appList!!)

            withContext(Dispatchers.Main) {
                try {
                    binding.recyclerView.adapter = adapter
                    binding.search.addTextChangedListener(textWatcher)

                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE

                    binding.clear.setOnClickListener {
                        binding.search.setText("")
                        binding.clear.visibility = View.GONE
                    }
                    if (binding.search.text.toString().trim().isNotEmpty()) {
                        filterList(binding.search.text.toString().trim { it <= ' ' })
                    }
                } catch (_: Exception) {
                    // Fragment was not attached to activity
                }
            }
        }
    }

    private fun filterList(query: String) {
        if (appList == null) return

        val startsWithNameList: MutableList<AppInfoModel> = ArrayList()
        val containsNameList: MutableList<AppInfoModel> = ArrayList()
        val startsWithPackageNameList: MutableList<AppInfoModel> = ArrayList()
        val containsPackageNameList: MutableList<AppInfoModel> = ArrayList()

        for (app in appList!!) {
            if (app.appName.lowercase(Locale.getDefault())
                    .startsWith(query.lowercase(Locale.getDefault()))
            ) {
                startsWithNameList.add(app)
            } else if (app.appName.lowercase(Locale.getDefault())
                    .contains(query.lowercase(Locale.getDefault()))
            ) {
                containsNameList.add(app)
            } else if (app.packageName.lowercase(Locale.getDefault()).startsWith(
                    query.lowercase(
                        Locale.getDefault()
                    )
                )
            ) {
                startsWithPackageNameList.add(app)
            } else if (app.packageName.lowercase(Locale.getDefault()).contains(
                    query.lowercase(
                        Locale.getDefault()
                    )
                )
            ) {
                containsPackageNameList.add(app)
            }
        }

        val filteredList: MutableList<AppInfoModel> = ArrayList()
        filteredList.addAll(startsWithNameList)
        filteredList.addAll(containsNameList)
        filteredList.addAll(startsWithPackageNameList)
        filteredList.addAll(containsPackageNameList)

        adapter = AppListAdapter(filteredList)
        binding.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        val intentFilterWithoutScheme = IntentFilter()
        intentFilterWithoutScheme.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilterWithoutScheme.addAction(Intent.ACTION_PACKAGE_REMOVED)

        val intentFilterWithScheme = IntentFilter()
        intentFilterWithScheme.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilterWithScheme.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilterWithScheme.addDataScheme("package")

        LocalBroadcastManager
            .getInstance(requireContext())
            .registerReceiver(packageReceiver, intentFilterWithoutScheme)
        LocalBroadcastManager
            .getInstance(requireContext())
            .registerReceiver(packageReceiver, intentFilterWithScheme)
    }

    override fun onDestroy() {
        try {
            LocalBroadcastManager
                .getInstance(requireContext())
                .unregisterReceiver(packageReceiver)
        } catch (_: Exception) {
            // Receiver was not registered
        }
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            parentFragmentManager.popBackStackImmediate()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private fun getAllLaunchableApps(): List<AppInfoModel> {
            val appBlockList = RPrefs.getStringSet(APP_BLOCK_LIST, emptySet())!!
            val appList: MutableList<AppInfoModel> = ArrayList()
            val packageManager = appContext.packageManager

            val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            for (appInfo in applications) {
                val packageName = appInfo.packageName

                packageManager.getLaunchIntentForPackage(packageName) ?: continue

                val appName = appInfo.loadLabel(packageManager).toString()
                val appIcon = appInfo.loadIcon(packageManager)
                val isSelected = appBlockList.contains(packageName)

                val app = AppInfoModel(appName, packageName, appIcon, isSelected)

                appList.add(app)
            }

            appList.sortWith(compareBy<AppInfoModel> { !it.isSelected }.thenBy { it.appName.lowercase() })

            return appList
        }
    }
}