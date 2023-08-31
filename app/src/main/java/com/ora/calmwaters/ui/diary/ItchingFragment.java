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

import calmwaters.R;

public class ItchingFragment extends SymptomFragment{

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

        entryName.setText("Itching");
        leftEyeEntryRating.setValueFrom(0);
        leftEyeEntryRating.setValueTo(5);
        leftEyeEntryRating.setStepSize(0.5f);
        rightEyeEntryRating.setValueFrom(0);
        rightEyeEntryRating.setValueTo(5);
        rightEyeEntryRating.setStepSize(0.5f);
        rightEntryMinRateText.setText("0");
        rightEntryMaxRateText.setText("5");
        leftEntryMinRateText.setText("0");
        leftEntryMaxRateText.setText("5");
        entryDescription.setText(getString(R.string.itching));

        submit.setText(getString(R.string.next));

        submit.setOnClickListener(v -> moveToNext());
    }

    @Override
    public void moveToNext() {
        writeToFile();
        NavHostFragment.findNavController(ItchingFragment.this).navigate(
                R.id.action_itchingFragment_to_itchyNoseFragment
        );
    }
}
