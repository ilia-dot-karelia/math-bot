package ru.tg.pawaptz.dao

import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.dao.RecoverableDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementSetter
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import ru.tg.api.inlined.FirstName
import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgUser
import ru.tg.pawaptz.achievments.Mood
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.task.MathIntTaskDescription
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.SimpleMathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity
import ru.tg.pawaptz.inlined.Answer
import ru.tg.pawaptz.inlined.Score
import java.sql.Statement


open class PostgresDaoImpl(private val template: JdbcTemplate) : PostgresDao {
    companion object {
        private var log = LoggerFactory.getLogger(PostgresDaoImpl::class.java)

        private const val TASK_INSERT =
            "insert into math_tasks(question,answer,complexity,is_generated) values (?,?,CAST(? AS math_task_complexity),?) on conflict(question) do update set question=EXCLUDED.question returning id"
        private const val USER_INSERT = "insert into users(id, name, chat_id, is_active) values (?,?,?,?)"
        private const val USER_ANSWER_GET = "select user_answer from user_math_tasks_answers where task_id = ?"
        private const val USER_ANSWER_SAVE =
            "insert into user_math_tasks_answers (task_id, user_id, user_answer, is_correct, answer_time) " +
                    "values (?, ?, ?, ?, current_timestamp) on conflict(task_id, user_id) do update set user_answer = ?, is_correct=?, answer_time=current_timestamp"
        private const val USER_SELECT = "select count(name) from users where name=?"
        private const val USER_SET_ACTIVITY = "update users set is_active=? where id=?"
        private const val USER_GET_ALL_ACTIVE =
            "select id, name, chat_id, score from users left join user_scores s on s.user_id = id where is_active=true"
        private const val USER_TASK_COMPLEXITY_GET = "select complexity from user_task_complexity where id=?"
        private const val USER_TASK_COMPLEXITY_UPDATE = "update user_task_complexity set complexity=? where id=?"
        private const val USER_TASK_COMPLEXITY_INSERT =
            "insert into user_task_complexity values(?, CAST(? AS math_task_complexity)) on conflict do nothing"
        private const val USER_NOT_COMPLETE_TASKS =
            "with user_answers as (select * from user_math_tasks_answers where user_id=?)\n" +
                    "select tsk.id, tsk.question, tsk.complexity, tsk.answer, tsk.is_generated from math_tasks tsk left join user_answers ans on tsk.id = ans.task_id\n" +
                    "where tsk.complexity=CAST(? AS math_task_complexity) and " +
                    "ans.user_answer is null or (ans.is_correct is false and current_timestamp - ans.answer_time > '00:10:00') limit ?"
        private const val TASK_COST_SELECT = "select complexity, cost from task_cost"
        private const val SCORES_INIT_FOR_USER = "insert into user_scores values (?) on conflict do nothing"
        private const val SCORES_UPDATE = "update user_scores set score = score + ? where user_id = ? returning score"
        private const val SCORES_GET = "select score from user_scores where user_id = ?"
        private const val ACHIEVE_MESSAGE =
            "select message from achieve_messages where mood=CAST(? AS achievement_message_mood) order by random() limit 1"
    }

    override fun getUserAnswer(taskId: Long): Float? {
        return try {
            template.queryForObject(
                USER_ANSWER_GET, Float::class.java, taskId
            )
        } catch (ex: EmptyResultDataAccessException) {
            null
        }
    }

    override fun createUserIfNotExist(
        user: TgUser,
        chatId: TgChatId
    ) {
        if (!isUserExists(user))
            template.update {
                val statement = it.prepareStatement(USER_INSERT)
                statement.setInt(1, user.id.toInt()) //todo migrate to int
                statement.setString(2, user.firstName.v)
                statement.setInt(3, chatId.v)
                statement.setBoolean(4, false)
                statement
            }
    }

    override fun readTaskCosts(): Map<TaskComplexity, Int> {
        val pss: PreparedStatementSetter? = null
        val map = mutableMapOf<TaskComplexity, Int>()
        template.query(TASK_COST_SELECT, pss) {
            map[TaskComplexity.valueOf(it.getString(1))] = it.getInt(2)
        }
        return map
    }

    override fun setUserActivityStatus(userDto: TgUser, isActive: Boolean) {
        template.update {
            val statement = it.prepareStatement(USER_SET_ACTIVITY)
            statement.setBoolean(1, isActive)
            statement.setInt(2, userDto.id.toInt()) //todo migrate to int
            statement
        }
    }

