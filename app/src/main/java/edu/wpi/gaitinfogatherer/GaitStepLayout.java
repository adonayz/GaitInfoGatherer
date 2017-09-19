package edu.wpi.gaitinfogatherer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.callbacks.StepCallbacks;
import org.researchstack.backbone.ui.step.layout.StepLayout;
import org.researchstack.backbone.utils.FileUtils;

import java.io.File;

import it.sephiroth.android.library.tooltip.Tooltip;

/**
 * Created by Adonay on 9/11/2017.
 */

public class GaitStepLayout extends RelativeLayout implements StepLayout {
    public static final String KEY_GAIT = "GaitStep.Gait";

    private StepCallbacks mStepCallbacks;
    private GaitStep mStep;
    private StepResult<String> mResult;
    private String mFilename;
    private GaitRecorder gaitRecorder;
    static boolean areAllSessionsCompleted = false;
    private boolean isFirstTime = true;

    public GaitStepLayout(Context context) {
        super(context);
    }

    public GaitStepLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GaitStepLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        isFirstTime = true;
    }

    @Override
    public void initialize(Step step, StepResult result) {
        this.mStep = (GaitStep) step;
        this.mResult = result == null ? new StepResult<>(step) : result;

        initializeStep();
    }

    @Override
    public View getLayout() {
        return this;
    }

    @Override
    public boolean isBackEventConsumed() {
        setDataToResult();
        mStepCallbacks.onSaveStep(StepCallbacks.ACTION_PREV, mStep, mResult);
        return false;
    }

    @Override
    public void setCallbacks(StepCallbacks callbacks) {
        this.mStepCallbacks = callbacks;
    }

    // 1
    private void setDataToResult() {
        mResult.setResultForIdentifier(KEY_GAIT, getBase64EncodedData());
    }

    // 2
    private String getBase64EncodedData() {
        if (areAllSessionsCompleted) {

            // 3
            File file = new File(mFilename);

            try {
                byte[] bytes = FileUtils.readAll(file);

                String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
                return encoded;

            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private void initializeStep() {
        LayoutInflater.from(getContext())
                .inflate(R.layout.gait_step_layout, this, true);

        TextView title = (TextView) findViewById(R.id.title);
        title.setText(mStep.getTitle());

        TextView text = (TextView) findViewById(R.id.summary);
        text.setText(mStep.getText());

        final TextView countdown = (TextView) findViewById(R.id.countdown);
        countdown.setText(Integer.toString(mStep.getDuration()));

        final TextView countdown_title = (TextView) findViewById(R.id.countdown_title);

        final Button beginButton = (Button) findViewById(R.id.begin_recording);

        final Button stopButton = (Button) findViewById(R.id.stop_recording);

        final TextView accDataDisplay = (TextView) findViewById(R.id.accDataDisplay);

        final TextView gyroDataDisplay = (TextView) findViewById(R.id.gyroDataDisplay);

        final TextView countAttemptsTextView = (TextView) findViewById(R.id.attemptsRecorded);

        final EditText bacInput = (EditText) findViewById(R.id.bacInput);

        final AppCompatTextView restartButton = (AppCompatTextView) findViewById(R.id.restartButton);

        final AppCompatTextView saveButton = (AppCompatTextView) findViewById(R.id.saveButton);

        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "/GaitData.csv";
        mFilename = baseDir + File.separator + fileName;

        // TODO: set onClick listener

        beginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isFirstTime){
                    isFirstTime = false;
                    gaitRecorder = new GaitRecorder(getContext(), mFilename, accDataDisplay, gyroDataDisplay, countAttemptsTextView);
                    gaitRecorder.registerListeners();
                }

                gaitRecorder.startRecording();
                // 3
                beginButton.setVisibility(GONE);
                bacInput.setEnabled(false);
                countdown_title.setVisibility(View.VISIBLE);
                countAttemptsTextView.setVisibility(View.VISIBLE);
                restartButton.setEnabled(false);
                saveButton.setEnabled(false);

                // 4
                final CountDownTimer count = new CountDownTimer(mStep.getDuration() * 1000, 1000) {

                    // 5
                    public void onTick(long millisUntilFinished) {
                        countdown.setText(String.valueOf(millisUntilFinished / 1000));
                    }

                    // 6
                    public void onFinish() {
                        stopCurrentSession(countdown_title, beginButton, stopButton, this);
                        bacInput.setEnabled(true);
                        bacInput.setText("");
                        bacInput.setHint("Update BAC");
                        createToolTip(bacInput, Tooltip.Gravity.RIGHT, "Update BAC to add data");
                        restartButton.setEnabled(true);
                        saveButton.setEnabled(true);
                    }
                };

                // 7
                count.start();

                stopButton.setVisibility(View.VISIBLE);
                stopButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        count.cancel();
                        count.onFinish();
                    }
                });

                restartButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gaitRecorder.restartDataCollection(bacInput, beginButton);
                        count.onTick(mStep.getDuration() * 1000);
                    }
                });

                saveButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestPermissions((Activity)getContext());

                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        gaitRecorder.saveSessions();
                                        GaitStepLayout.this.setDataToResult();
                                        mStepCallbacks.onSaveStep(StepCallbacks.ACTION_NEXT, mStep, mResult);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Do you want to complete survey and save data to a CSV file?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                    }
                });
            }
        });


        bacInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = bacInput.getText().toString().trim();
                beginButton.setEnabled(!input.equals(""));
            }
        });

    }

    private void stopCurrentSession(TextView countdown_title, Button beginButton, Button stopButton, CountDownTimer countDownTimer){
        countdown_title.setVisibility(View.GONE);
        beginButton.setVisibility(View.VISIBLE);
        beginButton.setText("Add Data");
        stopButton.setVisibility(View.GONE);
        countDownTimer.onTick(mStep.getDuration() * 1000);

        gaitRecorder.stopRecording();
    }

    @Override
    protected void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        gaitRecorder.unregisterListeners();
    }

    /**
     * Creates tooltips on the screen to guide the user through the application.
     * @param view View that the tooltip will be attached (pointing towards to).
     * @param gravity Specifies the position the tooltip will be placed relative to the attached view.
     * @param text The text that will be siplayed as a message on the tooltip.
     */
    public void createToolTip(View view, Tooltip.Gravity gravity, String text){
        Tooltip.make(getContext(),
                new Tooltip.Builder(101)
                        .anchor(view, gravity)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(true, false), 3000)
                        .activateDelay(800)
                        .showDelay(300)
                        .text(text)
                        .maxWidth(700)
                        .withArrow(true)
                        .withOverlay(true)
                        .withStyleId(R.style.ToolTipLayoutCustomStyle)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .build()
        ).show();
    }

    public void requestPermissions(Activity thisActivity){
        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        102);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }

}
