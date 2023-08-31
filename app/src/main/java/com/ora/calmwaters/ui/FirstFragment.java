package com.ora.calmwaters.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import calmwaters.R;

import java.lang.invoke.MethodHandles;

public class FirstFragment extends Fragment {
    private static String TAG = "OraHomePilot " +  MethodHandles.lookup().lookupClass();
    private int protocolPosition = 0;
    private static final String[] protocols = {
            "21-100-0003"
    };

    public enum Fields {
        SUBJECT_ID(1), PROTOCOL_ID(2), ASSESSMENT(4);
        private int mField;
        static int mask = mask();

        Fields(int i) {
            mField = i;
        }
        private static int mask() {
            int mask = 0;
            for(Fields f : Fields.values())
                mask |= f.mField;
            return mask;
        }
    }
    private int mState = Fields.PROTOCOL_ID.mField | Fields.ASSESSMENT.mField;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
//            }
//        });

        RadioGroup eyeGroup = view.findViewById(R.id.eyeLRGrp);
        int eyeSelected = eyeGroup.getCheckedRadioButtonId(); // Returns View.NO_ID if nothing is checked.
        switch (eyeSelected) {
            case R.id.leftEye:
                ProtocolActivity.side = ProtocolActivity.EyeSide.Left;
                break;
            case R.id.rightEye:
                ProtocolActivity.side = ProtocolActivity.EyeSide.Right;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + eyeSelected);
        }

        eyeGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    Log.v(TAG, "id" + checkedId);
                    SwitchMaterial dirSwitch = (SwitchMaterial) view.findViewById(R.id.directionSwitch);

                    if (checkedId == R.id.leftEye) {
                        ProtocolActivity.side = ProtocolActivity.EyeSide.Left;

                    } else if (checkedId == R.id.rightEye) {
                        ProtocolActivity.side = ProtocolActivity.EyeSide.Right;
                    }
                    else {
                        throw new IllegalStateException("Unexpected value: " + checkedId);
                    }
                    //flip direction to force a relayout
//                    ProtocolActivity.direction = ProtocolActivity.direction == ProtocolActivity.EyeDirection.Superior? ProtocolActivity.EyeDirection.Front : ProtocolActivity.EyeDirection.Superior;
//                    ((SwitchMaterial) view.findViewById(R.id.directionSwitch)).setChecked(true);
                });

        SwitchMaterial dirSwitch = (SwitchMaterial) view.findViewById(R.id.directionSwitch);
        TextView frontText = view.findViewById(R.id.frontText);
        TextView superiorText = view.findViewById(R.id.superiorText);
        superiorText.setVisibility(View.INVISIBLE);

        //ConstraintLayout constraintLayout = (ConstraintLayout) dirSwitch.getParent();
        dirSwitch.setOnCheckedChangeListener((matSwitch, b) -> {

            if (ProtocolActivity.direction == ProtocolActivity.EyeDirection.Front) {
                ProtocolActivity.direction = ProtocolActivity.EyeDirection.Up;

                //matSwitch.setText("Superior");
                frontText.setVisibility(View.INVISIBLE);
                superiorText.setVisibility(View.VISIBLE);
//                        layoutParams.startToStart = R.id.eyeLRGrp;
//                        layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;
            } else {
                ProtocolActivity.direction = ProtocolActivity.EyeDirection.Front;
                //matSwitch.setText("Front-facing");
                frontText.setVisibility(View.VISIBLE);
                superiorText.setVisibility(View.INVISIBLE);
//                        layoutParams.endToEnd = R.id.eyeLRGrp;
//                        layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET;
            }
        });
    }

    void validatePhotoFab(Fields field) {
        mState |= field.mField;

        if ((mState & Fields.mask) == Fields.mask) {
            ((ProtocolActivity)getActivity()).enableButton(true);
        }
    }

    void invalidatePhotoFab(Fields field) {
        mState &= ~field.mField;
        ((ProtocolActivity)getActivity()).enableButton(false);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

//        AutoCompleteTextView assessment = (AutoCompleteTextView) requireView().findViewById(R.id.assessment);
//        assessment.showDropDown();
//        //assessmentView.setSelection(assessmentIndex);
//        int pos = assessment.getListSelection();
//        assessment.setListSelection(1);
//        pos = assessment.getListSelection();
    }
}