package com.zendesk.colourguesser;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final LinearLayout.LayoutParams CONTAINER_LAYOUT_PARAMS = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    private static final LinearLayout.LayoutParams WRAP_LAYOUT_PARAMS = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    private static final int COLOUR_BLACK_ACTIVE = Color.argb(138, 0, 0, 0);
    private static final int COLOUR_WHITE_ACTIVE = Color.WHITE;

    private MenuItem toolbarSendMenuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        installToolBar(this);

        findViewById(R.id.main_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUiElements();

                if (toolbarSendMenuItem != null) {
                    toolbarSendMenuItem.setEnabled(!toolbarSendMenuItem.isEnabled());
                }
            }
        });
    }

    private void addUiElements() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.main_grid);
        linearLayout.removeAllViews();

        int padding = dpToPixels(8, getResources().getDisplayMetrics());

        Random random = new Random(System.currentTimeMillis());

        int maxColor = 256;

        for (int i = 0; i < 6; i++) {
            TextView textView = new TextView(this);
            textView.setPadding(padding, padding, padding, padding / 2);
            textView.setLayoutParams(CONTAINER_LAYOUT_PARAMS);

            int randomColor = Color.argb(255, random.nextInt(maxColor), random.nextInt(maxColor), random.nextInt(maxColor));
            textView.setBackgroundColor(randomColor);


            int contrastColor = getColorToContrastWith(randomColor);
            textView.setTextColor(contrastColor);

            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
            textView.setGravity(Gravity.CENTER);
            textView.setText(R.string.contrast_label);

            LinearLayout innerLayout = new LinearLayout(this);
            innerLayout.setOrientation(LinearLayout.HORIZONTAL);
            innerLayout.setLayoutParams(CONTAINER_LAYOUT_PARAMS);
            innerLayout.setBackgroundColor(randomColor);

            Drawable iconDrawable = tintDrawableToContrastWith(
                    getResources().getDrawable(R.drawable.ic_send_black_24dp),
                    randomColor
            );

            final AppCompatImageView buttonView = new AppCompatImageView(this);
            buttonView.setPadding(padding, padding / 2, padding, padding);
            buttonView.setImageDrawable(iconDrawable);
            buttonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonView.setEnabled(false);
                }
            });

            TextView colourInformation = new TextView(this);
            colourInformation.setTextColor(contrastColor);
            colourInformation.setLayoutParams(WRAP_LAYOUT_PARAMS);
            colourInformation.setText(String.format(Locale.US, "R:%d G:%d B:%d", Color.red(randomColor), Color.green(randomColor), Color.blue(randomColor)));

            innerLayout.addView(buttonView);
            innerLayout.addView(colourInformation);
            linearLayout.addView(textView);
            linearLayout.addView(innerLayout);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        toolbarSendMenuItem = menu.getItem(0);

        int colorPrimary = themeAttributeToColor(R.attr.colorPrimary, this, android.R.color.holo_blue_bright);

        Drawable tintedSendIcon = tintDrawableToContrastWith(toolbarSendMenuItem.getIcon(), colorPrimary);
        toolbarSendMenuItem.setIcon(tintedSendIcon);

        return true;
    }

    private @ColorInt int getColorToContrastWith(@ColorInt int otherColor) {
        double relativeLuminance = getRelativeLuminance(otherColor);

        return relativeLuminance > 0.179
                ? COLOUR_BLACK_ACTIVE
                : COLOUR_WHITE_ACTIVE;
    }

    private Drawable tintDrawableToContrastWith(Drawable iconDrawable, int otherColor) {
        double relativeLuminance = getRelativeLuminance(otherColor);

        return relativeLuminance > 0.179
                ? mutateList(iconDrawable, getResources().getColorStateList(R.color.dark))
                : mutateList(iconDrawable, getResources().getColorStateList(R.color.light));
    }

    private double getRelativeLuminance(@ColorInt int color) {

        double[] colourComponents = new double[] {
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        };

        for (int i = 0; i < colourComponents.length; i++) {
            colourComponents[i] /= 255.0;

            colourComponents[i] = colourComponents[i] <= 0.03928
                    ? colourComponents[i] / 12.92
                    : Math.pow((colourComponents[i] + 0.055) / 1.055, 2.4);
        }

        return (0.2126 * colourComponents[0]) + (0.7152 * colourComponents[1]) + (0.0722 * colourComponents[2]);
    }

    private Drawable mutateList(Drawable drawable, ColorStateList colorStateList) {
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable.mutate(), colorStateList);
        return wrappedDrawable;
    }

    @SuppressWarnings("unused")
    private void mutate(Drawable drawable, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        Drawable mutate = wrappedDrawable.mutate();
        DrawableCompat.setTint(mutate, color);
    }

    private void installToolBar(AppCompatActivity activity){
        final ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null){
            return;
        }

        final View toolBarContainer = activity.findViewById(R.id.zd_toolbar_container);

        if (toolBarContainer == null){
            return;
        }

        toolBarContainer.setVisibility(View.VISIBLE);

        final Toolbar toolbar = (Toolbar)activity.findViewById(R.id.zd_toolbar);
        activity.setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            final View toolBarShadow = activity.findViewById(R.id.zd_toolbar_shadow);
            if (toolBarShadow != null) {
                toolBarShadow.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Converts a attribute id in the current theme to the corresponding resolved color value.
     * <p>
     *     This method will resolve a color that has been specified in a style XML file like this:
     *     <br>
     *         {@code <toolbarSendMenuItem name="colorPrimary">@color/my_primary_color</toolbarSendMenuItem>}
     *     <br>
     *     This will return the value of {@code @color/my_primary_color}.
     * </p>
     * @param themeAttributeId The color attr id
     * @param context Must be a UI context, must not be an application context. It must also be
     *                using the theme the attribute is declared in.
     * @param fallbackColorId Only used when there was an issue looking up the attribute color ID,
     *                        as a second choice. This should be a color reference directly, as in
     *                        {@code R.color} rather than {@code R.attr}
     * @return The resolved color value of themeAttributeId, if found, or fallbackColorId, if not.
     *         If invalid IDs, or a null context is supplied, then {@link Color#BLACK} will be returned
     */
    public  int themeAttributeToColor(@AttrRes int themeAttributeId, @NonNull Context context,
                                            @ColorRes int fallbackColorId) {

        //noinspection ConstantConditions
        if (themeAttributeId == 0 || context == null || fallbackColorId == 0) {
            return Color.BLACK;
        }

        TypedValue outValue = new TypedValue();
        Resources.Theme theme = context.getTheme();

        boolean wasResolved = theme.resolveAttribute(themeAttributeId, outValue, true);

        if (!wasResolved) {
            return resolveColor(fallbackColorId, context);
        } else {

            /*
                Whoa Nelly! What's going on here. Well, it turns out that if you have an
                attribute pointing to a literal color as opposed to an @color/foo reference,
                then you get the actual color in outValue.data rather than getting a resourceId.
             */
            return outValue.resourceId == 0
                    ? outValue.data
                    : resolveColor(outValue.resourceId, context);
        }
    }

    /**
     * Resolves a color ID to a color resource.
     * @param colorId The color ID.
     * @param context Must be a UI context, must not an application context.
     * @return The resolved color resource.
     */
    private static int resolveColor(@ColorRes int colorId, @NonNull Context context) {
        return ContextCompat.getColor(context, colorId);
    }

    private static int dpToPixels(int sizeInDp, DisplayMetrics displayMetrics) {

        return Math.round(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        (float) sizeInDp,
                        displayMetrics)
        );
    }
}
