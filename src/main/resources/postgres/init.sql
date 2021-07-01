    drop table if exists math_tasks cascade;
    drop table if exists users cascade;
    drop table if exists user_task_complexity cascade;
    drop table if exists user_math_tasks_answers cascade;
    drop table if exists user_scores cascade;
    drop table if exists task_cost cascade;
    drop table if exists achieve_messages cascade;
    drop type if exists math_task_complexity cascade;
    drop type if exists achievement_message_mood cascade;

    CREATE TYPE math_task_complexity AS ENUM ('EASY', 'MIDDLE', 'ADVANCED', 'HARD');
    CREATE TYPE achievement_message_mood AS ENUM ('GOOD', 'BAD', 'NEUTRAL');

    create table if not exists math_tasks (
        id bigserial primary key,
        question varchar(20) unique,
        answer numeric(10, 4),
        complexity math_task_complexity,
        is_generated boolean
    );

    create table if not exists task_cost (
        complexity math_task_complexity unique,
        cost smallint
    );

    insert into task_cost(complexity, cost) values ('EASY', 10);
    insert into task_cost(complexity, cost) values ('MIDDLE', 15);
    insert into task_cost(complexity, cost) values ('ADVANCED', 20);
    insert into task_cost(complexity, cost) values ('HARD', 25);

 	create table if not exists users(
        id int unique,
        name varchar(20) unique,
        chat_id int unique,
        is_active boolean
    );

    create table if not exists user_scores (
        user_id int unique,
        score integer default 0,
        CONSTRAINT user_id_constraint
            FOREIGN KEY(user_id)
            REFERENCES users(id)
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
        answer_time timestamp default current_timestamp,
        UNIQUE (task_id, user_id),
        CONSTRAINT task_id_constraint
            FOREIGN KEY(task_id)
            REFERENCES math_tasks(id),
        CONSTRAINT user_id_constraint
            FOREIGN KEY(user_id)
            REFERENCES users(id)
    );

    create table achieve_messages (
        message varchar(100),
        mood achievement_message_mood
    );

    insert into achieve_messages values('Well done!', 'GOOD');
    insert into achieve_messages values('Keep going!', 'GOOD');
    insert into achieve_messages values('You are so cool', 'GOOD');
    insert into achieve_messages values('Math genius', 'GOOD');
    insert into achieve_messages values('Excellent!!!!', 'GOOD');
    insert into achieve_messages values('Very well', 'GOOD');
    insert into achieve_messages values('100/100', 'GOOD');
    insert into achieve_messages values('Best answer!', 'GOOD');
    insert into achieve_messages values('Good progress', 'GOOD');
    insert into achieve_messages values('Best of the best', 'GOOD');
    insert into achieve_messages values('(:', 'GOOD');

    insert into achieve_messages values('):', 'BAD');
    insert into achieve_messages values('Do not hurry!', 'BAD');
    insert into achieve_messages values('Try another one', 'BAD');
    insert into achieve_messages values('Have a rest', 'BAD');
    insert into achieve_messages values('Leave it for tomorrow', 'BAD');
    insert into achieve_messages values('Think!', 'BAD');
    insert into achieve_messages values('Be attentive!', 'BAD');
    insert into achieve_messages values(':`((((', 'BAD');
    insert into achieve_messages values('I am a sad panda', 'BAD');
    insert into achieve_messages values('I am a very sad panda', 'BAD');

	insert into achieve_messages values(':|', 'NEUTRAL');
    insert into achieve_messages values('Lets continue. Everything is OK.', 'NEUTRAL');
    insert into achieve_messages values('Pick up this one!', 'NEUTRAL');
    insert into achieve_messages values('What about the next task?', 'NEUTRAL');
    insert into achieve_messages values('How about this?', 'NEUTRAL');
    insert into achieve_messages values('Good.', 'NEUTRAL');
    insert into achieve_messages values('Proceed.', 'NEUTRAL');
    insert into achieve_messages values('Next task.', 'NEUTRAL');
    insert into achieve_messages values('Stability is what we need>', 'NEUTRAL');