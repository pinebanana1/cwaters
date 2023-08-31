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

public class SwellingFragment extends SymptomFragment{
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

        entryName.setText("Swelling");
        leftEyeEntryRating.setValueFrom(0);
        leftEyeEntryRating.setValueTo(3);
        rightEyeEntryRating.setValueFrom(0);
        rightEyeEntryRating.setValueTo(3);
        rightEntryMinRateText.setText("0");
        rightEntryMaxRateText.setText("3");
        leftEntryMinRateText.setText("0");
        leftEntryMaxRateText.setText("3");
        entryDescription.setText(getString(R.string.swelling));

        submit.setText(getString(R.string.next));

        submit.setOnClickListener(v -> moveToNext());
    }

    @Override
    public void moveToNext() {
        writeToFile();
        NavHostFragment.findNavController(SwellingFragment.this).navigate(
                R.id.action_swellingFragment_to_tearingFragment
        );
    }
}
