package org.example.project.domain.model

import androidx.compose.ui.graphics.Color
import org.example.project.ui.theme.freshColor
import org.example.project.ui.theme.staleColor

enum class RateStatus(
    val title: String,
    val color: Color
) {

    Idle(
        title = "Rates",
        color = Color.White
    ),

    Fresh(
        title = "Fresh",
        color = freshColor
    ),

    Stale(
        title = "Rates are not fresh",
        color = staleColor
    )

}