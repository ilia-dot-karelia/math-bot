package ru.tg.pawaptz.dao

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import ru.tg.api.inlined.FirstName
import ru.tg.api.inlined.TgChatId
import ru.tg.api.transport.TgUserDto
import ru.tg.pawaptz.chats.math.tasks.ActiveUser
import ru.tg.pawaptz.chats.math.tasks.task.MathIntTaskDescription
import ru.tg.pawaptz.chats.math.tasks.task.MathTask
import ru.tg.pawaptz.chats.math.tasks.task.SimpleMathTask
import ru.tg.pawaptz.chats.math.tasks.task.TaskComplexity
import ru.tg.pawaptz.inlined.Answer
import java.sql.Statement


class PostgresDaoImpl(private val template: JdbcTemplate) : PostgresDao {
    companion object {
        private const val TASK_INSERT =
            "insert into math_tasks(question,answer,complexity,is_generated) values (?,?,CAST(? AS math_task_complexity),?)"
        private const val USER_INSERT = "insert into users(id, name, chat_id, is_active) values (?,?,?,?)"

        // todo replace with insert into users(name) values (?) on conflict (name) do nothing after migration to 11 postgres
        private const val ANSWER_SAVE =
            "insert into user_math_tasks_answers (task_id, user_id, user_answer, is_correct) " +
                    "values (?, ?, ?, ?)"
        private const val USER_SELECT = "select count(name) from users where name=?"
        private const val USER_SET_ACTIVITY = "update users set is_active=? where id=?"
        private const val USER_GET_ALL_ACTIVE = "select id, name, chat_id from users where is_active=true"
        private const val USER_TASK_COMPLEXITY_GET = "select complexity from user_task_complexity where id=?"
        private const val USER_TASK_COMPLEXITY_UPDATE = "update user_task_complexity set complexity=? where id=?"
        private const val USER_NOT_COMPLETE_TASKS =
            "with user_answers as (select * from user_math_tasks_answers where user_id=?)\n" +
                    "select tsk.id, tsk.question, tsk.complexity, tsk.answer, tsk.is_generated from math_tasks tsk left join user_answers ans on tsk.id = ans.task_id\n" +
                    "where ans.user_answer is null and tsk.complexity=CAST(? AS math_task_complexity) limit ?"
    }

    override fun createUserIfNotExist(
        user: TgUserDto,
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

    override fun setUserActivityStatus(userDto: TgUserDto, isActive: Boolean) {
        template.update {
            val statement = it.prepareStatement(USER_SET_ACTIVITY)
            statement.setBoolean(1, isActive)
            statement.setInt(2, userDto.id.toInt()) //todo migrate to int
            statement
        }
    }

    override fun saveTask(task: MathTask): Long {
        val keyHolder: KeyHolder = GeneratedKeyHolder()
        template.update({
            val statement = it.prepareStatement(TASK_INSERT, Statement.RETURN_GENERATED_KEYS)
            statement.setString(1, task.description().get())
            statement.setDouble(2, task.answer().v.toDouble())
            statement.setString(3, task.complexity().name)
            statement.setBoolean(4, false)
            statement
        }, keyHolder)

        val key: Long? = keyHolder.keys?.get("id") as Long?
        if (key == null) {
            throw IllegalStateException("Primary key was not generated for task: $task")
        } else
            return key
    }

    override fun saveAnswer(taskId: Long, userId: Long, answer: Answer, isCorrect: Boolean) {
        template.update {
            val statement = it.prepareStatement(ANSWER_SAVE)
            statement.setLong(1, taskId)
            statement.setInt(2, userId.toInt()) //todo fixme
            statement.setDouble(3, answer.v.toDouble())
            statement.setBoolean(4, isCorrect)
            statement
        }
    }

    override fun getComplexityOfTaskForUser(tgUserDto: TgUserDto): TaskComplexity? {
        return template.query({ p0 ->
            val statement = p0.prepareStatement(USER_TASK_COMPLEXITY_GET)
            statement.setLong(1, tgUserDto.id)
            statement
        }, ResultSetExtractor {
            return@ResultSetExtractor if (it.next())
                it.getString(1)?.toTaskComplexity()
            else null
        })
    }

    override fun updateComplexityForUser(tgUserDto: TgUserDto, taskComplexity: TaskComplexity) {
        template.update {
            val statement = it.prepareStatement(USER_TASK_COMPLEXITY_UPDATE)
            statement.setString(1, taskComplexity.name)
            statement.setLong(2, tgUserDto.id)
            statement
        }
    }

    override fun getUnResolvedTasks(userDto: TgUserDto, complexity: TaskComplexity, limit: Int): Collection<MathTask> {
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
                Answer(p0.getFloat(4)),
                p0.getBoolean(5)
            )
        }
    }

    override fun isUserExists(userDto: TgUserDto): Boolean {
        return template.queryForObject(USER_SELECT, Long::class.java, userDto.firstName.v) > 0
    }

    override fun getAllActiveUsers(): List<ActiveUser> {
        return template.query(USER_GET_ALL_ACTIVE) { p0, _ ->
            ActiveUser(
                TgUserDto(
                    p0.getLong(1),
                    false,
                    FirstName(p0.getString(2))
                ), TgChatId(p0.getInt(3))
            )
        }
    }

    private fun String?.toTaskComplexity(): TaskComplexity? {
        return if (this != null) TaskComplexity.valueOf(this) else null
    }
}