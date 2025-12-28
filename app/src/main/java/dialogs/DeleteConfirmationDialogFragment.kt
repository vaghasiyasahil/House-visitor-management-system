package com.example.housevisitormanagementsystem.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.housevisitormanagementsystem.R

class DeleteConfirmationDialogFragment(
    private val title: String,
    private val message: String,
    private val onConfirm: () -> Unit,
    private val onCancel: (() -> Unit)? = null
) : DialogFragment() {
    companion object {
        fun newInstance(
            title: String,
            message: String,
            onConfirm: () -> Unit,
            onCancel: (() -> Unit)? = null
        ): DeleteConfirmationDialogFragment {
            return DeleteConfirmationDialogFragment(title, message, onConfirm, onCancel)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_delete_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val btnCancel: Button = view.findViewById(R.id.btnCancel)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)

        tvTitle.text = title
        tvMessage.text = message

        btnCancel.setOnClickListener {
            onCancel?.invoke()
            dismiss()
        }

        btnDelete.setOnClickListener {
            onConfirm.invoke()
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}
