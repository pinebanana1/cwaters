package com.ora.calmwaters.ui.diary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.ora.calmwaters.LogHelper;
import com.ora.calmwaters.ui.PatientInfoFragment;

import calmwaters.R;

public class TearingFragment extends SymptomFragment{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LogHelper.d("SymptomFragment - onViewCreated");

        entryName.setText("Tearing");
        leftEyeEntryRating.setValueFrom(0);
        leftEyeEntryRating.setValueTo(4);
        rightEyeEntryRating.setValueFrom(0);
        rightEyeEntryRating.setValueTo(4);
        rightEntryMinRateText.setText("0");
        rightEntryMaxRateText.setText("4");
        leftEntryMinRateText.setText("0");
        leftEntryMaxRateText.setText("4");
        entryDescription.setText(getString(R.string.tearing));

        submit.setText(getString(R.string.next));

        submit.setOnClickListener(v -> {
            writeToFile();
            requireActivity().getIntent().putExtra(PatientInfoFragment.CURRENT_STEP_EXTRA, 2);
            LogHelper.d("TearingFragment - submitOnClick : intentData is " +
                    requireActivity().getIntent().getIntExtra(PatientInfoFragment.CURRENT_STEP_EXTRA, -1));
            displayAlertDialog(getString(R.string.symptomFragment_alert_message));
        });
    }

    @Override
    public void moveToNext() {
        LogHelper.d("TearingFragment - moveToNext()");
        //return to the PatientInfoFragment
        NavHostFragment.findNavController(TearingFragment.this).navigate(
                R.id.action_tearingFragment_to_patientInfoFragment
        );
    }
}
