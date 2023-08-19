package ru.veider.eventsreminder.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

private const val TAG = "NativeGoogleLoginHelpers"

fun createGoogleBeginSignInRequest(clientId: String): BeginSignInRequest =
	BeginSignInRequest.builder()
		.setGoogleIdTokenRequestOptions(
			BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
				.setSupported(true)
				.setServerClientId(clientId)
				.setFilterByAuthorizedAccounts(true)
				.build()
		)
		.build()

@ExperimentalCoroutinesApi
fun getGoogleActivityResultLauncher(
	fragment: Fragment,
	fragmentActivity: FragmentActivity,
	oneTapClient: SignInClient,
	clientId: String,
	googleLegacyResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
	onToken: (String) -> Unit
): ActivityResultLauncher<IntentSenderRequest> =
	fragment.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
		try {
			val credential: SignInCredential =
				oneTapClient.getSignInCredentialFromIntent(activityResult.data)
			val token = credential.googleIdToken
			if (token != null) {
				onToken(token)
			}
		} catch (e: ApiException) {
			Log.e(fragment::class.java.toString(), e.toString(), e)
			val lastAccountToken = GoogleSignIn
				.getLastSignedInAccount(fragmentActivity)
				?.idToken
			if (lastAccountToken == null) {
				fragmentActivity.lifecycleScope.launchWhenStarted {
					val pendingIntent = beginSignInGoogleLegacy(
						fragmentActivity,
						clientId
					)
					val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender)
						.build()
					googleLegacyResultLauncher.launch(intentSenderRequest)
				}
			} else {
				fragmentActivity.lifecycleScope.launchWhenStarted {
					onToken(lastAccountToken)
				}
			}
		}
	}

fun getLegacyGoogleActivitySignInResultLauncher(
	fragment: Fragment,
	fragmentActivity: FragmentActivity,
	onIdToken: (String) -> Unit
): ActivityResultLauncher<IntentSenderRequest> =
	fragment.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
		try {
			val token: String? = Identity.getSignInClient(fragmentActivity)
				.getSignInCredentialFromIntent(activityResult.data)?.googleIdToken
			if (token != null) {
				onIdToken(token)
				Log.d(TAG, "getLegacyGoogleActivitySignInResultLauncher: $token")
			}
		} catch (e: Exception) {
			Log.e(fragment::class.java.toString(), e.toString(), e)
		}
	}

@ExperimentalCoroutinesApi
suspend fun beginSignInGoogleLegacy(
	fragmentActivity: FragmentActivity,
	clientId: String,
): PendingIntent =
	suspendCancellableCoroutine { continuation ->
		val request: GetSignInIntentRequest = GetSignInIntentRequest.builder()
			.setServerClientId(clientId)
			.build()

		Identity.getSignInClient(fragmentActivity)
			.getSignInIntent(request)
			.addOnSuccessListener { pendingIntent ->
				continuation.resume(pendingIntent) { throwable ->
					Log.e(TAG, "beginSignInGoogleLegacy: ", throwable )
				}
			}
			.addOnFailureListener { exception ->
				Log.e(TAG, "beginSignInGoogleLegacy", exception)
				continuation.resumeWithException(exception)
			}
			.addOnCanceledListener {
				Log.d(TAG, "beginSignInGoogleLegacy: cancelled")
				continuation.cancel()
			}
	}

// InvalidFragmentVersionForActivityResult: https://issuetracker.google.com/issues/182388985
@SuppressLint("InvalidFragmentVersionForActivityResult")
@ExperimentalCoroutinesApi
fun setupGoogleContinueButton(
	fragment: Fragment,
	fragmentActivity: FragmentActivity,
	oneTapClient: SignInClient,
	clientId: String,
	signInRequest: BeginSignInRequest,
	buttonContinueGoogle: View,
	onIdToken: suspend (String) -> Unit
) {
	// Must be registered when Fragment is created
	val legacyGoogleSignInLauncher: ActivityResultLauncher<IntentSenderRequest> =
		getLegacyGoogleActivitySignInResultLauncher(
			fragment,
			fragmentActivity
		) { token ->
			fragmentActivity.lifecycleScope.launchWhenStarted {
				onIdToken(token)
			}
		}

	// Must be registered when Fragment is created
	val oneTapLauncher: ActivityResultLauncher<IntentSenderRequest> =
		getGoogleActivityResultLauncher(
			fragment,
			fragmentActivity,
			oneTapClient,
			clientId,
			legacyGoogleSignInLauncher
		) { token ->
			fragmentActivity.lifecycleScope.launchWhenStarted {
				onIdToken(token)
			}
		}

	buttonContinueGoogle.setOnClickListener {
		fragmentActivity.lifecycleScope.launchWhenStarted {
			try {
				// First, try Google One Tap
				val result = beginSignInGoogleOneTap(
					fragmentActivity,
					oneTapClient,
					signInRequest,
					onCancel = {
						val lastAccountToken = GoogleSignIn
							.getLastSignedInAccount(fragmentActivity)
							?.idToken
						// If One Tap has been cancelled, and there's no last signed in account,
						// then you cannot use One Tap, and it will be cancelled.
						// In this case, we fall back to the Legacy Google Auth API.
						if (lastAccountToken == null) {
							fragmentActivity.lifecycleScope.launchWhenStarted {
								val pendingIntent = beginSignInGoogleLegacy(
									fragmentActivity,
									clientId
								)
								val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender)
									.build()
								legacyGoogleSignInLauncher.launch(intentSenderRequest)
							}
						} else {
							// If the One Tap flow has been cancelled,
							// and we found the last signed in Google account,
							// use that token to auth.
							// Have not been able to reach this state.
							fragmentActivity.lifecycleScope.launchWhenStarted {
								onIdToken(lastAccountToken)
							}
						}
					}
				)
				oneTapLauncher.launch(
					IntentSenderRequest.Builder(result.pendingIntent.intentSender)
						.build()
				)
			} catch (e: ApiException) {
				val lastAccountToken = GoogleSignIn
					.getLastSignedInAccount(fragmentActivity)
					?.idToken
				if (lastAccountToken == null) {
					val pendingIntent = beginSignInGoogleLegacy(
						fragmentActivity,
						clientId
					)
					val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender)
						.build()
					legacyGoogleSignInLauncher.launch(intentSenderRequest)
				} else {
					fragmentActivity.lifecycleScope.launchWhenStarted {
						onIdToken(lastAccountToken)
					}
				}
			}
		}
	}
}


@ExperimentalCoroutinesApi
suspend fun beginSignInGoogleOneTap(
	fragmentActivity: FragmentActivity,
	oneTapClient: SignInClient,
	signInRequest: BeginSignInRequest,
	onCancel: () -> Unit
): BeginSignInResult =
	suspendCancellableCoroutine { continuation ->
		oneTapClient.beginSignIn(signInRequest)
			.addOnSuccessListener(fragmentActivity) { result ->
				continuation.resume(result) { throwable ->
					Log.e("SignUp UI", "beginSignInGoogleOneTap: ", throwable)
				}
			}
			.addOnFailureListener(fragmentActivity) { e ->
				// No Google Accounts found. Just continue presenting the signed-out UI.
				continuation.resumeWithException(e)
			}
			.addOnCanceledListener {
				Log.d("SignUp UI", "beginSignInGoogleOneTap: cancelled")
				onCancel()
				continuation.cancel()
			}
	}