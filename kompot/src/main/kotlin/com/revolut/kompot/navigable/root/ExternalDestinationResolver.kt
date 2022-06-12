/*
 * Copyright (C) 2022 Revolut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.revolut.kompot.navigable.root

import android.content.Context
import android.content.pm.PackageManager
import com.revolut.kompot.common.ExternalDestination
import com.revolut.kompot.common.toIntent

class ExternalDestinationResolver(private val context: Context) {

    fun isExternalDestinationAvailable(destination: ExternalDestination): Boolean {
        return context
            .packageManager
            .resolveActivity(destination.toIntent(context), PackageManager.MATCH_DEFAULT_ONLY) != null
    }

}