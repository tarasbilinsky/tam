# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table feedback (
  id                            bigserial not null,
  date_user                     timestamptz,
  user_id                       bigint,
  url                           varchar(255),
  area_id                       bigint,
  feedback_type_id              bigint,
  description_short             varchar(255),
  description                   text,
  session_id                    varchar(255),
  date                          timestamptz not null,
  constraint pk_feedback primary key (id)
);

create table feedback_area (
  id                            bigserial not null,
  title                         varchar(255),
  sys_name                      varchar(255),
  order_number                  integer not null,
  active                        boolean default false not null,
  constraint pk_feedback_area primary key (id)
);

create table feedback_type (
  id                            bigserial not null,
  title                         varchar(255),
  sys_name                      varchar(255),
  order_number                  integer not null,
  active                        boolean default false not null,
  area_id                       bigint,
  constraint pk_feedback_type primary key (id)
);

create table lock (
  id                            bigserial not null,
  node_id                       bigint,
  date                          timestamptz,
  name                          varchar(255),
  constraint pk_lock primary key (id)
);

create table log (
  id                            bigserial not null,
  tag                           varchar(255),
  message                       text,
  date                          timestamptz not null,
  constraint pk_log primary key (id)
);

create table node (
  id                            bigserial not null,
  ipv4                          varchar(255),
  deploy_group                  integer not null,
  constraint pk_node primary key (id)
);

create table state (
  id                            bigserial not null,
  name                          varchar(255),
  short_name                    varchar(255),
  constraint pk_state primary key (id)
);

create table system_setting (
  id                            bigserial not null,
  ts                            timestamptz,
  constraint pk_system_setting primary key (id)
);

create table test1 (
  id                            bigserial not null,
  color                         integer,
  a                             float not null,
  b                             float not null,
  c                             float,
  money                         float,
  money2                        float,
  constraint ck_test1_color check ( color in (0,1,2)),
  constraint pk_test1 primary key (id)
);

create table users (
  id                            bigserial not null,
  name                          varchar(255),
  password                      varchar(255),
  active                        boolean default false not null,
  email                         varchar(255),
  phone                         varchar(255),
  primary_user_role_id          bigint,
  id_for_integration            bigint,
  constraint pk_users primary key (id)
);

create table users_user_role (
  users_id                      bigint not null,
  user_role_id                  bigint not null,
  constraint pk_users_user_role primary key (users_id,user_role_id)
);

create table users_user_permission (
  users_id                      bigint not null,
  user_permission_id            bigint not null,
  constraint pk_users_user_permission primary key (users_id,user_permission_id)
);

create table user_permission (
  id                            bigserial not null,
  title                         varchar(255),
  sys_name                      varchar(255),
  order_number                  integer not null,
  active                        boolean default false not null,
  constraint pk_user_permission primary key (id)
);

create table user_role (
  id                            bigserial not null,
  title                         varchar(255),
  sys_name                      varchar(255),
  order_number                  integer not null,
  active                        boolean default false not null,
  constraint pk_user_role primary key (id)
);

create table user_session (
  id                            bigserial not null,
  start                         bigint not null,
  last                          bigint not null,
  closed                        boolean default false not null,
  user_id                       bigint,
  constraint pk_user_session primary key (id)
);

create table user_session_integration (
  id                            bigserial not null,
  user_id                       bigint,
  token                         varchar(255),
  date                          timestamptz not null,
  constraint pk_user_session_integration primary key (id)
);

create table zip_code (
  id                            bigserial not null,
  zip_code                      varchar(255),
  city                          varchar(255),
  state_id                      bigint,
  latitude                      float,
  longitude                     float,
  constraint pk_zip_code primary key (id)
);

alter table feedback add constraint fk_feedback_user_id foreign key (user_id) references users (id) on delete restrict on update restrict;
create index ix_feedback_user_id on feedback (user_id);

alter table feedback add constraint fk_feedback_area_id foreign key (area_id) references feedback_area (id) on delete restrict on update restrict;
create index ix_feedback_area_id on feedback (area_id);

alter table feedback add constraint fk_feedback_feedback_type_id foreign key (feedback_type_id) references feedback_type (id) on delete restrict on update restrict;
create index ix_feedback_feedback_type_id on feedback (feedback_type_id);

