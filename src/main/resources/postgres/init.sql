drop table if exists math_tasks cascade;
drop table if exists users cascade;
drop table if exists user_task_complexity cascade;
drop table if exists user_math_tasks_answers cascade;
drop type if exists math_task_complexity cascade;

CREATE TYPE math_task_complexity AS ENUM ('EASY', 'MIDDLE', 'ADVANCED', 'HARD');

create table if not exists math_tasks (
    id bigserial primary key,
    question varchar(20) unique,
    answer numeric(10, 4),
    complexity math_task_complexity,
    is_generated boolean
);

create table if not exists users(
    id int unique,
    name varchar(20) unique,
    chat_id int unique,
    is_active boolean
);

create table if not exists user_task_complexity(
    id int,
    complexity math_task_complexity,
	UNIQUE(id),
    CONSTRAINT id
        FOREIGN KEY(id)
        REFERENCES users(id)
);

create table if not exists user_math_tasks_answers (
    task_id bigint,
    user_id int,
    user_answer numeric(10, 4),
    is_correct boolean,
    UNIQUE (task_id, user_id),
    CONSTRAINT task_id_constraint
        FOREIGN KEY(task_id)
    	REFERENCES math_tasks(id),
    CONSTRAINT user_id_constraint
        FOREIGN KEY(user_id)
        REFERENCES users(id)
);