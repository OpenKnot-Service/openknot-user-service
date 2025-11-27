package com.openknot.user.entity

enum class Position(
    val label: String,
) {
    DEVELOPER("개발자"),
    DESIGNER("디자이너"),
    PLANNER("기획자"),
    OTHER("기타");

    companion object {
        fun fromLabel(label: String): Position? =
            Position.entries.firstOrNull { it.label == label }
    }
}