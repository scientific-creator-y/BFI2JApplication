package com.dino.personalmonster.data


data class CustomTrainingMenu(
    var id: String = "",
    var title: String = "",
    var description: String = "",

    var triggerText: String = "",

    var habit: Boolean = true,
    var orderIndex: Int = 0,
    var parentRoutineId: String? = null,

    var streakCount: Int = 0,
    var lastCompletedDate: String = "",


)

