alter table auth_user
    add column email_verified boolean not null default false;

alter table auth_user
    add column verification_token varchar(255) unique;

alter table auth_user
    add column verification_token_expiration_date timestamptz;
