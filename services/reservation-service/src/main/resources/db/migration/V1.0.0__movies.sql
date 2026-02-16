create table movies
(
    id         uuid not null,
    genre      varchar(255),
    poster_url varchar(255),
    status     varchar(255) check ((status in ('NOW_PLAYING', 'COMING_SOON'))),
    title      varchar(255),
    primary key (id)
)
