package com.MeadowEast.audiotest;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Email extends Activity implements OnClickListener {

	EditText personEmail, emailSubject, emailComment;
	TextView languageText;
	String subject, comment, textComment;
	String emailAddress;
	Button emailButton;
	public static String hanziMessage;
	public static File mp3Location;
	String hanziMode = "This is the Hanzi text: ";
	String englishMode = "This is the English text: ";
	public static Boolean whatMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.email);
		initializeVars();
		emailButton.setOnClickListener(this);
	}
	
	public void onClick(View v) {
		convertTheText();
		String emailAddressArray[] = { emailAddress };
		Toast.makeText(this, "the text: " + hanziMessage, Toast.LENGTH_LONG).show();
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.putExtra(Intent.EXTRA_EMAIL, emailAddressArray);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(Intent.EXTRA_TEXT, comment + "       " + textComment);
		Log.d("TAG", mp3Location+"");
		Log.d("TAG", "getting mp3");
		emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mp3Location));
		
		if (emailAddress.trim().length() == 0){
			Log.d("TAG", "null string");
			emailButton.setOnClickListener(this);
		}
		if (emailAddress.trim().length() != 0){
			Log.d("TAG", "intent starting");
			startActivity(Intent.createChooser(emailIntent, "E-Mail"));
			Log.d("TAG", "intent done");
		}
	}
	
	private void initializeVars() {
		emailButton = (Button) findViewById(R.id.sendMail);
		personEmail = (EditText) findViewById(R.id.emailText);
		emailSubject = (EditText) findViewById(R.id.subjectText);
		emailComment = (EditText) findViewById(R.id.commentText);
		
	}
	
	private void convertTheText() {
		emailAddress = personEmail.getText().toString();
		subject = emailSubject.getText().toString();
		comment = emailComment.getText().toString();
	}
	
	protected void onPause() {
		super.onPause();
		finish();
	}
	
}
