package ru.veider.eventsreminder.presentation.ui.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import ru.veider.eventsreminder.R
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.android.support.DaggerDialogFragment
import ru.veider.eventsreminder.databinding.CreateNewEventDialogFragmentBinding
import ru.veider.eventsreminder.presentation.ui.SOURCE_ID_TO_NAVIGATE


class CreateNewEventDialogFragment : DaggerDialogFragment() {
    private val binding: CreateNewEventDialogFragmentBinding by viewBinding()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_round_corner_background)
            inflater.inflate(R.layout.create_new_event_dialog_fragment, container, false)
        } catch (t: Throwable) {
            logAndToast(t)
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            binding.negativeBtnChooseNewEventType.setOnClickListener {
                try {
                    findNavController().navigateUp()
                } catch (t: Throwable) {
                    logAndToast(t)
                }
            }
            binding.positiveBtnChooseNewEventType.setOnClickListener {
                onPositiveBtnClicked()
            }
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }

    private fun onPositiveBtnClicked() {
        try {
            val bundle = Bundle()
            bundle.putInt(
                SOURCE_ID_TO_NAVIGATE,
                arguments?.getInt(SOURCE_ID_TO_NAVIGATE) ?: R.id.dashboardFragment
            )
            when (binding.radioGroupChooseNewEventType.checkedRadioButtonId) {
                R.id.radiobtnBirthday -> {
                    findNavController().navigate(
                        R.id.action_chooseNewEventTypeDialog_to_editBirthdayDialog,
                        bundle
                    )
                }

                R.id.radiobtnHoliday -> {
                    findNavController().navigate(
                        R.id.action_chooseNewEventTypeDialog_to_editHolidayDialog,
                        bundle
                    )
                }

                R.id.radiobtnAnotherType -> {
                    findNavController().navigate(
                        R.id.action_chooseNewEventTypeDialog_to_editSimpleEventDialog,
                        bundle
                    )
                }

                else -> {
                    Toast.makeText(
                        context,
                        getString(R.string.toast_msg_create_event_dialog),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (t: Throwable) {
            logAndToast(t)
        }
    }

    private fun logAndToast(t:Throwable) = logAndToast(t,this::class.java.toString())

    private fun logAndToast(t: Throwable, tag:String?) {
        try {
            Log.e(tag, "", t)
            Toast.makeText(requireContext().applicationContext, t.toString(), Toast.LENGTH_LONG).show()
        } catch (_: Throwable) {
        }
    }
}
