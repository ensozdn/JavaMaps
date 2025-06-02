package com.enesozden.javamaps.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import java.util.List;

import com.enesozden.javamaps.model.Place;
import com.enesozden.javamaps.databinding.RecyclerRowBinding;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceHolder> {

    private final List<Place> placeList;
    private final OnPlaceClickListener listener;

    // 🔥 Constructor
    public PlaceAdapter(List<Place> placeList, OnPlaceClickListener listener) {
        this.placeList = placeList;
        this.listener = listener;
    }

    // 🔁 ViewHolder oluştur
    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PlaceHolder(binding);
    }

    // 🔄 ViewHolder’a veriyi bağla
    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {
        Place currentPlace = placeList.get(position);
        holder.binding.recyclerViewTextView.setText(currentPlace.name);

        // 🔥 Tıklama: dışarıdan gelen listener’a bildir
        holder.itemView.setOnClickListener(view -> listener.onPlaceClick(currentPlace));

    }

    // 📏 Eleman sayısı
    @Override
    public int getItemCount() {
        return placeList.size();
    }

    // 📦 ViewHolder sınıfı
    public static class PlaceHolder extends RecyclerView.ViewHolder {
        RecyclerRowBinding binding;

        public PlaceHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    // 👂 Interface (dışarıya olay bildirmek için)
    public interface OnPlaceClickListener {
        void onPlaceClick(Place place);
    }
}

