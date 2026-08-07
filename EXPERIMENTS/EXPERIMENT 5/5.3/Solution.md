---PL-SQL AND IF THEN , IF-THEN -ELSE , IF ELSIF THEN ELSE STATEMENTS 

--if then 
do $$
declare 
age int :=18;
begin
if age>=18
then
raise notice 'you are % and eligible', age;
end if;
end;
$$
---if else
do $$
declare 
age int :=17;
begin
if age>=18
then
raise notice 'you are % and eligible', age;
else
raise notice 'not eligible';
end if;
end;
$$

--if then elsif then else 

do $$
declare 
val int :=14;
begin
if val >0 and val <=10
then
raise notice 'val is % and range is between 1 to 10',val;
elsif val >10 and val <=20 then
raise notice 'val is % and range is between 11 to 20',val;
else
raise notice 'val is % and greater than 20',val;
end if;
end;
$$
-- $$ deliberator : converts the content inside this to string 
