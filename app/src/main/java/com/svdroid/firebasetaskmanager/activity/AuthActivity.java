package com.svdroid.firebasetaskmanager.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.svdroid.firebasetaskmanager.App;
import com.svdroid.firebasetaskmanager.R;

import java.io.IOException;

public class AuthActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
	GoogleApiClient.OnConnectionFailedListener, ValueEventListener, Firebase.AuthStateListener
{
	private static final String LOG_TAG = "AuthActivity";
	public static final int RC_GOOGLE_LOGIN = 1;

	private GoogleApiClient mGoogleApiClient;
	private ConnectionResult mGoogleConnectionResult;
	private boolean mGoogleIntentInProgress;
	private boolean mGoogleLoginClicked;
	private ProgressDialog mAuthProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);
		findViewById(R.id.btn_google).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				mGoogleLoginClicked = true;
				if (!mGoogleApiClient.isConnecting()) {
					if (mGoogleConnectionResult != null) {
						resolveSignInError();
					} else if (mGoogleApiClient.isConnected()) {
						getGoogleOAuthTokenAndLogin();
					} else {
						Log.d(LOG_TAG, "Trying to connect to Google API");
						mGoogleApiClient.connect();
					}
				}
			}
		});
		mGoogleApiClient = new GoogleApiClient.Builder(this)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.addApi(Plus.API)
			.addScope(Plus.SCOPE_PLUS_LOGIN)
			.build();

		mAuthProgressDialog = new ProgressDialog(this);
		mAuthProgressDialog.setTitle("Loading");
		mAuthProgressDialog.setMessage("Authenticating with Firebase...");
		mAuthProgressDialog.setCancelable(false);
		mAuthProgressDialog.show();
		((App) getApplication()).getBaseFireBase().addAuthStateListener(this);
	}

	@Override
	protected void onDestroy()
	{
		mAuthProgressDialog.dismiss();
		super.onDestroy();
		((App) getApplication()).getBaseFireBase().removeAuthStateListener(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RC_GOOGLE_LOGIN) {
			if (resultCode != RESULT_OK) {
				mGoogleLoginClicked = false;
			}
			mGoogleIntentInProgress = false;
			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
		}
	}

	private void startActivity(AuthData authData)
	{
		if (authData != null) {
			((App) getApplication()).setUserId(authData.getUid());
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(
				Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
			);
			startActivity(intent);
		}
	}

	private void showErrorDialog(String message)
	{
		new AlertDialog.Builder(this)
			.setTitle("Error")
			.setMessage(message)
			.setPositiveButton(android.R.string.ok, null)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.show();
	}

	private class AuthResultHandler implements Firebase.AuthResultHandler
	{
		private final String provider;

		public AuthResultHandler(String provider)
		{
			this.provider = provider;
		}

		@Override
		public void onAuthenticated(AuthData authData)
		{
			mAuthProgressDialog.hide();
			Log.i(LOG_TAG, provider + " auth successful");
			startActivity(authData);
		}

		@Override
		public void onAuthenticationError(FirebaseError firebaseError)
		{
			mAuthProgressDialog.hide();
			showErrorDialog(firebaseError.toString());
		}
	}

	private void resolveSignInError()
	{
		if (mGoogleConnectionResult.hasResolution()) {
			try {
				mGoogleIntentInProgress = true;
				mGoogleConnectionResult.startResolutionForResult(this, RC_GOOGLE_LOGIN);
			} catch (IntentSender.SendIntentException e) {
				mGoogleIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}
	}

	private void getGoogleOAuthTokenAndLogin()
	{
		mAuthProgressDialog.show();
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>()
		{
			String errorMessage = null;

			@Override
			protected String doInBackground(Void... params)
			{
				String token = null;

				try {
					String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
					token = GoogleAuthUtil.getToken(
						AuthActivity.this,
						Plus.AccountApi.getAccountName(mGoogleApiClient),
						scope
					);
				} catch (IOException transientEx) {
					Log.e(LOG_TAG, "Error authenticating with Google: " + transientEx);
					errorMessage = "Network error: " + transientEx.getMessage();
				} catch (UserRecoverableAuthException e) {
					Log.w(LOG_TAG, "Recoverable Google OAuth error: " + e.toString());
					if (!mGoogleIntentInProgress) {
						mGoogleIntentInProgress = true;
						Intent recover = e.getIntent();
						startActivityForResult(recover, RC_GOOGLE_LOGIN);
					}
				} catch (GoogleAuthException authEx) {
					Log.e(LOG_TAG, "Error authenticating with Google: " + authEx.getMessage(), authEx);
					errorMessage = "Error authenticating with Google: " + authEx.getMessage();
				}
				return token;
			}

			@Override
			protected void onPostExecute(String token)
			{
				mGoogleLoginClicked = false;
				if (token != null) {
					((App) getApplication()).getBaseFireBase()
						.authWithOAuthToken("google", token, new AuthResultHandler("google"));
				} else if (errorMessage != null) {
					mAuthProgressDialog.hide();
					showErrorDialog(errorMessage);
				}
			}
		};
		task.execute();
	}

	@Override
	public void onConnected(final Bundle bundle)
	{
		getGoogleOAuthTokenAndLogin();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result)
	{
		if (!mGoogleIntentInProgress) {
			mGoogleConnectionResult = result;

			if (mGoogleLoginClicked) {
				resolveSignInError();
			} else {
				Log.e(LOG_TAG, result.toString());
			}
		}
	}

	@Override
	public void onConnectionSuspended(int i)
	{

	}

	@Override
	public void onDataChange(DataSnapshot dataSnapshot)
	{
		Log.d(LOG_TAG, dataSnapshot.toString());
	}

	@Override
	public void onCancelled(FirebaseError firebaseError)
	{

	}

	@Override
	public void onAuthStateChanged(AuthData authData)
	{
		mAuthProgressDialog.hide();
		startActivity(authData);
	}

}
