package com.ora.calmwaters.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.ora.calmwaters.AzureUploader;
import com.ora.calmwaters.LogHelper;
import com.ora.calmwaters.OverlayService;
import com.ora.calmwaters.data.localstorage.FileZipHelper;
import com.ora.calmwaters.ui.diary.SymptomFragment;

import java.io.File;
import java.util.ArrayList;

import calmwaters.R;

public class PatientInfoFragment extends Fragment {

    private Button pavLoviaButton;
    private Button recordVideoButton;
    private Button takePictureButton;
    private Spinner caseSelection;
    private MaterialAutoCompleteTextView subjectTV;
    private TextInputEditText visitNumTV;
    private Button submitButton;
    private RadioGroup eyeSelection;
    private SwitchMaterial eyeDirection;

    private final int DIARY_REQUEST = 1777;
    private final int CAMERA_REQUEST = 1888;
    private final int VIDEO_REQUEST = 1889;

    private boolean diaryStep = true; //diary should be first
    private boolean breakUpStep = false;
    private boolean rednessStep = false;

    private enum StudyStep{
        NOT_A_STEP(0),
        ENTER_DIARY(1),
        TAKE_VIDEO(2),
        TAKE_PICTURE(3),
        SUBMIT(4);

        private int value;

        StudyStep(int value) {
            this.value = value;
        }

        public int stepValue(){
            return value;
        }

        public StudyStep getStep(int stepValue) {
            if (stepValue == ENTER_DIARY.stepValue()) {
                return ENTER_DIARY;
            }
            else if (stepValue == TAKE_VIDEO.stepValue()) {
                return TAKE_VIDEO;
            }
            else if (stepValue == TAKE_PICTURE.stepValue()) {
                return TAKE_PICTURE;
            }
            else if (stepValue == SUBMIT.stepValue()) {
                return SUBMIT;
            }
            return null;
        }
    }
    private StudyStep currentStep;
    public static final String CURRENT_STEP_EXTRA = "current_step";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pavLoviaButton = view.findViewById(R.id.pavloviaButton);
        pavLoviaButton.setOnClickListener(v -> {
            if (!infoFieldsAreEmpty()) {
                startDiary();
            }
        }
        );

        recordVideoButton = view.findViewById(R.id.videoButton);
        recordVideoButton.setOnClickListener(v -> {
            if (checkIfEyeSideSelected()) recordVideoForImagingBreakup();
        });

        takePictureButton = view.findViewById(R.id.photoButton);
        takePictureButton.setOnClickListener(v -> {
                    if (checkIfEyeSideSelected()) takePhotoForRednessAssessment();
                });

        caseSelection = view.findViewById(R.id.caseSelection);

        subjectTV = view.findViewById(R.id.subjectId);

        visitNumTV = view.findViewById(R.id.visitNumber);

        eyeSelection = view.findViewById(R.id.eyeLRGrp);
        eyeSelection.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    if (checkedId == R.id.leftEye) {
                        ProtocolActivity.side = ProtocolActivity.EyeSide.Left;
                    }
                    else if (checkedId == R.id.rightEye) {
                        ProtocolActivity.side = ProtocolActivity.EyeSide.Right;
                    }
                    else {
                        throw new IllegalStateException("Unexpected value: " + checkedId);
                    }
                }
        );

        eyeDirection = view.findViewById(R.id.directionSwitch);
        eyeDirection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (ProtocolActivity.direction == ProtocolActivity.EyeDirection.Front) {
                ProtocolActivity.direction = ProtocolActivity.EyeDirection.Up;
            } else {
                ProtocolActivity.direction = ProtocolActivity.EyeDirection.Front;
            }
        });

        submitButton = view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> {
            if (submitDiaryToAzure()) {
                //clear the intent data
                requireActivity().getIntent().removeExtra(SymptomFragment.SUBJECT_EXTRA);
                requireActivity().getIntent().removeExtra(SymptomFragment.VISIT_EXTRA);
                requireActivity().getIntent().removeExtra(SymptomFragment.PROTOCOL_EXTRA);
                requireActivity().getIntent().putExtra(CURRENT_STEP_EXTRA, 1);

                //reset the input
                subjectTV.setText("");
                visitNumTV.setText("");
                caseSelection.setSelection(0);

                updateStep();
                updateUIWithStep();
            }
        });

        updateStep();
        updateUIWithStep();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_patient_info, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        LogHelper.d("PatientInfo - onResume()");
        Intent activityIntent = requireActivity().getIntent();
        if (activityIntent.hasExtra(SymptomFragment.SUBJECT_EXTRA)) {
            subjectTV.setText(activityIntent.getStringExtra(SymptomFragment.SUBJECT_EXTRA));
            visitNumTV.setText(activityIntent.getStringExtra(SymptomFragment.VISIT_EXTRA));
            Adapter adapter = caseSelection.getAdapter();
            ArrayList<String> options = new ArrayList<>();
            for (int i = 0; i < adapter.getCount(); i++) {
                options.add((String)adapter.getItem(i));
            }
            caseSelection.setSelection(options.indexOf(activityIntent.getStringExtra(SymptomFragment.PROTOCOL_EXTRA)));
        }
        updateStep();
        updateUIWithStep();
    }

    public boolean infoFieldsAreEmpty() {
        String subjectID = subjectTV.getText().toString().trim();
        String visitNum = visitNumTV.getText().toString().trim();

        boolean emptyField = false;

        if (subjectID.isEmpty()) {
            emptyField = true;
            subjectTV.setError(getString(R.string.patient_info_error_general));
        }

        if (visitNum.isEmpty()) {
            emptyField = true;
            visitNumTV.setError(getString(R.string.patient_info_error_general));
        }

        return emptyField;
    }

