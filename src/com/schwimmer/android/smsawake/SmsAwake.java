package com.schwimmer.android.smsawake;

import java.util.HashSet;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsAwake extends BroadcastReceiver {

	private static final String TAG = "SmsAwake";
	private static final String PREF_IGNORE_TOGGLE = "ignore_toggle";
	private static final String PREF_IGNORED_NUMBERS = "ignored";
	
	@Override
	public void onReceive(final Context context, final Intent intent) {
		boolean wakeScreen = shouldAlwaysWake(context);
		
		// check if SMS number is not supposed to wake the device
		if (! wakeScreen) {
			final Set<String> phoneNumbers = getPhoneNumbers(intent);
			final Set<String> phoneNumbersToIgnore = getIgnoredNumbers(context);
			
			for (String phoneNumber : phoneNumbers) {
				if (! phoneNumbersToIgnore.contains(phoneNumber)) {
					wakeScreen = true;
					break;
				}
				else {
					Log.d(TAG, "Number " + phoneNumber + " won't wake screen.");
				}
			}
		}

		if (wakeScreen) {
			Log.d(TAG, "Waking screen.");
			final Intent i = new Intent();
			i.setClass(context, SmsAwakeService.class);
			context.startService(i);
		}
		else {
			Log.d(TAG, "SMS number doesn't wake device.");
		}
	}
	
	private Set<String> getPhoneNumbers(Intent smsIntent) {
		Set<String> phoneNumbers = new HashSet<String>();
		
		Bundle bundle = smsIntent.getExtras();
		Object[] pdus = (Object[]) bundle.get("pdus");
		if (pdus != null) {
			for (Object pdu : pdus) {
				byte[] pduBytes = (byte[]) pdu;
				SmsMessage msg = SmsMessage.createFromPdu(pduBytes);
				String from = msg.getOriginatingAddress();
				phoneNumbers.add(from);
			}
		}
		
		return phoneNumbers;
	}
	
	private Set<String> getIgnoredNumbers(final Context context) {
		final Set<String> phoneNumbers = new HashSet<String>();
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
				context);		
		
		String numbersFromPreference = prefs.getString(PREF_IGNORED_NUMBERS, null);
		if (numbersFromPreference != null) {
			for (String number : numbersFromPreference.split(",")) {
				phoneNumbers.add(number.trim());
			}
		}
		
		return phoneNumbers;
	}
	
	private boolean shouldAlwaysWake(final Context context) {
		boolean shouldWake = true;
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
				context);
		shouldWake = ! prefs.getBoolean(PREF_IGNORE_TOGGLE, false);
		return shouldWake;
	}
}
