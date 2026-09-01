package com.example.scaffold

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.example.scaffold.data.repository.ContactRepository
import com.example.scaffold.model.Contact
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class ScaffoldApp :
    Application(),
    SingletonImageLoader.Factory {
    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var contactRepository: ContactRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            if (contactRepository.observeContacts().first().isEmpty()) {
                DummyContacts.forEach { contactRepository.saveContact(it) }
            }
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader
            .Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient })) }
            .build()
}

// Lets a fresh install show the Contacts tab with something in it rather than the empty state.
private val DummyContacts =
    listOf(
        Contact(
            firstName = "Ada",
            lastName = "Lovelace",
            addressLine1 = "12 Curzon Street",
            addressLine2 = null,
            city = "London",
            postcode = "W1J 5HN",
        ),
        Contact(
            firstName = "Alan",
            lastName = "Turing",
            addressLine1 = "Hut 8, Bletchley Park",
            addressLine2 = "Sherwood Drive",
            city = "Milton Keynes",
            postcode = "MK3 6EB",
        ),
        Contact(
            firstName = "Grace",
            lastName = "Hopper",
            addressLine1 = "45 Harbour Road",
            addressLine2 = null,
            city = "Portsmouth",
            postcode = "PO1 3AX",
        ),
    )
