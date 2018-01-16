package edu.uwp.appfactory.rusd.ui.publications

import android.app.Activity
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import android.databinding.BindingAdapter
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import edu.uwp.appfactory.rusd.RUSDApplication
import edu.uwp.appfactory.rusd.data.model.Publication
import edu.uwp.appfactory.rusd.data.local.PublicationsRepository
import edu.uwp.appfactory.rusd.data.local.RealmPublicationsRepository

/**
 * Created by dakota on 6/30/17.
 * Edited by jeremiah on 7/21/17.
 */

/**
 * Do NOT reference Views, Fragments, or Activities in this class
 */
class PublicationsViewModel(context: Context) : ViewModel() {

    val publicationsRepository: PublicationsRepository

    init {
        publicationsRepository = RealmPublicationsRepository(context)
    }

    fun getPublications() : List<Publication>? {
        return publicationsRepository.getAllPublications()
    }

    override fun onCleared() {
        super.onCleared()
        publicationsRepository.close()
    }

    companion object {

        @JvmStatic
        @BindingAdapter("imageFile")
        fun setImageFile(imageView: ImageView, fileName: String) {
            val context = imageView.context
            imageView.setImageResource(context.resources.getIdentifier(
                    fileName.replace(".png", ""), "drawable", context.packageName))
        }

        @JvmStatic
        @BindingAdapter("onImageClick")
        fun onImageClick(imageView: ImageView, URL: String) {
            imageView.setOnClickListener {
                ContextCompat.startActivity(imageView.context, Intent(Intent.ACTION_VIEW).setData(Uri.parse(URL)), null)
            }
        }
    }
}