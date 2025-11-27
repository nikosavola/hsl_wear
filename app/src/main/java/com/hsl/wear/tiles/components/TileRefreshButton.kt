package com.hsl.wear.tiles.components

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.DimensionBuilders.wrap
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.FontStyle
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Text
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ModifiersBuilders.Modifiers

object TileRefreshButton {
    fun createRefreshButton(context: Context): LayoutElement {
        return Box.Builder()
            .setWidth(androidx.wear.protolayout.DimensionBuilders.expand())
            .setHeight(androidx.wear.protolayout.DimensionBuilders.wrap())
            .setModifiers(
                Modifiers.Builder()
                    .setClickable(
                        Clickable.Builder()
                            .setId("refresh_tile")
                            .setOnClick(
                                // LoadAction forces immediate onTileRequest() call
                                ActionBuilders.LoadAction.Builder()
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .addContent(
                Text.Builder()
                    .setText("↻")
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(20f))
                            .setColor(argb(0xFF888888.toInt()))
                            .build()
                    )
                    .build()
            )
            .build()
    }
}