# 6장. 영속성 어댑터 구현하기

이번 장에는 '데이터베이스 주도 설계' 가 되지 않게 위해 의존성을 역전 시켜 <br>
영속성 계층을 애플리케이션 계층의 플러그인으로 만드는 방법을 알아본다.

## 의존성 역전

애플리케이션 서비스에서는 영속성 기능을 사용하기 위해 포트 인터페이스를 사용한다. <br>
이 포트는 실제로 영속성 작업을 수행하고 DB 와 통신하는 영속성 어댑터 클래스에 의애 구현된다.

이 포트은 애플리케이션 로직에서 영속성 문제에 신경쓰지 않고 코어 코드를 개발하기 위함이다. <br>
영속성 코드를 리팩터링 하더라도, 코어 코드는 변경하지 않아도 된다.

## 영속성 어댑터의 책임

영속성 어댑터는 어떤일을 할까?

1. 입력을 받는다.
2. 입력을 데이터베이스 포맷으로 매핑한다.
3. 입력을 데이터베이스로 보낸다.
4. 데이터베이스 출력을 애플리케이션 포맷으로 매핑한다.
5. 출력을 반환한다.

영속성 어댑터는 포트 인터페이스를 통해 입력을 받는다. 그리고 나서 DB 에 쿼리를 할 수 있는 입력 모델로 매핑을 한다. <br>
자바 진영에서 보통 JPA 를 사용하기 때문에 JPA 엔티티로 매핑할 것이다. 그리고 쿼리를 날리고 결과를 받아온다. <br>
받아온 쿼리 결과를 `애플리케이션 코어에 위치한 출력 모델` 로 변환한다.

영속성 어댑터를 구현하다 보면 전통적인 영속성 계층을 구현할 때는 없었던 몇가지 의문들이 생긴다.

## 포트 인터페이스 나누기

그 의문 중 하나는 DB 연산을 정의하고 있는 포트 인터페이스를 어떻게 나눌 것인가? 이다. <br>
보통 특정 엔티티가 필요로 하는 모든 DB 연산을 하나의 Repository 인터페이스에 넣어두는게 일반적이다. <br>
그럼 데이터베이스 연산에 의존하는 각 서비스가 Repository 의 하나의 메서만 사용하더라도 넓은 Repository 에 의존성을 갖게 된다. <br>

이렇게 맥락 안에서 필요하지 않은 메서드에 생긴 의존성은 코드를 이해하고 테스트를 어렵게 만든다. <br>
하나의 큰 Repository 안에 어떤 메서드를 모킹해서 테스트 해야 하는지 찾아야 한다.

클라이언트가 오로지 자신이 필요로 하는 메서드만 알게 하도록 넓은 인터페이스를 특화된 인터페이스로 분리해야 한다. <br>
각 서비스에 특화된 좁은 포트 인터페이스로 나누고, 포트의 이름이 포트의 역할을 명확하게 잘 표현하게 하자

* LoadAccountPort
* UpdateAccountStatePort
* CreateAccountPort

## 영속성 어댑터 나누기

영속성 어댑터는 영속성 연산이 필요한 도메인 클래스당 하나의 영속성 어댑터를 만들 수도 있고 <br>
또 JPA 를 이용한 어댑터, OR 매퍼를 이용한 어댑터 로 나눌수도 있다.

## 스프링 데이터 JPA 예제

```java
@Entity
@Table(name = "account")
@Data
@AllArgsConstructor
@NoArgsConstructor
class AccountJpaEntity {

	@Id
	@GeneratedValue
	private Long id;

}
```

```java
@Entity
@Table(name = "activity")
@Data
@AllArgsConstructor
@NoArgsConstructor
class ActivityJpaEntity {

	@Id
	@GeneratedValue
	private Long id;

	@Column
	private LocalDateTime timestamp;

	@Column
	private Long ownerAccountId;

	@Column
	private Long sourceAccountId;

	@Column
	private Long targetAccountId;

	@Column
	private Long amount;

}

```

```java
@RequiredArgsConstructor
@PersistenceAdapter
class AccountPersistenceAdapter implements
		LoadAccountPort,
		UpdateAccountStatePort {

	private final SpringDataAccountRepository accountRepository;
	private final ActivityRepository activityRepository;
	private final AccountMapper accountMapper;

	@Override
	public Account loadAccount(
					AccountId accountId,
					LocalDateTime baselineDate) {

		AccountJpaEntity account =
				accountRepository.findById(accountId.getValue())
						.orElseThrow(EntityNotFoundException::new);

		List<ActivityJpaEntity> activities =
				activityRepository.findByOwnerSince(
						accountId.getValue(),
						baselineDate);

		Long withdrawalBalance = orZero(activityRepository
				.getWithdrawalBalanceUntil(
						accountId.getValue(),
						baselineDate));

		Long depositBalance = orZero(activityRepository
				.getDepositBalanceUntil(
						accountId.getValue(),
						baselineDate));

		return accountMapper.mapToDomainEntity(
				account,
				activities,
				withdrawalBalance,
				depositBalance);

	}

	private Long orZero(Long value){
		return value == null ? 0L : value;
	}


	@Override
	public void updateActivities(Account account) {
		for (Activity activity : account.getActivityWindow().getActivities()) {
			if (activity.getId() == null) {
				activityRepository.save(accountMapper.mapToJpaEntity(activity));
			}
		}
	}

}
```

```java
interface ActivityRepository extends JpaRepository<ActivityJpaEntity, Long> {

	@Query("select a from ActivityJpaEntity a " +
			"where a.ownerAccountId = :ownerAccountId " +
			"and a.timestamp >= :since")
	List<ActivityJpaEntity> findByOwnerSince(
			@Param("ownerAccountId") Long ownerAccountId,
			@Param("since") LocalDateTime since);

	@Query("select sum(a.amount) from ActivityJpaEntity a " +
			"where a.targetAccountId = :accountId " +
			"and a.ownerAccountId = :accountId " +
			"and a.timestamp < :until")
	Long getDepositBalanceUntil(
			@Param("accountId") Long accountId,
			@Param("until") LocalDateTime until);

	@Query("select sum(a.amount) from ActivityJpaEntity a " +
			"where a.sourceAccountId = :accountId " +
			"and a.ownerAccountId = :accountId " +
			"and a.timestamp < :until")
	Long getWithdrawalBalanceUntil(
			@Param("accountId") Long accountId,
			@Param("until") LocalDateTime until);

}
```

## 데이터베이스 트랜잭션은 어떻게 해야 할까?

트랜잭션은 하나의 특정한 유스케이스에 대해서 일어나는 모든 쓰기 작업에 걸쳐 있어야 한다. 그래야 그중 하나라도 실패할 경우 따 같이 롤백될 수 있기 때문이다.

영속성 어댑터는 어떤 데이터베이스 연산이 같은 유스케이스에 포함되는지 알지 못해서 트랜잭션을 언제 열고 닫을지 결정할 수 없다. <br>
이 책임은 영속성 어댑터 호출을 관장하는 서비스에 위임해야 한다.

스프링을 사용한다면 @Transactional 애너테이션을 사용하는 것이다.

## 유지보수 가능한 소프트웨어를 만드는 데 어떻게 도움이 될까?

도메인 코드에 플러그인 처럼 동작하는 영속성 어댑터를 만들면 도메인 코드가 영속성과 관련된 것들로부터 분리되어 풍부한 도메인 모델을 만들 수 있다.

좁은 포트 인터페이스를 사용하면 포트마다 다른 방식으로 구현할 수 있는 유연함이 생겨 다른 영속성 기술을 사용할 수 있다.


