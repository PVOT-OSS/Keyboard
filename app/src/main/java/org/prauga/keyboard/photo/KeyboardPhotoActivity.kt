/*
 * SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.prauga.keyboard.photo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import org.prauga.keyboard.R
import org.prauga.keyboard.utils.ExecutorUtils

class KeyboardPhotoActivity : ComponentActivity() {
    private val picker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) {
                finish()
                return@registerForActivityResult
            }
            val appContext = applicationContext
            ExecutorUtils.getBackgroundExecutor(ExecutorUtils.KEYBOARD).execute {
                val saved = KeyboardPhoto.save(appContext, uri)
                runOnUiThread {
                    if (!saved) {
                        Toast.makeText(
                                appContext,
                                R.string.keyboard_photo_failed,
                                Toast.LENGTH_SHORT,
                            )
                            .show()
                    }
                    finish()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}
