/*
 * Copyright (C) 2014 The Android Open Source Project
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

package org.prauga.keyboard.settings;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

public class SettingsValuesForSuggestion {
    public final boolean mBlockPotentiallyOffensive;
    @Nonnull
    public final Set<String> mBlockedWords;

    public SettingsValuesForSuggestion(final boolean blockPotentiallyOffensive) {
        this(blockPotentiallyOffensive, Collections.<String>emptySet());
    }

    public SettingsValuesForSuggestion(final boolean blockPotentiallyOffensive,
            @Nonnull final Set<String> blockedWords) {
        mBlockPotentiallyOffensive = blockPotentiallyOffensive;
        mBlockedWords = blockedWords;
    }
}
