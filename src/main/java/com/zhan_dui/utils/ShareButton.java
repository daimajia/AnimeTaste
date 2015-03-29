package com.zhan_dui.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhan_dui.animetaste.R;

public class ShareButton extends LinearLayout {

    private ImageView mButton;
    private TextView mSocial;
    private Drawable mImage;
    private String mText;

    public ShareButton(Context context) {
        this(context, null);
    }

    public ShareButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ShareButton,
                0, 0);
        mText = attributes.getString(R.styleable.ShareButton_description);
        mImage = attributes.getDrawable(R.styleable.ShareButton_icons);
        LayoutInflater inflater = LayoutInflater.from(context);
        mButton = (ImageView) inflater.inflate(R.layout.share_image_button, null);
        mSocial = (TextView) inflater.inflate(R.layout.share_image_text, null);
        mSocial.setText(mText);
        setOrientation(VERTICAL);
        mButton.setImageDrawable(mImage);
        setGravity(Gravity.CENTER);
        addView(mButton);
        addView(mSocial);
        attributes.recycle();
    }


    @Override
    public void setOnClickListener(OnClickListener l) {
        mButton.setOnClickListener(l);
    }
}
