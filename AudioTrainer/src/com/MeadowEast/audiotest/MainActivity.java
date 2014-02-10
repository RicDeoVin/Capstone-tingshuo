package com.MeadowEast.audiotest; 

import java.io.BufferedReader; 
import java.io.File; 
import java.io.FileReader; 
import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.Map; 
import java.util.Random; 

import android.app.Activity; 
import android.app.AlertDialog; 
import android.content.ActivityNotFoundException; 
import android.content.Context; 
import android.content.DialogInterface; 
import android.content.Intent; 
import android.content.SharedPreferences; 
import android.gesture.Gesture; 
import android.gesture.GestureLibraries; 
import android.gesture.GestureLibrary; 
import android.gesture.GestureOverlayView; 
import android.gesture.GestureOverlayView.OnGesturePerformedListener; 
import android.gesture.Prediction; 
import android.media.AudioManager; 
import android.media.MediaPlayer; 
import android.net.ConnectivityManager; 
import android.net.NetworkInfo; 
import android.net.Uri; 
import android.os.Bundle; 
import android.os.Environment; 
import android.os.Handler; 
import android.util.Log; 
import android.view.KeyEvent; 
import android.view.Menu; 
import android.view.MenuInflater; 
import android.view.MenuItem; 
import android.view.View; 
import android.widget.TextView; 
import android.widget.Toast; 

