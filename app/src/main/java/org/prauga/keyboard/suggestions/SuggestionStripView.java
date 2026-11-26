/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.prauga.keyboard.suggestions;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InlineSuggestion;
import android.view.inputmethod.InlineSuggestionsRequest;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.prauga.keyboard.accessibility.AccessibilityUtils;
import org.prauga.keyboard.keyboard.Keyboard;
import org.prauga.keyboard.keyboard.MainKeyboardView;
import org.prauga.keyboard.keyboard.MoreKeysPanel;
import org.prauga.keyboard.AudioAndHapticFeedbackManager;
import org.prauga.keyboard.R;
import org.prauga.keyboard.SuggestedWords;
import org.prauga.keyboard.SuggestedWords.SuggestedWordInfo;
import org.prauga.keyboard.clipboard.ClipboardSuggestion;
import org.prauga.keyboard.common.Constants;
import org.prauga.keyboard.define.DebugFlags;
import org.prauga.keyboard.settings.Settings;
import org.prauga.keyboard.settings.SettingsValues;
import org.prauga.keyboard.emojisuggestions.EmojiSearchBar;
import org.prauga.keyboard.toolbar.KeyboardToolbar;
import org.prauga.keyboard.toolbar.ToolbarKeys;
import org.prauga.keyboard.suggestions.MoreSuggestionsView.MoreSuggestionsListener;
import org.prauga.keyboard.utils.ImportantNoticeUtils;
import org.prauga.keyboard.utils.SplitKeyboardUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class SuggestionStripView extends RelativeLayout implements OnClickListener,
        OnLongClickListener {
    public interface Listener {
        public void showImportantNoticeContents();
        public void pickSuggestionManually(SuggestedWordInfo word);
        public void showClipboardHistory();
        public void showTextEditing();
        public void toggleOneHandedMode();
        public void showKeyboardResize();
        public void toggleIncognitoMode();
        public void removeSuggestion(SuggestedWordInfo word);
        public void onCodeInput(int primaryCode, int x, int y, boolean isKeyRepeat);
        public void toggleSplitKeyboard();
        public void toggleFloatingKeyboard();
        public void pickClipboardSuggestion(ClipboardSuggestion suggestion,
                ClipboardSuggestion.Match match);
    }

    static final boolean DBG = DebugFlags.DEBUG_ENABLED;
    private static final float DEBUG_INFO_TEXT_SIZE_IN_DIP = 6.0f;
    private static final int TOOL_KEY_ALPHA_ON = 255;
    private static final int TOOL_KEY_ALPHA_OFF = 128;
    private static final String SENSITIVE_CLIPBOARD_LABEL = "\u2022\u2022\u2022\u2022\u2022\u2022";

    private final ViewGroup mSuggestionsStrip;
    private final ImageButton mVoiceKey;
    private final ImageButton mSplitKey;
    private final ImageButton mFloatKey;
    private final ImageButton mToolbarToggleKey;
    private boolean mToolbarExpanded;
    private final int mDockedStripMargin;
    private final View mImportantNoticeStrip;
    private final InlineSuggestionsStrip mInlineSuggestionsStrip;
    private final ViewGroup mClipboardSuggestionStrip;
    @Nullable
    private ClipboardSuggestion mClipboardSuggestion;
    private final KeyboardToolbar mToolbar;
    private final EmojiSearchBar mEmojiSearchBar;
    private final View mToolbarClipboardKey;
    private final View mToolbarEditingKey;
    private final ImageButton mToolbarFloatKey;
    private final View mToolbarOneHandedKey;
    private final View mToolbarResizeKey;
    private final ImageButton mToolbarIncognitoKey;
    private final Map<String, View> mToolbarKeys = new HashMap<>();
    private List<String> mToolbarKeyOrder = Collections.emptyList();
    private final List<View> mModeKeys = new ArrayList<>();
    private final View mModeSplitKey;
    private boolean mShowingModes;
    private final ColorStateList mPanelTint;
    private boolean mCanShowClipboardHistory;
    private boolean mShowsToolbar;
    private boolean mIsIncognito;
    private boolean mShouldBeVisible = true;
    private boolean mIsFullscreenMode;
    MainKeyboardView mMainKeyboardView;

    private final View mMoreSuggestionsContainer;
    private final MoreSuggestionsView mMoreSuggestionsView;
    private final MoreSuggestions.Builder mMoreSuggestionsBuilder;

    private final ArrayList<TextView> mWordViews = new ArrayList<>();
    private final ArrayList<TextView> mDebugInfoViews = new ArrayList<>();
    private final ArrayList<View> mDividerViews = new ArrayList<>();

    Listener mListener;
    private SuggestedWords mSuggestedWords = SuggestedWords.getEmptyInstance();
    private int mStartIndexOfMoreSuggestions;

    private final SuggestionStripLayoutHelper mLayoutHelper;
    private final StripVisibilityGroup mStripVisibilityGroup;

    private static class StripVisibilityGroup {
        private final View mSuggestionStripView;
        private final View mSuggestionsStrip;
        private final View mImportantNoticeStrip;
        private final InlineSuggestionsStrip mInlineSuggestionsStrip;
        private final View mClipboardSuggestionStrip;
        private final View mToolbar;
        private final View mEmojiSearchBar;
        private boolean mShowsEmojiSearch;

        public StripVisibilityGroup(final View suggestionStripView,
                final ViewGroup suggestionsStrip, final View importantNoticeStrip,
                final InlineSuggestionsStrip inlineSuggestionsStrip,
                final View clipboardSuggestionStrip, final View toolbar,
                final View emojiSearchBar) {
            mSuggestionStripView = suggestionStripView;
            mSuggestionsStrip = suggestionsStrip;
            mImportantNoticeStrip = importantNoticeStrip;
            mInlineSuggestionsStrip = inlineSuggestionsStrip;
            mClipboardSuggestionStrip = clipboardSuggestionStrip;
            mToolbar = toolbar;
            mEmojiSearchBar = emojiSearchBar;
            showSuggestionsStrip(false /* showsClipboardSuggestion */, false /* showsToolbar */);
        }

        public void setLayoutDirection(final boolean isRtlLanguage) {
            final int layoutDirection = isRtlLanguage ? ViewCompat.LAYOUT_DIRECTION_RTL
                    : ViewCompat.LAYOUT_DIRECTION_LTR;
            ViewCompat.setLayoutDirection(mSuggestionStripView, layoutDirection);
            ViewCompat.setLayoutDirection(mSuggestionsStrip, layoutDirection);
            ViewCompat.setLayoutDirection(mImportantNoticeStrip, layoutDirection);
        }

        public void showSuggestionsStrip(final boolean showsClipboardSuggestion,
                final boolean showsToolbar) {
            mEmojiSearchBar.setVisibility(mShowsEmojiSearch ? VISIBLE : INVISIBLE);
            if (mShowsEmojiSearch) {
                mSuggestionsStrip.setVisibility(INVISIBLE);
                mImportantNoticeStrip.setVisibility(INVISIBLE);
                mInlineSuggestionsStrip.setVisibility(INVISIBLE);
                mClipboardSuggestionStrip.setVisibility(INVISIBLE);
                mToolbar.setVisibility(INVISIBLE);
                return;
            }
            final boolean showsInline = mInlineSuggestionsStrip.hasSuggestions();
            final boolean showsClipboard = !showsInline && showsClipboardSuggestion;
            final boolean showsTools = !showsInline && !showsClipboard && showsToolbar;
            mSuggestionsStrip.setVisibility(
                    showsInline || showsClipboard || showsTools ? INVISIBLE : VISIBLE);
            mImportantNoticeStrip.setVisibility(INVISIBLE);
            mInlineSuggestionsStrip.setVisibility(showsInline ? VISIBLE : INVISIBLE);
            mClipboardSuggestionStrip.setVisibility(showsClipboard ? VISIBLE : INVISIBLE);
            mToolbar.setVisibility(showsTools ? VISIBLE : INVISIBLE);
        }

        public void showImportantNoticeStrip() {
            mSuggestionsStrip.setVisibility(INVISIBLE);
            mImportantNoticeStrip.setVisibility(VISIBLE);
            mInlineSuggestionsStrip.setVisibility(INVISIBLE);
            mClipboardSuggestionStrip.setVisibility(INVISIBLE);
            mToolbar.setVisibility(INVISIBLE);
        }

        public boolean isShowingImportantNoticeStrip() {
            return mImportantNoticeStrip.getVisibility() == VISIBLE;
        }
    }

    /**
     * Construct a {@link SuggestionStripView} for showing suggestions to be picked by the user.
     * @param context
     * @param attrs
     */
    public SuggestionStripView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.suggestionStripViewStyle);
    }

    public SuggestionStripView(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);

        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.suggestions_strip, this);

        mSuggestionsStrip = (ViewGroup)findViewById(R.id.suggestions_strip);
        mVoiceKey = (ImageButton)findViewById(R.id.suggestions_strip_voice_key);
        mSplitKey = (ImageButton)findViewById(R.id.suggestions_strip_split_key);
        mFloatKey = (ImageButton)findViewById(R.id.suggestions_strip_float_key);
        mToolbarToggleKey = findViewById(R.id.suggestions_strip_toolbar_key);
        mDockedStripMargin =
                ((MarginLayoutParams) mSuggestionsStrip.getLayoutParams()).getMarginStart();
        mImportantNoticeStrip = findViewById(R.id.important_notice_strip);
        mInlineSuggestionsStrip = findViewById(R.id.inline_suggestions_strip);
        mClipboardSuggestionStrip = findViewById(R.id.clipboard_suggestion_strip);
        mToolbar = findViewById(R.id.keyboard_toolbar);
        mEmojiSearchBar = findViewById(R.id.emoji_search_bar);
        mStripVisibilityGroup = new StripVisibilityGroup(this, mSuggestionsStrip,
                mImportantNoticeStrip, mInlineSuggestionsStrip, mClipboardSuggestionStrip,
                mToolbar, mEmojiSearchBar);

        for (int pos = 0; pos < SuggestedWords.MAX_SUGGESTIONS; pos++) {
            final TextView word = new TextView(context, null, R.attr.suggestionWordStyle);
            word.setContentDescription(getResources().getString(R.string.spoken_empty_suggestion));
            word.setOnClickListener(this);
            word.setOnLongClickListener(this);
            mWordViews.add(word);
            final View divider = inflater.inflate(R.layout.suggestion_divider, null);
            mDividerViews.add(divider);
            final TextView info = new TextView(context, null, R.attr.suggestionWordStyle);
            info.setTextColor(Color.WHITE);
            info.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEBUG_INFO_TEXT_SIZE_IN_DIP);
            mDebugInfoViews.add(info);
        }

        mLayoutHelper = new SuggestionStripLayoutHelper(
                context, attrs, defStyle, mWordViews, mDividerViews, mDebugInfoViews);

        mMoreSuggestionsContainer = inflater.inflate(R.layout.more_suggestions, null);
        mMoreSuggestionsView = (MoreSuggestionsView)mMoreSuggestionsContainer
                .findViewById(R.id.more_suggestions_view);
        mMoreSuggestionsBuilder = new MoreSuggestions.Builder(context, mMoreSuggestionsView);

        final Resources res = context.getResources();
        mMoreSuggestionsModalTolerance = res.getDimensionPixelOffset(
                R.dimen.config_more_suggestions_modal_tolerance);
        mMoreSuggestionsSlidingDetector = new GestureDetector(
                context, mMoreSuggestionsSlidingListener);

        final TypedArray keyboardAttr = context.obtainStyledAttributes(attrs,
                R.styleable.Keyboard, defStyle, R.style.SuggestionStripView);
        final Drawable iconVoice = keyboardAttr.getDrawable(R.styleable.Keyboard_iconShortcutKey);
        keyboardAttr.recycle();
        mPanelTint = getToolKeyTintList();
        mToolbarClipboardKey = mToolbar.createKey(R.drawable.ic_toolbar_clipboard,
                getToolKeyTintList(), R.string.clipboard_history,
                () -> mListener.showClipboardHistory());
        mToolbarEditingKey = mToolbar.createKey(R.drawable.ic_toolbar_editing,
                getToolKeyTintList(), R.string.text_editing, () -> mListener.showTextEditing());
        mToolbarOneHandedKey = mToolbar.createKey(R.drawable.ic_toolbar_one_handed,
                getToolKeyTintList(), R.string.one_handed_mode,
                () -> mListener.toggleOneHandedMode());
        mToolbarResizeKey = mToolbar.createKey(R.drawable.ic_toolbar_resize,
                getToolKeyTintList(), R.string.keyboard_resize,
                () -> mListener.showKeyboardResize());
        mToolbarFloatKey = mToolbar.createKey(R.drawable.ic_floating_keyboard,
                getToolKeyTintList(), R.string.spoken_description_float_keyboard,
                () -> mListener.toggleFloatingKeyboard());
        mToolbarIncognitoKey = mToolbar.createKey(R.drawable.ic_toolbar_incognito,
                getToolKeyTintList(), R.string.incognito_mode,
                () -> mListener.toggleIncognitoMode());
        mToolbarKeys.put(ToolbarKeys.INCOGNITO, mToolbarIncognitoKey);
        mToolbarKeys.put(ToolbarKeys.MODES, mToolbar.createKey(R.drawable.ic_toolbar_modes,
                getToolKeyTintList(), R.string.keyboard_modes, this::showModes));
        mModeKeys.add(mToolbar.createKey(R.drawable.ic_editing_chevron_left, getToolKeyTintList(),
                R.string.keyboard_modes_back, this::hideModes));
        mModeKeys.add(mToolbar.createKey(R.drawable.ic_toolbar_one_handed, getToolKeyTintList(),
                R.string.one_handed_mode, () -> runModeAction(mListener::toggleOneHandedMode)));
        mModeKeys.add(mToolbar.createKey(R.drawable.ic_floating_keyboard, getToolKeyTintList(),
                R.string.spoken_description_float_keyboard,
                () -> runModeAction(mListener::toggleFloatingKeyboard)));
        mModeSplitKey = mToolbar.createKey(R.drawable.ic_split_keyboard, getToolKeyTintList(),
                R.string.spoken_description_split_keyboard,
                () -> runModeAction(mListener::toggleSplitKeyboard));
        mModeKeys.add(mModeSplitKey);
        mModeKeys.add(mToolbar.createKey(R.drawable.ic_toolbar_resize, getToolKeyTintList(),
                R.string.keyboard_resize, () -> runModeAction(mListener::showKeyboardResize)));
        mToolbarKeys.put(ToolbarKeys.CLIPBOARD, mToolbarClipboardKey);
        mToolbarKeys.put(ToolbarKeys.EDITING, mToolbarEditingKey);
        mToolbarKeys.put(ToolbarKeys.ONE_HANDED, mToolbarOneHandedKey);
        mToolbarKeys.put(ToolbarKeys.RESIZE, mToolbarResizeKey);
        mToolbarKeys.put(ToolbarKeys.FLOAT, mToolbarFloatKey);
        mToolbarKeys.put(ToolbarKeys.EMOJI, mToolbar.createKey(R.drawable.ic_toolbar_emoji,
                getToolKeyTintList(), R.string.spoken_description_emoji,
                () -> onToolbarCode(Constants.CODE_EMOJI)));
        mToolbarKeys.put(ToolbarKeys.SETTINGS, mToolbar.createKey(R.drawable.ic_toolbar_settings,
                getToolKeyTintList(), R.string.spoken_description_settings,
                () -> onToolbarCode(Constants.CODE_SETTINGS)));
        mVoiceKey.setImageDrawable(iconVoice);
        mVoiceKey.setOnClickListener(this);
        mSplitKey.setImageTintList(getToolKeyTintList());
        mSplitKey.setOnClickListener(this);
        mFloatKey.setImageTintList(getToolKeyTintList());
        mFloatKey.setOnClickListener(this);
        mToolbarToggleKey.setImageTintList(getToolKeyTintList());
        mToolbarToggleKey.setOnClickListener(view -> setToolbarExpanded(!mToolbarExpanded));
    }

    /**
     * A connection back to the input method.
     * @param listener
     */
    public void setListener(final Listener listener, final View inputView) {
        mListener = listener;
        mMainKeyboardView = (MainKeyboardView)inputView.findViewById(R.id.keyboard_view);
    }

    public void updateVisibility(final boolean shouldBeVisible, final boolean isFullscreenMode) {
        mShouldBeVisible = shouldBeVisible;
        mIsFullscreenMode = isFullscreenMode;
        final boolean isVisible = shouldBeVisible || mInlineSuggestionsStrip.hasSuggestions()
                || isEmojiSearchShown();
        final int visibility = isVisible ? VISIBLE : (isFullscreenMode ? GONE : INVISIBLE);
        setVisibility(visibility);
        final SettingsValues currentSettingsValues = Settings.getInstance().getCurrent();
        mVoiceKey.setVisibility(currentSettingsValues.mShowsVoiceInputKey ? VISIBLE : INVISIBLE);
        mShowsToolbar = currentSettingsValues.mShowsToolbar;
        mShowingModes = false;
        if (!mToolbarKeyOrder.equals(currentSettingsValues.mToolbarKeys)) {
            mToolbarKeyOrder = currentSettingsValues.mToolbarKeys;
            final List<View> keys = new ArrayList<>();
            for (final String id : mToolbarKeyOrder) {
                keys.add(mToolbarKeys.get(id));
            }
            mToolbar.setKeys(keys);
        }
        mIsIncognito = !currentSettingsValues.isPersonalizedLearningEnabled();
        final boolean incognitoMode = currentSettingsValues.mIncognitoModeEnabled;
        mToolbarIncognitoKey.setActivated(incognitoMode);
        mToolbarIncognitoKey.setImageAlpha(incognitoMode ? TOOL_KEY_ALPHA_ON : TOOL_KEY_ALPHA_OFF);
        updateToolbarToggleKey();
        mToolbarToggleKey.setVisibility(mShowsToolbar ? VISIBLE : GONE);
        mFloatKey.setVisibility(mShowsToolbar ? GONE : VISIBLE);
        if (!mShowsToolbar) {
            mToolbarExpanded = false;
        }
        mCanShowClipboardHistory = currentSettingsValues.mClipboardHistoryEnabled
                && !currentSettingsValues.mIsFloatingKeyboardEnabled;
        mToolbarClipboardKey.setVisibility(mCanShowClipboardHistory ? VISIBLE : GONE);
        mToolbarEditingKey.setVisibility(
                currentSettingsValues.mIsFloatingKeyboardEnabled ? GONE : VISIBLE);
        mToolbarOneHandedKey.setVisibility(
                currentSettingsValues.mIsFloatingKeyboardEnabled ? GONE : VISIBLE);
        mToolbarResizeKey.setVisibility(
                currentSettingsValues.mIsFloatingKeyboardEnabled ? GONE : VISIBLE);
        mStripVisibilityGroup.showSuggestionsStrip(shouldShowClipboardSuggestion(),
                shouldShowToolbar());
        updateSplitKey();
        updateFloatKey();
        if (isEmojiSearchShown()) {
            hideEdgeKeys();
        }
    }

    private void showModes() {
        mShowingModes = true;
        mModeSplitKey.setVisibility(mSplitKey.getVisibility() == VISIBLE ? VISIBLE : GONE);
        mToolbar.setKeys(mModeKeys);
    }

    private void hideModes() {
        mToolbarKeyOrder = Collections.emptyList();
        refresh();
    }

    private void runModeAction(@Nonnull final Runnable action) {
        hideModes();
        action.run();
    }

    public void refresh() {
        updateVisibility(mShouldBeVisible, mIsFullscreenMode);
    }

    public void setEmojiSearchListener(@Nonnull final EmojiSearchBar.Listener listener) {
        mEmojiSearchBar.setListener(listener);
    }

    public boolean isEmojiSearchShown() {
        return mStripVisibilityGroup.mShowsEmojiSearch;
    }

    public void showEmojiSearch() {
        mStripVisibilityGroup.mShowsEmojiSearch = true;
        mEmojiSearchBar.start(mPanelTint);
        setVisibility(VISIBLE);
        hideEdgeKeys();
        mStripVisibilityGroup.showSuggestionsStrip(false, false);
    }

    public void hideEmojiSearch() {
        if (!isEmojiSearchShown()) {
            return;
        }
        mStripVisibilityGroup.mShowsEmojiSearch = false;
        refresh();
    }

    public void typeIntoEmojiSearch(@Nonnull final String text) {
        mEmojiSearchBar.type(text);
    }

    public void deleteFromEmojiSearch() {
        mEmojiSearchBar.deleteLast();
    }

    private void hideEdgeKeys() {
        mToolbarToggleKey.setVisibility(INVISIBLE);
        mFloatKey.setVisibility(INVISIBLE);
        mSplitKey.setVisibility(INVISIBLE);
        mVoiceKey.setVisibility(INVISIBLE);
    }

    public ColorStateList getToolKeyTintList() {
        return mWordViews.get(0).getTextColors();
    }

    @Nonnull
    public ColorStateList getPanelTintList() {
        return mPanelTint;
    }

    private void updateFloatKey() {
        final boolean isFloating = Settings.getInstance().getCurrent().mIsFloatingKeyboardEnabled;
        mFloatKey.setActivated(isFloating);
        mFloatKey.setImageAlpha(isFloating ? TOOL_KEY_ALPHA_ON : TOOL_KEY_ALPHA_OFF);
        mFloatKey.setContentDescription(getResources().getString(isFloating
                ? R.string.spoken_description_dock_keyboard
                : R.string.spoken_description_float_keyboard));
        mToolbarFloatKey.setActivated(isFloating);
        mToolbarFloatKey.setImageAlpha(isFloating ? TOOL_KEY_ALPHA_ON : TOOL_KEY_ALPHA_OFF);
        mToolbarFloatKey.setContentDescription(mFloatKey.getContentDescription());
        final int margin = isFloating
                ? getResources().getDimensionPixelSize(
                        R.dimen.config_suggestions_strip_edge_key_width)
                : mDockedStripMargin;
        setHorizontalMargins(mSuggestionsStrip, margin);
        setHorizontalMargins(mImportantNoticeStrip, margin);
        setHorizontalMargins(mInlineSuggestionsStrip, margin);
        setHorizontalMargins(mClipboardSuggestionStrip, margin);
    }

    private static void setHorizontalMargins(final View view, final int margin) {
        final MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
        if (params.getMarginStart() == margin && params.getMarginEnd() == margin) {
            return;
        }
        params.setMargins(margin, params.topMargin, margin, params.bottomMargin);
        params.setMarginStart(margin);
        params.setMarginEnd(margin);
        view.setLayoutParams(params);
    }

    private void updateSplitKey() {
        if (Settings.getInstance().getCurrent().mIsFloatingKeyboardEnabled
                || !SplitKeyboardUtils.isSplitKeyboardAvailable(getResources())) {
            mSplitKey.setVisibility(GONE);
            return;
        }
        final boolean isSplit = Settings.getInstance().getCurrent().mIsSplitKeyboardEnabled;
        mSplitKey.setVisibility(VISIBLE);
        mSplitKey.setActivated(isSplit);
        mSplitKey.setImageAlpha(isSplit ? TOOL_KEY_ALPHA_ON : TOOL_KEY_ALPHA_OFF);
        mSplitKey.setContentDescription(getResources().getString(isSplit
                ? R.string.spoken_description_merge_keyboard
                : R.string.spoken_description_split_keyboard));
    }

    public void setSuggestions(final SuggestedWords suggestedWords, final boolean isRtlLanguage) {
        if (mToolbarExpanded && !suggestedWords.isEmpty() && !suggestedWords.isPrediction()
                && !suggestedWords.isPunctuationSuggestions()) {
            setToolbarExpanded(false);
        }
        clear();
        mStripVisibilityGroup.setLayoutDirection(isRtlLanguage);
        mSuggestedWords = suggestedWords;
        mStartIndexOfMoreSuggestions = mLayoutHelper.layoutAndReturnStartIndexOfMoreSuggestions(
                getContext(), mSuggestedWords, mSuggestionsStrip, this);
        mStripVisibilityGroup.showSuggestionsStrip(shouldShowClipboardSuggestion(),
                shouldShowToolbar());
    }

    @Nullable
    public InlineSuggestionsRequest createInlineSuggestionsRequest(@Nonnull final Bundle uiExtras) {
        return mInlineSuggestionsStrip.createRequest(uiExtras,
                getToolKeyTintList().getDefaultColor());
    }

    public void setInlineSuggestions(@Nonnull final List<InlineSuggestion> suggestions) {
        mInlineSuggestionsStrip.setSuggestions(suggestions);
        if (isShowingMoreSuggestionPanel()) {
            dismissMoreSuggestionsPanel();
        }
        mStripVisibilityGroup.showSuggestionsStrip(shouldShowClipboardSuggestion(),
                shouldShowToolbar());
        updateVisibility(mShouldBeVisible, mIsFullscreenMode);
    }

    public void setClipboardSuggestion(@Nullable final ClipboardSuggestion suggestion) {
        mClipboardSuggestion = suggestion;
        mClipboardSuggestionStrip.removeAllViews();
        if (suggestion != null) {
            if (mCanShowClipboardHistory) {
                mClipboardSuggestionStrip.addView(createClipboardHistoryButton());
            }
            for (final ClipboardSuggestion.Match match : suggestion.getMatches()) {
                mClipboardSuggestionStrip.addView(
                        createClipboardSuggestionChip(match, suggestion.isSensitive()));
            }
        }
        mStripVisibilityGroup.showSuggestionsStrip(shouldShowClipboardSuggestion(),
                shouldShowToolbar());
    }

    @Nonnull
    private ImageButton createClipboardHistoryButton() {
        final ImageButton button =
                new ImageButton(getContext(), null, R.attr.suggestionWordStyle);
        button.setImageResource(R.drawable.ic_toolbar_clipboard);
        button.setImageTintList(getToolKeyTintList());
        button.setScaleType(ImageView.ScaleType.CENTER);
        button.setContentDescription(getResources().getString(R.string.clipboard_history));
        button.setOnClickListener(view -> mListener.showClipboardHistory());
        button.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(
                R.dimen.config_suggestions_strip_edge_key_width), LayoutParams.MATCH_PARENT));
        return button;
    }

    @Nonnull
    private TextView createClipboardSuggestionChip(@Nonnull final ClipboardSuggestion.Match match,
            final boolean isSensitive) {
        final TextView chip = (TextView)LayoutInflater.from(getContext())
                .inflate(R.layout.clipboard_suggestion_chip, mClipboardSuggestionStrip, false);
        final ColorStateList tint = getToolKeyTintList();
        chip.setTextColor(tint);
        chip.setCompoundDrawableTintList(tint);
        chip.setBackgroundTintList(ColorStateList.valueOf(
                InlineSuggestionsStrip.getChipBackgroundColor(tint.getDefaultColor())));
        chip.setCompoundDrawablesRelativeWithIntrinsicBounds(
                match.getType() == ClipboardSuggestion.Type.CODE
                        ? R.drawable.ic_suggestion_code : R.drawable.ic_suggestion_clipboard,
                0, 0, 0);
        final Resources res = getResources();
        chip.setText(isSensitive ? SENSITIVE_CLIPBOARD_LABEL : match.getText());
        chip.setContentDescription(isSensitive
                ? res.getString(R.string.clipboard_suggestion_sensitive_description)
                : res.getString(R.string.clipboard_suggestion_description, match.getText()));
        chip.setTag(match);
        chip.setOnClickListener(this);
        return chip;
    }

    private void onToolbarCode(final int code) {
        mListener.onCodeInput(code, Constants.SUGGESTION_STRIP_COORDINATE,
                Constants.SUGGESTION_STRIP_COORDINATE, false /* isKeyRepeat */);
    }

    private boolean shouldShowToolbar() {
        return mShowsToolbar && (mToolbarExpanded
                || mSuggestedWords.isEmpty() || mSuggestedWords.isPunctuationSuggestions());
    }

    private void setToolbarExpanded(final boolean expanded) {
        mToolbarExpanded = expanded;
        updateToolbarToggleKey();
        mStripVisibilityGroup.showSuggestionsStrip(shouldShowClipboardSuggestion(),
                shouldShowToolbar());
    }

    private void updateToolbarToggleKey() {
        final int icon;
        final int description;
        if (mToolbarExpanded) {
            icon = R.drawable.ic_editing_chevron_left;
            description = R.string.hide_toolbar_key;
        } else if (mIsIncognito) {
            icon = R.drawable.ic_toolbar_incognito;
            description = R.string.show_toolbar_key_incognito;
        } else {
            icon = R.drawable.ic_toolbar_toggle;
            description = R.string.show_toolbar_key;
        }
        mToolbarToggleKey.setImageResource(icon);
        mToolbarToggleKey.setContentDescription(getResources().getString(description));
    }

    private boolean shouldShowClipboardSuggestion() {
        return mClipboardSuggestion != null
                && (mSuggestedWords.isEmpty() || mSuggestedWords.isPunctuationSuggestions()
                        || mSuggestedWords.mInputStyle
                                == SuggestedWords.INPUT_STYLE_BEGINNING_OF_SENTENCE_PREDICTION);
    }

    public void setMoreSuggestionsHeight(final int remainingHeight) {
        mLayoutHelper.setMoreSuggestionsHeight(remainingHeight);
    }

    // This method checks if we should show the important notice (checks on permanent storage if
    // it has been shown once already or not, and if in the setup wizard). If applicable, it shows
    // the notice. In all cases, it returns true if it was shown, false otherwise.
    public boolean maybeShowImportantNoticeTitle() {
        final SettingsValues currentSettingsValues = Settings.getInstance().getCurrent();
        if (!ImportantNoticeUtils.shouldShowImportantNotice(getContext(), currentSettingsValues)
                || mInlineSuggestionsStrip.hasSuggestions() || shouldShowClipboardSuggestion()) {
            return false;
        }
        if (getWidth() <= 0) {
            return false;
        }
        final String importantNoticeTitle = ImportantNoticeUtils.getSuggestContactsNoticeTitle(
                getContext());
        if (TextUtils.isEmpty(importantNoticeTitle)) {
            return false;
        }
        if (isShowingMoreSuggestionPanel()) {
            dismissMoreSuggestionsPanel();
        }
        mLayoutHelper.layoutImportantNotice(mImportantNoticeStrip, importantNoticeTitle);
        mStripVisibilityGroup.showImportantNoticeStrip();
        mImportantNoticeStrip.setOnClickListener(this);
        return true;
    }

    public void clear() {
        mSuggestionsStrip.removeAllViews();
        removeAllDebugInfoViews();
        mStripVisibilityGroup.showSuggestionsStrip(shouldShowClipboardSuggestion(),
                shouldShowToolbar());
        dismissMoreSuggestionsPanel();
    }

    private void removeAllDebugInfoViews() {
        // The debug info views may be placed as children views of this {@link SuggestionStripView}.
        for (final View debugInfoView : mDebugInfoViews) {
            final ViewParent parent = debugInfoView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup)parent).removeView(debugInfoView);
            }
        }
    }

    private final MoreSuggestionsListener mMoreSuggestionsListener = new MoreSuggestionsListener() {
        @Override
        public void onSuggestionSelected(final SuggestedWordInfo wordInfo) {
            mListener.pickSuggestionManually(wordInfo);
            dismissMoreSuggestionsPanel();
        }

        @Override
        public void onCancelInput() {
            dismissMoreSuggestionsPanel();
        }
    };

    private final MoreKeysPanel.Controller mMoreSuggestionsController =
            new MoreKeysPanel.Controller() {
        @Override
        public void onDismissMoreKeysPanel() {
            mMainKeyboardView.onDismissMoreKeysPanel();
        }

        @Override
        public void onShowMoreKeysPanel(final MoreKeysPanel panel) {
            mMainKeyboardView.onShowMoreKeysPanel(panel);
        }

        @Override
        public void onCancelMoreKeysPanel() {
            dismissMoreSuggestionsPanel();
        }
    };

    public boolean isShowingMoreSuggestionPanel() {
        return mMoreSuggestionsView.isShowingInParent();
    }

    public void dismissMoreSuggestionsPanel() {
        mMoreSuggestionsView.dismissMoreKeysPanel();
    }

    @Override
    public boolean onLongClick(final View view) {
        AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(
                Constants.NOT_A_CODE, this);
        final SuggestedWordInfo removableSuggestion = getRemovableSuggestion(view);
        if (removableSuggestion != null) {
            mListener.removeSuggestion(removableSuggestion);
            return true;
        }
        return showMoreSuggestions();
    }

    @Nullable
    private SuggestedWordInfo getRemovableSuggestion(final View view) {
        if (mSuggestedWords.isPunctuationSuggestions() || !(view.getTag() instanceof Integer)) {
            return null;
        }
        final int index = (Integer) view.getTag();
        if (index >= mSuggestedWords.size()) {
            return null;
        }
        final SuggestedWordInfo wordInfo = mSuggestedWords.getInfo(index);
        return wordInfo.isKindOf(SuggestedWordInfo.KIND_TYPED) ? null : wordInfo;
    }

    boolean showMoreSuggestions() {
        final Keyboard parentKeyboard = mMainKeyboardView.getKeyboard();
        if (parentKeyboard == null) {
            return false;
        }
        final SuggestionStripLayoutHelper layoutHelper = mLayoutHelper;
        if (mSuggestedWords.size() <= mStartIndexOfMoreSuggestions) {
            return false;
        }
        final int stripWidth = getWidth();
        final View container = mMoreSuggestionsContainer;
        final int maxWidth = stripWidth - container.getPaddingLeft() - container.getPaddingRight();
        final MoreSuggestions.Builder builder = mMoreSuggestionsBuilder;
        builder.layout(mSuggestedWords, mStartIndexOfMoreSuggestions, maxWidth,
                (int)(maxWidth * layoutHelper.mMinMoreSuggestionsWidth),
                layoutHelper.getMaxMoreSuggestionsRow(), parentKeyboard);
        mMoreSuggestionsView.setKeyboard(builder.build());
        container.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final MoreKeysPanel moreKeysPanel = mMoreSuggestionsView;
        final int pointX = stripWidth / 2;
        final int pointY = -layoutHelper.mMoreSuggestionsBottomGap;
        moreKeysPanel.showMoreKeysPanel(this, mMoreSuggestionsController, pointX, pointY,
                mMoreSuggestionsListener);
        mOriginX = mLastX;
        mOriginY = mLastY;
        for (int i = 0; i < mStartIndexOfMoreSuggestions; i++) {
            mWordViews.get(i).setPressed(false);
        }
        return true;
    }

    // Working variables for {@link onInterceptTouchEvent(MotionEvent)} and
    // {@link onTouchEvent(MotionEvent)}.
    private int mLastX;
    private int mLastY;
    private int mOriginX;
    private int mOriginY;
    private final int mMoreSuggestionsModalTolerance;
    private boolean mNeedsToTransformTouchEventToHoverEvent;
    private boolean mIsDispatchingHoverEventToMoreSuggestions;
    private final GestureDetector mMoreSuggestionsSlidingDetector;
    private final GestureDetector.OnGestureListener mMoreSuggestionsSlidingListener =
            new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent down, MotionEvent me, float deltaX, float deltaY) {
            if (down == null) {
                return false;
            }
            final float dy = me.getY() - down.getY();
            if (deltaY > 0 && dy < 0) {
                return showMoreSuggestions();
            }
            return false;
        }
    };

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent me) {
        if (mStripVisibilityGroup.isShowingImportantNoticeStrip()) {
            return false;
        }
        // Detecting sliding up finger to show {@link MoreSuggestionsView}.
        if (!mMoreSuggestionsView.isShowingInParent()) {
            mLastX = (int)me.getX();
            mLastY = (int)me.getY();
            return mMoreSuggestionsSlidingDetector.onTouchEvent(me);
        }
        if (mMoreSuggestionsView.isInModalMode()) {
            return false;
        }

        final int action = me.getAction();
        final int index = me.getActionIndex();
        final int x = (int)me.getX(index);
        final int y = (int)me.getY(index);
        if (Math.abs(x - mOriginX) >= mMoreSuggestionsModalTolerance
                || mOriginY - y >= mMoreSuggestionsModalTolerance) {
            // Decided to be in the sliding suggestion mode only when the touch point has been moved
            // upward. Further {@link MotionEvent}s will be delivered to
            // {@link #onTouchEvent(MotionEvent)}.
            mNeedsToTransformTouchEventToHoverEvent =
                    AccessibilityUtils.getInstance().isTouchExplorationEnabled();
            mIsDispatchingHoverEventToMoreSuggestions = false;
            return true;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            // Decided to be in the modal input mode.
            mMoreSuggestionsView.setModalMode();
        }
        return false;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(final AccessibilityEvent event) {
        // Don't populate accessibility event with suggested words and voice key.
        return true;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent me) {
        if (!mMoreSuggestionsView.isShowingInParent()) {
            // Ignore any touch event while more suggestions panel hasn't been shown.
            // Detecting sliding up is done at {@link #onInterceptTouchEvent}.
            return true;
        }
        // In the sliding input mode. {@link MotionEvent} should be forwarded to
        // {@link MoreSuggestionsView}.
        final int index = me.getActionIndex();
        final int x = mMoreSuggestionsView.translateX((int)me.getX(index));
        final int y = mMoreSuggestionsView.translateY((int)me.getY(index));
        me.setLocation(x, y);
        if (!mNeedsToTransformTouchEventToHoverEvent) {
            mMoreSuggestionsView.onTouchEvent(me);
            return true;
        }
        // In sliding suggestion mode with accessibility mode on, a touch event should be
        // transformed to a hover event.
        final int width = mMoreSuggestionsView.getWidth();
        final int height = mMoreSuggestionsView.getHeight();
        final boolean onMoreSuggestions = (x >= 0 && x < width && y >= 0 && y < height);
        if (!onMoreSuggestions && !mIsDispatchingHoverEventToMoreSuggestions) {
            // Just drop this touch event because dispatching hover event isn't started yet and
            // the touch event isn't on {@link MoreSuggestionsView}.
            return true;
        }
        final int hoverAction;
        if (onMoreSuggestions && !mIsDispatchingHoverEventToMoreSuggestions) {
            // Transform this touch event to a hover enter event and start dispatching a hover
            // event to {@link MoreSuggestionsView}.
            mIsDispatchingHoverEventToMoreSuggestions = true;
            hoverAction = MotionEvent.ACTION_HOVER_ENTER;
        } else if (me.getActionMasked() == MotionEvent.ACTION_UP) {
            // Transform this touch event to a hover exit event and stop dispatching a hover event
            // after this.
            mIsDispatchingHoverEventToMoreSuggestions = false;
            mNeedsToTransformTouchEventToHoverEvent = false;
            hoverAction = MotionEvent.ACTION_HOVER_EXIT;
        } else {
            // Transform this touch event to a hover move event.
            hoverAction = MotionEvent.ACTION_HOVER_MOVE;
        }
        me.setAction(hoverAction);
        mMoreSuggestionsView.onHoverEvent(me);
        return true;
    }

    @Override
    public void onClick(final View view) {
        AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(
                Constants.CODE_UNSPECIFIED, this);
        if (view == mImportantNoticeStrip) {
            mListener.showImportantNoticeContents();
            return;
        }
        if (view == mFloatKey) {
            mListener.toggleFloatingKeyboard();
            return;
        }
        if (view.getTag() instanceof ClipboardSuggestion.Match && mClipboardSuggestion != null) {
            mListener.pickClipboardSuggestion(mClipboardSuggestion,
                    (ClipboardSuggestion.Match) view.getTag());
            return;
        }
        if (view == mSplitKey) {
            mListener.toggleSplitKeyboard();
            updateSplitKey();
            return;
        }
        if (view == mVoiceKey) {
            mListener.onCodeInput(Constants.CODE_SHORTCUT,
                    Constants.SUGGESTION_STRIP_COORDINATE, Constants.SUGGESTION_STRIP_COORDINATE,
                    false /* isKeyRepeat */);
            return;
        }

        final Object tag = view.getTag();
        // {@link Integer} tag is set at
        // {@link SuggestionStripLayoutHelper#setupWordViewsTextAndColor(SuggestedWords,int)} and
        // {@link SuggestionStripLayoutHelper#layoutPunctuationSuggestions(SuggestedWords,ViewGroup}
        if (tag instanceof Integer) {
            final int index = (Integer) tag;
            if (index >= mSuggestedWords.size()) {
                return;
            }
            final SuggestedWordInfo wordInfo = mSuggestedWords.getInfo(index);
            mListener.pickSuggestionManually(wordInfo);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dismissMoreSuggestionsPanel();
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        // Called by the framework when the size is known. Show the important notice if applicable.
        // This may be overriden by showing suggestions later, if applicable.
        if (oldw <= 0 && w > 0) {
            maybeShowImportantNoticeTitle();
        }
    }
}
