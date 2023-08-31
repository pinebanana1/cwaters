package com.ora.calmwaters.ui.diary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.ora.calmwaters.LogHelper;

import calmwaters.R;

public class CongestionFragment extends SymptomFragment{

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

        entryName.setText("Congestion");
        leftEyeEntryRating.setValueFrom(0);
        leftEyeEntryRating.setValueTo(4);
        rightEyeEntryRating.setValueFrom(0);
        rightEyeEntryRating.setValueTo(4);
        rightEntryMinRateText.setText("0");
        rightEntryMaxRateText.setText("4");
        leftEntryMinRateText.setText("0");
        leftEntryMaxRateText.setText("4");
        entryDescription.setText(getString(R.string.congestion));

        submit.setText(getString(R.string.next));

        submit.setOnClickListener(v -> {
            moveToNext();
        });
    }

    @Override
    public void moveToNext() {
        writeToFile();
        NavHostFragment.findNavController(CongestionFragment.this).navigate(
                R.id.action_congestionFragment_to_itchingFragment
        );
    }
}
