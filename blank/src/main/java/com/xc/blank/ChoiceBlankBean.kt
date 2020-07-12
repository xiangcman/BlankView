package com.xc.blank

data class ChoiceBlankBean(

    val questionContent: String,
    val choiceOptions: List<ChoiceOptions>,
    val stuAnswer: List<ChoiceOptions>?
)

data class ChoiceOptions(val optValue: String?=null, val answer: Int?=null)