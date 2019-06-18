-- Polls schema

-- !Ups

CREATE TABLE poll
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    question      VARCHAR(255) NOT NULL,
    first_choice  VARCHAR(255) NOT NULL,
    second_choice VARCHAR(255) NOT NULL

);

CREATE TABLE poll_vote
(
    poll_id INTEGER      NOT NULL,
    user_id INTEGER      NOT NULL,
    choice  VARCHAR(255) NOT NULL,
    FOREIGN KEY (poll_id) REFERENCES poll (id),
    FOREIGN KEY (user_id) REFERENCES user (id)
);

-- !Downs

DROP TABLE poll_vote;
DROP TABLE poll;
