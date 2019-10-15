package com.pertamina.absensiapplication.view

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.databinding.ActivityMainBinding
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private var menu: Menu? = null
    private var searchView: SearchView? = null
    private val userViewModel: UserViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        val topDestination = setOf(
            R.id.status_dest,
            R.id.dashboard_dest,
            R.id.profile_dest,
            R.id.login_dest,
            R.id.history_dest
        )
        navController = findNavController(R.id.football_nav_fragment)
        appBarConfiguration = AppBarConfiguration(topDestination)
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.mainBottomNav.setupWithNavController(navController)
        onDestinationChanged()
    }

    private fun onDestinationChanged() =
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val destId = destination.id
            if (destId == R.id.status_dest || destId == R.id.dashboard_dest || destId == R.id.history_dest || destId == R.id.profile_dest) {
                supportActionBar?.title = ""
                supportActionBar?.show()
                val item = menu?.findItem(R.id.action_search)
                item?.isVisible = false
                binding.mainBottomNav.visibility = View.VISIBLE
                binding.ivToolbar.visibility = View.VISIBLE
            } else if (destId == R.id.login_dest) {
                supportActionBar?.hide()
                binding.mainBottomNav.visibility = View.GONE
            } else {
                binding.ivToolbar.visibility = View.GONE
                val item = menu?.findItem(R.id.action_search)
                when (destId) {
                    R.id.manage_dest -> item?.isVisible = true
                    R.id.search_dest -> {
                        item?.isVisible = true
                        item?.expandActionView()
                        userViewModel.getQuerySearch().observe(this, Observer { query -> searchView?.setQuery(query, false) })
                    }
                    else -> {
                        item?.isVisible = false
                        item?.collapseActionView()
                    }
                }
                supportActionBar?.show()
                supportActionBar?.title = ""
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_arrow_left)
                binding.mainBottomNav.visibility = View.GONE
            }
        }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.menu = menu
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView?.queryHint = resources.getString(R.string.search_hint)
        searchView?.isIconified = false
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                userViewModel.setQuerySearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean = false
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                navController.navigate(R.id.search_dest)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}