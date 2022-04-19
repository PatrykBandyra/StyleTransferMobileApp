package com.example.styletransfermobileapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity<B : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: B
    private lateinit var mProgressDialog: Dialog
    private var mDoubleBackToExitPressedOnce: Boolean = false

    companion object {
        var isProgressDialogShown: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        setContentView(binding.root)
    }

    protected abstract fun getViewBinding(): B

    protected fun showRationaleDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage(resources.getString(R.string.rationale_message))
            .setPositiveButton(resources.getString(R.string.rationale_positive_button)) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    protected fun setUpActionBarForReturnAction(
        toolbar: Toolbar,
        @StringRes title: Int = 0,
        @DrawableRes icon: Int = 0,
    ) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            if (title != 0) {
                actionBar.title = resources.getString(title)
            }
            if (icon != 0) {
                actionBar.setHomeAsUpIndicator(icon)
            }
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    protected fun hideKeyboard() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
            Log.i("KEYBOARD", "Tried to hide a keyboard but it is already hidden.")
        }
    }

    protected fun showProgressDialog(@StringRes message: Int = R.string.please_wait) {
        if (!isProgressDialogShown) {
            isProgressDialogShown = true
            mProgressDialog = Dialog(this)
            mProgressDialog.setContentView(R.layout.dialog_progress)
            mProgressDialog.findViewById<TextView>(R.id.tv_progress_text).text =
                resources.getString(message)
            mProgressDialog.show()
        }
    }

    protected fun hideProgressDialog() {
        if (isProgressDialogShown) {
            mProgressDialog.dismiss()
            isProgressDialogShown = false
        }
    }

    protected fun showErrorSnackBar(@StringRes message: Int) {
        val snackBar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .also { snackbar ->
                    snackbar.setAction("OK") {
                        snackbar.dismiss()
                    }
                }
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_error_color))
        snackBar.show()
    }

    protected fun doubleBackToExit() {
        if (mDoubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        mDoubleBackToExitPressedOnce = true
        Toast.makeText(
            this,
            resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()

        Handler(Looper.getMainLooper()).postDelayed(
            {
                mDoubleBackToExitPressedOnce = false
            },
            2000
        )
    }

    protected fun toggleRightDrawer(drawer: DrawerLayout) {
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END)
        } else {
            drawer.openDrawer(GravityCompat.END)
        }
    }

    protected fun toggleLeftDrawer(drawer: DrawerLayout) {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    protected fun getFileExtension(uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun View.snackbar(message: String) {
        Snackbar.make(this, message, Snackbar.LENGTH_LONG).also { snackbar ->
            snackbar.setAction("Ok") {
                snackbar.dismiss()
            }
        }.show()
    }
}