    override fun saveTask(task: MathTask, isGenerated: Boolean): Long {
        val keyHolder: KeyHolder = GeneratedKeyHolder()
        template.update({
            val statement = it.prepareStatement(TASK_INSERT, Statement.RETURN_GENERATED_KEYS)
            statement.setString(1, task.description().question())
            statement.setDouble(2, task.answer().v.toDouble())
            statement.setString(3, task.complexity().name)
            statement.setBoolean(4, isGenerated)
            statement
        }, keyHolder)

        val key: Long? = keyHolder.keys?.get("id") as Long?
        if (key == null) {
            log.info("Task $task is already exists in the database")
            throw RecoverableDataAccessException("Primary key was not generated for task: $task")
        } else {
            log.info("Task $task is saved in the database")
            return key
        }
    }

    override fun saveAnswer(taskId: Long, userId: Long, answer: Answer) {
        template.update {
            val statement = it.prepareStatement(USER_ANSWER_SAVE)
            statement.setLong(1, taskId)
            statement.setInt(2, userId.toInt()) //todo fixme
            statement.setDouble(3, answer.v.toDouble())
            statement.setBoolean(4, answer.isCorrect())
            statement.setDouble(5, answer.v.toDouble())
            statement.setBoolean(6, answer.isCorrect())
            statement
        }
    }

    override fun getComplexityOfTaskForUser(TgUser: TgUser): TaskComplexity? {
        return template.query({ p0 ->
            val statement = p0.prepareStatement(USER_TASK_COMPLEXITY_GET)
            statement.setLong(1, TgUser.id)
            statement
        }, ResultSetExtractor {
            return@ResultSetExtractor if (it.next())
                it.getString(1)?.toTaskComplexity()
            else null
        })
    }

    override fun updateComplexityForUser(TgUser: TgUser, taskComplexity: TaskComplexity) {
        template.update {
            val statement = it.prepareStatement(USER_TASK_COMPLEXITY_UPDATE)
            statement.setString(1, taskComplexity.name)
            statement.setLong(2, TgUser.id)
            statement
        }
    }

    override fun getUnResolvedTasks(userDto: TgUser, complexity: TaskComplexity, limit: Int): Collection<MathTask> {
        return template.query({ p0 ->
            val statement = p0.prepareStatement(USER_NOT_COMPLETE_TASKS)
            statement.setInt(1, userDto.id.toInt()) //todo migrate to int
            statement.setString(2, complexity.name)
            statement.setInt(3, limit)
            statement
        }) { p0, _ ->
            SimpleMathTask(
                p0.getLong(1),
                MathIntTaskDescription(p0.getString(2)),
                TaskComplexity.valueOf(p0.getString(3)),
                Answer.CorrectAnswer(p0.getFloat(4)),
                p0.getBoolean(5)
            )
        }
    }

    override fun isUserExists(userDto: TgUser): Boolean {
        return template.queryForObject(USER_SELECT, Long::class.java, userDto.firstName.v) > 0
    }

    override fun getAllActiveUsers(): List<Pair<ActiveUser, Score>> {
        return template.query(USER_GET_ALL_ACTIVE) { p0, _ ->
            ActiveUser(
                TgUser(
                    p0.getLong(1),
                    false,
                    FirstName(p0.getString(2))
                ), TgChatId(p0.getInt(3))
            ) to Score(p0.getInt(1))
        }
    }

    override fun createUserScoresIfNotExist(userDto: TgUser) {
        template.update {
            val statement = it.prepareStatement(SCORES_INIT_FOR_USER)
            statement.setLong(1, userDto.id)
            statement
        }
    }

    override fun initUserComplexityForUser(TgUser: TgUser, complexity: TaskComplexity) {
        template.update {
            val statement = it.prepareStatement(USER_TASK_COMPLEXITY_INSERT)
            statement.setLong(1, TgUser.id)
            statement.setString(2, complexity.name)
            statement
        }
    }

    override fun getUserScore(userDto: TgUser): Score? =
        template.queryForObject(SCORES_GET, arrayOf(userDto.id)) { p0, _ -> Score(p0.getInt(1)) }


    override fun getAchieveMessage(mood: Mood): String {
        return template.queryForObject(
            ACHIEVE_MESSAGE, String::class.java, mood.name
        )
    }

    override fun addUserScore(userDto: TgUser, scores: Score): Score {
        val res = template.query(
            { con -> con.prepareStatement(SCORES_UPDATE) },
            { ps -> ps.setInt(1, scores.v); ps.setLong(2, userDto.id) },
            { rs -> rs.next(); rs.getInt(1) }
        )
        log.info("Updated scores for user $userDto, $scores")
        return if (res == null) {
            log.warn("Unable to update scores for user $userDto, user not found")
            Score.ZERO
        } else Score(res)
    }

    private fun String?.toTaskComplexity(): TaskComplexity? {
        return if (this != null) TaskComplexity.valueOf(this) else null
    }
}