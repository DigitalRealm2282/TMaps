package com.tdi.tmaps

import android.content.Context
import android.preference.PreferenceManager
import java.util.*

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    // the method is used to set the language at runtime
    fun setLocale(context: Context, language: String): Context {
        persist(context, language)

        // updating the language for devices
        return updateResources(context, language)
    }

    private fun persist(context: Context, language: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(SELECTED_LANGUAGE, language)
        editor.apply()
    }

    // the method is used update the language of application by creating
    // object of inbuilt Locale class and passing language argument to it
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

//    private fun updateResourcesLegacy(context: Context, language: String): Context {
//        val locale = Locale(language)
//        Locale.setDefault(locale)
//        val resources = context.resources
//        val configuration = resources.configuration
//        configuration.locale = locale
//        configuration.setLayoutDirection(locale)
//        resources.updateConfiguration(configuration, resources.displayMetrics)
//        return context
//    }
}
