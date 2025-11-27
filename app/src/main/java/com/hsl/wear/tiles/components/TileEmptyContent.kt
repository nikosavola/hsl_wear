package com.hsl.wear.tiles.components

import android.content.Context
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.FontStyle
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.LayoutElementBuilders.Text
import com.hsl.wear.R

object TileEmptyContent {
    fun noActiveLegContent(context: Context): LayoutElement {
        return Column.Builder()
            .addContent(
                Text.Builder()
                    .setText(context.getString(R.string.no_active_route))
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(20f))
                            .setColor(argb(0xFFCCCCCC.toInt()))
                            .build()
                    )
                    .build()
            )
            .addContent(
                Spacer.Builder()
                    .setHeight(dp(4f))
                    .build()
            )
            .addContent(
                Text.Builder()
                    .setText(context.getString(R.string.route))
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(20f))
                            .setColor(argb(0xFFCCCCCC.toInt()))
                            .build()
                    )
                    .build()
            )
            .build()
    }
}