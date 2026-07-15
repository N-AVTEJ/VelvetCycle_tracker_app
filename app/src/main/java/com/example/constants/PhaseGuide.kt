package com.example.constants

data class PhaseGuideData(
    val body: String,
    val foodMorning: String,
    val foodAfternoon: String,
    val foodEvening: String,
    val exercise: String
)

object PhaseGuide {
    val guides = mapOf(
        "menstruation" to PhaseGuideData(
            body = "Your uterus lining is shedding. You may feel cramps, low energy, and mood changes. Rest is important.",
            foodMorning = "Iron-rich foods — spinach, dates, pomegranate juice, beetroot",
            foodAfternoon = "Dark chocolate, ginger tea, warm lemon water",
            foodEvening = "Warm soup, khichdi, light dal rice. Avoid cold or spicy food.",
            exercise = "Child's pose, cat-cow stretch, slow walking only. Avoid crunches, jumping, or heavy lifting."
        ),
        "follicular" to PhaseGuideData(
            body = "Your body is preparing a new egg. Energy is rising, mood is improving. Good time to start things.",
            foodMorning = "Eggs, nuts, fresh fruit smoothie, oats",
            foodAfternoon = "Fresh salad, sprouts, lean protein (chicken, paneer, lentils)",
            foodEvening = "Brown rice or roti with vegetables and protein",
            exercise = "Jogging, gym workout, strength training, dancing, cycling. Your body can handle intensity now."
        ),
        "ovulation" to PhaseGuideData(
            body = "Your egg has been released today. This is your most fertile day. Energy and confidence are at peak.",
            foodMorning = "Flaxseeds in smoothie, berries, pumpkin seeds",
            foodAfternoon = "Zinc-rich foods — chickpeas, pumpkin seeds, sesame",
            foodEvening = "Balanced meal — protein + complex carbs + vegetables",
            exercise = "Running, swimming, HIIT (high intensity interval training — short bursts of intense movement with rest gaps), any sport. Peak performance day."
        ),
        "luteal" to PhaseGuideData(
            body = "Progesterone rising. Energy may dip in the second half. PMS symptoms like bloating or mood swings are common.",
            foodMorning = "Banana, almonds, oats, magnesium-rich foods",
            foodAfternoon = "Complex carbs — sweet potato, brown rice, whole wheat. Avoid salt and sugar.",
            foodEvening = "Light dinner. Chamomile tea before bed. Avoid alcohol.",
            exercise = "Yoga, pilates, light walks. Avoid intense cardio especially in last 5 days before period."
        )
    )

    fun getGuide(phase: String, isDayBefore: Boolean = false): PhaseGuideData {
        if (isDayBefore) {
            return PhaseGuideData(
                body = "Your cycle is completing today. Hormones are dropping which causes PMS. Completely normal — your body is preparing to reset.",
                foodMorning = "Banana, warm oats, ginger tea. No coffee.",
                foodAfternoon = "Light meal. Sweet potato or brown rice. No salt or junk food.",
                foodEvening = "Warm soup or khichdi. Chamomile tea before bed. Eat early.",
                exercise = "No intense workout. Gentle yoga only. Child's pose and cat-cow. Rest is more important than exercise today."
            )
        }
        return guides[phase.lowercase()] ?: PhaseGuideData(
            body = "Stay tuned to your body's natural cycle and practice self-care.",
            foodMorning = "Healthy fruits, water, light breakfast.",
            foodAfternoon = "Balanced protein and green vegetables.",
            foodEvening = "Light warm dinner with herbal tea.",
            exercise = "Moderate stretching, yoga, or an evening stroll."
        )
    }
}
