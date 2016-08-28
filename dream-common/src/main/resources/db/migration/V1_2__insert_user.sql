insert into knos_user values (1,'user@l.com','小野妹子','user@l.com','$2a$10$CWqS8.KPJm/ysb829TvCz.V8Yku6tGJyL9BnDJG.ohMpwZT5VmkHe','male',null,'13500000000','title','china','bio','/img/avatar.jpg',0,20,0,0,'videoEmbeds',null,null,true,true,true,true);
insert into knos_user values (2,'admin@l.com','niceadmin','admin@qq.com','$2a$10$CWqS8.KPJm/ysb829TvCz.V8Yku6tGJyL9BnDJG.ohMpwZT5VmkHe','female',null,'13500000000','title','china','bio','/img/avatar2.jpg',0,10,0,0,'videoEmbeds',null,null,true,true,true,true);
insert into knos_user values (3,'user123@l.com','niceuser123','user123@l.com','$2a$10$CWqS8.KPJm/ysb829TvCz.V8Yku6tGJyL9BnDJG.ohMpwZT5VmkHe','male',null,'13500000000','title','china','bio',null,0,5,0,0,'videoEmbeds',null,null,true,true,true,true);

insert into authority values (1,'user@l.com','ROLE_USER');
--insert into authority values (2,'admin','ROLE_USER');
insert into authority values (3,'admin@l.com','ROLE_ADMIN');
commit;