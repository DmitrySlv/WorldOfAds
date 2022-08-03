package com.ds_create.worldofads.accounthelper

import android.util.Log
import android.widget.Toast
import com.ds_create.worldofads.MainActivity
import com.ds_create.worldofads.R
import com.ds_create.worldofads.constants.FirebaseAuthConstants
import com.ds_create.worldofads.dialoghelper.GoogleAccConst
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import java.lang.Exception

class AccountHelper(private val act: MainActivity) {
    private lateinit var signInClient: GoogleSignInClient

    fun signUpWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.currentUser?.delete()?.addOnCompleteListener {
                task ->
                if (task.isSuccessful) {
                    act.mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                            task ->
                        if (task.isSuccessful) {
                            signUpWithEmailSuccessful(task.result.user!!)
                        } else {
                            signUpWithEmailExceptions(task.exception!!, email, password)
                        }
                    }
                }
            }
        }
    }

    private fun signUpWithEmailSuccessful(user: FirebaseUser) {
        sendEmailVerification(user)
        act.uiUpdate(user)
    }

    private fun signUpWithEmailExceptions(e: Exception, email: String, password: String) {
//                    Toast.makeText(act, act.resources.getString(R.string.sign_up_error), Toast.LENGTH_LONG).show()
//                    Log.d("MyLog", "Exception: ${task.exception}")
//                    Log.d("MyLog", "Exception: ${exception.errorCode}")
            if (e is FirebaseAuthUserCollisionException) {
                if (e.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                    Toast.makeText(
                        act,
                        FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE,
                        Toast.LENGTH_LONG
                    ).show()
                    linkEmailToGoogle(email, password)
                }
            } else if (e is FirebaseAuthInvalidCredentialsException) {
                if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                    Toast.makeText(
                        act,
                        FirebaseAuthConstants.ERROR_INVALID_EMAIL,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            if (e is FirebaseAuthWeakPasswordException) {
                if (e.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                    Toast.makeText(
                        act,
                        FirebaseAuthConstants.ERROR_WEAK_PASSWORD,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.currentUser?.delete()?.addOnCompleteListener {
                task ->
                if (task.isSuccessful) {
                    act.mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            act.uiUpdate(task.result?.user)
                            Toast.makeText(act, act.getString(R.string.sign_verification_email_done),
                                Toast.LENGTH_LONG).show()
                        } else {
                            signInEmailExceptions(task.exception!!, email, password)
                        }
                    }
                }
            }
        }
    }

    private fun signInEmailExceptions(e: Exception, email: String, password: String) {
        Log.d("MyLog", "Exception: ${e}")
        if (e is FirebaseAuthInvalidCredentialsException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                Toast.makeText(
                    act, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG
                ).show()
            }
        }
        if (e is FirebaseAuthInvalidCredentialsException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) {
                Toast.makeText(
                    act, FirebaseAuthConstants.ERROR_WRONG_PASSWORD, Toast.LENGTH_LONG
                ).show()
            }
        }
        if (e is FirebaseAuthInvalidUserException) {
//                        Log.d("MyLog", "Exception: ${exception.errorCode}")
            if (e.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                Toast.makeText(
                    act, FirebaseAuthConstants.ERROR_USER_NOT_FOUND,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun linkEmailToGoogle(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        if (act.mAuth.currentUser != null) {
            act.mAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener {task->
                if (task.isSuccessful) {
                    Toast.makeText(act, act.resources.getString(R.string.link_done),
                        Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(act, act.resources.getString(R.string.enter_to_google),
                Toast.LENGTH_LONG).show()
        }
    }

    private fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(act.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(act, gso)
    }

    fun signInWithGoogle() {
        signInClient = getSignInClient()
        val intent = signInClient.signInIntent
        act.googleSignInLauncher.launch(intent)
    }

    fun signOutGoogle() {
        getSignInClient().signOut()
    }

    fun signInFirebaseWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                act.mAuth.signInWithCredential(credential).addOnCompleteListener { task2 ->
                    if (task2.isSuccessful) {
                        Toast.makeText(act, "Sign In Done", Toast.LENGTH_LONG).show()
                        act.uiUpdate(task2.result?.user)
                    } else {
                        Toast.makeText(
                            act,
                            "Google Sign In Exception: ${task2.exception}", Toast.LENGTH_LONG)
                            .show()
//                        Log.d("MyLog", "Google Sign In Exception: ${task2.exception}")
                    }
                }
            }
        }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener {task->
            if (task.isSuccessful) {
                Toast.makeText(
                    act, act.resources.getString(R.string.send_verification_done), Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    act, act.resources.getString(R.string.send_verification_error), Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    fun signInAnonymously(listener: CompleteListener) {
        act.mAuth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                listener.onComplete()
                Toast.makeText(act, "Вы вошли как Гость", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(act, "Не удалось войти как Гость", Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface CompleteListener {
        fun onComplete()
    }
}