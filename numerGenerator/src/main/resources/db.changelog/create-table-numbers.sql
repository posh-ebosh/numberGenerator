create table public.numbers
(
    id           serial
        constraint numbers_pk
            primary key,
    numbers      text,
    letters      text,
    full_number text
);

alter table public.numbers
    owner to postgres;
