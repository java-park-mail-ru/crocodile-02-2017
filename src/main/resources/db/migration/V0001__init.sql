CREATE TABLE account (
  id       SERIAL PRIMARY KEY NOT NULL,
  login    TEXT               NOT NULL UNIQUE,
  passhash TEXT               NOT NULL,
  email    TEXT               NOT NULL,
  rating   INTEGER DEFAULT 0  NOT NULL
);