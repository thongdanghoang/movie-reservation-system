create table seats
(
    id            uuid         not null,
    showtime_id   uuid         not null,
    seat_row      varchar(10)  not null,
    seat_column   integer      not null,
    status        varchar(20)  not null check ((status in ('AVAILABLE', 'HELD', 'SOLD'))),
    primary key (id),
    constraint fk_seats_showtime foreign key (showtime_id) references showtimes (id),
    constraint uq_seats_showtime_position unique (showtime_id, seat_row, seat_column)
);

create index idx_seats_showtime_id on seats(showtime_id);
create index idx_seats_status on seats(status);
