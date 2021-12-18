package com.luck.picture.lib.adapter.holder;

import android.graphics.ColorFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.style.SelectMainStyle;
import com.luck.picture.lib.utils.StyleUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author：luck
 * @date：2019-11-30 20:50
 * @describe：preview gallery
 */
public class PreviewGalleryAdapter extends RecyclerView.Adapter<PreviewGalleryAdapter.ViewHolder> {
    private final List<LocalMedia> mData;

    public PreviewGalleryAdapter(List<LocalMedia> list) {
        this.mData = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.preview_gallery_item, parent, false);
        return new ViewHolder(itemView);
    }

    public List<LocalMedia> getData() {
        return mData;
    }

    public void clear() {
        mData.clear();
    }

    /**
     * 添加选中的至画廊效果里
     *
     * @param currentMedia
     */
    public void addGalleryData(boolean isBottomPreview, LocalMedia currentMedia) {
        int lastCheckPosition = getLastCheckPosition();
        if (lastCheckPosition != RecyclerView.NO_POSITION) {
            LocalMedia lastSelectedMedia = mData.get(lastCheckPosition);
            lastSelectedMedia.setChecked(false);
            notifyItemChanged(lastCheckPosition);
        }
        if (isBottomPreview && mData.contains(currentMedia)) {
            int currentPosition = getCurrentPosition(currentMedia);
            LocalMedia media = mData.get(currentPosition);
            media.setMaxSelectEnabledMask(false);
            media.setChecked(true);
            notifyItemChanged(currentPosition);
        } else {
            currentMedia.setChecked(true);
            mData.add(currentMedia);
            notifyItemChanged(mData.size() - 1);
        }
    }

    /**
     * 移除画廊中未选中的结果
     *
     * @param currentMedia
     */
    public void removeGalleryData(boolean isBottomPreview, LocalMedia currentMedia) {
        int currentPosition = getCurrentPosition(currentMedia);
        if (currentPosition != RecyclerView.NO_POSITION) {
            if (isBottomPreview) {
                LocalMedia media = mData.get(currentPosition);
                media.setMaxSelectEnabledMask(true);
                notifyItemChanged(currentPosition);
            } else {
                mData.remove(currentPosition);
                notifyItemRemoved(currentPosition);
            }
        }
    }

    /**
     * 当前LocalMedia是否选中
     *
     * @param currentMedia
     */
    public void isSelectMedia(LocalMedia currentMedia) {
        int lastCheckPosition = getLastCheckPosition();
        if (lastCheckPosition != RecyclerView.NO_POSITION) {
            LocalMedia lastSelectedMedia = mData.get(lastCheckPosition);
            lastSelectedMedia.setChecked(false);
            notifyItemChanged(lastCheckPosition);
        }

        int currentPosition = getCurrentPosition(currentMedia);
        if (currentPosition != RecyclerView.NO_POSITION) {
            LocalMedia media = mData.get(currentPosition);
            media.setChecked(true);
            notifyItemChanged(currentPosition);
        }
    }

    /**
     * 获取画廊上一次选中的位置
     *
     * @return
     */
    public int getLastCheckPosition() {
        for (int i = 0; i < mData.size(); i++) {
            LocalMedia media = mData.get(i);
            if (media.isChecked()) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    /**
     * 获取当前画廊LocalMedia的位置
     *
     * @param currentMedia
     * @return
     */
    private int getCurrentPosition(LocalMedia currentMedia) {
        for (int i = 0; i < mData.size(); i++) {
            LocalMedia media = mData.get(i);
            if (TextUtils.equals(media.getPath(), currentMedia.getPath())
                    || media.getId() == currentMedia.getId()) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalMedia item = mData.get(position);
        ColorFilter colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                ContextCompat.getColor(holder.itemView.getContext(), item.isMaxSelectEnabledMask()
                        ? R.color.ps_color_half_white : R.color.ps_color_transparent), BlendModeCompat.SRC_ATOP);
        if (item.isChecked() && item.isMaxSelectEnabledMask()) {
            holder.viewBorder.setVisibility(View.VISIBLE);
        } else {
            holder.viewBorder.setVisibility(item.isChecked() ? View.VISIBLE : View.GONE);
        }
        String path = item.getPath();
        if (item.isEditorImage() && !TextUtils.isEmpty(item.getCutPath())) {
            path = item.getCutPath();
            holder.ivEditor.setVisibility(View.VISIBLE);
        } else {
            holder.ivEditor.setVisibility(View.GONE);
        }
        holder.ivImage.setColorFilter(colorFilter);
        if (PictureSelectionConfig.imageEngine != null) {
            PictureSelectionConfig.imageEngine.loadImage(holder.itemView.getContext(), path, holder.ivImage);
        }
        holder.ivPlay.setVisibility(PictureMimeType.isHasVideo(item.getMimeType()) ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onItemClick(holder.getAbsoluteAdapterPosition(), item, view);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mItemLongClickListener != null) {
                    int adapterPosition = holder.getAbsoluteAdapterPosition();
                    mItemLongClickListener.onItemLongClick(holder, adapterPosition, v);
                }
                return true;
            }
        });
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageView ivPlay;
        ImageView ivEditor;
        View viewBorder;

        public ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            ivPlay = itemView.findViewById(R.id.ivPlay);
            ivEditor = itemView.findViewById(R.id.ivEditor);
            viewBorder = itemView.findViewById(R.id.viewBorder);
            SelectMainStyle selectMainStyle = PictureSelectionConfig.selectorStyle.getSelectMainStyle();
            if (StyleUtils.checkStyleValidity(selectMainStyle.getAdapterImageEditorResources())) {
                ivEditor.setImageResource(selectMainStyle.getAdapterImageEditorResources());
            }
            if (StyleUtils.checkStyleValidity(selectMainStyle.getAdapterPreviewGalleryFrameResource())) {
                viewBorder.setBackgroundResource(selectMainStyle.getAdapterPreviewGalleryFrameResource());
            }

            int adapterPreviewGalleryItemSize = selectMainStyle.getAdapterPreviewGalleryItemSize();
            if (StyleUtils.checkSizeValidity(adapterPreviewGalleryItemSize)) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                        (adapterPreviewGalleryItemSize, adapterPreviewGalleryItemSize);
                itemView.setLayoutParams(params);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private OnItemClickListener listener;

    public void setItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, LocalMedia media, View v);
    }

    private OnItemLongClickListener mItemLongClickListener;

    public void setItemLongClickListener(OnItemLongClickListener listener) {
        this.mItemLongClickListener = listener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(RecyclerView.ViewHolder holder, int position, View v);
    }
}
