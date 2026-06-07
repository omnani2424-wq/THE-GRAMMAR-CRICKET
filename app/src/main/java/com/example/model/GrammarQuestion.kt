package com.example.model

data class GrammarQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val category: String,
    val explanation: String
) {
    companion object {
        val PRELOADED_QUESTIONS = listOf(
            GrammarQuestion(
                id = "q1",
                questionText = "Hardly ________ the pitch when it started to rain heavily, forcing the umpires to call for the covers.",
                options = listOf(
                    "the bowler had reached",
                    "had the bowler reached",
                    "reached the bowler",
                    "the bowler reached having"
                ),
                correctIndex = 1,
                category = "Inversion",
                explanation = "When 'hardly' starts a clause, the subject and auxiliary verb must invert. Hence, 'had the bowler reached' is correct."
            ),
            GrammarQuestion(
                id = "q2",
                questionText = "The selection committee requested that the captain ________ a press conference to clarify the sudden change in batting order.",
                options = listOf(
                    "calls",
                    "should call",
                    "call",
                    "called"
                ),
                correctIndex = 2,
                category = "Subjunctive Mood",
                explanation = "Verbs of demand, request, or suggestion like 'request that' trigger the subjunctive mood in the following clause, which uses the base form of the verb ('call')."
            ),
            GrammarQuestion(
                id = "q3",
                questionText = "Neither the players nor the head coach ________ satisfied with the controversial decision made by the third umpire.",
                options = listOf(
                    "was",
                    "were",
                    "has being",
                    "are"
                ),
                correctIndex = 0,
                category = "Subject-Verb Agreement",
                explanation = "In 'neither... nor...' structures, the verb agrees with the closer subject. 'The head coach' is singular, so the singular verb 'was' is required."
            ),
            GrammarQuestion(
                id = "q4",
                questionText = "If our batsmen ________ more defensively in the previous over, we would not have lost three consecutive wickets.",
                options = listOf(
                    "played",
                    "would have played",
                    "had played",
                    "were to play"
                ),
                correctIndex = 2,
                category = "Conditional Sentences",
                explanation = "This is a third conditional sentence referring to an unreal past event. The structure is 'If + past perfect' in the conditional clause, and 'would have + past participle' in the main clause. Therefore, 'had played' is correct."
            ),
            GrammarQuestion(
                id = "q5",
                questionText = "Running at full speed to catch the ball, ________.",
                options = listOf(
                    "the boundaries was crossed by the batsman",
                    "the fielder unfortunately tripped and fell near the boundary",
                    "the ball was eventually dropped on the grass",
                    "tripping and falling occurred near the boundary"
                ),
                correctIndex = 1,
                category = "Dangling Modifiers",
                explanation = "The introductory phrase 'Running at full speed to catch the ball' is a modifier. It must modify the subject of the clause, which must be the entity performing the actions (i.e. 'the fielder'). Other options cause a dangling modifier error by putting 'boundaries' or 'ball' as the subject."
            ),
            GrammarQuestion(
                id = "q6",
                questionText = "The team's strategy not only focused on improving spin bowling ________ for the critical powerplay overs.",
                options = listOf(
                    "but also on formulating better layouts",
                    "but also on the formulation of better field layouts",
                    "and formulating better field layouts",
                    "but also formulated better layouts"
                ),
                correctIndex = 1,
                category = "Parallelism",
                explanation = "The correlative conjunction 'not only... but also...' requires parallel structures. 'not only focused on improving [noun/gerund phrase]' must be parallel with 'but also on the formulation [noun phrase] or but also on formulating...'. Let's see: 'on improving...' matches 'on the formulation of...'. Option 1 uses 'formulating better layouts' style but let's check Option 1 and Option 2. Both maintain parallelism, but 'on the formulation...' keeps the noun-like structure balanced with gerund parallelism. 'but also on formulating...' is even closer in parallelism to 'on improving'. Ah, Option 1 'but also on formulating better layouts' is extremely clean!"
            ),
            GrammarQuestion(
                id = "q7",
                questionText = "So intricate ________ that even expert commentators could not easily identify the spin bowler's subtle variations.",
                options = listOf(
                    "were the ball spin",
                    "was the bowler's release",
                    "the bowler's release was",
                    "did the bowler release"
                ),
                correctIndex = 1,
                category = "Inversion",
                explanation = "An 'especially/so + adjective... that' phrase placed at the beginning of a sentence triggers subject-verb inversion. 'So intricate was the bowler's release...' is grammatically correct."
            ),
            GrammarQuestion(
                id = "q8",
                questionText = "It is essential that each player ________ a rigorous medical assessment before being registered for the elite league.",
                options = listOf(
                    "undergoes",
                    "undergo",
                    "should undergo",
                    "underwent"
                ),
                correctIndex = 1,
                category = "Subjunctive Mood",
                explanation = "Adjectives of necessity or importance ('essential that') trigger the subjective mood, demanding the base verb ('undergo') regardless of the subject's person or number."
            ),
            GrammarQuestion(
                id = "q9",
                questionText = "The board of governors, together with the IPL tournament directors and media coordinators, ________ drafting the new safety codes.",
                options = listOf(
                    "is",
                    "are",
                    "were",
                    "have been"
                ),
                correctIndex = 0,
                category = "Subject-Verb Agreement",
                explanation = "Parenthetical expressions like 'together with', 'along with', or 'as well as' do not alter the number of the subject. The main subject 'The board of governors' is singular, so 'is' is correct."
            ),
            GrammarQuestion(
                id = "q10",
                questionText = "Had the captain won the coin toss, he ________ to bowl first under the overcast skies.",
                options = listOf(
                    "will choose",
                    "would have chosen",
                    "would choose",
                    "had chosen"
                ),
                correctIndex = 1,
                category = "Conditional Sentences",
                explanation = "The inverted conditional clause 'Had the captain won' corresponds to 'If the captain had won' (third conditional). The consequence takes 'would have + past participle' ('would have chosen')."
            ),
            GrammarQuestion(
                id = "q11",
                questionText = "Having been declared unfit by the medical staff, ________.",
                options = listOf(
                    "the selectors dropped the opening batsman",
                    "the opening batsman was dropped from the upcoming match",
                    "it was decided to drop the opening batsman",
                    "dropping the opening batsman was confirmed"
                ),
                correctIndex = 1,
                category = "Dangling Modifiers",
                explanation = "The participial phrase 'Having been declared unfit' must logically refer to 'the opening batsman', who acts as the subject of the main clause. Otherwise, a dangling modifier is created."
            ),
            GrammarQuestion(
                id = "q12",
                questionText = "Professional cricket athletes are trained not only to handle extreme match pressure ________ under adverse conditions.",
                options = listOf(
                    "but also to maintain peak physical stamina",
                    "and maintaining peak physical stamina",
                    "but also they maintain peak physical stamina",
                    "but as well maintain physical stamina"
                ),
                correctIndex = 0,
                category = "Parallelism",
                explanation = "To maintain parallelism with 'not only [to handle...]', you must use the infinitive 'but also [to maintain...]' in the second element."
            ),
            GrammarQuestion(
                id = "q13",
                questionText = "No sooner ________ their massive victory than the stadium erupted into a spectacular display of fireworks.",
                options = listOf(
                    "did the crowd celebrate",
                    "had the home team sealed",
                    "the home team had sealed",
                    "sealed the home team"
                ),
                correctIndex = 1,
                category = "Inversion",
                explanation = "Clauses starting with negative or restrictive adverbs like 'No sooner' invert subject and verb, typically in the past perfect when followed by 'than'. Thus, 'had the home team sealed' is correct."
            ),
            GrammarQuestion(
                id = "q14",
                questionText = "The coach recommends that the batsman ________ more attention to the bowler's wrist movement rather than the pitch.",
                options = listOf(
                    "pay",
                    "pays",
                    "should pay",
                    "paid"
                ),
                correctIndex = 0,
                category = "Subjunctive Mood",
                explanation = "The verb 'recommend that' requires the following clause to use the subjunctive mood, which takes the base verb 'pay'."
            ),
            GrammarQuestion(
                id = "q15",
                questionText = "Every player, commentator, and spectator ________ of the high stakes involved in this final over.",
                options = listOf(
                    "is conscious",
                    "are conscious",
                    "were conscious",
                    "have been conscious"
                ),
                correctIndex = 0,
                category = "Subject-Verb Agreement",
                explanation = "When a compound subject is preceded by 'every' or 'each', it takes a singular verb. 'is conscious' is the correct singular form."
            ),
            GrammarQuestion(
                id = "q16",
                questionText = "But for the stellar performance of the bowler, our team ________ the critical match.",
                options = listOf(
                    "would lose",
                    "had lost",
                    "has lost",
                    "would have lost"
                ),
                correctIndex = 3,
                category = "Conditional Sentences",
                explanation = "'But for' is a conditional construction meaning 'if it had not been for'. It triggers a third conditional ('would have lost') when discussing historical matches."
            ),
            GrammarQuestion(
                id = "q17",
                questionText = "Left unattended on the outfield grass, ________.",
                options = listOf(
                    "the heavy dew damaged the leather ball",
                    "the bowler forgot to dry his wet trousers",
                    "the leather ball was severely damp from the heavy dew",
                    "damaging dew accumulated on the leather ball"
                ),
                correctIndex = 2,
                category = "Dangling Modifiers",
                explanation = "The modifier 'Left unattended on the outfield grass' must describe the subject. The 'leather ball' is the only logical entity that was left on the grass."
            )
        )
    }
}
