package com.hsl.wear.tiles.components

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.FontStyle
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Row
import androidx.wear.protolayout.LayoutElementBuilders.Text
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import com.hsl.wear.tiles.TileActionReceiver

object TileNavigationButtons {
    private const val TILE_SIZE = 120f

    fun createNavigationButtons(context: Context): LayoutElement {
        return androidx.wear.protolayout.LayoutElementBuilders.Row.Builder()
            .setWidth(dp(TILE_SIZE))
            .setHeight(dp(30f))
            .addContent(
                // Previous button
                Box.Builder()
                    .setWidth(dp(60f))
                    .setHeight(dp(30f))
                    .setModifiers(
                        Modifiers.Builder()
                            .setClickable(
                                Clickable.Builder()
                                    .setId("prev_leg")
                                    .setOnClick(
                                        ActionBuilders.LaunchAction.Builder()
                                            .setAndroidActivity(
                                                ActionBuilders.AndroidActivity.Builder()
                                                    .setPackageName(context.packageName)
                                                    .setClassName("com.hsl.wear.tiles.TileActionReceiver")
                                                    .addKeyToExtraMapping(
                                                        TileActionReceiver.EXTRA_ACTION,
                                                        ActionBuilders.AndroidStringExtra.Builder()
                                                            .setValue(TileActionReceiver.ACTION_PREV_LEG)
                                                            .build()
                                                    )
                                                    .build()
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .addContent(
                        Text.Builder()
                            .setText("◀")
                            .setFontStyle(
                                FontStyle.Builder()
                                    .setSize(sp(16f))
                                    .setColor(argb(0xFF888888.toInt()))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .addContent(
                // Next button
                Box.Builder()
                    .setWidth(dp(60f))
                    .setHeight(dp(30f))
                    .setModifiers(
                        Modifiers.Builder()
                            .setClickable(
                                Clickable.Builder()
                                    .setId("next_leg")
                                    .setOnClick(
                                        ActionBuilders.LaunchAction.Builder()
                                            .setAndroidActivity(
                                                ActionBuilders.AndroidActivity.Builder()
                                                    .setPackageName(context.packageName)
                                                    .setClassName("com.hsl.wear.tiles.TileActionReceiver")
                                                    .addKeyToExtraMapping(
                                                        TileActionReceiver.EXTRA_ACTION,
                                                        ActionBuilders.AndroidStringExtra.Builder()
                                                            .setValue(TileActionReceiver.ACTION_NEXT_LEG)
                                                            .build()
                                                    )
                                                    .build()
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .addContent(
                        Text.Builder()
                            .setText("▶")
                            .setFontStyle(
                                FontStyle.Builder()
                                    .setSize(sp(16f))
                                    .setColor(argb(0xFF888888.toInt()))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }
}