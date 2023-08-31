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

public class SneezingFragment extends SymptomFragment{
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

        entryName.setText("Sneezing");
        leftEyeEntryRating.setValueFrom(0);
        leftEyeEntryRating.setValueTo(4);
        rightEyeEntryRating.setValueFrom(0);
        rightEyeEntryRating.setValueTo(4);
        rightEntryMinRateText.setText("0");
        rightEntryMaxRateText.setText("4");
        leftEntryMinRateText.setText("0");
        leftEntryMaxRateText.setText("4");
        entryDescription.setText(getString(R.string.sneezing));

        submit.setText(getString(R.string.next));

        submit.setOnClickListener(v -> moveToNext());
    }

    @Override
    public void moveToNext() {
        writeToFile();
        NavHostFragment.findNavController(SneezingFragment.this).navigate(
                R.id.action_sneezingFragment_to_swellingFragment
        );
    }
}
