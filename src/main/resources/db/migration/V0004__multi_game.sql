CREATE TABLE multi_game (
  id    SERIAL PRIMARY KEY NOT NULL,
  word  TEXT               NOT NULL,
  users TEXT []            NOT NULL
)