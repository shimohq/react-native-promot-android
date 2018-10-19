package im.shimo.react.prompt;


import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.dialog.DialogModule;

import java.util.Map;

import javax.annotation.Nullable;

@ReactModule(name = RNPromptModule.NAME)
public class RNPromptModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    /* package */ static final String FRAGMENT_TAG =
            "im.shimo.react.prompt.RNPromptModule";

    /* package */ static final String NAME = "PromptAndroid";

    /* package */ static final String ACTION_BUTTON_CLICKED = "buttonClicked";
    /* package */ static final String ACTION_DISMISSED = "dismissed";
    /* package */ static final String KEY_TITLE = "title";
    /* package */ static final String KEY_MESSAGE = "message";
    /* package */ static final String KEY_BUTTON_POSITIVE = "buttonPositive";
    /* package */ static final String KEY_BUTTON_NEGATIVE = "buttonNegative";
    /* package */ static final String KEY_BUTTON_NEUTRAL = "buttonNeutral";
    /* package */ static final String KEY_ITEMS = "items";
    /* package */ static final String KEY_CANCELABLE = "cancelable";
    /* package */ static final String KEY_TYPE = "type";
    /* package */ static final String KEY_STYLE = "style";
    /* package */ static final String KEY_DEFAULT_VALUE = "defaultValue";
    /* package */ static final String KEY_PLACEHOLDER = "placeholder";
    /* package */ static final String KEY_PLACEHOLDER_COLOR = "placeholderColor";
    /* package */ static final String KEY_DISABLE_FULL_SCREEN_UI = "disableFullscreenUI";
    /* package */ static final String KEY_HIGHLIGHT_COLOR = "highlightColor";
    /* package */ static final String KEY_COLOR = "color";
    /* package */ static final String KEY_BUTTON_COLOR = "buttonColor";

    /* package */ static final Map<String, Object> CONSTANTS = MapBuilder.<String, Object>of(
            ACTION_BUTTON_CLICKED, ACTION_BUTTON_CLICKED,
            ACTION_DISMISSED, ACTION_DISMISSED,
            KEY_BUTTON_POSITIVE, DialogInterface.BUTTON_POSITIVE,
            KEY_BUTTON_NEGATIVE, DialogInterface.BUTTON_NEGATIVE,
            KEY_BUTTON_NEUTRAL, DialogInterface.BUTTON_NEUTRAL);

    private boolean mIsInForeground;

    public RNPromptModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void initialize() {
        getReactApplicationContext().addLifecycleEventListener(this);
    }

    @Override
    public void onHostPause() {
        // Don't show the dialog if the host is paused.
        mIsInForeground = false;
    }

    @Override
    public void onHostDestroy() {
    }

    @Override
    public void onHostResume() {
        mIsInForeground = true;
        // Check if a dialog has been created while the host was paused, so that we can show it now.
        FragmentManagerHelper fragmentManagerHelper = getFragmentManagerHelper();
        if (fragmentManagerHelper != null) {
            fragmentManagerHelper.showPendingAlert();
        } else {
            FLog.w(DialogModule.class, "onHostResume called but no FragmentManager found");
        }
    }


    @ReactMethod
    public void promptWithArgs(ReadableMap options, final Callback callback) {
        final FragmentManagerHelper fragmentManagerHelper = getFragmentManagerHelper();
        if (fragmentManagerHelper == null) {
            FLog.w(RNPromptModule.class, "Tried to show an alert while not attached to an Activity");
            return;
        }

        final Bundle args = new Bundle();
        if (options.hasKey(KEY_TITLE)) {
            args.putString(RNPromptFragment.ARG_TITLE, options.getString(KEY_TITLE));
        }
        if (options.hasKey(KEY_MESSAGE)) {
            String message = options.getString(KEY_MESSAGE);
            if (!message.isEmpty()) {
                args.putString(RNPromptFragment.ARG_MESSAGE, options.getString(KEY_MESSAGE));
            }
        }
        if (options.hasKey(KEY_BUTTON_POSITIVE)) {
            args.putString(RNPromptFragment.ARG_BUTTON_POSITIVE, options.getString(KEY_BUTTON_POSITIVE));
        }
        if (options.hasKey(KEY_BUTTON_NEGATIVE)) {
            args.putString(RNPromptFragment.ARG_BUTTON_NEGATIVE, options.getString(KEY_BUTTON_NEGATIVE));
        }
        if (options.hasKey(KEY_BUTTON_NEUTRAL)) {
            args.putString(RNPromptFragment.ARG_BUTTON_NEUTRAL, options.getString(KEY_BUTTON_NEUTRAL));
        }
        if (options.hasKey(KEY_ITEMS)) {
            ReadableArray items = options.getArray(KEY_ITEMS);
            CharSequence[] itemsArray = new CharSequence[items.size()];
            for (int i = 0; i < items.size(); i++) {
                itemsArray[i] = items.getString(i);
            }
            args.putCharSequenceArray(RNPromptFragment.ARG_ITEMS, itemsArray);
        }
        if (options.hasKey(KEY_CANCELABLE)) {
            args.putBoolean(KEY_CANCELABLE, options.getBoolean(KEY_CANCELABLE));
        }
        if (options.hasKey(KEY_DISABLE_FULL_SCREEN_UI)) {
            args.putBoolean(KEY_DISABLE_FULL_SCREEN_UI, options.getBoolean(KEY_DISABLE_FULL_SCREEN_UI));
        }
        if (options.hasKey(KEY_HIGHLIGHT_COLOR) && !options.isNull(KEY_HIGHLIGHT_COLOR)) {
            args.putInt(KEY_HIGHLIGHT_COLOR, options.getInt(KEY_HIGHLIGHT_COLOR));
        }
        if (options.hasKey(KEY_COLOR) && !options.isNull(KEY_COLOR)) {
            args.putInt(KEY_COLOR, options.getInt(KEY_COLOR));
        }
        if (options.hasKey(KEY_TYPE)) {
            args.putString(KEY_TYPE, options.getString(KEY_TYPE));
        }
        if (options.hasKey(KEY_STYLE)) {
            args.putString(KEY_STYLE, options.getString(KEY_STYLE));
        }
        if (options.hasKey(KEY_DEFAULT_VALUE)) {
            args.putString(KEY_DEFAULT_VALUE, options.getString(KEY_DEFAULT_VALUE));
        }
        if (options.hasKey(KEY_PLACEHOLDER)) {
            args.putString(KEY_PLACEHOLDER, options.getString(KEY_PLACEHOLDER));
        }
        if (options.hasKey(KEY_PLACEHOLDER_COLOR) && !options.isNull(KEY_PLACEHOLDER_COLOR)) {
            args.putInt(KEY_PLACEHOLDER_COLOR, options.getInt(KEY_PLACEHOLDER_COLOR));
        }
        if (options.hasKey(KEY_BUTTON_COLOR) && !options.isNull(KEY_BUTTON_COLOR)) {
            args.putInt(KEY_BUTTON_COLOR, options.getInt(KEY_BUTTON_COLOR));
        }
        fragmentManagerHelper.showNewAlert(mIsInForeground, args, callback);
    }


    private class FragmentManagerHelper {

        // Exactly one of the two is null
        private final
        @Nullable
        android.app.FragmentManager mFragmentManager;

        private
        @Nullable
        RNPromptFragment mFragmentToShow;


        public FragmentManagerHelper(android.app.FragmentManager fragmentManager) {
            mFragmentManager = fragmentManager;
        }

        public void showPendingAlert() {
            if (mFragmentToShow == null) {
                return;
            }
            mFragmentToShow.show(mFragmentManager, FRAGMENT_TAG);
            mFragmentToShow = null;
        }

        private void dismissExisting() {
            if (mFragmentManager != null) {
                RNPromptFragment oldFragment =
                        (RNPromptFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TAG);
                if (oldFragment != null) {
                    oldFragment.dismiss();
                }
            }
        }

        public void showNewAlert(boolean isInForeground, Bundle arguments, Callback actionCallback) {
            dismissExisting();

            PromptFragmentListener actionListener =
                    actionCallback != null ? new PromptFragmentListener(actionCallback) : null;

            final RNPromptFragment promptFragment = new RNPromptFragment();
            promptFragment.setListener(actionListener);
            promptFragment.setArguments(arguments);

            if (isInForeground) {
                if (arguments.containsKey(KEY_CANCELABLE)) {
                    promptFragment.setCancelable(arguments.getBoolean(KEY_CANCELABLE));
                }
                promptFragment.show(mFragmentManager, FRAGMENT_TAG);
            } else {
                mFragmentToShow = promptFragment;
            }
        }
    }

    class PromptFragmentListener implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

        private final Callback mCallback;
        private boolean mCallbackConsumed = false;

        public PromptFragmentListener(Callback callback) {
            mCallback = callback;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            onConfirm(which, "");
        }

        public void onConfirm(int which, String input) {
            if (!mCallbackConsumed) {
                if (getReactApplicationContext().hasActiveCatalystInstance()) {
                    mCallback.invoke(ACTION_BUTTON_CLICKED, which, input);
                    mCallbackConsumed = true;
                }
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (!mCallbackConsumed) {
                if (getReactApplicationContext().hasActiveCatalystInstance()) {
                    mCallback.invoke(ACTION_DISMISSED);
                    mCallbackConsumed = true;
                }
            }
        }
    }

    @Override
    public Map<String, Object> getConstants() {
        return CONSTANTS;
    }

    private
    @Nullable
    FragmentManagerHelper getFragmentManagerHelper() {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            return null;
        }
        return new FragmentManagerHelper(activity.getFragmentManager());
    }
}
