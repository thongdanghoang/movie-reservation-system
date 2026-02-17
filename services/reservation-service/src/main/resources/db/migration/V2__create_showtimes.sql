create table showtimes
(
    id               uuid         not null,
    movie_id         uuid         not null,
    start_time       timestamp with time zone not null,
    theater_name     varchar(255) not null,
    available_seats  integer      not null,
    primary key (id),
    constraint fk_showtimes_movie foreign key (movie_id) references movies (id)
)