//    public void startPavlovDiary() {
//
//        String protocol = caseSelection.getSelectedItem().toString();
//        String subjectID = subjectTV.getText().toString();
//        String assessment = "";
//        String visitNum = visitNumTV.getText().toString();
//
//
//        Intent diaryIntent = new Intent(requireActivity(), DiaryActivity.class);
//        diaryIntent.setData(Uri.parse(getResources().getString(R.string.diary_url)));
//        diaryIntent.putExtra("protocol", protocol);
//        diaryIntent.putExtra("visit number", visitNum);
//        diaryIntent.putExtra("subjectid", subjectID);
//        diaryIntent.putExtra("Assessment", assessment);
//        diaryIntent.putExtra("Timepoint", new Date(System.currentTimeMillis()).toString());
//        diaryIntent.putExtra("DeviceID", Settings.Global.getString(requireActivity().getContentResolver(), Settings.Global.DEVICE_NAME));
//        startActivityForResult(diaryIntent, DIARY_REQUEST);
//
//    }

    public void startDiary() {
        requireActivity().getIntent().putExtra(SymptomFragment.SUBJECT_EXTRA, subjectTV.getText().toString().trim());
        requireActivity().getIntent().putExtra(SymptomFragment.VISIT_EXTRA, visitNumTV.getText().toString().trim());
        requireActivity().getIntent().putExtra(SymptomFragment.PROTOCOL_EXTRA, caseSelection.getSelectedItem().toString());
        NavHostFragment.findNavController(PatientInfoFragment.this)
                .navigate(R.id.action_patientInfoFragment_to_congestionFragment);
    }

    public void recordVideoForImagingBreakup() {
        OverlayService.instance.startVideoRecording(requireActivity(), VIDEO_REQUEST);
    }

    public void takePhotoForRednessAssessment() {
        OverlayService.instance.startCamera(requireActivity(), CAMERA_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case DIARY_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        LogHelper.d("Diary completed successfully.");
                        diaryStep = false;
                        breakUpStep = true;
                        checkStepProcess();
                        break;

                    case Activity.RESULT_CANCELED:
                        LogHelper.d("Diary was cancelled.");
                        break;

                    default:
                        LogHelper.e("Error performing Pavlovian Diary error code: " + resultCode);
                        break;
                }
                break;

            case VIDEO_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        LogHelper.d("Camera video request completed successfully.");
                        breakUpStep = false;
                        rednessStep = true;
                        checkStepProcess();
                        break;

                    case Activity.RESULT_CANCELED:
                        LogHelper.d("Camera video request was cancelled.");
                        break;

                    default:
                        LogHelper.e("Error using camera for video: " + resultCode);
                        break;
                }
                break;
            case CAMERA_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        LogHelper.d("Camera picture request completed successfully.");
                        rednessStep = false;
                        checkStepProcess();
                        break;

                    case Activity.RESULT_CANCELED:
                        LogHelper.d("Camera picture request was cancelled.");
                        break;

                    default:
                        LogHelper.e("Error using camera for picture: " + resultCode);
                        break;
                }
                break;


        }
    }

    private void checkStepProcess(){
        if (diaryStep) {
            pavLoviaButton.setEnabled(true);
            recordVideoButton.setEnabled(false);
            recordVideoButton.setEnabled(false);
        } else if (breakUpStep) {
            pavLoviaButton.setEnabled(false);
            recordVideoButton.setEnabled(true);
            recordVideoButton.setEnabled(false);
        } else if (rednessStep) {
            pavLoviaButton.setEnabled(false);
            recordVideoButton.setEnabled(false);
            recordVideoButton.setEnabled(true);
        } else {

        }
    }

    private boolean submitDiaryToAzure() {
        String visitNum = requireActivity().getIntent().getStringExtra(SymptomFragment.VISIT_EXTRA);
        String subjectId = requireActivity().getIntent().getStringExtra(SymptomFragment.SUBJECT_EXTRA);
        String zipFileName = subjectId + "_" + visitNum + ".zip";
        String mediaDirectory = requireActivity().getExternalMediaDirs()[0].toString();

        File visitSessionFolder = new File(requireActivity().getExternalMediaDirs()[0].toString() + File.separator +
                "diaries" + File.separator
                + "subject_" + subjectId + File.separator
                + "visit_" + visitNum + File.separator);
        LogHelper.d(visitSessionFolder.toString());
        //zip the visit session
        try {
            LogHelper.d("PatientInfoFragment - submitDiaryToAzure(): attempting to zip folder \n" +
                    visitSessionFolder.toPath() + " " +
                    "then saving it to " +
                    mediaDirectory + File.separator + zipFileName);

            FileZipHelper.zipFolder(
                    visitSessionFolder.toPath(), //source
                    new File( mediaDirectory + File.separator + zipFileName).toPath()); //output

            LogHelper.d("PatientInfoFragment - submitDiaryToAzure(): attempting to grab folder from\n" +
                    mediaDirectory + File.separator +
                    zipFileName);

            AzureUploader.uploadDiary(requireContext(),
                    Uri.fromFile(new File(mediaDirectory + File.separator +
                    zipFileName))
            );
            return true;
        } catch (Exception e) {
            LogHelper.e("PatientInfoFragment - submitDiaryToAzure(): error is\n" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkIfEyeSideSelected() {
        if (eyeSelection.getCheckedRadioButtonId() == -1) {
            //show error
            new MaterialAlertDialogBuilder(requireContext())
                    .setMessage("Please select an eye before continuing.")
                    .setPositiveButton("OK",
                            (dialog, which) -> {
                                //do nothing on click.
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return false;
        } else {
            return true;
        }
    }

    private void updateStep() {
        LogHelper.d("patientInfoFragment - updateStep()");
        if (requireActivity().getIntent().getIntExtra(CURRENT_STEP_EXTRA, -1) != -1) {
            LogHelper.d("activity intent.data is not null. Assigning value from Int Extra");
            if (requireActivity().getIntent().getIntExtra(CURRENT_STEP_EXTRA, -1) > StudyStep.values().length) {
                LogHelper.e("activity intent.data is out of bounds: resetting to 1.");
                requireActivity().getIntent().putExtra(CURRENT_STEP_EXTRA, 1);
            }
            currentStep = StudyStep.values()[requireActivity().getIntent().getIntExtra(CURRENT_STEP_EXTRA, 1)];

        } else {
            LogHelper.d("activity intent.data is null. Assigning default value");
            currentStep = StudyStep.ENTER_DIARY;
        }
        LogHelper.d("currentStep: " + currentStep.toString());
    }

    private void updateUIWithStep() {
        LogHelper.d("Updating UI with step " + currentStep.toString());
        switch (currentStep) {
            case ENTER_DIARY:
                setInputEnabled(true);
                pavLoviaButton.setEnabled(true);
                recordVideoButton.setEnabled(false);
                takePictureButton.setEnabled(false);
                submitButton.setEnabled(false);
                break;

            case TAKE_VIDEO:
                setInputEnabled(false);
                pavLoviaButton.setEnabled(false);
                recordVideoButton.setEnabled(true);
                takePictureButton.setEnabled(false);
                submitButton.setEnabled(false);
                break;

            case TAKE_PICTURE:
                setInputEnabled(false);
                pavLoviaButton.setEnabled(false);
                recordVideoButton.setEnabled(false);
                takePictureButton.setEnabled(true);
                submitButton.setEnabled(false);
                break;

            case SUBMIT:
                setInputEnabled(false);
                pavLoviaButton.setEnabled(false);
                recordVideoButton.setEnabled(false);
                takePictureButton.setEnabled(false);
                submitButton.setEnabled(true);
                break;

            default:
                //do nothing in case an unexpected value is brought
                break;
        }
    }

    private void setInputEnabled(boolean isEnabled) {
        subjectTV.setEnabled(isEnabled);
        visitNumTV.setEnabled(isEnabled);
    }
}
