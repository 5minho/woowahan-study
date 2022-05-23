### LeetCode #672 Swap Salary

https://leetcode.com/problems/swap-salary/

case 문을 이용하는 문제

```sql
update Salary 
set sex = (CASE when sex = 'f' then 'm' ELSE 'f' END);
```

### LeetCode #196 Delete Duplicate Emails

https://leetcode.com/problems/delete-duplicate-emails/

서브 쿼리, 셀프 조인을 이용할 수 있음.

```sql
delete p1 
from Person as p1 join Person as p2 on p1.email = p2.email
where p1.id > p2.id
```