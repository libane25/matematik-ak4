package se.ak4.ova.data

data class Question(
    val id: Int,
    val subject: String,
    val category: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val facit: String
)
