/*
 * Credit:
 *
 * Octowolve - Mod menu: https://github.com/z3r0Sec/Substrate-Template-With-Mod-Menu
 * And hooking: https://github.com/z3r0Sec/Substrate-Hooking-Example
 * VanHoevenTR A.K.A Nixi: https://github.com/LGLTeam/VanHoevenTR_Android_Mod_Menu
 * MrIkso - Mod menu: https://github.com/MrIkso/FloatingModMenu
 * Rprop - https://github.com/Rprop/And64InlineHook
 * MJx0 A.K.A Ruit - KittyMemory: https://github.com/MJx0/KittyMemory
 * */

package uk.lgl.modmenu;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class FloatingModMenuService extends Service {
    private MediaPlayer FXPlayer;
    public RelativeLayout mCollapsed, mRootContainer;
    public LinearLayout mExpanded, patches, mSettings;
    public WindowManager mWindowManager;
    public WindowManager.LayoutParams params;
    private ImageView startimage;
    private FrameLayout rootFrame;
    private AlertDialog alert;
    private EditText edittextvalue;
    private ScrollView scrollView;
    //For alert dialog
    private TextView inputFieldTextView;
    private String inputFieldFeatureName;
    private int inputFieldFeatureNum;
    private EditTextValue inputFieldTxtValue;

    boolean soundDelayed;
    public String cacheDir;

    //initialize methods from the native library
    public static native void LoadSounds(String dir);

    public native void ToastStartup();

    private native String Title();

    private native String Heading();

    private native String Icon();

    private native String IconWebViewData();

    private native String[] getFeatureList();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //When this Class is called the code in this function will be executed
    @Override
    public void onCreate() {
        super.onCreate();
        Preferences.context = getApplicationContext();
        cacheDir = getCacheDir().getPath() + "/";
        LoadSounds(cacheDir);
        //A little message for the user when he opens the app
        ToastStartup();


        initFloating();
        initAlertDiag();

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            public void run() {
                Thread();
                handler.postDelayed(this, 1000);
            }
        });
    }

    //Here we write the code for our Menu
    private void initFloating() {
        rootFrame = new FrameLayout(getBaseContext()); // Global markup
        rootFrame.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        rootFrame.setOnTouchListener(onTouchListener());
        mRootContainer = new RelativeLayout(getBaseContext()); // Markup on which two markups of the icon and the menu itself will be placed
        mRootContainer.setLayoutParams(new FrameLayout.LayoutParams(-2, -2));
        mCollapsed = new RelativeLayout(getBaseContext()); // Markup of the icon (when the menu is minimized)
        mCollapsed.setLayoutParams(new RelativeLayout.LayoutParams(-2, -2));
        mCollapsed.setVisibility(View.VISIBLE);
        mExpanded = new LinearLayout(getBaseContext()); // Menu markup (when the menu is expanded)
        patches = new LinearLayout(getBaseContext());
        mSettings = new LinearLayout(getBaseContext());

        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(-2, -1));
        relativeLayout.setPadding(10, 3, 10, 3);
        relativeLayout.setVerticalGravity(16);

        //**********  Hide/Kill button **********
        Button hideBtn = new Button(this);
        hideBtn.setBackgroundColor(Color.TRANSPARENT);
        hideBtn.setText("Esconder/Kill");
        hideBtn.setTextColor(Color.parseColor("#FF0000"));
        hideBtn.setTextSize(15.0f);
        hideBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCollapsed.setVisibility(View.VISIBLE);
                mCollapsed.setAlpha(0);
                mExpanded.setVisibility(View.GONE);
                Toast.makeText(view.getContext(), "Icone escondido. Lembre-se a posi??ao dele!", Toast.LENGTH_LONG).show();
                playSound(Uri.fromFile(new File(cacheDir + "Back.ogg")));
            }
        });
        hideBtn.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                Toast.makeText(view.getContext(), "Kill Sucess :)", Toast.LENGTH_LONG).show();
                playSound(Uri.fromFile(new File(cacheDir + "Back.ogg")));
                FloatingModMenuService.this.stopSelf();
                return false;
            }
        });

        //********** Close button **********
        Button closeBtn = new Button(this);
        closeBtn.setBackgroundColor(Color.TRANSPARENT);
        closeBtn.setText("Minimizar");
        closeBtn.setTextSize(15.0f);
        closeBtn.setTextColor(Color.parseColor("#FF0000"));
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCollapsed.setVisibility(View.VISIBLE);
                mCollapsed.setAlpha(0.95f);
                mExpanded.setVisibility(View.GONE);
                playSound(Uri.fromFile(new File(cacheDir + "Back.ogg")));
            }
        });
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        layoutParams.addRule(11);
        closeBtn.setLayoutParams(layoutParams);

        //********** The icon to open mod menu **********
        startimage = new ImageView(getBaseContext());
        startimage.setLayoutParams(new RelativeLayout.LayoutParams(-2, -2));
        int applyDimension = (int) TypedValue.applyDimension(1, 60, getResources().getDisplayMetrics()); //Icon size 50
        startimage.getLayoutParams().height = applyDimension;
        startimage.getLayoutParams().width = applyDimension;
        startimage.requestLayout();
        startimage.setScaleType(ImageView.ScaleType.FIT_XY);
        byte[] decode = Base64.decode(Icon(), 0);
        startimage.setImageBitmap(BitmapFactory.decodeByteArray(decode, 0, decode.length));
        startimage.setImageAlpha(200);
        ((ViewGroup.MarginLayoutParams) startimage.getLayoutParams()).topMargin = convertDipToPixels(10);
        //Initialize event handlers for buttons, etc.
        startimage.setOnTouchListener(onTouchListener());
        startimage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCollapsed.setVisibility(View.GONE);
                mExpanded.setVisibility(View.VISIBLE);
            }
        });

        //********** Webview **********
        WebView wView = new WebView(this); //Icon size width=\"50\" height=\"50\"
        wView.loadData("<html><head><body style=\"margin: 0; padding: 0\"><img src=\"" + IconWebViewData() + "\" width=\"50\" height=\"50\"</body></html>", "text/html", "utf-8");
        wView.setBackgroundColor(0x00000000); //Transparent
        wView.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        wView.getLayoutParams().height = applyDimension;
        wView.getLayoutParams().width = applyDimension;
        wView.requestLayout();
        wView.setAlpha(0.9f);
        wView.getSettings().setAppCachePath("/data/data/" + getPackageName() + "/cache");
        wView.getSettings().setAppCacheEnabled(true);
        wView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        wView.setOnTouchListener(onTouchListener());

        //********** The box of the mod menu **********
        mExpanded.setVisibility(View.GONE);
        mExpanded.setBackgroundColor(Color.parseColor("#1C2A35"));
        mExpanded.setAlpha(0.95f);
        mExpanded.setGravity(17);
        mExpanded.setOrientation(LinearLayout.VERTICAL);
        mExpanded.setPadding(0, 0, 0, 0);
        //Auto size. To set size manually, change the width and height example 500, 500
        mExpanded.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        GradientDrawable gradientdrawable = new GradientDrawable();
        gradientdrawable.setCornerRadius(20); //Set corner
        gradientdrawable.setColor(Color.parseColor("#1C2A35")); //Set background color
        //gradientdrawable.setStroke(1, Color.parseColor("#32cb00")); //Set border
        mExpanded.setBackground(gradientdrawable); //Apply GradientDrawable to it

        //********** Mod menu feature list **********
        scrollView = new ScrollView(getBaseContext());
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, dp(200)));
        scrollView.setBackgroundColor(Color.parseColor("#171E24"));

        patches.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        patches.setOrientation(LinearLayout.VERTICAL);

        //********** Title text **********
        RelativeLayout titleText = new RelativeLayout(this);
        titleText.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
        titleText.setPadding(10, 5, 10, 5);
        titleText.setVerticalGravity(16);

        TextView title = new TextView(getBaseContext());
        title.setText(Title());
        title.setTextColor(Color.parseColor("#FF0000"));
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextSize(25.0f);
        title.setPadding(0, 10, 0, 5);

        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
        title.setLayoutParams(rl);
        layoutParams.addRule(11);

        //********** Heading text **********
        TextView heading = new TextView(getBaseContext());
        heading.setText(Html.fromHtml(Heading()));
        heading.setTextColor(Color.parseColor("#FFFF00"));
        heading.setTypeface(Typeface.DEFAULT_BOLD);
        heading.setTextSize(17.0f);
        heading.setPadding(10, 5, 10, 10);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        layoutParams3.gravity = 17;
        heading.setLayoutParams(layoutParams3);

        //********** Settings icon ********

        TextView settings = new TextView(getBaseContext());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            settings.setText("???");
        } else {
            settings.setText("\uD83D\uDD27"); //Android 5 and below can't display ??? emoji so display "????" instead
        }
        settings.setTextColor(Color.parseColor("#00FF00"));
        settings.setTypeface(Typeface.DEFAULT_BOLD);
        settings.setTextSize(20.0f);
        RelativeLayout.LayoutParams rlsettings = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        rlsettings.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        settings.setLayoutParams(rlsettings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    playSound(Uri.fromFile(new File(cacheDir + "Select.ogg")));
                    scrollView.removeView(patches);
                    scrollView.addView(mSettings);
                } catch (IllegalStateException e) {

                }
            }
        });

        //********** Settings **********
        mSettings.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        mSettings.setOrientation(LinearLayout.VERTICAL);
        addSwitch(1000, "Sons ON/OFF");
        addSwitch(1001, "Salvar Preferencias ON/OFF");
        addButton(1002, "Voltar");

        //********** Params **********
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WRAP_CONTENT,
                    WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    WRAP_CONTENT,
                    WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }
        params.gravity = 51;
        params.x = 0;
        params.y = 100;

        //********** Adding view components **********
        rootFrame.addView(mRootContainer);
        mRootContainer.addView(mCollapsed);
        mRootContainer.addView(mExpanded);
        if (IconWebViewData() != null) {
            mCollapsed.addView(wView);
        } else {
            mCollapsed.addView(startimage);
        }
        titleText.addView(title);
        titleText.addView(settings);
        mExpanded.addView(titleText);
        mExpanded.addView(heading);
        mExpanded.addView(scrollView);
        scrollView.addView(patches);
        relativeLayout.addView(hideBtn);
        relativeLayout.addView(closeBtn);
        mExpanded.addView(relativeLayout);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(rootFrame, params);

        //********** Create menu list **********
        String[] listFT = getFeatureList();
        for (int i = 0; i < listFT.length; i++) {
            final int feature = i;
            String str = listFT[i];
            if (str.contains("Toggle_")) {
                addSwitch(feature, str.replace("Toggle_", ""));
            } else if (str.contains("SeekBar_")) {
                String[] split = str.split("_");
                addSeekBar(feature, split[1], Integer.parseInt(split[2]), Integer.parseInt(split[3]));
            } else if (str.contains("Button_")) {
                addButton(feature, str.replace("Button_", ""));
            } else if (str.contains("Spinner_")) {
                addSpinner(feature, str.replace("Spinner_", ""));
            } else if (str.contains("InputValue_")) {
                addTextField(feature, str.replace("InputValue_", ""));
            } else if (str.contains("Category_")) {
                addCategory(str.replace("Category_", ""));
            }
        }
    }

    //Dialog for changing value
    private void initAlertDiag() {
        //LinearLayout
        LinearLayout linearLayout1 = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        linearLayout1.setPadding(10, 5, 0, 5);
        linearLayout1.setOrientation(LinearLayout.VERTICAL);
        linearLayout1.setGravity(17);
        linearLayout1.setLayoutParams(layoutParams);
        linearLayout1.setBackgroundColor(Color.parseColor("#171E24"));
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        linearLayout.setBackgroundColor(Color.parseColor("#14171f"));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //FrameLayout
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        frameLayout.addView(linearLayout);

        //TextView
        final TextView textView = new TextView(this);
        textView.setText(Html.fromHtml("<font face='roboto'>Tap OK to apply changes. Tap outside to cancel</font>"));
        textView.setTextColor(Color.parseColor("#DEEDF6"));
        textView.setLayoutParams(layoutParams);

        edittextvalue = new EditText(this);
        edittextvalue.setLayoutParams(layoutParams);
        edittextvalue.setMaxLines(1);
        edittextvalue.setWidth(convertDipToPixels(300));
        edittextvalue.setTextColor(Color.parseColor("#93a6ae"));
        edittextvalue.setTextSize(13.0f);
        edittextvalue.setHintTextColor(Color.parseColor("#434d52"));
        edittextvalue.setInputType(InputType.TYPE_CLASS_NUMBER);
        edittextvalue.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));

        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(10);
        edittextvalue.setFilters(FilterArray);

        //Button
        Button button = new Button(this);
        button.setBackgroundColor(Color.parseColor("#1C262D"));
        button.setTextColor(Color.parseColor("#D5E3EB"));
        button.setText("OK");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputFieldTxtValue.setValue(Integer.parseInt(edittextvalue.getText().toString()));
                inputFieldTextView.setText(Html.fromHtml("<font face='roboto'>" + inputFieldFeatureName + ": <font color='#41c300'>" + edittextvalue.getText().toString() + "</font></font>"));
                alert.dismiss();
                Preferences.changeFeatureInt(inputFieldFeatureName, inputFieldFeatureNum, Integer.parseInt(edittextvalue.getText().toString()));
                playSound(Uri.fromFile(new File(cacheDir + "Select.ogg")));
            }
        });

        alert = new AlertDialog.Builder(this, 2).create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(alert.getWindow()).setType(Build.VERSION.SDK_INT >= 26 ? 2038 : 2002);
        }
        linearLayout1.addView(textView);
        linearLayout1.addView(edittextvalue);
        linearLayout1.addView(button);
        alert.setView(linearLayout1);
    }

    private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {
            final View collapsedView = mCollapsed;
            final View expandedView = mExpanded;
            private float initialTouchX;
            private float initialTouchY;
            private int initialX;
            private int initialY;

            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int rawX = (int) (motionEvent.getRawX() - initialTouchX);
                        int rawY = (int) (motionEvent.getRawY() - initialTouchY);

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (rawX < 10 && rawY < 10 && isViewCollapsed()) {
                            //When user clicks on the image view of the collapsed layout,
                            //visibility of the collapsed layout will be changed to "View.GONE"
                            //and expanded view will become visible.
                            try {
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                                playSound(Uri.fromFile(new File(cacheDir + "OpenMenu.ogg")));
                                //Toast.makeText(FloatingModMenuService.this, Html.fromHtml(Toast()), Toast.LENGTH_SHORT).show();
                            } catch (NullPointerException e) {

                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + ((int) (motionEvent.getRawX() - initialTouchX));
                        params.y = initialY + ((int) (motionEvent.getRawY() - initialTouchY));
                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(rootFrame, params);
                        return true;
                    default:
                        return false;
                }
            }
        };
    }

    private void addSwitch(final int featureNum, final String featureName) {
        final Switch switchR = new Switch(this);
        switchR.setBackgroundColor(Color.parseColor("#171E24"));
        switchR.setText(Html.fromHtml("<font face='roboto'>" + featureName + "</font>"));
        switchR.setTextColor(Color.parseColor("#DEEDF6"));
        switchR.setPadding(10, 5, 0, 5);
        switchR.setTextSize(20.0f);
        switchR.setChecked(Preferences.loadPrefBoolean(featureName));
        switchR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (z) {
                    playSound(Uri.fromFile(new File(cacheDir + "On.ogg")));
                } else {
                    playSound(Uri.fromFile(new File(cacheDir + "Off.ogg")));
                }
                Preferences.changeFeatureBoolean(featureName, featureNum, switchR.isChecked());
            }
        });
        Preferences.changeFeatureBoolean(featureName, featureNum, switchR.isChecked());
        if (featureNum >= 1000)
            mSettings.addView(switchR);
        else
            patches.addView(switchR);
    }

    private void addSeekBar(final int featureNum, final String featureName, int prog, int max) {
        prog = Preferences.loadPrefInt(featureName);
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        linearLayout.setPadding(10, 5, 0, 5);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(17);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setBackgroundColor(Color.parseColor("#171E24"));

        final TextView textView = new TextView(this);
        textView.setText(Html.fromHtml("<font face='roboto'>" + featureName + ": <font color='#41c300'>" + prog + "</font>"));
        textView.setTextColor(Color.parseColor("#DEEDF6"));
        textView.setTextSize(20.0f);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setPadding(25, 10, 35, 10);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        seekBar.setMax(max);
        seekBar.setProgress(prog);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            int l;

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (l < i) {
                    playSound(Uri.fromFile(new File(cacheDir + "SliderIncrease.ogg")));
                } else {
                    playSound(Uri.fromFile(new File(cacheDir + "SliderDecrease.ogg")));
                }
                l = i;

                Preferences.changeFeatureInt(featureName, featureNum, i);
                textView.setText(Html.fromHtml("<font face='roboto'>" + featureName + ": <font color='#41c300'>" + i + "</font>"));
            }
        });
        Preferences.changeFeatureInt(featureName, featureNum, prog);
        linearLayout.addView(textView);
        linearLayout.addView(seekBar);
        patches.addView(linearLayout);
    }

    private boolean isActive = true;

    private void addButton(final int featureNum, String featureName) {
        final Button button = new Button(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.setMargins(7, 5, 7, 5);
        button.setLayoutParams(layoutParams);
        button.setPadding(10, 5, 10, 5);
        button.setTextSize(18.0f);
        button.setTextColor(Color.parseColor("#FFFF00"));
        button.setGravity(17);

        if (featureName.contains("OnOff_")) {
            featureName = featureName.replace("OnOff_", "");
            final String finalFeatureName = featureName;
            isActive = Preferences.loadPrefBoolean(featureName);
            Preferences.changeFeatureBoolean(finalFeatureName, featureNum, isActive);
            if (isActive) {
                button.setText(finalFeatureName + ": ON");
                button.setBackgroundColor(Color.parseColor("#003300"));
                isActive = false;
            } else {
                button.setText(finalFeatureName + ": OFF");
                button.setBackgroundColor(Color.parseColor("#7f0000"));
                isActive = true;
            }
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Preferences.changeFeatureBoolean(finalFeatureName, featureNum, isActive);
                    if (isActive) {
                        playSound(Uri.fromFile(new File(cacheDir + "On.ogg")));
                        button.setText(finalFeatureName + ": ON");
                        button.setBackgroundColor(Color.parseColor("#003300"));
                        isActive = false;
                    } else {
                        playSound(Uri.fromFile(new File(cacheDir + "Off.ogg")));
                        button.setText(finalFeatureName + ": OFF");
                        button.setBackgroundColor(Color.parseColor("#7f0000"));
                        isActive = true;
                    }
                }
            });
        } else {
            button.setText(featureName);
            button.setBackgroundColor(Color.parseColor("#1C262D"));
            final String finalFeatureName1 = featureName;
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    playSound(Uri.fromFile(new File(cacheDir + "Select.ogg")));
                    if (featureNum == 1002) {
                        scrollView.removeView(mSettings);
                        scrollView.addView(patches);
                        return;
                    } else
                        Preferences.changeFeatureInt(finalFeatureName1, featureNum, 0);
                }
            });
        }

        if (featureNum >= 1000)
            mSettings.addView(button);
        else
            patches.addView(button);
    }

    private void addSpinner(final int featureNum, String featureName) {
        final List<String> list = new LinkedList<>(Arrays.asList(featureName.split("_")));

        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        linearLayout.setPadding(10, 5, 10, 5);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(17);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setBackgroundColor(Color.parseColor("#171E24"));

        final TextView textView = new TextView(this);
        textView.setText(Html.fromHtml("<font face='roboto'>" + list.get(0) + ": <font color='#41c300'></font>"));
        textView.setTextColor(Color.parseColor("#DEEDF6"));

        // Create another LinearLayout as a workaround to use it as a background
        // and to keep the 'down' arrow symbol
        // If spinner had the setBackgroundColor set, there would be no arrow symbol
        LinearLayout linearLayout2 = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams2.setMargins(10, 2, 10, 5);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setGravity(17);
        linearLayout2.setBackgroundColor(Color.parseColor("#1C262D"));
        linearLayout2.setLayoutParams(layoutParams2);

        Spinner spinner = new Spinner(this);
        spinner.setPadding(5, 10, 5, 8);
        spinner.setLayoutParams(layoutParams2);
        spinner.getBackground().setColorFilter(1, PorterDuff.Mode.SRC_ATOP); //trick to show white down arrow color
        //Creating the ArrayAdapter instance having the list
        list.remove(0);
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinner.setAdapter(aa);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Preferences.changeFeatureInt(list.get(0), featureNum, position);
                ((TextView) parentView.getChildAt(0)).setTextColor(Color.parseColor("#f5f5f5"));
                playSound(Uri.fromFile(new File(cacheDir + "Select.ogg")));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                playSound(Uri.fromFile(new File(cacheDir + "Select.ogg")));
            }
        });
        linearLayout.addView(textView);
        linearLayout2.addView(spinner);
        patches.addView(linearLayout);
        patches.addView(linearLayout2);
    }

    private void addTextField(final int feature, final String featureName) {
        RelativeLayout relativeLayout2 = new RelativeLayout(this);
        relativeLayout2.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
        relativeLayout2.setPadding(10, 5, 10, 5);
        relativeLayout2.setVerticalGravity(16);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        layoutParams.topMargin = 15;

        final TextView textView = new TextView(this);
        int num = Preferences.loadPrefInt(featureName);
        textView.setText(Html.fromHtml("<font face='roboto'>" + featureName + ": <font color='#41c300'>" + num + "</font></font>"));
        textView.setTextColor(Color.parseColor("#DEEDF6"));
        textView.setLayoutParams(layoutParams);

        Preferences.changeFeatureInt(featureName, feature, num);

        final EditTextValue edittextval = new EditTextValue();

        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        Button button2 = new Button(this);
        button2.setLayoutParams(layoutParams2);
        button2.setBackgroundColor(Color.parseColor("#1C262D"));
        button2.setText("SET");
        button2.setTextColor(Color.parseColor("#D5E3EB"));
        button2.setGravity(17);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.show();
                inputFieldTextView = textView;
                inputFieldFeatureNum = feature;
                inputFieldFeatureName = featureName;
                inputFieldTxtValue = edittextval;
                edittextvalue.setText(String.valueOf(edittextval.getValue()));
            }
        });

        relativeLayout2.addView(textView);
        relativeLayout2.addView(button2);
        patches.addView(relativeLayout2);
    }

    private void addCategory(String text) {
        TextView textView = new TextView(this);
        textView.setBackgroundColor(Color.parseColor("#2F3D4C"));
        textView.setText(text);
        textView.setGravity(17);
        textView.setTextSize(18.0f);
        textView.setTextColor(Color.parseColor("#00FF00"));
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(0, 5, 0, 5);
        patches.addView(textView);
    }

    //Play sounds
    public void playSound(Uri uri) {
        if (Preferences.isSoundEnabled) {
            if (!soundDelayed) {
                soundDelayed = true;
                if (FXPlayer != null) {
                    FXPlayer.stop();
                    FXPlayer.release();
                }
                FXPlayer = MediaPlayer.create(this, uri);
                if (FXPlayer != null) {
                    //Volume reduced so sounds are not too loud
                    FXPlayer.setVolume(0.4f, 0.4f);
                    FXPlayer.start();
                }

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        soundDelayed = false;
                    }
                }, 100);
            }
        }
    }

    //Override our Start Command so the Service doesnt try to recreate itself when the App is closed
   // public int onStartCommand(Intent intent, int i, int i2) {
       // return Service.START_NOT_STICKY;
   // }

    public int onStartCommand(Intent intent, int i, int i2) {
        return START_NOT_STICKY;
    }

    public boolean isViewCollapsed() {
        return rootFrame == null || mCollapsed.getVisibility() == View.VISIBLE;
    }

    //For our image a little converter
    private int convertDipToPixels(int i) {
        return (int) ((((float) i) * getResources().getDisplayMetrics().density) + 0.5f);
    }

    private int dp(int i) {
        return (int) TypedValue.applyDimension(1, (float) i, getResources().getDisplayMetrics());
    }

    //Check if we are still in the game. If now our menu and menu button will dissapear
    private boolean isNotInGame() {
        RunningAppProcessInfo runningAppProcessInfo = new RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(runningAppProcessInfo);
        return runningAppProcessInfo.importance != 100;
    }

    //Destroy our View
    public void onDestroy() {
        super.onDestroy();
        View view = rootFrame;
        if (view != null) {
            mWindowManager.removeView(view);
        }
    }

    //Same as above so it wont crash in the background and therefore use alot of Battery life
    public void onTaskRemoved(Intent intent) {
        stopSelf();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onTaskRemoved(intent);
    }

    public void Thread() {
        if (rootFrame == null) {
            return;
        }
        if (isNotInGame()) {
            rootFrame.setVisibility(View.INVISIBLE);
        } else {
            rootFrame.setVisibility(View.VISIBLE);
        }
    }

    public class EditTextValue {
        private int val;

        public void setValue(int i) {
            val = i;
        }

        public int getValue() {
            return val;
        }
    }
}