package edu.uwp.appfactory.rusd.ui.notifications



import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.*
import edu.uwp.appfactory.rusd.R
import edu.uwp.appfactory.rusd.RUSDApplication
import edu.uwp.appfactory.rusd.data.model.SectionNotification
import edu.uwp.appfactory.rusd.ui.notifications.dialog.FilterDialogFragment
import kotlinx.android.synthetic.main.fragment_swipe_refresh.view.*
import org.zakariya.stickyheaders.StickyHeaderLayoutManager

/**
 * A simple [Fragment] subclass.
 */
class NotificationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: NotificationsSectionsAdapter
    private lateinit var layoutManager: StickyHeaderLayoutManager
    private lateinit var viewModel: NotificationsViewModel

    companion object {

        @JvmStatic
        fun newInstance() : NotificationsFragment {
            val notificationsFragment: NotificationsFragment = NotificationsFragment()
            return notificationsFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this, NotificationsViewModelFactory(context)).get(NotificationsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_swipe_refresh, container, false)
        recyclerView = rootView.swipe_refresh_rv
        swipeRefresh = rootView.swipe_refresh
        swipeRefresh.setColorSchemeResources(R.color.colorAccent)

        swipeRefresh.setOnRefreshListener {
            viewModel.updateNotifications()
        }
        adapter = NotificationsSectionsAdapter(viewModel.getNotifications().value.orEmpty())

        subscribeUI(viewModel)

        layoutManager = StickyHeaderLayoutManager()

        layoutManager.setHeaderPositionChangedCallback { _, header, _, newPosition ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (newPosition == StickyHeaderLayoutManager.HeaderPosition.STICKY) {

                    header.elevation = RUSDApplication.convertDp(context, 10F)

                } else {
                    header.elevation = RUSDApplication.convertDp(context, 8F)
                }
            }
        }

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        return rootView
    }

    override fun onStart() {
        super.onStart()
        viewModel.updateNotifications()
        swipeRefresh.isRefreshing = true
    }

    private fun subscribeUI(viewModel: NotificationsViewModel) {
        viewModel.getNotifications().observe(this, Observer<List<SectionNotification>> { notifications ->
            if (notifications != null) {
                adapter.updateNotifications(notifications)
                swipeRefresh.isRefreshing = false
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.filter_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.filter -> showFilterDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFilterDialog() {
        val fm: FragmentManager = fragmentManager
        val dialog: FilterDialogFragment = FilterDialogFragment.newInstance(viewModel.notificationsRepository)
        dialog.show(fm, "filter")
    }
}