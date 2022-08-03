package com.ds_create.worldofads.dialoghelper

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.ds_create.worldofads.MainActivity
import com.ds_create.worldofads.R
import com.ds_create.worldofads.accounthelper.AccountHelper
import com.ds_create.worldofads.databinding.SignDialogBinding

class DialogHelper(val act: MainActivity) {
    val accHelper = AccountHelper(act)

    fun createSignDialog(index: Int) {
        val builder = AlertDialog.Builder(act)
        val binding = SignDialogBinding.inflate(act.layoutInflater)
        builder.setView(binding.root)
        setDialogState(index, binding)

        val dialog = builder.create()
        binding.btSignUpIn.setOnClickListener {
            setOnClickSignUpIn(index, binding, dialog)
        }
        binding.btForgetP.setOnClickListener {
            setOnClickResetPassword(binding, dialog)
        }
        binding.btGoogleSignIn.setOnClickListener {
            accHelper.signInWithGoogle()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setOnClickResetPassword(binding: SignDialogBinding, dialog: AlertDialog?) {
        if (binding.edSignEmail.text.isNotEmpty()) {
            act.mAuth.sendPasswordResetEmail(binding.edSignEmail.text.toString())
                .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        act, R.string.email_reset_password_was_sent, Toast.LENGTH_LONG
                    ).show()
                }
            }
            dialog?.dismiss()
                } else {
                    binding.tvDialogMessage.visibility = View.VISIBLE
                }
            }


    private fun setOnClickSignUpIn(index: Int, binding: SignDialogBinding, dialog: AlertDialog?) {
        dialog?.dismiss()
        if (index == DialogConst.SIGN_UP_STATE) {
            accHelper.signUpWithEmail(
                binding.edSignEmail.text.toString(),
                binding.edSignPassword.text.toString()
            )
        } else {
            accHelper.signInWithEmail(
                binding.edSignEmail.text.toString(),
                binding.edSignPassword.text.toString()
            )
        }
    }

    private fun setDialogState(index: Int, binding: SignDialogBinding) {
        if (index == DialogConst.SIGN_UP_STATE) {
            binding.tvSignTitle.text = act.resources.getString(R.string.aс_sign_up)
            binding.btSignUpIn.text = act.resources.getString(R.string.sign_up_action)
        } else {
            binding.tvSignTitle.text = act.resources.getString(R.string.aс_sign_in)
            binding.btSignUpIn.text = act.resources.getString(R.string.sign_in_action)
            binding.btForgetP.visibility = View.VISIBLE
        }
    }
}