-- Database: knosdb
create table knos_user(
id serial NOT NULL PRIMARY KEY,
username VARCHAR(255),
name VARCHAR(255),
email VARCHAR(255),
password VARCHAR(255),
gender VARCHAR(6),
birthDate TIMESTAMP(23),
phone VARCHAR(20),
jobTitle VARCHAR(255),
location VARCHAR(255),
bio VARCHAR(255),
avatarUrl VARCHAR(255),
integral integer,
level integer,
latitude numeric,
longitude numeric,
videoEmbeds VARCHAR(255),
createdAt TIMESTAMP(23),
updateAt TIMESTAMP(23),
enabled BOOLEAN,
accountNonExpired BOOLEAN,
accountNonLocked BOOLEAN,
credentialsNonExpired BOOLEAN
);

create table authority(
id serial NOT NULL PRIMARY KEY,
username VARCHAR(255),
authority VARCHAR(255)
);