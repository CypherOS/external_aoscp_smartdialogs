package com.aoscp.utils.smartdialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.res.Resources;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SmartDialog {
    protected final Builder mBuilder;

    public final Builder getBuilder() {
        return mBuilder;
    }

    protected SmartDialog(Builder builder) {
        mBuilder = builder;
        mBuilder.smartDialog = initSmartDialog(builder);
    }

    @UiThread
    public void show() {
        if (mBuilder != null && mBuilder.smartDialog != null)
            mBuilder.smartDialog.show();
    }

    @UiThread
    public void dismiss() {
        if (mBuilder != null && mBuilder.smartDialog != null)
            mBuilder.smartDialog.dismiss();
    }

    @UiThread
    private Dialog initSmartDialog(final Builder builder) {
        final Dialog smartDialog = new Dialog(builder.context, R.style.SmartDialog);
        View view = LayoutInflater.from(builder.context).inflate(R.layout.smart_dialogs, null);

        ImageView vIcon = (ImageView) view.findViewById(R.id.smartDialog_icon);
        TextView vTitle = (TextView) view.findViewById(R.id.smartDialog_title);
        TextView vContent = (TextView) view.findViewById(R.id.smartDialog_content);
        FrameLayout vCustomView = (FrameLayout) view.findViewById(R.id.smartDialog_custom_view);
        Button vNegative = (Button) view.findViewById(R.id.smartDialog_cancel);
        Button vPositive = (Button) view.findViewById(R.id.smartDialog_ok);

        if (builder.icon != null) {
            vIcon.setVisibility(View.VISIBLE);
            vIcon.setImageDrawable(builder.icon);
        }

        if (builder.title != null) {
            vTitle.setText(builder.title);
        }

        if (builder.content != null) {
            vContent.setText(builder.content);
        }

        if (builder.customView != null) {
            if (builder.customView.getParent() != null)
                ((ViewGroup) builder.customView.getParent()).removeAllViews();
            vCustomView.addView(builder.customView);
            vCustomView.setPadding(builder.customViewPaddingLeft, builder.customViewPaddingTop, builder.customViewPaddingRight, builder.customViewPaddingBottom);
        }

        if (builder.btn_positive != null) {
            vPositive.setVisibility(View.VISIBLE);
            vPositive.setText(builder.btn_positive);
            vPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (builder.btn_positive_callback != null)
                        builder.btn_positive_callback.onClick(SmartDialog.this);
                    if (builder.isAutoDismiss)
                        smartDialog.dismiss();
                }
            });
        }

        if (builder.btn_negative != null) {
            vNegative.setVisibility(View.VISIBLE);
            vNegative.setText(builder.btn_negative);
            vNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (builder.btn_negative_callback != null)
                        builder.btn_negative_callback.onClick(SmartDialog.this);
                    if (builder.isAutoDismiss)
                        smartDialog.dismiss();
                }
            });
        }

        smartDialog.setContentView(view);
        smartDialog.setCancelable(builder.isCancelable);
        smartDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        smartDialog.getWindow().setGravity(Gravity.BOTTOM);

        return smartDialog;
    }

    public static class Builder {
        protected Context context;

        // Smart Dialog
        protected Dialog smartDialog;

        // Icon, Title and Content
        protected Drawable icon;
        protected CharSequence title, content;

        // Buttons
        protected CharSequence btn_negative, btn_positive;
        protected ButtonCallback btn_negative_callback, btn_positive_callback;
        protected boolean isAutoDismiss;

        // Custom View
        protected View customView;
        protected int customViewPaddingLeft, customViewPaddingTop, customViewPaddingRight, customViewPaddingBottom;

        // Other options
        protected boolean isCancelable;

        public Builder(@NonNull Context context) {
            this.context = context;
            this.isCancelable = true;
            this.isAutoDismiss = true;
        }

        public Builder setTitle(@StringRes int titleRes) {
            setTitle(this.context.getString(titleRes));
            return this;
        }

        public Builder setTitle(@NonNull CharSequence title) {
            this.title = title;
            return this;
        }

        public Builder setContent(@StringRes int contentRes) {
            setContent(this.context.getString(contentRes));
            return this;
        }

        public Builder setContent(@NonNull CharSequence content) {
            this.content = content;
            return this;
        }

        public Builder setIcon(@NonNull Drawable icon) {
            this.icon = icon;
            return this;
        }

        public Builder setIcon(@DrawableRes int iconRes) {
            this.icon = ResourcesCompat.getDrawable(context.getResources(), iconRes, null);
            return this;
        }

        public Builder setPositiveBackgroundColorResource(@ColorRes int buttonColorRes) {
            this.btn_colorPositiveBackground = ResourcesCompat.getColor(context.getResources(), buttonColorRes, null);
            return this;
        }

        public Builder setPositiveBackgroundColor(int color) {
            this.btn_colorPositiveBackground = color;
            return this;
        }

        public Builder setPositiveText(@StringRes int buttonTextRes) {
            setPositiveText(this.context.getString(buttonTextRes));
            return this;
        }

        public Builder setPositiveText(@NonNull CharSequence buttonText) {
            this.btn_positive = buttonText;
            return this;
        }

        public Builder onPositive(@NonNull ButtonCallback buttonCallback) {
            this.btn_positive_callback = buttonCallback;
            return this;
        }

        public Builder setNegativeText(@StringRes int buttonTextRes) {
            setNegativeText(this.context.getString(buttonTextRes));
            return this;
        }

        public Builder setNegativeText(@NonNull CharSequence buttonText) {
            this.btn_negative = buttonText;
            return this;
        }

        public Builder onNegative(@NonNull ButtonCallback buttonCallback) {
            this.btn_negative_callback = buttonCallback;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.isCancelable = cancelable;
            return this;
        }

        public Builder autoDismiss(boolean autodismiss) {
            this.isAutoDismiss = autodismiss;
            return this;
        }

        public Builder setCustomView(View customView) {
            this.customView = customView;
            this.customViewPaddingLeft = 0;
            this.customViewPaddingRight = 0;
            this.customViewPaddingTop = 0;
            this.customViewPaddingBottom = 0;
            return this;
        }

        public Builder setCustomView(View customView, int left, int top, int right, int bottom) {
            this.customView = customView;
            this.customViewPaddingLeft = UtilsLibrary.dpToPixels(context, left);
            this.customViewPaddingRight = UtilsLibrary.dpToPixels(context, right);
            this.customViewPaddingTop = UtilsLibrary.dpToPixels(context, top);
            this.customViewPaddingBottom = UtilsLibrary.dpToPixels(context, bottom);
            return this;
        }

        @UiThread
        public SmartDialog build() {
            return new SmartDialog(this);
        }

        @UiThread
        public SmartDialog show() {
            SmartDialog smartDialog = build();
            smartDialog.show();
            return smartDialog;
        }

    }

    public interface ButtonCallback {

        void onClick(@NonNull SmartDialog dialog);
    }

}