alter table feedback_type add constraint fk_feedback_type_area_id foreign key (area_id) references feedback_area (id) on delete restrict on update restrict;
create index ix_feedback_type_area_id on feedback_type (area_id);

alter table lock add constraint fk_lock_node_id foreign key (node_id) references node (id) on delete restrict on update restrict;
create index ix_lock_node_id on lock (node_id);

alter table users add constraint fk_users_primary_user_role_id foreign key (primary_user_role_id) references user_role (id) on delete restrict on update restrict;
create index ix_users_primary_user_role_id on users (primary_user_role_id);

alter table users_user_role add constraint fk_users_user_role_users foreign key (users_id) references users (id) on delete restrict on update restrict;
create index ix_users_user_role_users on users_user_role (users_id);

alter table users_user_role add constraint fk_users_user_role_user_role foreign key (user_role_id) references user_role (id) on delete restrict on update restrict;
create index ix_users_user_role_user_role on users_user_role (user_role_id);

alter table users_user_permission add constraint fk_users_user_permission_users foreign key (users_id) references users (id) on delete restrict on update restrict;
create index ix_users_user_permission_users on users_user_permission (users_id);

alter table users_user_permission add constraint fk_users_user_permission_user_permission foreign key (user_permission_id) references user_permission (id) on delete restrict on update restrict;
create index ix_users_user_permission_user_permission on users_user_permission (user_permission_id);

alter table user_session add constraint fk_user_session_user_id foreign key (user_id) references users (id) on delete restrict on update restrict;
create index ix_user_session_user_id on user_session (user_id);

alter table user_session_integration add constraint fk_user_session_integration_user_id foreign key (user_id) references users (id) on delete restrict on update restrict;
create index ix_user_session_integration_user_id on user_session_integration (user_id);

alter table zip_code add constraint fk_zip_code_state_id foreign key (state_id) references state (id) on delete restrict on update restrict;
create index ix_zip_code_state_id on zip_code (state_id);


# --- !Downs

alter table if exists feedback drop constraint if exists fk_feedback_user_id;
drop index if exists ix_feedback_user_id;

alter table if exists feedback drop constraint if exists fk_feedback_area_id;
drop index if exists ix_feedback_area_id;

alter table if exists feedback drop constraint if exists fk_feedback_feedback_type_id;
drop index if exists ix_feedback_feedback_type_id;

alter table if exists feedback_type drop constraint if exists fk_feedback_type_area_id;
drop index if exists ix_feedback_type_area_id;

alter table if exists lock drop constraint if exists fk_lock_node_id;
drop index if exists ix_lock_node_id;

alter table if exists users drop constraint if exists fk_users_primary_user_role_id;
drop index if exists ix_users_primary_user_role_id;

alter table if exists users_user_role drop constraint if exists fk_users_user_role_users;
drop index if exists ix_users_user_role_users;

alter table if exists users_user_role drop constraint if exists fk_users_user_role_user_role;
drop index if exists ix_users_user_role_user_role;

alter table if exists users_user_permission drop constraint if exists fk_users_user_permission_users;
drop index if exists ix_users_user_permission_users;

alter table if exists users_user_permission drop constraint if exists fk_users_user_permission_user_permission;
drop index if exists ix_users_user_permission_user_permission;

alter table if exists user_session drop constraint if exists fk_user_session_user_id;
drop index if exists ix_user_session_user_id;

alter table if exists user_session_integration drop constraint if exists fk_user_session_integration_user_id;
drop index if exists ix_user_session_integration_user_id;

alter table if exists zip_code drop constraint if exists fk_zip_code_state_id;
drop index if exists ix_zip_code_state_id;

drop table if exists feedback cascade;

drop table if exists feedback_area cascade;

drop table if exists feedback_type cascade;

drop table if exists lock cascade;

drop table if exists log cascade;

drop table if exists node cascade;

drop table if exists state cascade;

drop table if exists system_setting cascade;

drop table if exists test1 cascade;

drop table if exists users cascade;

drop table if exists users_user_role cascade;

drop table if exists users_user_permission cascade;

drop table if exists user_permission cascade;

drop table if exists user_role cascade;

drop table if exists user_session cascade;

drop table if exists user_session_integration cascade;

drop table if exists zip_code cascade;

