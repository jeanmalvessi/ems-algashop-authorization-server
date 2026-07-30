create table auth_user_type_client_allowed (
    auth_user_type varchar(255) not null check (auth_user_type in ('MANAGER', 'OPERATOR', 'CUSTOMER')),
    client_id varchar(100) not null,
    primary key (auth_user_type, client_id)
);

create index idx_auth_user_type_client_allowed_lookup
    on auth_user_type_client_allowed (auth_user_type);