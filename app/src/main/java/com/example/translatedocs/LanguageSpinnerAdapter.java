package com.example.translatedocs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class LanguageSpinnerAdapter extends ArrayAdapter<LanguageItem> {
    public LanguageSpinnerAdapter(Context context, List<LanguageItem> languageList){
        super(context,0, languageList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item, parent, false);
        }

        ImageView imageViewFlag = convertView.findViewById(R.id.imageViewFlag);
        TextView textViewLanguageCode = convertView.findViewById(R.id.textViewLanguageCode);

        LanguageItem currentItem = getItem(position);

        if (currentItem != null) {
            imageViewFlag.setImageResource(currentItem.getFlagImage());
            textViewLanguageCode.setText(currentItem.getLanguageCode());
        }

        return convertView;
    }
}
