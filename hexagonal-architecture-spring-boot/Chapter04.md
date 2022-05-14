# 4장. (계좌 송금) 유스케이스 구현하기

이번 장에서는 앞에서 소개한 육각형 아키텍처 스타일에서 **유스케이스** 를 구현하는 방법을 소개한다. <br>
육각형 아키텍처 스타일에서 도메인 중심의 아키텍처이다. <br>
때문에 **유스케이스 구현은 도메인 엔티티를 만드는 것으로 시작한다.**

## 도메인 엔티티 구현

입금과 출금을 기능을 가진 Account 엔티티를 만들어보자.

```java
/**
 * 계좌 엔티티
 */
public class Account {
	
	private final AccountId id;
    
    // 계좌의 최근 거래 내역들
    private final ActivityWindow activityWindow;

    // ActivityWindow 의 첫번째 Activity 바로 전의 잔고
    private final Money baselineBalance;

    // 계좌의 잔고 계산 
    public Money calculateBalance() {
		return Money.add(
				this.baselineBalance,
				this.activityWindow.calculateBalance(this.id));
	}

    // 출금 하기
	public boolean withdraw(Money money, AccountId targetAccountId) {
		if (!mayWithdraw(money)) {
			return false;
		}

		Activity withdrawal = new Activity(
				this.id,
				this.id,
				targetAccountId,
				LocalDateTime.now(),
				money);
		this.activityWindow.addActivity(withdrawal);
		return true;
	}

    // 입금 하기
	public boolean deposit(Money money, AccountId sourceAccountId) {
		Activity deposit = new Activity(
				this.id,
				sourceAccountId,
				this.id,
				LocalDateTime.now(),
				money);
		this.activityWindow.addActivity(deposit);
		return true;
	}

    // 출금 가능한지 검사
    private boolean mayWithdraw(Money money) {
        return Money.add(
                        this.calculateBalance(),
                        money.negate())
                .isPositiveOrZero();
    }

}
```

* 한 계좌에서 일어나는 입금과 출금은 deposit(), withdraw() 메서드에서 Activity 추가를 통해 구현한다.
* 계좌 잔고를 초과하는 금액은 출금할 수 없도록 하는 **비즈니스 규칙**이 있다.

입출금 할 수 있는 **Account 중심으로 유스케이스를 구현해보자.**

## 유스케이스 구현

유스케이스는 보통 다음과 같은 로직들이 존재한다.

1. 인커밍 어댑터로 부터 입력을 받는다. (입력 유효성 검증은 다른곳에서...)
2. **비즈니스 규칙을 검증한다. (with 도메인 엔티티)**
3. **엔티티 상태를 조작한다.** 
4. 출력을 반환한다.

송금하기 유스케이스 경우 아래와 같은 구조가 될 것이다. 

```java
public class SendMoneyService implements SendMoneyUseCase {

	private final LoadAccountPort loadAccountPort;
	
	private final UpdateAccountStatePort updateAccountStatePort;

	@Override
	public boolean sendMoney(SendMoneyCommand command) {
		// 비즈니스 규칙 검증

		// 엔티티 상태 조작

		// 출력 반환
	}

}
```

### 입력 유효성 검증
입력 유효성 검증은 유스케이스 클래스 책임은 아니지만, 애플리케이션 계층에서 입력 유효성 검증을 하지 않을 수는 없다. <br>
애플리케이션 코어 바깥쪽으로 부터 유효하지 않은 입력값을 받게 되면 엔티티 상태를 해칠 수 있기 때문이다. <br>
유스케이스 클래스가 아니면 어디에서 입력 유효성을 검증하면 좋을까?

유스케이스의 입력 모델 클래스에서 입력 유효성을 검증하는게 좋다.

* 유스케이스의 호출자 마다 입력 유효성 검증 코드를 넣지 않아도 된다.
* 유스케이스 입력 모델은 유스케이스 인터페이스의 일부이기 때문에 유스케이스 구현 코드를 오염시키지 않는다.
* Bean Validation API 의 도움을 받아서 객체 생성 시 유효성 검증을 자동으로 할 수 있다.

```java
@Value
@EqualsAndHashCode(callSuper = false)
public class SendMoneyCommand extends SelfValidating<SendMoneyCommand> {

    @NotNull
    private final AccountId sourceAccountId;

    @NotNull
    private final AccountId targetAccountId;

    @NotNull
    private final Money money;

    public SendMoneyCommand(
            AccountId sourceAccountId,
            AccountId targetAccountId,
            Money money) {
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.money = money;
        this.validateSelf();
    }
}
```

입력 모델의 유효성 검증 코드로 애플리케이션 (코어) 내부에 유효하지 않은 입력이 들어오지 못하도록 하자.

### 생성자의 힘

현재 `SendMoneyCommand` 생성자에는 3개의 파라미터만 있지만, 더 많아지면 빌더 패턴을 고려할 수도 있을것이다.

```java
new SendMoneyCommandBuilder()
        .sourceAccountId()
        .targetAccountId()
        // .. 다른 필드
        .build();
```

어떤 이유로 `SendMoneyCommandBuilder` 에 필드를 누락할 수도 있지만 컴파일러는 잡아내지 못한다. <br>
생성자를 직접 사용하면 `SendMoneyCommand` 클래스 모든 필드에 final 키워드가 붙어 있기 때문에 생성자에 필드를 누락하면 컴파일러 에러가 발생할 것이다.

