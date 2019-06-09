-- Users schema

-- !Ups

CREATE TABLE user
(
  id             INTEGER PRIMARY KEY AUTOINCREMENT,
  username       VARCHAR(255) NOT NULL,
  is_subscribed  INTEGER      NOT NULL DEFAULT 0,
  is_blacklisted INTEGER      NOT NULL DEFAULT 0
);

-- !Downs

DROP TABLE user;