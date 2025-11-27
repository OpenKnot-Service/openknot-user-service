package com.openknot.user.entity

enum class CareerLevel(
    val label: String,
) {
    BEGINNER("초보자"),
    INTERMEDIATE("중급"),
    ADVANCED("고급"),
    EXPERT("전문가");

    companion object {
        fun fromLabel(label: String): CareerLevel? =
            CareerLevel.entries.firstOrNull { it.label == label }
    }
}