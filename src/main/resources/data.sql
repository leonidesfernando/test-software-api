DROP TABLE IF EXISTS Role;
--commit;

--CREATE TABLE Role AS SELECT * FROM CSVREAD('classpath:/tables/roles.csv');
--commit;
insert into roles values(1,'ROLE_USER');
insert into roles values(2,'ROLE_MODERATOR');
insert into roles values(3,'ROLE_ADMIN');


DROP TABLE IF EXISTS users;
--commit;

--CREATE TABLE users AS SELECT * FROM CSVREAD('classpath:/tables/users.csv');
insert into users (id,username,email, password) values(1,'user','user@mail.com','$2a$10$nwzzqyr4aAsmmBYEe4XZ8OQ9pq/yEAtvEOwMRAXmDcum5Tqkji90S');
--commit;


--select * from users;
--commit;
--update users set username = 'user' where id = 1;
--commit;