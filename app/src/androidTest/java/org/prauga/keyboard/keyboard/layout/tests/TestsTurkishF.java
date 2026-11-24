/*
 * Copyright (C) 2025 The Android Open Source Project
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

package org.prauga.keyboard.keyboard.layout.tests;

import androidx.test.filters.SmallTest;

import org.prauga.keyboard.keyboard.layout.LayoutBase;
import org.prauga.keyboard.keyboard.layout.TurkishF;
import org.prauga.keyboard.keyboard.layout.customizer.EuroCustomizer;
import org.prauga.keyboard.keyboard.layout.customizer.TurkishFCustomizer;
import org.prauga.keyboard.keyboard.layout.expected.ExpectedKeyboardBuilder;

import java.util.Locale;

/**
 * tr: Turkish/turkish_f
 */
@SmallTest
public final class TestsTurkishF extends LayoutTestsBase {
    private static final Locale LOCALE = new Locale("tr");
    private static final LayoutBase LAYOUT = new TurkishF(new TurkishFCustomizer(LOCALE));

    @Override
    LayoutBase getLayout() { return LAYOUT; }

    private static class TurkishFCustomizer extends EuroCustomizer {
        private final org.prauga.keyboard.keyboard.layout.customizer.TurkishFCustomizer
        mTurkishFCustomizer;

        TurkishFCustomizer(final Locale locale) {
            super(locale);
            mTurkishFCustomizer = new org.prauga.keyboard.keyboard.layout.customizer
        .TurkishFCustomizer(locale);
        }

        @Override
        public ExpectedKeyboardBuilder setAccentedLetters(final ExpectedKeyboardBuilder builder) {
            return mTurkishFCustomizer.setAccentedLetters(builder);
        }
    }
}
