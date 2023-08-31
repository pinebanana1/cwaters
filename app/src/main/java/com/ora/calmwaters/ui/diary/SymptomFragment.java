package com.ora.calmwaters.ui.diary;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.ora.calmwaters.AzureUploader;
import com.ora.calmwaters.LogHelper;
import com.ora.calmwaters.data.localstorage.DiaryFileFormat;
import com.ora.calmwaters.data.localstorage.FileZipHelper;
import com.ora.calmwaters.data.localstorage.LocalDiary;

import java.io.File;

import calmwaters.R;

public class SymptomFragment extends Fragment {

    protected Slider leftEyeEntryRating;
    protected Slider rightEyeEntryRating;
    protected TextView rightEntryMinRateText;
    protected TextView rightEntryMaxRateText;
    protected TextView leftEntryMinRateText;
    protected TextView leftEntryMaxRateText;
    protected TextView entryDescription;
    protected TextView entryName;
    protected Button submit;

    public static final String SUBJECT_EXTRA = "subjectID";
    public static final String VISIT_EXTRA = "visitNumber";
    public static final String PROTOCOL_EXTRA = "protocolType";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d("SymptomFragment-> " + getClass().getSimpleName() + " - onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_symptom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LogHelper.d("SymptomFragment - onViewCreated");

        entryName = view.findViewById(R.id.entryName);
        leftEyeEntryRating = view.findViewById(R.id.leftEyeEntryRating);
        rightEyeEntryRating = view.findViewById(R.id.rightEyeEntryRating);
        rightEntryMinRateText = view.findViewById(R.id.rightEntryMinRateText);
        rightEntryMaxRateText = view.findViewById(R.id.rightEntryMaxRateText);
        leftEntryMinRateText = view.findViewById(R.id.leftEntryMinRateText);;
        leftEntryMaxRateText = view.findViewById(R.id.leftEntryMaxRateText);;
        entryDescription = view.findViewById(R.id.entryDescription);
        submit = view.findViewById(R.id.submitButton);

        entryName.setText("A Case");
        leftEyeEntryRating.setValueFrom(0);
        leftEyeEntryRating.setValueTo(5);
        rightEyeEntryRating.setValueFrom(0);
        rightEyeEntryRating.setValueTo(5);
        rightEntryMinRateText.setText("0");
        rightEntryMaxRateText.setText("5");
        leftEntryMinRateText.setText("0");
        leftEntryMaxRateText.setText("5");
        entryDescription.setText("A description");

        submit.setText(getString(R.string.next));

        //override in child classes
        submit.setOnClickListener(v -> moveToNext());

    }

    @Override
    public void onResume() {
        super.onResume();
        LogHelper.d("SymptomFragment - onResume");
    }

    /**
     * moveToNext needs to be overridden so that navigation is done either via:
     * -navGraph
     * -by fragmentTransaction using supportFragmentManager supplied by the hosting activity.
     */
    public void moveToNext() {
    }

    public String getSymptomName(){
     return this.getClass().getSimpleName().replace("Fragment", "");
    }

    /**
     * writeToFile will take the data of the  given as the parameter String data and append it to the file
     * of the current SymptomFragment class or its child classes.
     * Format is given by the enum DiaryFileFormat.Header's value
     * currently as of 9/26/2022:
     * SubjectID
     * Visit Number
     * Symptom Type
     * Rating Value
     */
    public void writeToFile() {
        String directoryName = requireActivity().getExternalMediaDirs()[0].toString();
        LogHelper.d("SymptomFragment - writeToFile(): directory name is " + directoryName);

        //file meta info should be: protocol, subjectID, vistID as per ImageBlob uploads

        new LocalDiary(
                new File(directoryName), //get directory

                //prefix = initial fileName
                this.getClass().getSimpleName().replace("Fragment", "") + '_' +
                requireActivity().getIntent().getStringExtra(PROTOCOL_EXTRA) + '_' +
                requireActivity().getIntent().getStringExtra(SUBJECT_EXTRA) + '_' +
                requireActivity().getIntent().getStringExtra(VISIT_EXTRA),

                //visit, subject, protocol per LocalDiary constructor
                requireActivity().getIntent().getStringExtra(VISIT_EXTRA),
                requireActivity().getIntent().getStringExtra(SUBJECT_EXTRA),
                requireActivity().getIntent().getStringExtra(PROTOCOL_EXTRA)
        )
                //content to write to the file
                .append(DiaryFileFormat.HEADER.value + //we apply the header values for CSV format
                        //the data to write (in CSV format)
                requireActivity().getIntent().getStringExtra(SUBJECT_EXTRA) +";" //SubjectID
                + requireActivity().getIntent().getStringExtra(VISIT_EXTRA) +";" //Visit Number
                + requireActivity().getIntent().getStringExtra(PROTOCOL_EXTRA) + ";"
                + getSymptomName() + ";" //Symptom Type
                + leftEyeEntryRating.getValue() + ";"//Rating Values
                + rightEyeEntryRating.getValue()
                );
    }

    public void displayAlertDialog(String text) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Diary Entries")
                .setMessage(text)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    this.moveToNext();
                })
                .create()
                .show();
    }

    protected boolean submitDiaryToAzure() {
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
            LogHelper.e("SymptomFragment - submitDiaryToAzure(): error is\n" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
