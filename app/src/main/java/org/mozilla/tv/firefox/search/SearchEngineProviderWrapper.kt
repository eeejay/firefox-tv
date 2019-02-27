/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.tv.firefox.search

import android.content.Context
import android.util.Log
import java.util.Locale
import mozilla.components.browser.search.SearchEngine
import mozilla.components.browser.search.provider.AssetsSearchEngineProvider
import mozilla.components.browser.search.provider.SearchEngineProvider
import mozilla.components.browser.search.provider.localization.SearchLocalizationProvider

private const val LOGTAG = "Search"

/**
 * Wraps an [AssetsSearchEngineProvider] to allow for replacements of
 * search plugins.
 *
 * @property replacements a map specifying which plugins to replace e.g.
 * mapOf("google" to "google-fftv") to replace google with google-fftv.
 */
class SearchEngineProviderWrapper(private val replacements: Map<String, String>) : SearchEngineProvider {

    val myLocalizationProvider = object : SearchLocalizationProvider() {
        override val language: String
            get() = Locale.getDefault().language

        override val country: String
            get() = Locale.getDefault().country

        override val region: String? = country
    }

    private val inner = AssetsSearchEngineProvider(
        localizationProvider = myLocalizationProvider,
        additionalIdentifiers = replacements.values.toList()
    )

    override suspend fun loadSearchEngines(context: Context): List<SearchEngine> {
        val searchEngines = inner.loadSearchEngines(context).toMutableList()

        replacements.forEach { (new, old) ->
            val newIndex = searchEngines.indexOfFirst { it.identifier == new }
            val oldIndex = searchEngines.indexOfFirst { it.identifier == old }
            if (newIndex != -1 && oldIndex != -1) {
                searchEngines[newIndex] = searchEngines.removeAt(oldIndex)
            } else {
                Log.d(LOGTAG, "Failed to replace plugin $new with $old")
            }
        }

        return searchEngines
    }
}