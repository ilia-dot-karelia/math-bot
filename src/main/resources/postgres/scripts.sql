with user_answers as (select * from user_math_tasks_answers where user_id=115417017)
                    select tsk.id, tsk.question, tsk.complexity, tsk.answer, tsk.is_generated from math_tasks tsk left join user_answers ans on tsk.id = ans.task_id
                    where tsk.complexity='EASY' and  +
                    ans.user_answer is null or (ans.is_correct is false and current_timestamp - ans.answer_time > '00:10:00') limit 10