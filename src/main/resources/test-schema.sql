DROP TABLE  public.films_genres;
DROP TABLE  public.films_mpa;
DROP TABLE  public.films_directors;
DROP TABLE  public.likes;
DROP TABLE  public.friends;
DROP TABLE  public.films;
DROP TABLE  public.users;
DROP TABLE  public.directors;


CREATE TABLE IF NOT EXISTS public.mpa (
mpa_id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
mpa_name VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.directors (
director_id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
director_name VARCHAR(200) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.films (
film_id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
name VARCHAR(255) NOT NULL,
description VARCHAR,
release_date DATE NOT NULL,
duration INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS public.genres (
genre_id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
genre_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.films_directors (
film_id BIGINT NOT NULL REFERENCES public.films (film_id) ON DELETE SET NULL,
director_id BIGINT NOT NULL REFERENCES public.directors (director_id) ON DELETE SET NULL,
UNIQUE (film_id, director_id)
);

CREATE TABLE IF NOT EXISTS public.films_genres (
film_id BIGINT NOT NULL REFERENCES public.films (film_id) ON DELETE SET NULL,
genre_id BIGINT NOT NULL REFERENCES public.genres (genre_id) ON DELETE SET NULL,
UNIQUE (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS public.films_mpa (
film_id BIGINT NOT NULL REFERENCES public.films (film_id) ON DELETE SET NULL,
mpa_id BIGINT NOT NULL REFERENCES public.mpa (mpa_id) ON DELETE SET NULL,
UNIQUE (film_id, mpa_id)
);

CREATE TABLE IF NOT EXISTS public.users (
user_id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
email VARCHAR(100),
login VARCHAR(100),
name VARCHAR(100),
birthday DATE
);

CREATE TABLE IF NOT EXISTS public.friends (
user_id BIGINT REFERENCES public.users (user_id) ON DELETE SET NULL,
friend_id BIGINT REFERENCES public.users (user_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS public.likes (
film_id BIGINT REFERENCES public.films (film_id) ON DELETE SET NULL,
user_id BIGINT REFERENCES public.users (user_id) ON DELETE SET NULL,
UNIQUE (film_id, user_id)
);