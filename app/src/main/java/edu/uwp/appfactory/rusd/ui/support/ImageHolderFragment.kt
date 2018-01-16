package edu.uwp.appfactory.rusd.ui.support


import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.databinding.library.baseAdapters.BR
import edu.uwp.appfactory.rusd.R
import edu.uwp.appfactory.rusd.data.model.Publication
import edu.uwp.appfactory.rusd.databinding.ListItemPublicationBinding
import kotlinx.android.synthetic.main.list_item_publication.view.*


/**
 * A simple [Fragment] subclass.
 *
 * Edited by Jeremiah on 7/21/2017.
 */
class ImageHolderFragment : Fragment() {

    val PUBLICATION_KEY = "Publication"
    lateinit var cardView: CardView
    lateinit var publication: Publication

    companion object {

        @JvmStatic
        fun newInstance(publication: Publication) : ImageHolderFragment {
            val imageHolderFragment: ImageHolderFragment = ImageHolderFragment()
            val args = Bundle()
            args.putParcelable(imageHolderFragment.PUBLICATION_KEY, publication)
            imageHolderFragment.arguments = args
            return imageHolderFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        publication = arguments.getParcelable(PUBLICATION_KEY)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: ListItemPublicationBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.list_item_publication, container, false)
        binding.setVariable(BR.publication, publication)
        binding.executePendingBindings()

        val rootView = binding.root
        cardView = rootView.publication_card_view
        cardView.setContentPadding(0,0,0,0)
        return rootView
    }
}