### 유스케이스마다 다른 입력 모델
'계좌 등록하기' 유스케이스와 '계좌 정보 업데이트하기' 유스케이스의 입력 모델을 구현한다고 가정했을때, <br>
두 입력 모델은 거의 비슷할 것이다. 차이는 계좌 정보 업데이트하기 유스케이스 입력 모델에 어떤 계좌를 수정할 것인지 계좌 Id 를 필요로 하는 점 정도 <br>
만약 두 입력 모델을 동일하게 간다고 했을때 계좌 Id 필드는 null 값을 허용 해야한다. <br>

Id 필드에 null 을 허용하게 되면 입력 유효성을 어떻게 검증할 것인가? 고민을 하게 된다.

* 계좌 등록 유스케이스에는 id 가 null 이어야함
* 계좌 수정 유스케이스에는 id 가 not null 이어야함

이는 유스케이스에 입력 유효성 검증과 관련된 코드가 침투할 수 있다. <br>
`계좌 등록 유스케이스에 계좌 id 가 not null 일때는 어떡할까?` 이런 고민도 필요하다.

유스케이스마다 다른 임력 모델을 사용한다면,`각 유스케이스 전용 입력 모델은 유스케이스를 훨씬 명확하게 만들고 다른 유스케이스와의 결합도 제거해서 불필요한 부수효과가 발생하지 않게 한다.`

## 비즈니스 규칙 검증하기

`입력 유효성 검증`은 유스케이스 로직의 일부가 아니지만, `비즈니스 규칙 검증`은 유스케이스 로직의 일부이다.

둘 사이의 실용적인 구분점은
* 입력 유효성 검증: 도메인 모델의 현재 상태에 접근할 필요가 없다.
  * 송금되는 금액은 0보다 커야한다. : Account 의 현재 상태에 접근할 필요 없다.
* 비즈니스 규칙 검증: 도메인 모델의 현재 상태에 접근해야 한다.
  * 출금 계좌는 초과 출금되어서는 안된다. : Account 의 잔고 상태에 접근해야 한다.

송금액은 매우 중요한데 `송금되는 금액은 0보다 커야한다.`는 비즈니스 규칙으로 다뤄야 하는거 아닐까?

도메인 모델의 현재 상태에 접근 유무로 구분하는 방법은 실용적인 방법이다.<br>
이 방법을 사용하면 유효성 검증 로직을 어디에 둘지 결정하고 그것이 어디있는지 찾는데 도움을 주기 때문이다.

비즈니스 규칙을 구현하는 가장 좋은 방법은 비즈니스 규칙을 도메인 엔티티안에 넣는 것이다.

```java
public boolean withdraw(Money money, AccountId targetAccountId) {

    if (!mayWithdraw(money)) {
        return false;
    }
    
    // ...
}
```

비즈니스 로직에 규칙이 위치하기 대문에 위치를 정하는 것도 쉽고 추론하는 것도 쉽다. <br>
비즈니스 규칙을 검증할때 외부 의존성이 필요하다면 도메인 엔티티에서 비즈니스 규칙을 검증하기가 어려울 수 있다.
이럴 경우 유스케이스 에서 검증해도 된다. 

## 유스케이스마다 다른 출력 모델

입력과 비슷하게 출력도 각 유스케이스에 맞게 구체적일수록 좋다. `출력은 호출자에게 꼭 필요한 데이터만 들고 있어야 한다.` <br>
송금하기 유스케이스에서는 boolean 을 반환했지만, 어떤 호출자는 업데이트된 Account 의 잔액이 필요할 수도 있는데, <br>
해당 호출자 전용 유스케이스를 만들어야 할까?

정답은 없지만, 우리는 유스케이스를 가능한 구체적으로 유지하기 위해 계속 고민을 해야하고, 가능한 적게 반환하는게 좋다. <br>
유스케이스들 간에 같은 출력 모델을 공유하게 되면 유스케이스들이 강하게 결합된다. <br>
단일 책임 원칙을 적용하고 모델을 분리해서 유지하는게 유스케이스의 결합을 제거하는데 도움이 되기 때문이다.

## 읽기 전용 유스케이스

UI 에 보여주기 위한 로직은 보통 데이터 쿼리로 구현해야할 상황이 많다. <br> 
프로젝트 맥락에서 이런 유스케이스는 실제 유스케이스과 구분되도록 Query Service 로 구현할 수 있다.

```java
@RequiredArgsConstructor
class GetAccountBalanceService implements GetAccountBalanceQuery {

	private final LoadAccountPort loadAccountPort;

	@Override
	public Money getAccountBalance(AccountId accountId) {
		return loadAccountPort.loadAccount(accountId, LocalDateTime.now())
				.calculateBalance();
	}
}
```

읽기 전용 쿼리는 쓰기가 가능한 유스케이스와 명확하게 구분되므로, CQS (Command-Query Separation) 나 CQRS (Command-Query Responsibility Segregation)
같은 개념과 잘 맞는다.

## 유지보수 가능한 소프트웨어를 만드는데 어떻게 도움이 될까?

유스케이스의 입출력 모델을 독립적으로 모델링 하면 원치 않는 부수효과를 피할 수 있지만, <br>
유스케이스마다 별도의 모델을 만들어야 하고, 이 모델과 엔티티를 매핑해야 한다.

하지만 유스케이스를 명확하게 이해할 수 있고, 장기적으로 유지보수하기도 쉽다. <br>
여러 명의 개발자가 다른 사람이 작업중인 유스케이스를 던드리지 않은채로 동시에 작업할 수 있다. 

꼼꼼한 입력 유효성 검증, 유스케이스별 입출력 모델은 지속 가능한 코드를 만드는데 큰 도움이 된다.


