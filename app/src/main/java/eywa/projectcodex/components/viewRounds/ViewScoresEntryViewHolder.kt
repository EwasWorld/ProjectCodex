package eywa.projectcodex.components.viewRounds

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.*
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.components.commonUtils.ToastSpamPrevention
import eywa.projectcodex.database.archerRound.ArcherRound

/**
 * Container to display an [ArcherRound] in a recycler view
 */
abstract class ViewScoresEntryViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
    companion object {
        private const val LOG_TAG = "ViewScoresEntryViewHolder"
    }

    internal var entry: ViewScoresEntry? = null
    var viewModel: ViewRoundsViewModel? = null

    abstract fun bind(viewScoresEntry: ViewScoresEntry)

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        ContextMenuItem.values().forEach {
            it.addItemToMenu(menu!!, v!!, entry!!, viewModel!!)
        }
    }

    enum class ContextMenuItem(val titleId: Int) {
        SCORE_PAD(R.string.view_rounds_menu__score_pad) {
            override fun onClick(view: View, entry: ViewScoresEntry, viewModel: ViewRoundsViewModel) {
                view.findNavController().navigate(
                        ViewRoundsFragmentDirections.actionViewRoundsFragmentToScorePadFragment(entry.id)
                )
            }
        },
        CONTINUE(R.string.view_rounds_menu__continue) {
            override fun onClick(view: View, entry: ViewScoresEntry, viewModel: ViewRoundsViewModel) {
                if (entry.isRoundComplete()) {
                    CustomLogger.customLogger.w(LOG_TAG, "Tried to continue completed round")
                    ToastSpamPrevention.displayToast(
                            view.context,
                            view.resources.getString(R.string.err_view_round__round_already_complete)
                    )
                }
                else {
                    view.findNavController().navigate(
                            ViewRoundsFragmentDirections.actionViewRoundsFragmentToInputEndFragment(entry.id)
                    )
                }
            }

            override fun addItemToMenu(
                    menu: ContextMenu,
                    view: View,
                    entry: ViewScoresEntry,
                    viewModel: ViewRoundsViewModel
            ) {
                if (entry.isRoundComplete()) {
                    return
                }
                super.addItemToMenu(menu, view, entry, viewModel)
            }
        },
        EDIT_INFO(R.string.view_rounds_menu__edit) {
            override fun onClick(view: View, entry: ViewScoresEntry, viewModel: ViewRoundsViewModel) {
                view.findNavController().navigate(
                        ViewRoundsFragmentDirections.actionViewRoundsFragmentToNewRoundFragment(archerRoundId = entry.id)
                )
            }
        },
        DELETE(R.string.view_rounds_menu__delete) {
            override fun onClick(view: View, entry: ViewScoresEntry, viewModel: ViewRoundsViewModel) {
                viewModel.deleteRound(entry.id)
            }
        },
        CONVERT(R.string.view_rounds_menu__convert) {
            private var context: Context? = null
            private var entry: ViewScoresEntry? = null
            private var viewModel: ViewRoundsViewModel? = null

            /**
             * Currently selected item in the [convertDialog]
             */
            private var selectedConversionType: ConvertScore? = null

            /**
             * Displayed when the user selects the 'convert' option from the menu
             */
            private val convertDialog by lazy {
                val builder = AlertDialog.Builder(context)
                builder.setTitle(R.string.view_round__convert_score_dialog_title)
                val menuItems = listOf(
                        R.string.view_rounds__convert_xs_to_tens to ConvertScore.XS_TO_TENS,
                        R.string.view_rounds__convert_to_five_zone to ConvertScore.TO_FIVE_ZONE
                )
                val layoutInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                @SuppressLint("InflateParams")
                val content = layoutInflater.inflate(R.layout.list_msg_dialog, null)
                content.findViewById<TextView>(R.id.text_list_msg_dialog__message).text =
                        context!!.resources.getString(R.string.view_round__convert_score_dialog_body)

                val radioGroup = content.findViewById<RadioGroup>(R.id.radios_list_msg_dialog__items)

                menuItems.forEachIndexed { i, menuItem ->
                    @SuppressLint("InflateParams")
                    val layout = layoutInflater.inflate(R.layout.radio_button, null, false)
                    val radioButton = layout.findViewById<RadioButton>(R.id.radio_button)
                    radioButton.text = context!!.resources.getString(menuItem.first)
                    radioButton.setOnClickListener {
                        selectedConversionType = menuItem.second
                    }
                    layout.id = i
                    radioGroup.addView(radioButton)
                    if (i == 0) {
                        radioButton.callOnClick()
                    }
                }
                radioGroup.check(0)

                builder.setView(content)
                builder.setPositiveButton(R.string.general_ok) { dialog, _ ->
                    ToastSpamPrevention.displayToast(
                            context!!,
                            context!!.resources.getString(R.string.view_round__convert_score_started_message)
                    )
                    val completedMessage =
                            context!!.resources.getString(R.string.view_round__convert_score_completed_message)
                    selectedConversionType!!.convertScore(entry!!.arrows!!, viewModel!!)?.invokeOnCompletion {
                        ToastSpamPrevention.displayToast(context!!, completedMessage)
                    } ?: ToastSpamPrevention.displayToast(context!!, completedMessage)
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.general_cancel) { _, _ -> }
                builder.create()
            }

            override fun onClick(view: View, entry: ViewScoresEntry, viewModel: ViewRoundsViewModel) {
                this.viewModel = viewModel
                this.context = view.context
                this.entry = entry

                convertDialog.show()
            }
        };

        open fun addItemToMenu(menu: ContextMenu, view: View, entry: ViewScoresEntry, viewModel: ViewRoundsViewModel) {
            val newItem = menu.add(Menu.NONE, ordinal, ordinal, titleId)
            newItem.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    if (item == null || item.itemId >= values().size) {
                        return false
                    }
                    values()[item.itemId].onClick(view, entry, viewModel)
                    return true
                }
            })
        }

        abstract fun onClick(view: View, entry: ViewScoresEntry, viewModel: ViewRoundsViewModel)
    }
}