public class MainActivity extends Activity implements 
       OnGesturePerformedListener { 
   public static final String PREFRENCES_FILE = "AudioTrainerPrefrences"; 

   final String mainAddress = "http://chinesedictionary.mobi/?handler=QueryWorddict&mwdqb="; 
   String chineseSampleTest = ""; 
   String currentLanguage = ""; 
   private MediaPlayer mp; 
   private String[] cliplist; 
   private File[] recentClips = new File[10]; 
   private int recentClipsSize = 0; 
   private int totalMenuSize = 0; 
   private String[] recentClipText = new String[10]; 
   private boolean historicalChanges = false; 
   private File sample; 
   private File mainDir; 
   private File clipDir; 
   private Random rnd; 
   private Handler clockHandler; 
   private Runnable updateTimeTask; 
   private boolean clockRunning; 
   private boolean clockWasRunning; 
   private boolean nightModeOn; 
   private boolean currLangIsMan; 
   private Long elapsedMillis; 
   private Long start; 
   private Map<String, String> hanzi; 
   private Map<String, String> instructions; 
   private String key; 
   static final String TAG = "CAT"; 
   private GestureLibrary gestureLib; 
   private static final String GESTURE_TAG = "complex gestures"; 
   private ListOfRemainingClips temporaryClipList; 
   private ListOfRemainingClips secondaryClipList; 
   //List<String> test = new ArrayList<String>(); 
   
   Weighted p; 
   private int repeatCount = 0; 
   Integer index = 0; 
   private int totalClipsPlayed = 0; 
   
   
   // Email class allows for clips to be emailed 
   Email emailClass = new Email(); 

   /* 
    * Attempt at another menu 1 February 2014 RR this menu is created when the 
    * menu button is selected for the first time 
    */ 
   @Override 
   public boolean onCreateOptionsMenu(Menu menu) { 
       // menu.add(1, 1, 1, "made on start up"); 
       // menu.findItem(R.id.display_history).getSubMenu().addSubMenu(1, 11, 1, 
       // "Orange"); 
       MenuInflater inflater = getMenuInflater(); 
       inflater.inflate(R.menu.fragment_options_list, menu); 
       return true; 
   } 

   /* 
    * Attempt on rebuilding the menu when needed 8February 2014 RR rebuilds the 
    * menu every time a user presses menu button(except first time) 
    */ 
   @Override 
   public boolean onPrepareOptionsMenu(Menu menu) { 
       Log.d("MEN", recentClipsSize + " num"); 
       // Menu temporaryMenu = null; 
       // menu.findItem(R.id.clipUno).setTitle("Apple"); 
       // menu.findItem(R.id.display_history).getSubMenu().addSubMenu(1, 11, 1, 
       // "Orange"); 
       Log.d(TAG, "did the name change"); 

       if (historicalChanges == true && recentClipsSize > 0 
               && recentClipsSize > totalMenuSize) { 
           historicalChanges = false; 
           for (int i = totalMenuSize; i < recentClipsSize; i++) {// .assSubMenu(GroupID,MenuID,OrderNum,MenuText) 
               menu.findItem(R.id.display_history) 
                       .getSubMenu() 
                       .addSubMenu(1, i + 10, recentClipsSize, 
                               recentClipText[i]); 
               Log.d(TAG, "loop"); 
           } 
           totalMenuSize = recentClipsSize; 
       } else if (historicalChanges == true && recentClipsSize > 9) { 
           historicalChanges = false; 
           for (int i = 0; i < recentClipsSize; i++) { 
               menu.findItem(i + 10).setTitle(recentClipText[i]); 
           } 
       } 
       return super.onPrepareOptionsMenu(menu); 
   } 

   /* 
    * Adding nightMode Toggle to Menu 1 February 2014 RR This is the main menu 
    * tapped menu creates an ID case 
    */ 
   @Override 
   public boolean onOptionsItemSelected(MenuItem item) { 
       Log.d(TAG, item.getItemId() + " the menu option passed"); 
       // Handle item selection 
       switch (item.getItemId()) { 
       case R.id.new_game: 
           toggleNightMode(); 
           return true; 
       case R.id.help: 
           showHelp(); 
           return true; 
       case 10: 
           playClips(10); 
           return true; 
       case 11: 
           playClips(11); 
           return true; 
       case 12: 
           playClips(12); 
           return true; 
       case 13: 
           playClips(13); 
           return true; 
       case 14: 
           playClips(14); 
           return true; 
       case 15: 
           playClips(15); 
           return true; 
       case 16: 
           playClips(16); 
           return true; 
       case 17: 
           playClips(17); 
           return true; 
       case 18: 
           playClips(18); 
           return true; 
       case 19: 
           playClips(19); 
           return true; 
       case R.id.email_friend: 
           emailAFriend(); 
           return true; 
       case R.id.mandarinMode: 
           changeLanguageMode("Mandarin"); 
           key = null; 
           return true; 
       case R.id.englishMode: 
           changeLanguageMode("English"); 
           key = null; 
           return true; 
       case R.id.show_use: 
           showUseStatistics(); 
           return true; 
       case R.id.getTranslation: 
           if (isNetworkAvailable() == true) { 
               if (key == null) { 
                   Toast.makeText(this, "Nothing to translate!", 
                           Toast.LENGTH_LONG).show(); 
               } else { 
                   String translationText = hanzi.get(key); 
                   String webTranslationURL = mainAddress + translationText; 
                   try { 
                       Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                               Uri.parse(webTranslationURL)); 
                       startActivity(browserIntent); 
                   } catch (ActivityNotFoundException e) { 

                   } 
               } 
           } else { 
               Toast.makeText(this, "ERROR: No network connectivity found", 
                       Toast.LENGTH_LONG).show(); 
           } 
           return true; 
       default: 
           return super.onOptionsItemSelected(item); 
       } 
   } 

   /* 
    * RR - Play clips from previous 10 clips played These should be triggered 
    * only from the menu options 
    */ 
   public void playClips(int clipNum) { 
       switch (clipNum) { 
       case 10: 
           if (sample != null) { 
               setHanzi(""); 
           } 
           sample = recentClips[0]; 
           key = recentClips[0].getName(); 
           Log.d(TAG, "key before substring" + key); 
           key = key.substring(0, key.length() - 4); 
           Log.d(TAG, "key after substring" + key); 
           break; 
       case 11: 
           if (sample != null) { 
               setHanzi(""); 
           } 
           sample = recentClips[1]; 
           key = recentClips[1].getName(); 
           Log.d(TAG, "key before substring" + key); 
           key = key.substring(0, key.length() - 4); 
           Log.d(TAG, "key after substring" + key); 
           break; 
       case 12: 
           if (sample != null) { 
               setHanzi(""); 
           } 
           sample = recentClips[2]; 
           key = recentClips[2].getName(); 
           Log.d(TAG, "key before substring" + key); 
           key = key.substring(0, key.length() - 4); 
           Log.d(TAG, "key after substring" + key); 
           break; 
       case 13: 
           if (sample != null) { 
               setHanzi(""); 
           } 
           sample = recentClips[3]; 
           key = recentClips[3].getName(); 
           Log.d(TAG, "key before substring" + key); 
           key = key.substring(0, key.length() - 4); 
           Log.d(TAG, "key after substring" + key); 
           break; 
       case 14: 
           if (sample != null) { 
               setHanzi(""); 
           } 
           sample = recentClips[4]; 
           key = recentClips[4].getName(); 
           Log.d(TAG, "key before substring" + key); 
           key = key.substring(0, key.length() - 4); 
           Log.d(TAG, "key after substring" + key); 
           break; 
       case 15: 
           if (sample != null) { 
               setHanzi(""); 
           } 
           sample = recentClips[5]; 
           key = recentClips[5].getName(); 
           Log.d(TAG, "key before substring" + key); 
           key = key.substring(0, key.length() - 4); 
           Log.d(TAG, "key after substring" + key); 
           break; 
       case 16: 
           if (sample != null) { 
               setHanzi(""); 
           } 
           sample = recentClips[6]; 
           key = recentClips[6].getName(); 
           Log.d(TAG, "key before substring" + key); 
           key = key.substring(0, key.length() - 4); 
           Log.d(TAG, "key after substring" + key); 
           break; 
       case 17: 
           if (sample != null) { 
               setHanzi(""); 
           } 
           sample = recentClips[7]; 
           key = recentClips[7].getName(); 
           Log.d(TAG, "key before substring" + key); 
           key = key.substring(0, key.length() - 4); 
           Log.d(TAG, "key after substring" + key); 
           break; 
       case 18: 
           if (sample != null) { 
               setHanzi(""); 
           } 
           sample = recentClips[8]; 
           key = recentClips[8].getName(); 
           Log.d(TAG, "key before substring" + key); 
           key = key.substring(0, key.length() - 4); 
           Log.d(TAG, "key after substring" + key); 
           break; 
       case 19: 
           if (sample != null) { 
               setHanzi(""); 
           } 
           sample = recentClips[9]; 
           key = recentClips[9].getName(); 
           Log.d(TAG, "key before substring" + key); 
           key = key.substring(0, key.length() - 4); 
           Log.d(TAG, "key after substring" + key); 
           break; 
       } 
   } 

   /* 
    * This function email a person a copy of the current transcription and 
    * curent mp3 clip, allows for comments 
    */ 
   public void emailAFriend() { 
       String className = "Email"; 
       Email.hanziMessage = hanzi.get(key); 
       Email.whatMode = currLangIsMan; 
       Toast.makeText(this, "the mess: " + hanzi.get(key), Toast.LENGTH_LONG) 
               .show(); 
       try { 
           @SuppressWarnings("rawtypes") 
           Class ourClasses; 
           ourClasses = Class.forName("com.MeadowEast.audiotest." + className); 
           Intent ourIntent = new Intent(this, ourClasses); 
           startActivity(ourIntent); 

       } catch (ClassNotFoundException e) { 
           e.printStackTrace(); 
       } 
   } 

   // toggle between learn English and Mandarin 
   public void changeLanguageMode(String lang) { 
       Log.d("TAG", "chngln_lang: " + lang); 
       if (lang.equals("Mandarin")) { 
           currentLanguage = lang; 
           Log.d("TAG", "man_lang: " + lang); 
           SharedPreferences sharedObject = getSharedPreferences( 
                   "com.MeadowEast.audiotest", Context.MODE_PRIVATE); 
           SharedPreferences.Editor sharedObjectEditor = sharedObject.edit(); 
           sharedObjectEditor.putString("DefaultLanguage", currentLanguage); 
           sharedObjectEditor.commit(); 
           Log.d("TAG", "man_com_lang: " + lang); 
           currLangIsMan = true; 
           File sdCard = Environment.getExternalStorageDirectory(); 
           mainDir = new File(sdCard.getAbsolutePath() 
                   + "/Android/data/com.MeadowEast.audiotest/files"); 
           clipDir = new File(mainDir, "clips"); 
           //int i = clipDir.list().length; 
           //test.addAll((clipDir.list()).asList()); 
           
           String[]j = clipDir.list(); 
           Log.d(TAG, "clipDir = " + j.length); 
           cliplist = new String[50]; 
           secondaryClipList = new ListOfRemainingClips(50); 
           
           for (int i = 0; i < j.length; i++ ){ 
               if (i < 50) { 
                   cliplist[i] = (clipDir.list())[i]; 
                   Log.d(GESTURE_TAG, "cliplist[" + i + "]: " + cliplist[i]); 
               } 
               else { 
                   secondaryClipList.setStringInArr(clipDir.list()[i], (i % 50)); 
                   Log.d(GESTURE_TAG, "secondaryClipList[" + i%50 + "]: " + secondaryClipList.getStringInArr(i%50)); 
               } 
               
           } 
           
           
           //cliplist = clipDir.list(); 
           //test.addAll(cliplist.asList()); 
           readClipInfo(); 
           rnd = new Random(); 

           int cl = cliplist.length; 
           Log.d(GESTURE_TAG, "length of cliplist.length: " + cl); 
           p = new Weighted(cliplist.length); 
           
           setHanzi(""); 

           // above code points back to Mandarin files 
           /* 
            * Button mandarinPlay = (Button) findViewById(R.id.playButton); 
            * mandarinPlay.setText(getString(R.string.playButtonLabel)); 
            * mandarinPlay.setTextSize(70); Button mandarinHanzi = (Button) 
            * findViewById(R.id.hanziButton); 
            * mandarinHanzi.setText(getString(R.string.hanziButtonLabel)); 
            * mandarinHanzi.setTextSize(70); Button mandarinRepeat = (Button) 
            * findViewById(R.id.repeatButton); 
            * mandarinRepeat.setText(getString(R.string.repeatButtonLabel)); 
            * mandarinRepeat.setTextSize(70); Button mandarinPause = (Button) 
            * findViewById(R.id.pauseButton); 
            * mandarinPause.setText(getString(R.string.pauseButtonLabel)); 
            * mandarinPause.setTextSize(70); 
            */ 
       } else if (lang.equals("English")) { 
           Log.d("TAG", "eng_lang: " + lang); 
           currentLanguage = lang; 
           SharedPreferences sharedObject = getSharedPreferences( 
                   "com.MeadowEast.audiotest", Context.MODE_PRIVATE); 
           SharedPreferences.Editor sharedObjectEditor = sharedObject.edit(); 
           sharedObjectEditor.putString("DefaultLanguage", currentLanguage); 
           sharedObjectEditor.commit(); 
           Log.d("TAG", "eng_com_lang: " + lang); 
           currLangIsMan = false; 
           Log.d("TAG", "set false"); 
           File sdCard = Environment.getExternalStorageDirectory(); 
           Log.d("TAG", "get directory"); 
           mainDir = new File(sdCard.getAbsolutePath() 
                   + "/Android/data/com.MeadowEast.audiotest/files-eng"); 
           Log.d("TAG", "mainDir set"); 
           clipDir = new File(mainDir, "clips"); 
           Log.d("TAG", "clip dir set"); 
           cliplist = clipDir.list(); 
           readClipInfo(); 
           rnd = new Random(); 
           
           int cl = cliplist.length; 
           Log.d(GESTURE_TAG, "length of cliplist.length: " + cl); 
           p = new Weighted(cliplist.length); 
           
           Log.d("TAG", "rand gen"); 
           setHanzi(""); 
           Log.d("TAG", "hanzi set"); 
           // Above code points to English files 
           /* 
            * Button englishPlay = (Button) findViewById(R.id.playButton); 
            * englishPlay.setText(getString(R.string.eng_Play)); 
            * englishPlay.setTextSize(12); Button englishHanzi = (Button) 
            * findViewById(R.id.hanziButton); 
            * englishHanzi.setText(getString(R.string.eng_ViewText)); 
            * englishHanzi.setTextSize(12); Button englishRepeat = (Button) 
            * findViewById(R.id.repeatButton); 
            * englishRepeat.setText(getString(R.string.eng_Repeat)); 
            * englishRepeat.setTextSize(12); Button englishPause = (Button) 
            * findViewById(R.id.pauseButton); 
            * englishPause.setText(getString(R.string.eng_Pause)); 
            * englishPause.setTextSize(12); 
            */ 
       } else { 
           Log.d("TAG", "No suitable language selected"); 
       } 
       Log.d("TAG", "exiting changlang"); 
       totalClipsPlayed = 0; 
   } 

   public void showUseStatistics() { 
       // print statics relating to app useage 
   } 

   /* 
    * public String showUseStatistics(String mandarinText) throws IOException { 
    * // Concatenate the translation POST Url to have the hanzi String 
    * webTranslator = 
    * "http://chinesedictionary.mobi/?handler=QueryWorddict&mwdqb=" + 
    * mandarinText; // show current app useage stats Log.v(TAG, webTranslator); 
    * // log what you emailed the server URL url = new URL(webTranslator); 
    * HttpURLConnection urlConnection = (HttpURLConnection) url 
    * .openConnection(); try { InputStream in = new BufferedInputStream( 
    * urlConnection.getInputStream()); String msgFromWeb = readStream(in); 
    * Log.v(TAG, msgFromWeb); return msgFromWeb; } finally { 
    * urlConnection.disconnect(); } } 
    */ 

   // print information about how to use the app 
   public void showHelp() { 
       // maybe display touch control gestures 
   } 

   /* 
    * RR - Toggle the app screen between Night mode and Day mode button changes 
    * where commented out since they are no longer used 
    */ 
   public void toggleNightMode() { 
       if (nightModeOn == false) { 
           nightModeOn = true; 
           /* 
            * findViewById(R.id.LinearLayout1).setBackgroundColor( 
            * getResources().getColor(R.color.black)); 
            * findViewById(R.id.playButton).setBackgroundColor( 
            * getResources().getColor(R.color.darkGray)); ((TextView) 
            * findViewById(R.id.playButton)) 
            * .setTextColor(getResources().getColor(R.color.mediumGray)); 
            * findViewById(R.id.hanziButton).setBackgroundColor( 
            * getResources().getColor(R.color.darkGray)); ((TextView) 
            * findViewById(R.id.hanziButton)) 
            * .setTextColor(getResources().getColor(R.color.mediumGray)); 
            * findViewById(R.id.repeatButton).setBackgroundColor( 
            * getResources().getColor(R.color.darkGray)); ((TextView) 
            * findViewById(R.id.repeatButton)) 
            * .setTextColor(getResources().getColor(R.color.mediumGray)); 
            * findViewById(R.id.pauseButton).setBackgroundColor( 
            * getResources().getColor(R.color.darkGray)); ((TextView) 
            * findViewById(R.id.pauseButton)) 
            * .setTextColor(getResources().getColor(R.color.mediumGray)); 
            */ 
           ((TextView) findViewById(R.id.timerTextView)) 
                   .setTextColor(getResources().getColor(R.color.mediumGray)); 
           ((TextView) findViewById(R.id.hanziTextView)) 
                   .setTextColor(getResources().getColor(R.color.mediumGray)); 
           ((TextView) findViewById(R.id.instructionTextView)) 
                   .setTextColor(getResources().getColor(R.color.mediumGray)); 
       } else { 
           nightModeOn = false; 
           findViewById(R.id.LinearLayout1).setBackgroundColor( 
                   getResources().getColor(R.color.white)); 
           /* 
            * findViewById(R.id.playButton).setBackgroundColor( 
            * getResources().getColor(R.color.lightGray)); ((TextView) 
            * findViewById(R.id.playButton)) 
            * .setTextColor(getResources().getColor(R.color.black)); 
            * findViewById(R.id.hanziButton).setBackgroundColor( 
            * getResources().getColor(R.color.lightGray)); ((TextView) 
            * findViewById(R.id.hanziButton)) 
            * .setTextColor(getResources().getColor(R.color.black)); 
            * findViewById(R.id.repeatButton).setBackgroundColor( 
            * getResources().getColor(R.color.lightGray)); ((TextView) 
            * findViewById(R.id.repeatButton)) 
            * .setTextColor(getResources().getColor(R.color.black)); 
            * findViewById(R.id.pauseButton).setBackgroundColor( 
            * getResources().getColor(R.color.lightGray)); ((TextView) 
            * findViewById(R.id.pauseButton)) 
            * .setTextColor(getResources().getColor(R.color.black)); 
            */ 
           ((TextView) findViewById(R.id.timerTextView)) 
                   .setTextColor(getResources().getColor(R.color.black)); 
           ((TextView) findViewById(R.id.hanziTextView)) 
                   .setTextColor(getResources().getColor(R.color.black)); 
           ((TextView) findViewById(R.id.instructionTextView)) 
                   .setTextColor(getResources().getColor(R.color.black)); 
       } 
   } 

   /* 
    * Original - Create 2 Hash map that saves the transcriptions and 
    * instructions and uses the clip number as a key 
    */ 
   private void readClipInfo() { 
       hanzi = new HashMap<String, String>(); 
       instructions = new HashMap<String, String>(); 
       File file = new File(mainDir, "clipinfo.txt"); 
       Log.d(TAG, "before"); 
       Log.d(TAG, "after"); 
       try { 
           FileReader fr = new FileReader(file); 
           BufferedReader in = new BufferedReader(fr); 
           String line; 
           while ((line = in.readLine()) != null) { 
               String fixedline = new String(line.getBytes(), "utf-8"); 
               String[] fields = fixedline.split("\\t"); 
               if (fields.length == 3) { 
                   hanzi.put(fields[0], fields[1]); 
                   instructions.put(fields[0], fields[2]); 
               } else { 
                   Log.d(TAG, "Bad line: " + fields.length + " elements"); 
                   Log.d(TAG, fixedline); 
               } 
           } 
           in.close(); 
       } catch (Exception e) { 
           Log.d(TAG, "Problem reading clipinfo"); 
       } 
   } 

   /* 
    * Original - retrieve instructions from the hash map using the clip number 
    * as a key. set the instructions in instruction view 
    */ 
   private String getInstruction(String key) { 
       String instructionCodes = instructions.get(key); 
       int n = instructionCodes.length(); 
       if (n == 0) { 
           return "No instruction codes for " + key; 
       } 
       int index = rnd.nextInt(n); 
       switch (instructionCodes.charAt(index)) { 
       case 'C': 
           return "continue the conversation"; 
       case 'A': 
           return "answer the question"; 
       case 'R': 
           return "repeat"; 
       case 'P': 
           return "paraphrase"; 
       case 'Q': 
           return "ask questions"; 
       case 'V': 
           return "create variations"; 
       default: 
           return "Bad instruction code " + instructionCodes.charAt(index) 
                   + " for " + key; 
       } 
   } 

   /* Original - toggle the clock between running and standing still */ 
   private void toggleClock() { 
       if (clockRunning) { 
           elapsedMillis += System.currentTimeMillis() - start; 
           setHanzi(""); 
       } else 
           start = System.currentTimeMillis(); 
       clockRunning = !clockRunning; 
       clockHandler.removeCallbacks(updateTimeTask); 
       if (clockRunning) 
           clockHandler.postDelayed(updateTimeTask, 200); 
   } 

   /* Original - does the math to display correct time on the timer */ 
   private void showTime(Long totalMillis) { 
       int seconds = (int) (totalMillis / 1000); 
       int minutes = seconds / 60; 
       seconds = seconds % 60; 
       TextView t = (TextView) findViewById(R.id.timerTextView); 
       if (seconds < 10) 
           t.setText("" + minutes + ":0" + seconds); 
       else 
           t.setText("" + minutes + ":" + seconds); 
   } 

   /* Original - get system time */ 
   private void createUpdateTimeTask() { 
       updateTimeTask = new Runnable() { 
           public void run() { 
               Long totalMillis = elapsedMillis + System.currentTimeMillis() 
                       - start; 
               showTime(totalMillis); 
               clockHandler.postDelayed(this, 1000); 
           } 
       }; 
   } 

   /* Original - place the string on Hanzi view */ 
   private void setHanzi(String s) { 
       TextView t = (TextView) findViewById(R.id.hanziTextView); 
       t.setText(s); 
   } 

   /* 
    * Modified - This is run on startup, it has been edited to work with 
    * enhancements 
    */ 
   @Override 
   public void onCreate(Bundle savedInstanceState) { 
       super.onCreate(savedInstanceState); 

       // get shared preferences settings for this app, such as current 
       // Language 
       SharedPreferences sharedObject = getSharedPreferences( 
               "com.MeadowEast.audiotest", Context.MODE_PRIVATE); 
       // prompt the user for a default language the first time the app starts 
       if (!sharedObject.contains("DefaultLanguage")) { 
           Toast.makeText(MainActivity.this, "NO default Language set", 
                   Toast.LENGTH_SHORT).show(); 
           setLanguageOnstartup(); 
           SharedPreferences.Editor sharedObjectEditor = sharedObject.edit(); 
           sharedObjectEditor.putString("DefaultLanguage", currentLanguage); 
           sharedObjectEditor.commit(); 
       } 
       // retrieve the default language 
       currentLanguage = sharedObject.getString("DefaultLanguage", "NONE"); 
       nightModeOn = false; 
       Log.d(TAG, "testing only"); 
       // File filesDir = getFilesDir(); // Use on virtual device 

       /* 
        * RR - use preferences to determine what audio file should be loaded 
        * WARNING: if user backs out of initial prompt this app might crash 
        */ 
       if (currentLanguage.equalsIgnoreCase("English")) { 
           Log.d(TAG, "English"); 
           File sdCard = Environment.getExternalStorageDirectory(); 
           mainDir = new File(sdCard.getAbsolutePath() 
                   + "/Android/data/com.MeadowEast.audiotest/files-eng"); 
           clipDir = new File(mainDir, "clips"); 
           Log.d(TAG, "clipDir = " + clipDir.length()); 
           //secondaryClipList = new ListOfRemainingClips((int)clipDir.length()); 
           /* 
           for (int i = 0; i < (int)clipDir.length(); i++ ){ 
               if (i < 50) { 
                   cliplist[i] = (clipDir.length())[i]; 
               } 
               else { 
                   secondaryClipList[i] = (clipDir.length())[i]; 
               } 
            */ 
           
           
           cliplist = clipDir.list(); 
           readClipInfo(); 
           rnd = new Random(); 
           
           
           int cl = cliplist.length; 
           Log.d(GESTURE_TAG, "length of cliplist.length: " + cl); 
           p = new Weighted(cliplist.length); 
           
           
           setContentView(R.layout.activity_main); 
           Log.d(TAG, "English set"); 
       } else if (currentLanguage.equalsIgnoreCase("Mandarin")) { 
           Log.d(TAG, "mandarin"); 
           File sdCard = Environment.getExternalStorageDirectory(); 
           mainDir = new File(sdCard.getAbsolutePath() 
                   + "/Android/data/com.MeadowEast.audiotest/files"); 
           clipDir = new File(mainDir, "clips"); 
           
           Log.d(TAG, "clipDir = " + clipDir.length()); 

           String[]j = clipDir.list(); 
           Log.d(TAG, "clipDir = " + j.length); 
           cliplist = new String[50]; 
           secondaryClipList = new ListOfRemainingClips(50); 
           
           for (int i = 0; i < j.length; i++ ){ 
               if (i < 50) { 
                   cliplist[i] = (clipDir.list())[i]; 
                   Log.d(GESTURE_TAG, "cliplist[" + i + "]: " + cliplist[i]); 
               } 
               else { 
                   secondaryClipList.setStringInArr(clipDir.list()[i], (i % 50)); 
                   Log.d(GESTURE_TAG, "secondaryClipList[" + i%50 + "]: " + secondaryClipList.getStringInArr(i%50)); 
               } 
               
           } 
           /* 
           for (int i = 0; i < cliplist.length; i++){ 
               Log.d(GESTURE_TAG, "cliplist[" + i + "]: " + cliplist[i]); 
           } 
           for (int i = 0; i < secondaryClipList.returnSizeOfArr(); i++){ 
               Log.d(GESTURE_TAG, "secondaryClipList[" + i + "]: " + secondaryClipList.getStringInArr(i)); 
           } 
           */ 
           //cliplist = clipDir.list(); 
           readClipInfo(); 
           rnd = new Random(); 
           
           /* 
           int cl = cliplist.length; 
           Log.d(GESTURE_TAG, "length of cliplist.length: " + cl); 
           */ 
           p = new Weighted(cliplist.length); 
           
           
           setContentView(R.layout.activity_main); 
           Log.d(TAG, "mandarin set"); 
       } 

       /* 
        * Gestures begins -- Vinson 
        */ 
       GestureOverlayView gestureOverlayView = new GestureOverlayView(this); 
       View inflate = getLayoutInflater() 
               .inflate(R.layout.activity_main, null); 
       gestureOverlayView.addView(inflate); 
       gestureOverlayView.setGestureVisible(false); 
       gestureOverlayView.addOnGesturePerformedListener(this); 

       /* 
        * NEED THE RAW FOLD WITH GESTURES IN PACKAGE EXPLORER 
        */ 
       gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures); 
       if (!gestureLib.load()) { 
           // finish(); 
           Log.i(GESTURE_TAG, "FAILED"); 
       } else { 
           Log.i(GESTURE_TAG, "LOADED"); 
       } 
       setContentView(gestureOverlayView); 
       /* 
        * All actions are now triggered byt touch gestures, button actions have 
        * been removed from the app 
        */ 
       findViewById(R.id.hanziTextView).setOnTouchListener( 
               new OnSwipeTouchListener(this) { 
                   @Override 
                   public void onTap() { 
                       if (!clockRunning) 
                           toggleClock(); 
                       if (sample != null) 
                           setHanzi(hanzi.get(key)); 

                       Toast.makeText(MainActivity.this, "Tap", 
                               Toast.LENGTH_SHORT).show(); 
                   } 

                   @Override 
                   public void onSwipeBottom() { 
                       toggleClock(); 
                       Toast.makeText(MainActivity.this, "bottom", 
                               Toast.LENGTH_SHORT).show(); 
                   } 

                   @Override 
                   public void onLongPressClick() { 
                       Toast.makeText(MainActivity.this, "Clip: " + key, 
                               Toast.LENGTH_SHORT).show(); 
                       Log.d(TAG, "Long clicked"); 
                   } 
               }); 
       /* 
        * Gesture ends 
        */ 

       clockHandler = new Handler(); 
       start = System.currentTimeMillis(); 
       elapsedMillis = 0L; 
       clockRunning = false; 
       createUpdateTimeTask(); 
       /* 
        * findViewById(R.id.pauseButton).setOnClickListener( new 
        * OnClickListener() { public void onClick(View v) { toggleClock(); } 
        * }); 
        */ 
       if (savedInstanceState != null) { 
           elapsedMillis = savedInstanceState.getLong("elapsedMillis"); 
           Log.d(TAG, "elapsedMillis restored to" + elapsedMillis); 
           key = savedInstanceState.getString("key"); 
           String sampleName = savedInstanceState.getString("sample"); 
           if (sampleName.length() > 0) 
               sample = new File(clipDir, sampleName); 
           if (savedInstanceState.getBoolean("running")) 
               toggleClock(); 
           else 
               showTime(elapsedMillis); 
           Log.d(TAG, "About to restore instruction"); 
           String instruction = savedInstanceState.getString("instruction"); 
           if (instruction.length() > 0) { 
               Log.d(TAG, "Restoring instruction value of " + instruction); 
               TextView t = (TextView) findViewById(R.id.instructionTextView); 
               t.setText(instruction); 
           } 
       } 
   } 

   /* Original - function called when the app is paused(not closed) */ 
   public void onPause() { 
       super.onPause(); 
       Log.d(TAG, "!!!! onPause is being run"); 
       clockWasRunning = clockRunning; 
       if (clockRunning) 
           toggleClock(); 
   } 

   /* 
    * Gesture begin 
    */ 
   public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) { 
       ArrayList<Prediction> predictions = gestureLib.recognize(gesture); 
       if (predictions.size() > 0) { 
           Prediction prediction = predictions.get(0); 
           if (prediction.score > 1.0) { 
               String s = prediction.name; 

               if (s.equals("SwipeLeft")) { 
                   Log.d("TAG", "lang: " + currentLanguage); 
                   
                   
                   p.setProbabilities(repeatCount, index, totalClipsPlayed); 
                   
                   index = rnd.nextInt(p.getSumOfWeights()) + 1; 
                   Log.d(TAG, "before loop value index after rnd" + index); 
                   int x = 0; 
                   while (x < p.getSize()/* && index > 0*/) { 
                       index -= p.getClipsWeight(x); 
                       if (index <= 0){ 
                           index = x; 
                           break; 
                       } 
                       //Log.d(TAG, "in loop " + index); 
                       x++; 
                   } 
                   
                   p.updateTimesClipsPlayed(index); 
                   
                   
                   //Integer index = rnd.nextInt(cliplist.length); 
                   Log.d(TAG, "after loop value index after rnd" + index); 
                   sample = new File(clipDir, cliplist[index]); 
                   Log.d("TAG", "pie " + sample); 
                   Email.mp3Location = sample; 
                   addToHistory(sample); 
                   key = sample.getName(); 
                   key = key.substring(0, key.length() - 4); 
                   TextView t = (TextView) findViewById(R.id.instructionTextView); 
                   t.setText(getInstruction(key)); 
                   if (!clockRunning) 
                       toggleClock(); 
                   if (sample != null) { 
                       setHanzi(""); 
                       if (mp != null) { 
                           mp.stop(); 
                           mp.release(); 
                       } 
                       mp = new MediaPlayer(); 
                       mp.setAudioStreamType(AudioManager.STREAM_MUSIC); 
                       try { 
                           mp.setDataSource(getApplicationContext(), 
                                   Uri.fromFile(sample)); 
                           mp.prepare(); 
                           mp.start(); 
                       } catch (Exception e) { 
                           Log.d(TAG, "Couldn't get mp3 file"); 
                       } 
                   } 
                   
                   totalClipsPlayed += 1; 
                   repeatCount = 0; 
                   
                   Toast.makeText(MainActivity.this, "left", 
                           Toast.LENGTH_SHORT).show(); 
               } else if (s.equals("NorthLeftCircle") 
                       || s.equals("NorthRightCircle") 
                       || s.equals("SouthLeftCircle") 
                       || s.equals("SouthRightCircle") 
                       || s.equals("WestLeftCircle") 
                       || s.equals("WestRightCircle") 
                       || s.equals("EastLeftCircle") 
                       || s.equals("EastRightCircle")) { 
                   repeatCount += 1; 
                   if (!clockRunning) 
                       toggleClock(); 
                   if (sample != null) { 
                       setHanzi(""); 
                       Log.d(TAG, "Hanzi set"); 
                       if (mp != null) { 
                           mp.stop(); 
                           mp.release(); 
                           Log.d(TAG, "Media player cleared"); 
                       } 
                       mp = new MediaPlayer(); 
                       mp.setAudioStreamType(AudioManager.STREAM_MUSIC); 
                       try { 
                           Log.d(TAG, "trying old sample"); 
                           mp.setDataSource(getApplicationContext(), 
                                   Uri.fromFile(sample)); 
                           Log.d(TAG, "Prepairing"); 
                           mp.prepare(); 
                           Log.d(TAG, "starting"); 
                           mp.start(); 
                           Log.d(TAG, "set"); 
                       } catch (Exception e) { 
                           Log.d(TAG, "Couldn't get mp3 file"); 
                       } 
                   } 

                   Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT) 
                           .show(); 
               } 

           } 
       } 
   } 

   /* 
    * Gestures end 
    */ 

   /* Original - ??? */ 
   public void onSaveInstanceState(Bundle outState) { 
       super.onSaveInstanceState(outState); 
       String sampleName = ""; 
       if (sample != null) 
           sampleName = sample.getName(); 
       outState.putString("sample", sampleName); 
       // onPause has stopped the clock if it was running, so we just save 
       // elapsedMillis 
       outState.putLong("elapsedMillis", elapsedMillis); 
       TextView t = (TextView) findViewById(R.id.instructionTextView); 
       outState.putString("instruction", t.getText().toString()); 
       outState.putString("key", key); 
       outState.putBoolean("running", clockWasRunning); 
   } 

   /* Original - reset the timer, clear current clip */ 
   public void reset() { 
       TextView t; 
       if (clockRunning) 
           toggleClock(); 
       start = 0L; 
       elapsedMillis = 0L; 
       sample = null; 
       t = (TextView) findViewById(R.id.timerTextView); 
       t.setText("0:00"); 
       setHanzi(""); 
       t = (TextView) findViewById(R.id.instructionTextView); 
       t.setText(""); 
   } 

   // save the name of the clip into array, if more than ten clips erase from 
   // the earliest one. Does not check for duplicates 
   public void addToHistory(File currSample) { 
       historicalChanges = true; 
       Log.d(TAG, "added stuuf to history"); 
       String temp = currSample.getName(); 
       if (recentClipsSize < 10) { 
           recentClipText[recentClipsSize] = temp.substring(0, 
                   temp.length() - 4); 
           recentClipText[recentClipsSize] += (" : " + (hanzi 
                   .get(recentClipText[recentClipsSize])).substring(0, 4)); 
           Log.d(TAG, recentClipText[recentClipsSize]); 
           recentClips[recentClipsSize] = currSample; 
           recentClipsSize++; 
       } else { 
           for (int i = 1; i < 10; i++) { 
               recentClips[i - 1] = recentClips[i]; 
               recentClipText[i - 1] = recentClipText[i]; 
           } 
           recentClips[9] = currSample; 
           recentClipText[9] = temp.substring(0, temp.length() - 4); 
           recentClipText[9] += (" : " + (hanzi.get(recentClipText[9])) 
                   .substring(0, 4)); 
       } 
   } 

   /* 
    * Original - when back button is pressed, prompt user if they really want 
    * to exit app clears current clip 
    */ 
   @Override 
   public boolean onKeyDown(int keyCode, KeyEvent event) { 
       if (keyCode == KeyEvent.KEYCODE_BACK) { 
           Log.d(TAG, "llkj"); 
           new AlertDialog.Builder(this) 
                   .setIcon(android.R.drawable.ic_dialog_alert) 
                   .setTitle(R.string.quit) 
                   .setMessage(R.string.reallyQuit) 
                   .setPositiveButton(R.string.yes, 
                           new DialogInterface.OnClickListener() { 
                               public void onClick(DialogInterface dialog, 
                                       int which) { 
                                   MainActivity.this.finish(); 
                               } 
                           }).setNegativeButton(R.string.no, null).show(); 
           return true; 
       } else { 
           return super.onKeyDown(keyCode, event); 
       } 
   } 

   // Check if device has network connectivity 
   private boolean isNetworkAvailable() { 
       ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); 
       NetworkInfo activeNetworkInfo = connectivityManager 
               .getActiveNetworkInfo(); 
       return activeNetworkInfo != null && activeNetworkInfo.isConnected(); 
   } 

   /* RR - create a dialog box and prompt user to select a default language */ 
   private void setLanguageOnstartup() { 
       AlertDialog.Builder builder = new AlertDialog.Builder(this); 
       builder.setCancelable(true); 
       builder.setTitle("Select a Language"); 
       builder.setInverseBackgroundForced(true); 
       builder.setPositiveButton("Mandarin", 
               new DialogInterface.OnClickListener() { 
                   public void onClick(DialogInterface dialog, int which) { 
                       currentLanguage = "Mandarin"; 
                       changeLanguageMode(currentLanguage); 
                       dialog.dismiss(); 
                   } 
               }); 
       builder.setNegativeButton("English", 
               new DialogInterface.OnClickListener() { 
                   public void onClick(DialogInterface dialog, int which) { 
                       currentLanguage = "English"; 
                       dialog.dismiss(); 
                       changeLanguageMode(currentLanguage); 
                   } 
               }); 
       AlertDialog alert = builder.create(); 
       alert.show(); 
   } 
} 