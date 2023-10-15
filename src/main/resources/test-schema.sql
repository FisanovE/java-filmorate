DROP TABLE IF EXISTS users, mpa, films, films_mpa, genres, films_genres, likes, friends, directors, films_directors, reviews, reviews_like  CASCADE;

CREATE TABLE IF NOT EXISTS public.mpa (
mpa_id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
mpa_name VARCHAR(10) NOT NULL
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

CREATE TABLE IF NOT EXISTS public.films_genres (
film_id BIGINT NOT NULL REFERENCES public.films (film_id) on update cascade ON DELETE CASCADE,
genre_id BIGINT NOT NULL REFERENCES public.genres (genre_id) on update cascade ON DELETE CASCADE,
UNIQUE (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS public.films_mpa (
film_id BIGINT NOT NULL REFERENCES public.films (film_id) on update cascade ON DELETE CASCADE,
mpa_id BIGINT NOT NULL REFERENCES public.mpa (mpa_id) on update cascade ON DELETE CASCADE,
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
user_id BIGINT REFERENCES public.users (user_id) on update cascade ON DELETE CASCADE,
friend_id BIGINT REFERENCES public.users (user_id) on update cascade ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.likes (
film_id BIGINT REFERENCES public.films (film_id) on update cascade ON DELETE CASCADE,
user_id BIGINT REFERENCES public.users (user_id) on update cascade ON DELETE CASCADE,
UNIQUE (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS public.directors (
director_id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
director_name VARCHAR(200) NOT NULL
);
CREATE TABLE IF NOT EXISTS public.reviews (
review_id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
content VARCHAR(100),
is_positive BOOLEAN,
user_id BIGINT REFERENCES public.users (user_id) ON DELETE SET NULL,
film_id BIGINT REFERENCES public.films (film_id) ON DELETE SET NULL,
useful BIGINT
);

CREATE TABLE IF NOT EXISTS public.reviews_like (
review_id BIGINT REFERENCES public.reviews (review_id) ON DELETE SET NULL,
user_id BIGINT REFERENCES public.users (user_id) ON DELETE SET NULL,
is_useful BOOLEAN NOT NULL,
UNIQUE (review_id, user_id)
);

--TRUNCATE TABLE  public.films_genres;
--TRUNCATE TABLE  public.films_mpa;
--TRUNCATE TABLE  public.likes;
--TRUNCATE TABLE  public.friends;
--TRUNCATE TABLE  public.films;
--TRUNCATE TABLE  public.users;

CREATE TABLE IF NOT EXISTS public.films_directors (
film_id BIGINT NOT NULL REFERENCES public.films (film_id) ON UPDATE CASCADE ON DELETE CASCADE,
director_id BIGINT NOT NULL REFERENCES public.directors (director_id) ON UPDATE CASCADE ON DELETE CASCADE,
UNIQUE (film_id, director_id)
);

INSERT INTO directors (director_name) VALUES
    ('Стивен Спилберг'),
    ('Мартин Скорсезе'),
    ('Джеймс Кэмерон'),
    ('Джордж Лукас'),
    ('Квентин Тарантино'),
    ('Дэвид Финчер');